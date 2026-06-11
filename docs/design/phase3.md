# Phase 3 — enum 정의

> 목적: `int` 상수로 관리되던 부품 타입을 enum으로 교체.
> Primitive Obsession, Magic Numbers 해소.

---

## 대상 파일

| 파일 | 위치 |
|------|------|
| `CarType.java` | `src/main/java/model/CarType.java` |
| `Engine.java` | `src/main/java/model/Engine.java` |
| `BrakeSystem.java` | `src/main/java/model/BrakeSystem.java` |
| `SteeringSystem.java` | `src/main/java/model/SteeringSystem.java` |

---

## 3-1. CarType

```java
public enum CarType {
    SEDAN("Sedan"),
    SUV("SUV"),
    TRUCK("Truck");

    public final String displayName;

    CarType(String displayName) {
        this.displayName = displayName;
    }
}
```

- 기존: `private static final int SEDAN = 1, SUV = 2, TRUCK = 3`
- `displayName` 필드로 이름 매핑 중복 제거 (`selectCarType()`, `runProducedCar()` 내 ternary 체인)

---

## 3-2. Engine

```java
public enum Engine {
    GM("GM"),
    TOYOTA("TOYOTA"),
    WIA("WIA"),
    BROKEN("고장난 엔진");

    public final String displayName;

    Engine(String displayName) {
        this.displayName = displayName;
    }

    public boolean isBroken() {
        return this == BROKEN;
    }
}
```

- 기존: `private static final int GM = 1, TOYOTA = 2, WIA = 3` + 마법 숫자 `4`
- `isBroken()` — `stack[Engine_Q] == 4` 마법 숫자 제거

---

## 3-3. BrakeSystem

```java
public enum BrakeSystem {
    MANDO("Mando"),
    CONTINENTAL("Continental"),
    BOSCH("Bosch");

    public final String displayName;

    BrakeSystem(String displayName) {
        this.displayName = displayName;
    }
}
```

- 기존: `private static final int MANDO = 1, CONTINENTAL = 2, BOSCH_B = 3`
- `BOSCH_B` 네이밍 혼란 제거 (`BOSCH_S`와 구분하기 위한 접미사 불필요)

---

## 3-4. SteeringSystem

```java
public enum SteeringSystem {
    BOSCH("Bosch"),
    MOBIS("Mobis");

    public final String displayName;

    SteeringSystem(String displayName) {
        this.displayName = displayName;
    }
}
```

- 기존: `private static final int BOSCH_S = 1, MOBIS = 2`
- `BOSCH_S` 접미사 제거 — `BrakeSystem.BOSCH`와 타입으로 구분되므로 불필요

---

## 설계 결정

| 결정 | 이유 |
|------|------|
| `displayName` 필드 공통 적용 | 이름 매핑 ternary 체인 4곳 제거 |
| `isBroken()` Engine에만 추가 | 고장 상태는 Engine 고유 개념 |
| `BrakeSystem.BOSCH`, `SteeringSystem.BOSCH` 동일 이름 허용 | 타입이 다르므로 충돌 없음, 접미사 제거가 가독성 향상 |

---

## Phase 3 완료 후 상태

- `Assemble.java` 내 `int` 상수 블록 (L6~L15) 제거
- enum 4개 추가, `int[] stack` 은 아직 유지 (Phase 4에서 `Car` 객체로 교체)
- `./gradlew test` 25개 통과 유지 확인 필요
