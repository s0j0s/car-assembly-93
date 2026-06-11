package rule;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompatibilityCheckerTest {

    private CompatibilityChecker checker;

    @BeforeEach
    void setUp() {
        checker = new CompatibilityChecker();
    }

    private Car car(CarType ct, Engine e, BrakeSystem b, SteeringSystem s) {
        Car car = new Car();
        car.setCarType(ct);
        car.setEngine(e);
        car.setBrakeSystem(b);
        car.setSteeringSystem(s);
        return car;
    }

    // ── isValid — 호환성 실패 케이스 ────────────────────────────

    @Nested
    @DisplayName("isValid — 호환성 규칙 위반")
    class InvalidCombinations {

        @Test @DisplayName("Sedan + Continental → 불가")
        void sedan_continental_invalid() {
            assertFalse(checker.isValid(car(CarType.SEDAN, Engine.GM, BrakeSystem.CONTINENTAL, SteeringSystem.BOSCH)));
        }

        @Test @DisplayName("SUV + TOYOTA → 불가")
        void suv_toyota_invalid() {
            assertFalse(checker.isValid(car(CarType.SUV, Engine.TOYOTA, BrakeSystem.MANDO, SteeringSystem.BOSCH)));
        }

        @Test @DisplayName("Truck + WIA → 불가")
        void truck_wia_invalid() {
            assertFalse(checker.isValid(car(CarType.TRUCK, Engine.WIA, BrakeSystem.BOSCH, SteeringSystem.BOSCH)));
        }

        @Test @DisplayName("Truck + MANDO → 불가")
        void truck_mando_invalid() {
            assertFalse(checker.isValid(car(CarType.TRUCK, Engine.GM, BrakeSystem.MANDO, SteeringSystem.BOSCH)));
        }

        @Test @DisplayName("BOSCH 제동 + MOBIS 조향 → 불가")
        void bosch_brake_mobis_steering_invalid() {
            assertFalse(checker.isValid(car(CarType.SEDAN, Engine.GM, BrakeSystem.BOSCH, SteeringSystem.MOBIS)));
        }
    }

    // ── isValid — 정상 케이스 ────────────────────────────────────

    @Nested
    @DisplayName("isValid — 유효 조합")
    class ValidCombinations {

        @Test @DisplayName("Sedan + GM + MANDO + BOSCH → 유효")
        void sedan_gm_mando_bosch_valid() {
            assertTrue(checker.isValid(car(CarType.SEDAN, Engine.GM, BrakeSystem.MANDO, SteeringSystem.BOSCH)));
        }

        @Test @DisplayName("SUV + GM + BOSCH + BOSCH → 유효")
        void suv_gm_bosch_bosch_valid() {
            assertTrue(checker.isValid(car(CarType.SUV, Engine.GM, BrakeSystem.BOSCH, SteeringSystem.BOSCH)));
        }

        @Test @DisplayName("Truck + GM + BOSCH + BOSCH → 유효")
        void truck_gm_bosch_bosch_valid() {
            assertTrue(checker.isValid(car(CarType.TRUCK, Engine.GM, BrakeSystem.BOSCH, SteeringSystem.BOSCH)));
        }

        @Test @DisplayName("Sedan + WIA + MANDO + MOBIS → 유효")
        void sedan_wia_mando_mobis_valid() {
            assertTrue(checker.isValid(car(CarType.SEDAN, Engine.WIA, BrakeSystem.MANDO, SteeringSystem.MOBIS)));
        }
    }

    // ── validate — 위반 메시지 검증 ──────────────────────────────

    @Nested
    @DisplayName("validate — 위반 메시지")
    class ViolationMessages {

        @Test @DisplayName("위반 시 메시지 포함")
        void validate_returns_violation_message() {
            List<String> violations = checker.validate(
                car(CarType.SEDAN, Engine.GM, BrakeSystem.CONTINENTAL, SteeringSystem.BOSCH));
            assertEquals(1, violations.size());
            assertTrue(violations.get(0).contains("Continental"));
        }

        @Test @DisplayName("유효 조합 → 빈 리스트")
        void validate_returns_empty_for_valid() {
            List<String> violations = checker.validate(
                car(CarType.SEDAN, Engine.GM, BrakeSystem.MANDO, SteeringSystem.BOSCH));
            assertTrue(violations.isEmpty());
        }

        @Test @DisplayName("위반 메시지에 규칙 원인 명시")
        void validate_message_describes_cause() {
            List<String> violations = checker.validate(
                car(CarType.SUV, Engine.TOYOTA, BrakeSystem.MANDO, SteeringSystem.BOSCH));
            assertEquals(1, violations.size());
            assertTrue(violations.get(0).contains("TOYOTA"));
        }
    }
}
