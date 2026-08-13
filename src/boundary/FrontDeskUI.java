package boundary;

import control.FrontDeskController;
import entity.Reservation;

import java.util.Scanner;

/**
 * FrontDeskUI
 *
 * Console interface for Front Desk staff.
 */
public class FrontDeskUI {

    private Scanner scanner;

    private FrontDeskController controller;

    public FrontDeskUI(
            FrontDeskController controller) {

        this.controller =
                controller;

        scanner =
                new Scanner(System.in);
    }

    // =====================================================
    // RUN
    // =====================================================

    public void run() {

        boolean running = true;

        while (running) {

            displayMenu();

            int choice =
                    readInt("Enter choice: ");

            System.out.println();

            switch (choice) {

                case 1:
                    searchGuest();
                    break;

                case 2:
                    showAllReservations();
                    break;

                case 3:
                    showReport();
                    break;

                case 0:

                    running = false;

                    System.out.println(
                            "Returning to Hotel Menu...");

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
                "\n======================================");

        System.out.println(
                "          FRONT DESK SYSTEM");

        System.out.println(
                "======================================");

        System.out.println(
                "1. Search Guest / Reservation");

        System.out.println(
                "2. View All Reservations");

        System.out.println(
                "3. Generate Front Desk Report");

        System.out.println(
                "0. Back");

        System.out.println(
                "======================================");
    }

    // =====================================================
    // SEARCH
    // =====================================================

    private void searchGuest() {

        System.out.println(
                "===== SEARCH GUEST =====");

        String confirmation =
                readString(
                        "Enter 8-digit confirmation number: ");

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

        if (reservations.length == 0) {

            System.out.println(
                    "No reservations found.");

            return;
        }

        for (int i = 0;
                i < reservations.length;
                i++) {

            if (reservations[i] != null) {

                System.out.println(
                        "\n------------------------------");

                System.out.println(
                        controller.formatReservationDetails(
                                reservations[i]));
            }
        }
    }

    // =====================================================
    // REPORT
    // =====================================================

    private void showReport() {

        System.out.println(
                controller.generateFrontDeskReport());
    }

    // =====================================================
    // INPUT
    // =====================================================

    private String readString(
            String message) {

        System.out.print(message);

        return scanner.nextLine().trim();
    }

    private int readInt(
            String message) {

        while (true) {

            try {

                System.out.print(message);

                return Integer.parseInt(
                        scanner.nextLine().trim());

            } catch (NumberFormatException e) {

                System.out.println(
                        "Please enter a valid number.");
            }
        }
    }
}