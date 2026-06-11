package ui;

import java.util.Scanner;

public class ConsoleUI {
    private static final String CLEAR_SCREEN = "\033[H\033[2J";
    private final Scanner scanner;

    public ConsoleUI(Scanner scanner) {
        this.scanner = scanner;
    }

    public void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    public void printCarBanner() {
        System.out.println("        ______________");
        System.out.println("       /|            |");
        System.out.println("  ____/_|_____________|____");
        System.out.println(" |                      O  |");
        System.out.println(" '-(@)----------------(@)--'");
        System.out.println("===============================");
    }

    public void displayMenu(String question, String[] options) {
        System.out.println(question);
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%d. %s%n", i + 1, options[i]);
        }
        System.out.println("===============================");
    }

    public void displayMenuWithBack(String question, String[] options) {
        System.out.println(question);
        System.out.println("0. 뒤로가기");
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%d. %s%n", i + 1, options[i]);
        }
        System.out.println("===============================");
    }

    public String readLine() {
        System.out.print("INPUT > ");
        return scanner.nextLine().trim();
    }

    public void print(String msg) {
        System.out.println(msg);
    }

    public void printf(String fmt, Object... args) {
        System.out.printf(fmt, args);
    }
}
