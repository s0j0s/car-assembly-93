package service;

import model.*;
import rule.CompatibilityChecker;
import ui.ConsoleUI;

import java.util.Arrays;
import java.util.List;

public class CarAssembler {
    private static final int CarType_Q        = 0;
    private static final int Engine_Q         = 1;
    private static final int BrakeSystem_Q    = 2;
    private static final int SteeringSystem_Q = 3;
    private static final int Run_Test         = 4;

    private final ConsoleUI ui;
    private final CompatibilityChecker checker;
    private final Car car;

    public CarAssembler(ConsoleUI ui) {
        this.ui      = ui;
        this.checker = new CompatibilityChecker();
        this.car     = new Car();
    }

    public void run() {
        int step = CarType_Q;

        while (true) {
            ui.clearScreen();
            showMenu(step);

            String buf = ui.readLine();

            if (buf.equalsIgnoreCase("exit")) {
                ui.print("바이바이");
                break;
            }

            int answer;
            try {
                answer = Integer.parseInt(buf);
            } catch (NumberFormatException e) {
                ui.print("ERROR :: 숫자만 입력 가능");
                delay(800);
                continue;
            }

            if (!isValidRange(step, answer)) {
                delay(800);
                continue;
            }

            if (answer == 0) {
                if (step == Run_Test) {
                    step = CarType_Q;
                    car.reset();
                } else if (step > CarType_Q) {
                    step--;
                }
                continue;
            }

            switch (step) {
                case CarType_Q:
                    car.setCarType(CarType.values()[answer - 1]);
                    ui.printf("차량 타입으로 %s을 선택하셨습니다.%n", car.getCarType().displayName);
                    delay(800); step = Engine_Q; break;
                case Engine_Q:
                    car.setEngine(Engine.values()[answer - 1]);
                    ui.printf("%s 엔진을 선택하셨습니다.%n", car.getEngine().displayName);
                    delay(800); step = BrakeSystem_Q; break;
                case BrakeSystem_Q:
                    car.setBrakeSystem(BrakeSystem.values()[answer - 1]);
                    ui.printf("%s 제동장치를 선택하셨습니다.%n", car.getBrakeSystem().displayName);
                    delay(800); step = SteeringSystem_Q; break;
                case SteeringSystem_Q:
                    car.setSteeringSystem(SteeringSystem.values()[answer - 1]);
                    ui.printf("%s 조향장치를 선택하셨습니다.%n", car.getSteeringSystem().displayName);
                    delay(800); step = Run_Test; break;
                case Run_Test:
                    if (answer == 1) {
                        runProducedCar();
                        delay(2000);
                    } else if (answer == 2) {
                        ui.print("Test...");
                        delay(1500);
                        testProducedCar();
                        delay(2000);
                    }
                    break;
            }
        }
    }

    private void showMenu(int step) {
        switch (step) {
            case CarType_Q:
                ui.printCarBanner();
                ui.displayMenu("어떤 차량 타입을 선택할까요?", namesOf(CarType.values()));
                break;
            case Engine_Q:
                ui.displayMenuWithBack("어떤 엔진을 탑재할까요?", namesOf(Engine.values()));
                break;
            case BrakeSystem_Q:
                ui.displayMenuWithBack("어떤 제동장치를 선택할까요?", namesOf(BrakeSystem.values()));
                break;
            case SteeringSystem_Q:
                ui.displayMenuWithBack("어떤 조향장치를 선택할까요?", namesOf(SteeringSystem.values()));
                break;
            case Run_Test:
                ui.displayMenuWithBack("멋진 차량이 완성되었습니다.\n어떤 동작을 할까요?",
                    new String[]{"RUN", "Test"});
                break;
        }
    }

    private boolean isValidRange(int step, int ans) {
        switch (step) {
            case CarType_Q:
                if (ans < 1 || ans > CarType.values().length) {
                    ui.print("ERROR :: 차량 타입은 1 ~ " + CarType.values().length + " 범위만 선택 가능");
                    return false;
                }
                break;
            case Engine_Q:
                if (ans < 0 || ans > Engine.values().length) {
                    ui.print("ERROR :: 엔진은 0(뒤로가기) 또는 1 ~ " + Engine.values().length + " 범위만 선택 가능");
                    return false;
                }
                break;
            case BrakeSystem_Q:
                if (ans < 0 || ans > BrakeSystem.values().length) {
                    ui.print("ERROR :: 제동장치는 0(뒤로가기) 또는 1 ~ " + BrakeSystem.values().length + " 범위만 선택 가능");
                    return false;
                }
                break;
            case SteeringSystem_Q:
                if (ans < 0 || ans > SteeringSystem.values().length) {
                    ui.print("ERROR :: 조향장치는 0(뒤로가기) 또는 1 ~ " + SteeringSystem.values().length + " 범위만 선택 가능");
                    return false;
                }
                break;
            case Run_Test:
                if (ans < 0 || ans > 2) {
                    ui.print("ERROR :: 0(처음으로), 1(RUN), 2(Test) 중 선택 필요");
                    return false;
                }
                break;
        }
        return true;
    }

    private void runProducedCar() {
        if (!checker.isValid(car)) {
            ui.print("자동차가 동작되지 않습니다");
            return;
        }
        if (car.getEngine().isBroken()) {
            ui.print("엔진이 고장나있습니다.");
            ui.print("자동차가 움직이지 않습니다.");
            return;
        }
        ui.printf("Car Type : %s%n", car.getCarType().displayName);
        ui.printf("Engine   : %s%n", car.getEngine().displayName);
        ui.printf("Brake    : %s%n", car.getBrakeSystem().displayName);
        ui.printf("Steering : %s%n", car.getSteeringSystem().displayName);
        ui.print("자동차가 동작됩니다.");
    }

    private void testProducedCar() {
        List<String> violations = checker.validate(car);
        if (violations.isEmpty()) {
            ui.print("자동차 부품 조합 테스트 결과 : PASS");
        } else {
            ui.print("자동차 부품 조합 테스트 결과 : FAIL");
            violations.forEach(ui::print);
        }
    }

    protected void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static <T extends Enum<T>> String[] namesOf(T[] values) {
        return Arrays.stream(values)
            .map(e -> {
                try {
                    return (String) e.getClass().getField("displayName").get(e);
                } catch (Exception ex) {
                    throw new IllegalStateException(
                        e.getClass().getSimpleName() + " enum에 displayName 필드 없음", ex);
                }
            })
            .toArray(String[]::new);
    }
}
