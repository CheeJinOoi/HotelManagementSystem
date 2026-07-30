package boundary;

import control.HousekeepingController;
import entity.HousekeepingStatus;
import entity.Room;

import java.util.Scanner;

public class ConsoleUI {
    private final HousekeepingController controller;
    private final Scanner scanner;

    public ConsoleUI(HousekeepingController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            printMenu();
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    updateRoomStatus();
                    break;
                case "2":
                    undoLastAction();
                    break;
                case "3":
                    redoLastAction();
                    break;
                case "4":
                    viewRoomDetails();
                    break;
                case "0":
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("=== TARUMT Housekeeping Menu ===");
        System.out.println("1. Update Room Status");
        System.out.println("2. Undo Last Action");
        System.out.println("3. Redo Last Action");
        System.out.println("4. View Room Details");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private void updateRoomStatus() {
        System.out.print("Enter room ID: ");
        String roomId = scanner.nextLine().trim();
        System.out.println("Choose new status:");
        HousekeepingStatus[] statuses = HousekeepingStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i]);
        }

        int statusIndex = -1;
        while (true) {
            System.out.print("Enter status number: ");
            String input = scanner.nextLine().trim();
            try {
                statusIndex = Integer.parseInt(input) - 1;
                if (statusIndex >= 0 && statusIndex < statuses.length) {
                    break;
                }
                System.out.println("Please enter a number between 1 and " + statuses.length + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid status number.");
            }
        }

        HousekeepingStatus newStatus = statuses[statusIndex];
        System.out.print("Enter staff name: ");
        String staffName = scanner.nextLine().trim();
        System.out.print("Enter note: ");
        String note = scanner.nextLine().trim();

        String result = controller.updateRoomStatus(roomId, newStatus, staffName, note);
        Reporter.printMessage(result);
    }

    private void undoLastAction() {
        String result = controller.undoLastAction();
        Reporter.printMessage(result);
    }

    private void redoLastAction() {
        String result = controller.redoLastAction();
        Reporter.printMessage(result);
    }

    private void viewRoomDetails() {
        System.out.print("Enter room ID: ");
        String roomId = scanner.nextLine().trim();
        Room room = controller.findRoomById(roomId);
        Reporter.printRoomDetails(room);
    }
}
