import service.CarAssembler;
import ui.ConsoleUI;

import java.util.Scanner;

public class Assemble {
    public static void main(String[] args) {
        new CarAssembler(new ConsoleUI(new Scanner(System.in))).run();
    }
}
