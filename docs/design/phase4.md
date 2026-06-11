# Phase 4 — Car 도메인 모델

> 목적: `int[] stack` 전역 가변 상태를 `Car` 객체로 교체.
> Data Clump, Primitive Obsession, Inappropriate Intimacy 해소.

---

## 4-1. Car 클래스 설계

```java
package model;

public class Car {
    private CarType        carType;
    private Engine         engine;
    private BrakeSystem    brakeSystem;
    private SteeringSystem steeringSystem;

    public CarType        getCarType()        { return carType; }
    public Engine         getEngine()         { return engine; }
    public BrakeSystem    getBrakeSystem()    { return brakeSystem; }
    public SteeringSystem getSteeringSystem() { return steeringSystem; }

    public void setCarType(CarType carType)               { this.carType = carType; }
    public void setEngine(Engine engine)                  { this.engine = engine; }
    public void setBrakeSystem(BrakeSystem brakeSystem)   { this.brakeSystem = brakeSystem; }
    public void setSteeringSystem(SteeringSystem s)       { this.steeringSystem = s; }

    public boolean isFullyConfigured() {
        return carType != null && engine != null
            && brakeSystem != null && steeringSystem != null;
    }

    public void reset() {
        carType = null; engine = null;
        brakeSystem = null; steeringSystem = null;
    }
}
```

- 미선택 부품 = `null` (기존 `0` 대체)
- `isFullyConfigured()` — RUN/Test 전 완전 조립 여부 확인
- `reset()` — 처음 화면으로 돌아갈 때 상태 초기화

---

## 4-2. Assemble.java 변경 사항

### stack 제거

```java
// Before
static int[] stack = new int[5];

// After
static Car car = new Car();
```

### selectXxx 메서드 — enum 직접 저장

```java
// Before
private static void selectCarType(int a) {
    stack[CarType_Q] = a;
    System.out.printf("차량 타입으로 %s을 선택하셨습니다.%n", CarType.values()[a - 1].displayName);
}

// After
private static void selectCarType(int a) {
    car.setCarType(CarType.values()[a - 1]);
    System.out.printf("차량 타입으로 %s을 선택하셨습니다.%n", car.getCarType().displayName);
}
```

### isValidCheck — Car 객체 기준

```java
static boolean isValidCheck() {
    CarType        carType = car.getCarType();
    Engine         engine  = car.getEngine();
    BrakeSystem    brake   = car.getBrakeSystem();
    SteeringSystem steer   = car.getSteeringSystem();

    if (carType == CarType.SEDAN && brake  == BrakeSystem.CONTINENTAL) return false;
    if (carType == CarType.SUV   && engine == Engine.TOYOTA)           return false;
    if (carType == CarType.TRUCK && engine == Engine.WIA)              return false;
    if (carType == CarType.TRUCK && brake  == BrakeSystem.MANDO)      return false;
    if (brake   == BrakeSystem.BOSCH && steer != SteeringSystem.BOSCH) return false;
    return true;
}
```

### 처음 화면 복귀 — car.reset()

```java
// Before
step = CarType_Q;  // stack은 그대로 남아있었음 (버그 가능성)

// After
step = CarType_Q;
car.reset();
```

---

## 4-3. AssembleTest 수정

`stack` 직접 접근 → `car` 객체 세팅으로 전환.

```java
// Before
Assemble.stack[CAR_TYPE] = SEDAN;
Assemble.stack[BRAKE]    = CONTINENTAL;

// After
Assemble.car.setCarType(CarType.SEDAN);
Assemble.car.setBrakeSystem(BrakeSystem.CONTINENTAL);
```

`@BeforeEach` 변경:
```java
// Before
Arrays.fill(Assemble.stack, 0);

// After
Assemble.car.reset();
```

---

## 설계 결정

| 결정 | 이유 |
|------|------|
| `Car` 필드 기본값 `null` | 미선택 명시적 표현, 0 마법 숫자 제거 |
| `car` 필드 `package-private` | 기존 테스트 접근 방식 유지 (Phase 5 교체 전) |
| `reset()` 메서드 추가 | 처음 화면 복귀 시 기존 코드의 잠재 버그 제거 |
| `int[] stack` 완전 제거 | 이 Phase 완료 후 `stack` 관련 코드 0줄 |

---

## Phase 4 완료 후 상태

- `int[] stack` 제거
- `Car` 객체 1개로 조립 상태 관리
- `AssembleTest` — `stack` 접근 없이 `Car` 객체 기준으로 수정
- `./gradlew test` 25개 통과 유지
