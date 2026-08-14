package boundary;

import control.FrontDeskController;
import control.FrontDeskReports;
import entity.Reservation;
import java.util.Scanner;


public class FrontDeskUI {

    private Scanner scanner;

    private FrontDeskController controller;

    private FrontDeskReports reports;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FrontDeskUI(FrontDeskController controller) {

        scanner = new Scanner(System.in);

        this.controller = controller;

        reports = new FrontDeskReports(controller);
    }


    // =====================================================
    // MAIN
    // =====================================================

    public static void main(String[] args) {

        System.out.println(
                "FrontDeskUI should be started from HotelMain.");

    }


    // =====================================================
    // RUN
    // =====================================================

    public void runFrontDesk() {

        boolean running = true;

        while (running) {

            displayMenu();

            int choice = readInt(
                    "Enter your choice: ");

            System.out.println();

            switch (choice) {

                case 1:
                    searchReservation();
                    break;

                case 2:
                    showReservationDetails();
                    break;

                case 3:
                    showAllReservations();
                    break;

                case 4:
                    generateReservationReport();
                    break;

                case 5:
                    generateGuestReport();
                    break;

                case 6:
                    generateRoomAvailabilityReport();
                    break;

                case 0:

                    running = false;

                    System.out.println(
                            "Returning to Hotel Main Menu...");

                    break;

                default:

                    System.out.println(
                            "Invalid choice.");
            }

            System.out.println();
        }
    }


    // =====================================================
    // MENU
    // =====================================================

    private void displayMenu() {

        System.out.println(
                "\n==============================================");

        System.out.println(
                "              FRONT DESK");

        System.out.println(
                "==============================================");

        System.out.println(
                "1. Search Reservation");

        System.out.println(
                "2. View Complete Reservation Details");

        System.out.println(
                "3. View All Reservations");

        System.out.println(
                "4. Generate Reservation Report");

        System.out.println(
                "5. Generate Guest Report");

        System.out.println(
                "6. Generate Room Availability Report");

        System.out.println(
                "0. Back");

        System.out.println(
                "==============================================");
    }


    // =====================================================
    // SEARCH RESERVATION
    // =====================================================

    private void searchReservation() {

        System.out.println(
                "===== SEARCH RESERVATION =====");

        String confirmation =
                readString(
                        "Enter 8-digit confirmation number: ");

        if (!isValidConfirmationNumber(confirmation)) {

            System.out.println(
                    "Invalid confirmation number.");

            System.out.println(
                    "Confirmation number must contain "
                    + "exactly 8 digits.");

            return;
        }

        Reservation reservation =
                controller.findReservation(
                        confirmation);

        if (reservation == null) {

            System.out.println(
                    "Reservation not found.");

        } else {

            System.out.println(
                    "\nReservation found.");

            System.out.println(
                    "Confirmation : "
                    + reservation.getConfirmationNumber());

            System.out.println(
                    "Guest        : "
                    + reservation.getGuest().getName());

            System.out.println(
                    "Room Type    : "
                    + reservation.getRoomType());

            System.out.println(
                    "Status       : "
                    + reservation.getStatus());
        }
    }


    // =====================================================
    // COMPLETE DETAILS
    // =====================================================

    private void showReservationDetails() {

        System.out.println(
                "===== RESERVATION DETAILS =====");

        String confirmation =
                readString(
                        "Enter 8-digit confirmation number: ");

        if (!isValidConfirmationNumber(confirmation)) {

            System.out.println(
                    "Invalid confirmation number.");

            return;
        }

        Reservation reservation =
                controller.findReservation(
                        confirmation);

        if (reservation == null) {

            System.out.println(
                    "Reservation not found.");

            return;
        }

        System.out.println();

        System.out.println(
                controller.formatReservationDetails(
                        reservation));
    }


    // =====================================================
    // ALL RESERVATIONS
    // =====================================================

    private void showAllReservations() {

        System.out.println(
                "===== ALL RESERVATIONS =====");

        Reservation[] reservations =
                controller.getAllReservations();

        if (reservations == null
                || reservations.length == 0) {

            System.out.println(
                    "No reservations found.");

            return;
        }

        System.out.println(
                "------------------------------------------------------------");

        System.out.printf(
                "%-10s %-20s %-12s %-12s%n",
                "Confirm",
                "Guest",
                "Room Type",
                "Status");

        System.out.println(
                "------------------------------------------------------------");

        for (int i = 0;
                i < reservations.length;
                i++) {

            Reservation reservation =
                    reservations[i];

            if (reservation == null) {
                continue;
            }

            String guestName = "-";

            if (reservation.getGuest() != null) {

                guestName =
                        reservation.getGuest().getName();
            }

            System.out.printf(
                    "%-10s %-20s %-12s %-12s%n",

                    reservation.getConfirmationNumber(),

                    guestName,

                    reservation.getRoomType(),

                    reservation.getStatus());
        }

        System.out.println(
                "------------------------------------------------------------");

        System.out.println(
                "Total reservations: "
                + reservations.length);
    }


    // =====================================================
    // RESERVATION REPORT
    // =====================================================

    private void generateReservationReport() {

        System.out.println(
                reports.generateReservationReport());
    }


    // =====================================================
    // GUEST REPORT
    // =====================================================

    private void generateGuestReport() {

        System.out.println(
                reports.generateGuestReport());
    }


    // =====================================================
    // ROOM AVAILABILITY REPORT
    // =====================================================

    private void generateRoomAvailabilityReport() {

        System.out.println(
                reports.generateRoomAvailabilityReport());
    }


    // =====================================================
    // VALIDATE CONFIRMATION NUMBER
    // =====================================================

    private boolean isValidConfirmationNumber(
            String confirmation) {

        if (confirmation == null) {

            return false;
        }

        if (confirmation.length() != 8) {

            return false;
        }

        for (int i = 0;
                i < confirmation.length();
                i++) {

            if (!Character.isDigit(
                    confirmation.charAt(i))) {

                return false;
            }
        }

        return true;
    }


    // =====================================================
    // INPUT
    // =====================================================

    private String readString(String message) {

        System.out.print(message);

        return scanner.nextLine().trim();
    }


    private int readInt(String message) {

        while (true) {

            try {

                System.out.print(message);

                String input =
                        scanner.nextLine().trim();

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Please enter a valid number.");
            }
        }
    }
}