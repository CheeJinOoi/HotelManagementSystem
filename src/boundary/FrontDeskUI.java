package boundary;

import control.FrontDeskController;
import control.FrontDeskReports;
import entity.Reservation;
import java.util.Scanner;

public class FrontDeskUI {

    private Scanner scanner;
    private FrontDeskController controller;
    private FrontDeskReports reports;

    public FrontDeskUI(FrontDeskController controller) {
        scanner = new Scanner(System.in);
        this.controller = controller;
        reports = new FrontDeskReports(controller);
    }

    public static void main(String[] args) {
        ConsoleStyle.info("FrontDeskUI should be started from HotelMain.");
    }

    public void runFrontDesk() {
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = readInt("Enter your choice: ");
            ConsoleStyle.blank();

            switch (choice) {
                case 1:
                    ConsoleStyle.clear();
                    searchReservation();
                    ConsoleStyle.pause(scanner);
                    break;
                case 2:
                    ConsoleStyle.clear();
                    showReservationDetails();
                    ConsoleStyle.pause(scanner);
                    break;
                case 3:
                    ConsoleStyle.clear();
                    showAllReservations();
                    ConsoleStyle.pause(scanner);
                    break;
                case 4:
                    ConsoleStyle.clear();
                    generateReservationReport();
                    ConsoleStyle.pause(scanner);
                    break;
                case 5:
                    ConsoleStyle.clear();
                    generateGuestReport();
                    ConsoleStyle.pause(scanner);
                    break;
                case 6:
                    ConsoleStyle.clear();
                    generateRoomAvailabilityReport();
                    ConsoleStyle.pause(scanner);
                    break;
                case 0:
                    running = false;
                    ConsoleStyle.info("Returning to hotel main menu...");
                    break;
                default:
                    ConsoleStyle.error("Invalid choice.");
                    ConsoleStyle.pause(scanner);
            }
            ConsoleStyle.blank();
        }
    }

    private void displayMenu() {
        ConsoleStyle.header("FRONT DESK", "Lookup · details · operational reports");
        ConsoleStyle.menuItem(1, "Search Reservation");
        ConsoleStyle.menuItem(2, "View Complete Reservation Details");
        ConsoleStyle.menuItem(3, "View All Reservations");
        ConsoleStyle.menuItem(4, "Generate Reservation Report");
        ConsoleStyle.menuItem(5, "Generate Guest Report");
        ConsoleStyle.menuItem(6, "Generate Room Availability Report");
        ConsoleStyle.menuExit(0, "Back");
    }

    private void searchReservation() {
        ConsoleStyle.section("Search reservation");
        String confirmation = readString("Enter 8-digit confirmation number: ");

        if (!isValidConfirmationNumber(confirmation)) {
            ConsoleStyle.error("Invalid confirmation number.");
            ConsoleStyle.info("Confirmation number must contain exactly 8 digits.");
            return;
        }

        Reservation reservation = controller.findReservation(confirmation);
        if (reservation == null) {
            ConsoleStyle.warn("Reservation not found.");
        } else {
            ConsoleStyle.success("Reservation found.");
            ConsoleStyle.keyValue("Confirmation", reservation.getConfirmationNumber());
            ConsoleStyle.keyValue("Guest", reservation.getGuest().getName());
            ConsoleStyle.keyValue("Room Type", reservation.getRoomType());
            ConsoleStyle.keyValue("Status", String.valueOf(reservation.getStatus()));
        }
    }

    private void showReservationDetails() {
        ConsoleStyle.section("Reservation details");
        String confirmation = readString("Enter 8-digit confirmation number: ");

        if (!isValidConfirmationNumber(confirmation)) {
            ConsoleStyle.error("Invalid confirmation number.");
            return;
        }

        Reservation reservation = controller.findReservation(confirmation);
        if (reservation == null) {
            ConsoleStyle.warn("Reservation not found.");
            return;
        }

        ConsoleStyle.blank();
        ConsoleStyle.info(controller.formatReservationDetails(reservation));
    }

    private void showAllReservations() {
        ConsoleStyle.section("All reservations");
        Reservation[] reservations = controller.getAllReservations();

        if (reservations == null || reservations.length == 0) {
            ConsoleStyle.warn("No reservations found.");
            return;
        }

        ConsoleStyle.tableHeader(String.format("%-10s %-20s %-12s %-12s",
                "Confirm", "Guest", "Room Type", "Status"));

        for (int i = 0; i < reservations.length; i++) {
            Reservation reservation = reservations[i];
            if (reservation == null) {
                continue;
            }
            String guestName = "-";
            if (reservation.getGuest() != null) {
                guestName = reservation.getGuest().getName();
            }
            ConsoleStyle.tableRow(String.format("%-10s %-20s %-12s %-12s",
                    reservation.getConfirmationNumber(),
                    guestName,
                    reservation.getRoomType(),
                    reservation.getStatus()));
        }

        ConsoleStyle.tableFooter("Total reservations: " + reservations.length);
    }

    private void generateReservationReport() {
        ConsoleStyle.section("Reservation report");
        ConsoleStyle.info(reports.generateReservationReport());
    }

    private void generateGuestReport() {
        ConsoleStyle.section("Guest report");
        ConsoleStyle.info(reports.generateGuestReport());
    }

    private void generateRoomAvailabilityReport() {
        ConsoleStyle.section("Room availability report");
        ConsoleStyle.info(reports.generateRoomAvailabilityReport());
    }

    private boolean isValidConfirmationNumber(String confirmation) {
        if (confirmation == null) {
            return false;
        }
        if (confirmation.length() != 8) {
            return false;
        }
        for (int i = 0; i < confirmation.length(); i++) {
            if (!Character.isDigit(confirmation.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String readString(String message) {
        ConsoleStyle.prompt(message);
        return scanner.nextLine().trim();
    }

    private int readInt(String message) {
        while (true) {
            try {
                ConsoleStyle.prompt(message);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                ConsoleStyle.error("Please enter a valid number.");
            }
        }
    }
}
