package boundary;

import control.HousekeepingController;
import entity.HousekeepingStatus;

import java.util.Scanner;

/**
 * ConsoleUI.java
 * BOUNDARY (console): Housekeeping text menu.
 *
 * 1 Update status, 2 Undo, 3 Redo, 4 View all rooms, 0 Return to hotel menu
 */
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
                    ConsoleStyle.clear();
                    updateRoomStatus();
                    ConsoleStyle.pause(scanner);
                    break;
                case "2":
                    ConsoleStyle.clear();
                    undoLastAction();
                    ConsoleStyle.pause(scanner);
                    break;
                case "3":
                    ConsoleStyle.clear();
                    redoLastAction();
                    ConsoleStyle.pause(scanner);
                    break;
                case "4":
                    ConsoleStyle.clear();
                    viewAllRooms();
                    ConsoleStyle.pause(scanner);
                    break;
                case "0":
                    ConsoleStyle.info("Returning to hotel menu...");
                    return;
                default:
                    ConsoleStyle.error("Invalid option. Please try again.");
                    ConsoleStyle.pause(scanner);
            }
        }
    }

    private void printMenu() {
        ConsoleStyle.header("HOUSEKEEPING", "Room status · undo / redo · task log");
        ConsoleStyle.menuItem(1, "Update Room Status");
        ConsoleStyle.menuItem(2, "Undo Last Action");
        ConsoleStyle.menuItem(3, "Redo Last Action");
        ConsoleStyle.menuItem(4, "View All Rooms");
        ConsoleStyle.menuExit(0, "Return to hotel menu");
        ConsoleStyle.prompt("Choose an option: ");
    }

    private void updateRoomStatus() {
        viewAllRooms();
        ConsoleStyle.prompt("Enter room ID: ");
        String roomId = scanner.nextLine().trim();
        ConsoleStyle.section("Select new status");
        HousekeepingStatus[] statuses = HousekeepingStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            ConsoleStyle.menuItem(i + 1, statuses[i].toString());
        }

        int statusIndex = -1;
        while (true) {
            ConsoleStyle.prompt("Enter status number: ");
            String input = scanner.nextLine().trim();
            try {
                statusIndex = Integer.parseInt(input) - 1;
                if (statusIndex >= 0 && statusIndex < statuses.length) {
                    break;
                }
                ConsoleStyle.warn("Please enter a number between 1 and " + statuses.length + ".");
            } catch (NumberFormatException e) {
                ConsoleStyle.error("Invalid number. Please enter a valid status number.");
            }
        }

        HousekeepingStatus newStatus = statuses[statusIndex];
        ConsoleStyle.prompt("Enter staff name: ");
        String staffName = scanner.nextLine().trim();
        ConsoleStyle.prompt("Enter note: ");
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

    private void viewAllRooms() {
        Reporter.printAllRooms(controller.getAllRooms());
    }
}
