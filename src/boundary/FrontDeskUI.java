package boundary;

import control.FrontDeskController;
import control.FrontDeskReports;
import entity.Guest;
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
        app.HotelMain.main(args);
    }

    public void run() {
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = readInt("Enter your choice: ");
            ConsoleStyle.blank();

            switch (choice) {
                case 1:
                    ConsoleStyle.clear();
                    searchGuest();
                    ConsoleStyle.pause(scanner);
                    break;
                case 2:
                    ConsoleStyle.clear();
                    searchReservation();
                    ConsoleStyle.pause(scanner);
                    break;
                case 3:
                    ConsoleStyle.clear();
                    searchRoomAvailability();
                    ConsoleStyle.pause(scanner);
                    break;
                case 4:
                    ConsoleStyle.clear();
                    checkBill();
                    ConsoleStyle.pause(scanner);
                    break;
                case 5:
                    ConsoleStyle.clear();
                    showVIPGuestReport();
                    ConsoleStyle.pause(scanner);
                    break;
                case 6:
                    ConsoleStyle.clear();
                    showFrontDeskReport();
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
        ConsoleStyle.header("FRONT DESK", "Guest · reservation · rooms · billing · reports");
        ConsoleStyle.menuItem(1, "Search Guest");
        ConsoleStyle.menuItem(2, "Search Reservation");
        ConsoleStyle.menuItem(3, "Search Room Availability");
        ConsoleStyle.menuItem(4, "Check Guest Bill");
        ConsoleStyle.menuItem(5, "View VIP Guest Report");
        ConsoleStyle.menuItem(6, "View Front Desk Report");
        ConsoleStyle.menuExit(0, "Back");
    }

    private void searchGuest() {
        ConsoleStyle.section("Search guest");
        ConsoleStyle.info("Search using 8-digit confirmation number.");
        String confirmationNumber = readString("Confirmation number: ");
        if (!controller.isValidConfirmationNumber(confirmationNumber)) {
            ConsoleStyle.error("Invalid confirmation number.");
            ConsoleStyle.info("Confirmation number must contain exactly 8 digits.");
            return;
        }

        Reservation reservation = controller.findReservation(confirmationNumber);
        if (reservation == null) {
            ConsoleStyle.warn("Reservation not found.");
            return;
        }

        Guest guest = reservation.getGuest();
        if (guest == null) {
            ConsoleStyle.warn("Guest information not available.");
            return;
        }

        ConsoleStyle.success("Guest found.");
        ConsoleStyle.keyValue("Confirmation", reservation.getConfirmationNumber());
        ConsoleStyle.keyValue("Name", guest.getName());
        ConsoleStyle.keyValue("IC/Passport", guest.getIdentityNumber());
        ConsoleStyle.keyValue("Phone", guest.getPhone());
        ConsoleStyle.keyValue("Room Type", reservation.getRoomType());
        ConsoleStyle.keyValue("Room", getRoomNumber(reservation));
        ConsoleStyle.keyValue("Status", String.valueOf(reservation.getStatus()));
        ConsoleStyle.keyValue("Check-in", String.valueOf(reservation.getCheckInDate()));
        ConsoleStyle.keyValue("Check-out", String.valueOf(reservation.getCheckOutDate()));
    }

    private void searchReservation() {
        ConsoleStyle.section("Search reservation");
        String confirmationNumber = readString("Enter 8-digit confirmation number: ");
        if (!controller.isValidConfirmationNumber(confirmationNumber)) {
            ConsoleStyle.error("Invalid confirmation number.");
            ConsoleStyle.info("Confirmation number must contain exactly 8 digits.");
            return;
        }

        Reservation reservation = controller.findReservation(confirmationNumber);
        if (reservation == null) {
            ConsoleStyle.warn("Reservation not found.");
            return;
        }

        ConsoleStyle.success("Reservation found.");
        ConsoleStyle.info(controller.formatReservationDetails(reservation));
    }

    private void searchRoomAvailability() {
        ConsoleStyle.section("Room availability");
        String roomType = readString("Enter room type (Standard/Deluxe/Suite): ");
        ConsoleStyle.blank();
        ConsoleStyle.info(controller.searchRoomAvailability(roomType));
    }

    private void checkBill() {
        ConsoleStyle.section("Check guest bill");
        String confirmationNumber = readString("Enter 8-digit confirmation number: ");
        if (!controller.isValidConfirmationNumber(confirmationNumber)) {
            ConsoleStyle.error("Invalid confirmation number.");
            ConsoleStyle.info("Confirmation number must contain exactly 8 digits.");
            return;
        }
        ConsoleStyle.blank();
        ConsoleStyle.info(controller.getGuestBill(confirmationNumber));
    }

    private void showVIPGuestReport() {
        ConsoleStyle.section("VIP guest report");
        ConsoleStyle.info(reports.generateVIPGuestReport());
    }

    private void showFrontDeskReport() {
        ConsoleStyle.section("Front desk report");
        ConsoleStyle.info(reports.generateFrontDeskReport());
    }

    private String getRoomNumber(Reservation reservation) {
        if (reservation == null || reservation.getAssignedRoomId() == null) {
            return "-";
        }
        return reservation.getAssignedRoomId();
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
