package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarTest {

    @Test @DisplayName("부품 미선택 시 isFullyConfigured() → false")
    void isFullyConfigured_returns_false_when_partial() {
        Car car = new Car();
        assertFalse(car.isFullyConfigured());

        car.setCarType(CarType.SEDAN);
        assertFalse(car.isFullyConfigured());

        car.setEngine(Engine.GM);
        assertFalse(car.isFullyConfigured());

        car.setBrakeSystem(BrakeSystem.MANDO);
        assertFalse(car.isFullyConfigured());
    }

    @Test @DisplayName("4개 부품 모두 선택 시 isFullyConfigured() → true")
    void isFullyConfigured_returns_true_when_all_set() {
        Car car = new Car();
        car.setCarType(CarType.SEDAN);
        car.setEngine(Engine.GM);
        car.setBrakeSystem(BrakeSystem.MANDO);
        car.setSteeringSystem(SteeringSystem.BOSCH);
        assertTrue(car.isFullyConfigured());
    }

    @Test @DisplayName("reset() 호출 시 모든 필드 null → isFullyConfigured() → false")
    void reset_clears_all_fields() {
        Car car = new Car();
        car.setCarType(CarType.SUV);
        car.setEngine(Engine.TOYOTA);
        car.setBrakeSystem(BrakeSystem.CONTINENTAL);
        car.setSteeringSystem(SteeringSystem.MOBIS);

        car.reset();

        assertFalse(car.isFullyConfigured());
        assertNull(car.getCarType());
        assertNull(car.getEngine());
        assertNull(car.getBrakeSystem());
        assertNull(car.getSteeringSystem());
    }
}
