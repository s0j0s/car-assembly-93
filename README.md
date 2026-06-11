# assemblyCar

차량 조립 시뮬레이터. 차량 타입 → 엔진 → 제동장치 → 조향장치 순으로 부품을 선택하고, 호환성 검증 후 RUN / Test를 수행한다.

---

## 기술 스택

- Java 17 (Azul Zulu 17.0.19)
- Gradle 9.3.0
- JUnit 5 + JaCoCo
- Lombok 1.18.34

---

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew run

# 테스트
./gradlew test

# 커버리지 리포트 (build/reports/jacoco/test/html/index.html)
./gradlew jacocoTestReport
```

---

## 프로젝트 구조

```
src/
  main/java/
    model/
      Displayable.java        인터페이스 — getDisplayName() 공통 계약
      CarType.java            enum: SEDAN, SUV, TRUCK
      Engine.java             enum: GM, TOYOTA, WIA, BROKEN
      BrakeSystem.java        enum: MANDO, CONTINENTAL, BOSCH
      SteeringSystem.java     enum: BOSCH, MOBIS
      Car.java                도메인 모델 (부품 4개 + isFullyConfigured / reset)
    rule/
      CompatibilityRule.java  @FunctionalInterface — Optional<String> check(Car)
      CompatibilityChecker.java 호환성 규칙 5개 단일 관리
    service/
      CarAssembler.java       조립 흐름 제어 (상태 머신)
    ui/
      ConsoleUI.java          입출력 전담
    Assemble.java             main 진입점
  test/java/
    model/
      CarTest.java
    rule/
      CompatibilityCheckerTest.java
    service/
      CarAssemblerTest.java
docs/
  PLAN.md                     리팩토링 7단계 체크리스트
  design/                     각 Phase 설계 문서
```

---

## 호환성 규칙

| # | 규칙 |
|---|------|
| 1 | Sedan + Continental 제동장치 → 불가 |
| 2 | SUV + TOYOTA 엔진 → 불가 |
| 3 | Truck + WIA 엔진 → 불가 |
| 4 | Truck + MANDO 제동장치 → 불가 |
| 5 | BOSCH 제동장치 → BOSCH 조향장치만 가능 |

---

## 테스트 커버리지

| 지표 | 커버리지 |
|------|---------|
| Instructions | 82% |
| Branches | 85% |
| 테스트 수 | 32개 |

---

## 리팩토링 개요

절차지향 단일 클래스(`Assemble.java` 300줄)를 객체지향 구조로 전환.

| 항목 | Before | After |
|------|--------|-------|
| 파일 수 | 1 | 10 (main) + 3 (test) |
| 빌드 도구 | plain javac | Gradle 9.3.0 |
| 타입 안전성 | `int[]` | enum + Car |
| 호환성 규칙 위치 | 2곳 중복 | CompatibilityChecker 단일화 |
| 테스트 | 0개 | 32개 |
| Coverage | 0% | Instructions 82% / Branches 85% |
