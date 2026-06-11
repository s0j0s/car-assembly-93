import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AssembleTest {

    // stack indices
    private static final int CAR_TYPE  = 0;
    private static final int ENGINE    = 1;
    private static final int BRAKE     = 2;
    private static final int STEERING  = 3;

    // CarType
    private static final int SEDAN = 1, SUV = 2, TRUCK = 3;
    // Engine
    private static final int GM = 1, TOYOTA = 2, WIA = 3, BROKEN = 4;
    // BrakeSystem
    private static final int MANDO = 1, CONTINENTAL = 2, BOSCH_B = 3;
    // SteeringSystem
    private static final int BOSCH_S = 1, MOBIS = 2;

    @BeforeEach
    void setUp() {
        Arrays.fill(Assemble.stack, 0);
    }

    // --- 호환성 실패 케이스 ---

    @Test
    @DisplayName("Sedan + Continental 제동장치 → 불가")
    void sedan_with_continental_brake_fails() {
        Assemble.stack[CAR_TYPE] = SEDAN;
        Assemble.stack[BRAKE]    = CONTINENTAL;
        assertFalse(Assemble.isValidCheck());
    }

    @Test
    @DisplayName("SUV + TOYOTA 엔진 → 불가")
    void suv_with_toyota_engine_fails() {
        Assemble.stack[CAR_TYPE] = SUV;
        Assemble.stack[ENGINE]   = TOYOTA;
        assertFalse(Assemble.isValidCheck());
    }

    @Test
    @DisplayName("Truck + WIA 엔진 → 불가")
    void truck_with_wia_engine_fails() {
        Assemble.stack[CAR_TYPE] = TRUCK;
        Assemble.stack[ENGINE]   = WIA;
        assertFalse(Assemble.isValidCheck());
    }

    @Test
    @DisplayName("Truck + MANDO 제동장치 → 불가")
    void truck_with_mando_brake_fails() {
        Assemble.stack[CAR_TYPE] = TRUCK;
        Assemble.stack[BRAKE]    = MANDO;
        assertFalse(Assemble.isValidCheck());
    }

    @Test
    @DisplayName("BOSCH 제동장치 + MOBIS 조향장치 → 불가")
    void bosch_brake_requires_bosch_steering_fails() {
        Assemble.stack[BRAKE]    = BOSCH_B;
        Assemble.stack[STEERING] = MOBIS;
        assertFalse(Assemble.isValidCheck());
    }

    // --- 정상 케이스 ---

    @Test
    @DisplayName("유효한 조합 (Sedan + GM + MANDO + BOSCH) → 통과")
    void valid_combination_passes() {
        Assemble.stack[CAR_TYPE] = SEDAN;
        Assemble.stack[ENGINE]   = GM;
        Assemble.stack[BRAKE]    = MANDO;
        Assemble.stack[STEERING] = BOSCH_S;
        assertTrue(Assemble.isValidCheck());
    }

    @Test
    @DisplayName("고장난 엔진 → '고장' 출력 포함")
    void broken_engine_does_not_run() {
        Assemble.stack[CAR_TYPE] = SEDAN;
        Assemble.stack[ENGINE]   = BROKEN;
        Assemble.stack[BRAKE]    = MANDO;
        Assemble.stack[STEERING] = BOSCH_S;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            Assemble.runProducedCar();
        } finally {
            System.setOut(original);
        }

        assertTrue(out.toString().contains("고장"));
    }
}
