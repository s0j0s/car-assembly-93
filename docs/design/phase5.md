# Phase 5 — 호환성 규칙 분리

> 목적: `isValidCheck()`와 `testProducedCar()`에 중복된 5개 규칙을 단일 책임 클래스로 통합.
> Divergent Change, Duplicate Code 해소.

---

## 5-1. CompatibilityRule 인터페이스

```java
package rule;

import model.Car;
import java.util.Optional;

@FunctionalInterface
public interface CompatibilityRule {
    Optional<String> check(Car car);
}
```

- `Optional.empty()` — 규칙 통과
- `Optional.of("메시지")` — 규칙 위반 + 이유 반환
- `@FunctionalInterface` — 람다로 규칙 정의 가능

---

## 5-2. CompatibilityChecker 클래스

```java
package rule;

import model.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompatibilityChecker {

    private static final List<CompatibilityRule> RULES = List.of(
        car -> car.getCarType() == CarType.SEDAN && car.getBrakeSystem() == BrakeSystem.CONTINENTAL
            ? Optional.of("Sedan에는 Continental제동장치 사용 불가") : Optional.empty(),

        car -> car.getCarType() == CarType.SUV && car.getEngine() == Engine.TOYOTA
            ? Optional.of("SUV에는 TOYOTA엔진 사용 불가") : Optional.empty(),

        car -> car.getCarType() == CarType.TRUCK && car.getEngine() == Engine.WIA
            ? Optional.of("Truck에는 WIA엔진 사용 불가") : Optional.empty(),

        car -> car.getCarType() == CarType.TRUCK && car.getBrakeSystem() == BrakeSystem.MANDO
            ? Optional.of("Truck에는 Mando제동장치 사용 불가") : Optional.empty(),

        car -> car.getBrakeSystem() == BrakeSystem.BOSCH && car.getSteeringSystem() != SteeringSystem.BOSCH
            ? Optional.of("Bosch제동장치에는 Bosch조향장치 이외 사용 불가") : Optional.empty()
    );

    public List<String> validate(Car car) {
        return RULES.stream()
            .map(rule -> rule.check(car))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    public boolean isValid(Car car) {
        return RULES.stream().allMatch(rule -> rule.check(car).isEmpty());
    }
}
```

---

## 5-3. Assemble.java 변경 사항

### isValidCheck() 제거 → CompatibilityChecker 위임

```java
// Before
static boolean isValidCheck() {
    CarType carType = car.getCarType();
    // ... 5개 규칙 직접 기술
}

// After — isValidCheck() 삭제, CompatibilityChecker 사용
private static final CompatibilityChecker checker = new CompatibilityChecker();
```

### runProducedCar()

```java
// Before
if (!isValidCheck()) { ... }

// After
if (!checker.isValid(car)) { ... }
```

### testProducedCar() 전면 교체

```java
// Before — 5개 규칙 중복 기술
static void testProducedCar() {
    if (carType == CarType.SEDAN && brake == BrakeSystem.CONTINENTAL) { fail(...); }
    else if ...

// After — validate() 결과 출력
static void testProducedCar() {
    List<String> violations = checker.validate(car);
    if (violations.isEmpty()) {
        System.out.println("자동차 부품 조합 테스트 결과 : PASS");
    } else {
        System.out.println("자동차 부품 조합 테스트 결과 : FAIL");
        violations.forEach(System.out::println);
    }
}
```

---

## 5-4. 테스트 교체

### 기존 AssembleTest.java 제거

`Assemble.car`, `Assemble.isValidCheck()` 등 구현 세부사항에 결합 — 삭제.

### CompatibilityCheckerTest.java 신규 작성 (행동 기반)

```java
package rule;

class CompatibilityCheckerTest {
    private final CompatibilityChecker checker = new CompatibilityChecker();

    // Car 객체를 직접 생성 — Assemble 내부 상태에 무관
    private Car car(CarType ct, Engine e, BrakeSystem b, SteeringSystem s) {
        Car car = new Car();
        car.setCarType(ct); car.setEngine(e);
        car.setBrakeSystem(b); car.setSteeringSystem(s);
        return car;
    }
}
```

**테스트 케이스 (7개 → 12개 이상으로 확장)**

| 테스트명 | 조합 | 기대 결과 |
|---------|------|---------|
| `sedan_continental_invalid` | SEDAN+GM+CONTINENTAL+BOSCH | `isValid()` false |
| `suv_toyota_invalid` | SUV+TOYOTA+MANDO+BOSCH | `isValid()` false |
| `truck_wia_invalid` | TRUCK+WIA+BOSCH+BOSCH | `isValid()` false |
| `truck_mando_invalid` | TRUCK+GM+MANDO+BOSCH | `isValid()` false |
| `bosch_brake_mobis_steering_invalid` | SEDAN+GM+BOSCH+MOBIS | `isValid()` false |
| `sedan_gm_mando_bosch_valid` | SEDAN+GM+MANDO+BOSCH | `isValid()` true |
| `suv_gm_bosch_bosch_valid` | SUV+GM+BOSCH+BOSCH | `isValid()` true |
| `truck_gm_bosch_bosch_valid` | TRUCK+GM+BOSCH+BOSCH | `isValid()` true |
| `validate_returns_violation_message` | SEDAN+GM+CONTINENTAL+BOSCH | `validate()` 메시지 포함 |
| `validate_returns_empty_for_valid` | SEDAN+GM+MANDO+BOSCH | `validate()` 빈 리스트 |

---

## 설계 결정

| 결정 | 이유 |
|------|------|
| `RULES` static final List | 규칙은 불변 — 인스턴스마다 생성 불필요 |
| `validate()` 위반 메시지 목록 반환 | `testProducedCar()` 출력에 직접 사용 가능 |
| `isValid()` 별도 제공 | `runProducedCar()` 단순 boolean 판단용 |
| `AssembleTest.java` 삭제 | 구현 결합 제거, 행동 기반 테스트로 완전 교체 |

---

## Phase 5 완료 후 상태

- `Assemble.java` 에서 호환성 규칙 코드 0줄
- `CompatibilityChecker` — 규칙 추가/수정 시 단일 파일만 수정
- `CompatibilityCheckerTest` — `Assemble` 내부 구조와 무관한 행동 기반 테스트
- `./gradlew test` 전체 통과
