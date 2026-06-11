import model.BrakeSystem;
import model.CarType;
import model.Engine;
import model.SteeringSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ⚠️ 임시 안전망 테스트 — Car 객체 직접 접근으로 구현에 결합됨.
 * Phase 5 완료 후 CompatibilityCheckerTest(행동 기반)로 교체 예정.
 */
class AssembleTest {

    private static final int CAR_TYPE_STEP = 0;
    private static final int ENGINE_STEP   = 1;
    private static final int BRAKE_STEP    = 2;
    private static final int STEERING_STEP = 3;
    private static final int RUN_TEST_STEP = 4;

    @BeforeEach
    void setUp() {
        Assemble.car.reset();
    }

    private String captureOutput(Runnable action) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return out.toString();
    }

    // ── isValidCheck ────────────────────────────────────────────

    @Nested
    @DisplayName("isValidCheck — 호환성 규칙")
    class IsValidCheckTest {

        @Test @DisplayName("Sedan + Continental → 불가")
        void sedan_with_continental_brake_fails() {
            Assemble.car.setCarType(CarType.SEDAN);
            Assemble.car.setBrakeSystem(BrakeSystem.CONTINENTAL);
            assertFalse(Assemble.isValidCheck());
        }

        @Test @DisplayName("SUV + TOYOTA → 불가")
        void suv_with_toyota_engine_fails() {
            Assemble.car.setCarType(CarType.SUV);
            Assemble.car.setEngine(Engine.TOYOTA);
            assertFalse(Assemble.isValidCheck());
        }

        @Test @DisplayName("Truck + WIA → 불가")
        void truck_with_wia_engine_fails() {
            Assemble.car.setCarType(CarType.TRUCK);
            Assemble.car.setEngine(Engine.WIA);
            assertFalse(Assemble.isValidCheck());
        }

        @Test @DisplayName("Truck + MANDO → 불가")
        void truck_with_mando_brake_fails() {
            Assemble.car.setCarType(CarType.TRUCK);
            Assemble.car.setBrakeSystem(BrakeSystem.MANDO);
            assertFalse(Assemble.isValidCheck());
        }

        @Test @DisplayName("BOSCH 제동 + MOBIS 조향 → 불가")
        void bosch_brake_requires_bosch_steering_fails() {
            Assemble.car.setBrakeSystem(BrakeSystem.BOSCH);
            Assemble.car.setSteeringSystem(SteeringSystem.MOBIS);
            assertFalse(Assemble.isValidCheck());
        }

        @Test @DisplayName("유효 조합 (Sedan + GM + MANDO + BOSCH) → 통과")
        void valid_combination_passes() {
            Assemble.car.setCarType(CarType.SEDAN);
            Assemble.car.setEngine(Engine.GM);
            Assemble.car.setBrakeSystem(BrakeSystem.MANDO);
            Assemble.car.setSteeringSystem(SteeringSystem.BOSCH);
            assertTrue(Assemble.isValidCheck());
        }
    }

    // ── isValidRange ────────────────────────────────────────────

    @Nested
    @DisplayName("isValidRange — 입력 범위 검증")
    class IsValidRangeTest {

        @Test @DisplayName("CarType: 유효 범위(1~3)")
        void carType_valid_range() {
            captureOutput(() -> assertTrue(Assemble.isValidRange(CAR_TYPE_STEP, 1)));
            captureOutput(() -> assertTrue(Assemble.isValidRange(CAR_TYPE_STEP, 3)));
        }

        @Test @DisplayName("CarType: 무효 범위(0, 4)")
        void carType_invalid_range() {
            captureOutput(() -> assertFalse(Assemble.isValidRange(CAR_TYPE_STEP, 0)));
            captureOutput(() -> assertFalse(Assemble.isValidRange(CAR_TYPE_STEP, 4)));
        }

        @Test @DisplayName("Engine: 유효 범위(0~4)")
        void engine_valid_range() {
            captureOutput(() -> assertTrue(Assemble.isValidRange(ENGINE_STEP, 0)));
            captureOutput(() -> assertTrue(Assemble.isValidRange(ENGINE_STEP, 4)));
        }

        @Test @DisplayName("Engine: 무효 범위(5)")
        void engine_invalid_range() {
            captureOutput(() -> assertFalse(Assemble.isValidRange(ENGINE_STEP, 5)));
        }

        @Test @DisplayName("BrakeSystem: 유효 범위(0~3)")
        void brake_valid_range() {
            captureOutput(() -> assertTrue(Assemble.isValidRange(BRAKE_STEP, 0)));
            captureOutput(() -> assertTrue(Assemble.isValidRange(BRAKE_STEP, 3)));
        }

        @Test @DisplayName("BrakeSystem: 무효 범위(4)")
        void brake_invalid_range() {
            captureOutput(() -> assertFalse(Assemble.isValidRange(BRAKE_STEP, 4)));
        }

        @Test @DisplayName("SteeringSystem: 유효 범위(0~2)")
        void steering_valid_range() {
            captureOutput(() -> assertTrue(Assemble.isValidRange(STEERING_STEP, 0)));
            captureOutput(() -> assertTrue(Assemble.isValidRange(STEERING_STEP, 2)));
        }

        @Test @DisplayName("SteeringSystem: 무효 범위(3)")
        void steering_invalid_range() {
            captureOutput(() -> assertFalse(Assemble.isValidRange(STEERING_STEP, 3)));
        }

        @Test @DisplayName("RunTest: 유효 범위(0~2)")
        void runTest_valid_range() {
            captureOutput(() -> assertTrue(Assemble.isValidRange(RUN_TEST_STEP, 0)));
            captureOutput(() -> assertTrue(Assemble.isValidRange(RUN_TEST_STEP, 2)));
        }

        @Test @DisplayName("RunTest: 무효 범위(3)")
        void runTest_invalid_range() {
            captureOutput(() -> assertFalse(Assemble.isValidRange(RUN_TEST_STEP, 3)));
        }
    }

    // ── testProducedCar ─────────────────────────────────────────

    @Nested
    @DisplayName("testProducedCar — 조합 테스트 결과")
    class TestProducedCarTest {

        @Test @DisplayName("Sedan + Continental → FAIL")
        void sedan_continental_fail() {
            Assemble.car.setCarType(CarType.SEDAN);
            Assemble.car.setBrakeSystem(BrakeSystem.CONTINENTAL);
            String out = captureOutput(Assemble::testProducedCar);
            assertTrue(out.contains("FAIL"));
        }

        @Test @DisplayName("SUV + TOYOTA → FAIL")
        void suv_toyota_fail() {
            Assemble.car.setCarType(CarType.SUV);
            Assemble.car.setEngine(Engine.TOYOTA);
            String out = captureOutput(Assemble::testProducedCar);
            assertTrue(out.contains("FAIL"));
        }

        @Test @DisplayName("Truck + WIA → FAIL")
        void truck_wia_fail() {
            Assemble.car.setCarType(CarType.TRUCK);
            Assemble.car.setEngine(Engine.WIA);
            String out = captureOutput(Assemble::testProducedCar);
            assertTrue(out.contains("FAIL"));
        }

        @Test @DisplayName("Truck + MANDO → FAIL")
        void truck_mando_fail() {
            Assemble.car.setCarType(CarType.TRUCK);
            Assemble.car.setBrakeSystem(BrakeSystem.MANDO);
            String out = captureOutput(Assemble::testProducedCar);
            assertTrue(out.contains("FAIL"));
        }

        @Test @DisplayName("BOSCH 제동 + MOBIS 조향 → FAIL")
        void bosch_mobis_fail() {
            Assemble.car.setBrakeSystem(BrakeSystem.BOSCH);
            Assemble.car.setSteeringSystem(SteeringSystem.MOBIS);
            String out = captureOutput(Assemble::testProducedCar);
            assertTrue(out.contains("FAIL"));
        }

        @Test @DisplayName("유효 조합 → PASS")
        void valid_combination_pass() {
            Assemble.car.setCarType(CarType.SEDAN);
            Assemble.car.setEngine(Engine.GM);
            Assemble.car.setBrakeSystem(BrakeSystem.MANDO);
            Assemble.car.setSteeringSystem(SteeringSystem.BOSCH);
            String out = captureOutput(Assemble::testProducedCar);
            assertTrue(out.contains("PASS"));
        }
    }

    // ── runProducedCar ──────────────────────────────────────────

    @Nested
    @DisplayName("runProducedCar — 차량 실행")
    class RunProducedCarTest {

        @Test @DisplayName("고장난 엔진 → '고장' 출력")
        void broken_engine_does_not_run() {
            Assemble.car.setCarType(CarType.SEDAN);
            Assemble.car.setEngine(Engine.BROKEN);
            Assemble.car.setBrakeSystem(BrakeSystem.MANDO);
            Assemble.car.setSteeringSystem(SteeringSystem.BOSCH);
            String out = captureOutput(Assemble::runProducedCar);
            assertTrue(out.contains("고장"));
        }

        @Test @DisplayName("비호환 조합 → '동작되지 않습니다' 출력")
        void incompatible_combination_does_not_run() {
            Assemble.car.setCarType(CarType.SEDAN);
            Assemble.car.setEngine(Engine.GM);
            Assemble.car.setBrakeSystem(BrakeSystem.CONTINENTAL);
            Assemble.car.setSteeringSystem(SteeringSystem.BOSCH);
            String out = captureOutput(Assemble::runProducedCar);
            assertTrue(out.contains("동작되지 않습니다"));
        }

        @Test @DisplayName("유효 조합 → '동작됩니다' 출력")
        void valid_combination_runs() {
            Assemble.car.setCarType(CarType.SEDAN);
            Assemble.car.setEngine(Engine.GM);
            Assemble.car.setBrakeSystem(BrakeSystem.MANDO);
            Assemble.car.setSteeringSystem(SteeringSystem.BOSCH);
            String out = captureOutput(Assemble::runProducedCar);
            assertTrue(out.contains("동작됩니다"));
        }
    }
}
