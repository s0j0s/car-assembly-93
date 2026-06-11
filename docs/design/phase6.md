# Phase 6 — UI / Service 분리

> 목적: `Assemble.java` God Class 해체.
> UI 출력/입력 → `ConsoleUI`, 조립 흐름 제어 → `CarAssembler`, 진입점 → `Assemble`.
> God Class, Long Method, Duplicate Code(메뉴) 해소.

---

## 6-1. ConsoleUI 클래스

```java
package ui;

public class ConsoleUI {
    private final Scanner scanner;

    public ConsoleUI(Scanner scanner) { this.scanner = scanner; }

    public void clearScreen() { ... }

    public void displayMenu(String title, String[] options) {
        System.out.println(title);
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%d. %s%n", i + 1, options[i]);
        }
        System.out.println("===============================");
    }

    public void displayMenuWithBack(String title, String[] options) {
        System.out.println(title);
        System.out.println("0. 뒤로가기");
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%d. %s%n", i + 1, options[i]);
        }
        System.out.println("===============================");
    }

    public String readLine() { return scanner.nextLine().trim(); }
    public void print(String msg) { System.out.println(msg); }
    public void printf(String fmt, Object... args) { System.out.printf(fmt, args); }
}
```

**책임:**
- 화면 출력 전담 (`displayMenu`, `displayMenuWithBack`)
- 입력 읽기 (`readLine`)
- `show*Menu()` 4개 메서드 → `displayMenu/displayMenuWithBack` 단일 메서드로 통합

---

## 6-2. CarAssembler 클래스

```java
package service;

public class CarAssembler {
    private final ConsoleUI ui;
    private final CompatibilityChecker checker;
    private final Car car;

    public CarAssembler(ConsoleUI ui) {
        this.ui      = ui;
        this.checker = new CompatibilityChecker();
        this.car     = new Car();
    }

    public void run() { /* 기존 main() 루프 이전 */ }

    private int parseInput(String buf) { ... }   // NumberFormatException 처리
    private boolean isValidRange(int step, int ans) { ... }
    private void selectPart(int step, int answer) { ... }
    private void runProducedCar() { ... }
    private void testProducedCar() { ... }
}
```

**책임:**
- 조립 흐름 상태 머신 (`step` 관리)
- 입력 파싱 + 범위 검증
- 부품 선택 위임
- RUN / Test 실행

**생성자 주입 이유:** `ConsoleUI`를 주입받아야 테스트 시 mock/stub 대체 가능.

---

## 6-3. Assemble.java 진입점 정리

```java
import service.CarAssembler;
import ui.ConsoleUI;
import java.util.Scanner;

public class Assemble {
    public static void main(String[] args) {
        new CarAssembler(new ConsoleUI(new Scanner(System.in))).run();
    }
}
```

3줄로 축소.

---

## 6-4. 테스트 추가 — CarAssemblerTest

`ConsoleUI`를 stub으로 주입해 `CarAssembler` 단독 테스트.

```java
package service;

class CarAssemblerTest {
    // ConsoleUI stub: 미리 정의된 입력 시퀀스 반환
    private ConsoleUI stubUI(String... inputs) {
        Queue<String> queue = new ArrayDeque<>(Arrays.asList(inputs));
        return new ConsoleUI(null) {
            @Override public String readLine() { return queue.poll(); }
            @Override public void print(String msg) {}
            @Override public void printf(String fmt, Object... args) {}
            @Override public void clearScreen() {}
            @Override public void displayMenu(...) {}
            @Override public void displayMenuWithBack(...) {}
        };
    }
}
```

**테스트 케이스:**
| 테스트명 | 입력 시퀀스 | 검증 |
|---------|-----------|------|
| `exit_from_main_menu` | `"exit"` | 정상 종료 |
| `invalid_input_retries` | `"abc"`, `"1"`, `"exit"` | 숫자 오류 후 재시도 |
| `out_of_range_retries` | `"9"`, `"1"`, `"exit"` | 범위 오류 후 재시도 |
| `back_navigation` | `"1"`, `"0"`, `"exit"` | 뒤로가기 동작 |
| `full_selection_run` | `"1"`,`"1"`,`"1"`,`"1"`,`"1"`,`"exit"` | 전체 조립 후 RUN |
| `full_selection_test_pass` | `"1"`,`"1"`,`"1"`,`"1"`,`"2"`,`"exit"` | 전체 조립 후 Test PASS |

---

## 설계 결정

| 결정 | 이유 |
|------|------|
| `ConsoleUI` 생성자에 `Scanner` 주입 | 테스트 시 커스텀 Scanner 주입 가능 |
| `CarAssembler` 생성자에 `ConsoleUI` 주입 | UI stub으로 흐름 테스트 가능 |
| `car`, `checker` CarAssembler 내부 생성 | 외부 노출 불필요, 테스트는 UI 동작만 검증 |
| `Assemble.java` 3줄 유지 | 진입점만 담당, 로직 없음 |

---

## Phase 6 완료 후 상태

```
src/main/java/
  model/          Car, CarType, Engine, BrakeSystem, SteeringSystem
  rule/           CompatibilityRule, CompatibilityChecker
  service/        CarAssembler
  ui/             ConsoleUI
  Assemble.java   (3줄)

src/test/java/
  rule/           CompatibilityCheckerTest  (12개, 행동 기반)
  service/        CarAssemblerTest          (6개+, 흐름 기반)
```

- `Assemble.java` 내 로직 0줄
- 코드 스멜 17개 중 God Class, Long Method, Duplicate Code(메뉴), Feature Envy 해소
- `./gradlew test` 전체 통과
