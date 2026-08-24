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
                case "5":
                    ConsoleStyle.clear();
                    showStatusWorkloadReport();
                    ConsoleStyle.pause(scanner);
                    break;
                case "6":
                    ConsoleStyle.clear();
                    showTaskHistoryReport();
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
        ConsoleStyle.menuItem(5, "Status Workload Report");
        ConsoleStyle.menuItem(6, "Task History Report");
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

    private void showStatusWorkloadReport() {
        ConsoleStyle.section("Status workload report filters");
        HousekeepingStatus status = readStatusFilter();
        String roomType = readRoomTypeFilter();
        Boolean occupied = readOccupancyFilter();
        Reporter.printMessage(controller.generateStatusWorkloadReport(status, roomType, occupied));
    }

    private void showTaskHistoryReport() {
        ConsoleStyle.section("Task history report filters");
        String roomType = readRoomTypeFilter();
        ConsoleStyle.prompt("Minimum task-log entries (0 for all): ");
        int minimumEntries = readNonNegativeInteger();
        Reporter.printMessage(controller.generateTaskHistoryReport(roomType, minimumEntries));
    }

    private HousekeepingStatus readStatusFilter() {
        ConsoleStyle.prompt("Status number (0 for all): ");
        int choice = readNonNegativeInteger();
        if (choice == 0) {
            return null;
        }
        HousekeepingStatus[] statuses = HousekeepingStatus.values();
        return choice <= statuses.length ? statuses[choice - 1] : null;
    }

    private Boolean readOccupancyFilter() {
        ConsoleStyle.prompt("Occupancy (1 occupied, 2 free, 0 all): ");
        int choice = readNonNegativeInteger();
        if (choice == 1) {
            return true;
        }
        return choice == 2 ? false : null;
    }

    private String readRoomTypeFilter() {
        String[] roomTypes = { "All", "Standard", "Deluxe", "Suite" };
        ConsoleStyle.prompt("Room type: 0 All, 1 Standard, 2 Deluxe, 3 Suite: ");
        int choice = readNonNegativeInteger();
        return choice >= 1 && choice <= roomTypes.length - 1 ? roomTypes[choice] : null;
    }

    private int readNonNegativeInteger() {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Prompt again for malformed input.
            }
            ConsoleStyle.warn("Please enter a non-negative whole number.");
            ConsoleStyle.prompt("Enter number: ");
        }
    }
}
