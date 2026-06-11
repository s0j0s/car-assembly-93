# Phase 2 — Unit Test 작성 (리팩토링 전 안전망)

> 기존 `Assemble.java` 코드 기준으로 테스트 작성.
> 목적: 리팩토링 후 동작이 깨지지 않았음을 검증하는 안전망 확보.

---

## 2-1. 테스트 클래스 생성

- 파일: `src/test/java/AssembleTest.java`
- `stack`, `isValidCheck()`, `runProducedCar()` 모두 `private static` → `package-private` 변경 필요
- `@BeforeEach` 에서 `Arrays.fill(Assemble.stack, 0)` 으로 상태 초기화

---

## 2-2. 호환성 실패 케이스 (5개)

| 테스트명 | stack 세팅 | 검증 대상 |
|---------|-----------|---------|
| `sedan_with_continental_brake_fails` | CarType=1, BrakeSystem=2 | `isValidCheck()` → false |
| `suv_with_toyota_engine_fails` | CarType=2, Engine=2 | `isValidCheck()` → false |
| `truck_with_wia_engine_fails` | CarType=3, Engine=3 | `isValidCheck()` → false |
| `truck_with_mando_brake_fails` | CarType=3, BrakeSystem=1 | `isValidCheck()` → false |
| `bosch_brake_requires_bosch_steering_fails` | BrakeSystem=3, SteeringSystem=2 | `isValidCheck()` → false |

---

## 2-3. 정상 케이스 (2개)

| 테스트명 | stack 세팅 | 검증 대상 |
|---------|-----------|---------|
| `valid_combination_passes` | CarType=1, Engine=1, BrakeSystem=1, SteeringSystem=1 | `isValidCheck()` → true |
| `broken_engine_does_not_run` | Engine=4 | `runProducedCar()` 출력에 "고장" 포함 |

---

## 2-4. Coverage 측정

```bash
./gradlew test
./gradlew jacocoTestReport
# 결과: build/reports/jacoco/test/html/index.html
```

- 테스트 전체 통과 확인 ✓
- baseline coverage 수치 기록 → 리팩토링 후 비교 기준

### Baseline Coverage (리팩토링 전)
| 항목 | 커버리지 |
|------|---------|
| Instructions | 11% (80 / 671) |
| Branches | 16% (19 / 118) |

> UI/메뉴/main 로직 미포함, 도메인 로직(`isValidCheck`, `runProducedCar`) 위주 측정.

---

## 검토 포인트

| # | 항목 | 결정 |
|---|------|------|
| 1 | `broken_engine_does_not_run` stdout 캡처 | `System.setOut(new PrintStream(outContent))` 방식 사용 |
| 2 | `stack` 접근자 변경 여부 | `private` → `package-private` (reflection 회피) |
| 3 | Coverage 측정 범위 | 도메인 로직 위주 (`isValidCheck`, `runProducedCar`) |
