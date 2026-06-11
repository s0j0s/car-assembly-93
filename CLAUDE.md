# assemblyCar — CLAUDE.md

## 프로젝트 개요
차량 조립 시뮬레이터. 사용자가 차량 타입 → 엔진 → 제동장치 → 조향장치 순으로 선택하고, 부품 호환성 검증 후 RUN/Test 수행.

## 기술 스택
- Java 17 (Azul Zulu 17.0.19)
- 빌드 도구 없음 (plain javac) — Maven 도입 예정
- 컴파일: `javac -encoding UTF-8 java/Assemble.java`
- 실행: `cd java && java Assemble`

## 파일 구조
```
assemblyCar/
├── java/
│   └── Assemble.java    # 단일 진입점 + 전체 로직
├── README.md
├── CLAUDE.md
└── .gitignore
```

---

## Explore 분석 결과

### 현재 구조 (리팩토링 전)
`Assemble.java` 단일 클래스에 아래 모든 책임 혼재:
- 화면 출력 (메뉴 표시)
- 사용자 입력 처리
- 상태 관리 (`int[] stack`)
- 부품 호환성 검증
- RUN / Test 실행

### 상태 관리 방식
```java
private static int[] stack = new int[5];
// stack[0] = CarType, stack[1] = Engine, stack[2] = BrakeSystem, stack[3] = SteeringSystem
```
정수 인덱스로 부품 구분 — 타입 안전성 없음.

### 호환성 규칙 (5가지)
| # | 규칙 |
|---|------|
| 1 | Sedan + Continental 제동장치 → 불가 |
| 2 | SUV + TOYOTA 엔진 → 불가 |
| 3 | Truck + WIA 엔진 → 불가 |
| 4 | Truck + MANDO 제동장치 → 불가 |
| 5 | BOSCH 제동장치 → BOSCH 조향장치만 가능 |

동일 규칙이 `isValidCheck()` (L212)와 `testProducedCar()` (L244) 두 곳에 중복.

### 코드 스멜 목록 (17개)

| 스멜 | 위치 | 설명 |
|------|------|------|
| God Class | L3 | 단일 클래스에 UI·도메인·검증·입출력 혼재 |
| Primitive Obsession | L17 | `int[] stack` — 부품을 정수로 표현 |
| Data Clump | L17 | stack 인덱스 4개 항상 묶여서 사용 |
| Long Method | L19-107 | `main()` 89줄 — 파싱·검증·상태전이 혼합 |
| Switch Statements | L27-38 | 메뉴 표시 switch |
| Switch Statements | L71-103 | 선택 처리 switch |
| Switch Statements | L155-189 | 범위 검증 switch |
| Feature Envy | L191-209 | `select*()` 4개 메서드 — stack에 직접 접근 |
| Duplicate Code | L109-153 | 메뉴 출력 4개 메서드 — 구조 동일 |
| Duplicate Code | L192-208 | int→이름 매핑 ternary 반복 |
| Duplicate Code | L232-240 | `runProducedCar()` 내 이름 매핑 재반복 |
| Duplicate Code | L244-258 | `testProducedCar()` — 호환성 규칙 중복 |
| Divergent Change | L212 + L244 | 규칙 추가 시 두 메서드 모두 수정 필요 |
| Inappropriate Intimacy | L213-217 | `stack[idx]` 직접 인덱스 접근 |
| Magic Numbers | L4-15 | 부품 상수를 int로 정의 |
| Magic Numbers | L226 | `4` = 고장난 엔진 (상수 미사용) |
| Dead Code | L269 | `InterruptedException` 묵살 — interrupt 플래그 소실 |

---

## 리팩토링 계획 (Plan 단계)

> **세부 작업 항목 및 체크리스트는 [`docs/PLAN.md`](docs/PLAN.md) 참고.**
> 각 Phase 진행 전 PLAN.md 체크리스트 기준으로 검토 후 진행.

### 진행 순서
1. Gradle 프로젝트 세팅 (JUnit 5 + JaCoCo)
2. **기존 코드 기준 Unit Test 먼저 작성**
3. Coverage 측정 후 리팩토링 시작
4. 리팩토링 완료 후 테스트 재통과 확인

### 목표
1. 절차지향 → 객체지향 구조
2. 타입 안전성 확보 (enum)
3. 호환성 규칙 단일화
4. JUnit 5 유닛 테스트
5. Gradle 빌드 도구 도입 (JaCoCo coverage 측정)

### 예정 패키지 구조
```
src/
  main/java/
    model/
      CarType.java          (enum: SEDAN, SUV, TRUCK)
      Engine.java           (enum: GM, TOYOTA, WIA, BROKEN)
      BrakeSystem.java      (enum: MANDO, CONTINENTAL, BOSCH)
      SteeringSystem.java   (enum: BOSCH, MOBIS)
      Car.java              (도메인 모델)
    rule/
      CompatibilityRule.java     (functional interface)
      CompatibilityChecker.java  (규칙 목록 + 검증)
    service/
      CarAssembler.java     (조립 흐름 제어)
    ui/
      ConsoleUI.java        (입출력 전담)
    Assemble.java           (main 진입점)
  test/java/
    rule/
      CompatibilityCheckerTest.java
build.gradle
settings.gradle
```

### 코드 스멜 해결 매핑
| 스멜 | 해결 |
|------|------|
| Primitive Obsession, Data Clump | `int[]` → `Car` 객체 + enum |
| God Class | 4개 클래스로 책임 분리 |
| Duplicate Code (규칙) | `CompatibilityChecker` 단일화 |
| Duplicate Code (메뉴) | `ConsoleUI` 공통 메서드 |
| Switch Statements | enum 메서드 위임 |
| Magic Number `4` | `Engine.BROKEN` |
| Dead Code | `Thread.currentThread().interrupt()` 복원 |
| Long Method | `main()` → `CarAssembler.run()` 위임 |

### 예정 테스트 케이스
- `sedan_with_continental_brake_fails`
- `suv_with_toyota_engine_fails`
- `truck_with_wia_engine_fails`
- `truck_with_mando_brake_fails`
- `bosch_brake_requires_bosch_steering_fails`
- `valid_combination_passes`
- `broken_engine_car_does_not_run`
