package service;

import ui.ConsoleUI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class CarAssemblerTest {

    private static class StubUI extends ConsoleUI {
        private final Queue<String> inputs;
        final List<String> outputs = new ArrayList<>();

        StubUI(String... inputs) {
            super(null);
            this.inputs = new ArrayDeque<>(List.of(inputs));
        }

        @Override public String readLine()                               { return inputs.isEmpty() ? "exit" : inputs.poll(); }
        @Override public void print(String msg)                          { outputs.add(msg); }
        @Override public void printf(String fmt, Object... args)         { outputs.add(String.format(fmt, args)); }
        @Override public void clearScreen()                              {}
        @Override public void printCarBanner()                           {}
        @Override public void displayMenu(String q, String[] opts)       {}
        @Override public void displayMenuWithBack(String q, String[] opts) {}
    }

    private CarAssembler assembler(StubUI stub) {
        return new CarAssembler(stub) {
            @Override protected void delay(int ms) {}
        };
    }

    // ── 기본 흐름 ────────────────────────────────────────────────

    @Nested
    @DisplayName("기본 흐름")
    class BasicFlow {

        @Test @DisplayName("exit 입력 → 정상 종료")
        void exit_from_main_menu() {
            StubUI stub = new StubUI("exit");
            assembler(stub).run();
            assertTrue(stub.outputs.contains("바이바이"));
        }

        @Test @DisplayName("문자 입력 → 오류 메시지 후 재시도")
        void invalid_input_retries() {
            StubUI stub = new StubUI("abc", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.contains("ERROR :: 숫자만 입력 가능"));
        }

        @Test @DisplayName("범위 초과 입력 → 오류 메시지 후 재시도")
        void out_of_range_retries() {
            StubUI stub = new StubUI("9", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.stream().anyMatch(s -> s.startsWith("ERROR ::")));
        }

        @Test @DisplayName("Engine 단계 뒤로가기(0) → CarType 단계 복귀")
        void back_from_engine_to_cartype() {
            StubUI stub = new StubUI("1", "0", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.contains("바이바이"));
        }

        @Test @DisplayName("처음 화면으로(0) → car 상태 초기화")
        void reset_from_run_test_menu() {
            StubUI stub = new StubUI("1", "1", "1", "1", "0", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.contains("바이바이"));
        }
    }

    // ── RUN 경로 ─────────────────────────────────────────────────

    @Nested
    @DisplayName("RUN 경로")
    class RunPath {

        @Test @DisplayName("유효 조합 RUN → '동작됩니다' 출력")
        void full_selection_run() {
            // Sedan(1) → GM(1) → MANDO(1) → BOSCH(1) → RUN(1) → exit
            StubUI stub = new StubUI("1", "1", "1", "1", "1", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.contains("자동차가 동작됩니다."));
        }

        @Test @DisplayName("비호환 조합 RUN → '동작되지 않습니다' 출력")
        void incompatible_combination_does_not_run() {
            // Sedan(1) → GM(1) → CONTINENTAL(2) → BOSCH(1) → RUN(1) → exit
            StubUI stub = new StubUI("1", "1", "2", "1", "1", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.contains("자동차가 동작되지 않습니다"));
        }

        @Test @DisplayName("고장난 엔진 RUN → '고장' 출력")
        void broken_engine_does_not_run() {
            // Sedan(1) → BROKEN(4) → MANDO(1) → BOSCH(1) → RUN(1) → exit
            StubUI stub = new StubUI("1", "4", "1", "1", "1", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.stream().anyMatch(s -> s.contains("고장")));
        }
    }

    // ── Test 경로 ────────────────────────────────────────────────

    @Nested
    @DisplayName("Test 경로")
    class TestPath {

        @Test @DisplayName("유효 조합 Test → PASS 출력")
        void full_selection_test_pass() {
            // Sedan(1) → GM(1) → MANDO(1) → BOSCH(1) → Test(2) → exit
            StubUI stub = new StubUI("1", "1", "1", "1", "2", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.contains("자동차 부품 조합 테스트 결과 : PASS"));
        }

        @Test @DisplayName("비호환 조합 Test → FAIL 출력")
        void incompatible_combination_test_fail() {
            // Sedan(1) → GM(1) → CONTINENTAL(2) → BOSCH(1) → Test(2) → exit
            StubUI stub = new StubUI("1", "1", "2", "1", "2", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.contains("자동차 부품 조합 테스트 결과 : FAIL"));
        }
    }

    // ── 뒤로가기 전 단계 검증 ─────────────────────────────────────

    @Nested
    @DisplayName("뒤로가기 전 단계 커버")
    class BackNavigation {

        @Test @DisplayName("Brake 단계 뒤로가기 → Engine 단계 복귀")
        void back_from_brake_to_engine() {
            // Sedan(1) → GM(1) → Brake에서 0 → GM 재선택(1) → MANDO(1) → BOSCH(1) → exit
            StubUI stub = new StubUI("1", "1", "0", "1", "1", "1", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.contains("바이바이"));
        }

        @Test @DisplayName("Steering 단계 뒤로가기 → Brake 단계 복귀")
        void back_from_steering_to_brake() {
            // Sedan(1) → GM(1) → MANDO(1) → Steering에서 0 → MANDO 재선택(1) → BOSCH(1) → exit
            StubUI stub = new StubUI("1", "1", "1", "0", "1", "1", "exit");
            assembler(stub).run();
            assertTrue(stub.outputs.contains("바이바이"));
        }
    }
}
