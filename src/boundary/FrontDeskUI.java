package boundary;

import control.FrontDeskController;
import entity.Reservation;
import java.util.Scanner;

/**
 * FrontDeskUI.java
 *
 * BOUNDARY class for Front Desk module.
 *
 * @author Tan Jun Ren
 */
public class FrontDeskUI {

    private Scanner scanner;

    private FrontDeskController controller;

    public FrontDeskUI(
            FrontDeskController controller) {

        this.scanner =
                new Scanner(System.in);

        this.controller =
                controller;
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
                    searchReservation();
                    break;

                case 2:
                    checkRoomAvailability();
                    break;

                case 3:
                    checkBill();
                    break;

                case 4:
                    checkOutGuest();
                    break;

                case 5:
                    showReport();
                    break;

                case 6:
                    showTotalCheckOut();
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
                "\n========================================");

        System.out.println(
                "          FRONT DESK SERVICE");

        System.out.println(
                "========================================");

        System.out.println(
                "1. Search Guest / Reservation");

        System.out.println(
                "2. Check Room Availability");

        System.out.println(
                "3. Check Bill");

        System.out.println(
                "4. Check Out Guest");

        System.out.println(
                "5. Front Desk Report");

        System.out.println(
                "6. Total Check-Out");

        System.out.println(
                "0. Return to Hotel Menu");

        System.out.println(
                "========================================");
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

        Reservation reservation =
                controller.findReservation(
                        confirmation);

        if (reservation == null) {

            System.out.println(
                    "Reservation not found.");

            return;
        }

        System.out.println(
                controller.formatReservationDetails(
                        reservation));
    }

    // =====================================================
    // ROOM AVAILABILITY
    // =====================================================

    private void checkRoomAvailability() {

        System.out.println(
                "===== CHECK ROOM AVAILABILITY =====");

        String roomType =
                readString(
                        "Enter room type "
                        + "(Standard/Deluxe/Suite): ");

        System.out.println(
                controller.checkRoomAvailability(
                        roomType));
    }

    // =====================================================
    // CHECK BILL
    // =====================================================

    private void checkBill() {

        System.out.println(
                "===== CHECK BILL =====");

        String confirmation =
                readString(
                        "Enter 8-digit confirmation number: ");

        System.out.println(
                controller.checkBill(
                        confirmation));
    }

    // =====================================================
    // CHECK OUT
    // =====================================================

    private void checkOutGuest() {

        System.out.println(
                "===== CHECK OUT GUEST =====");

        String confirmation =
                readString(
                        "Enter 8-digit confirmation number: ");

        String result =
                controller.checkOutGuest(
                        confirmation);

        System.out.println();
        System.out.println(result);
    }

    // =====================================================
    // REPORT
    // =====================================================

    private void showReport() {

        System.out.println(
                controller.generateFrontDeskReport());
    }

    // =====================================================
    // TOTAL CHECK OUT
    // =====================================================

    private void showTotalCheckOut() {

        int total =
                controller.getTotalCheckOut();

        System.out.println(
                "========================================");

        System.out.println(
                "       TOTAL CHECK-OUT REPORT");

        System.out.println(
                "========================================");

        System.out.println(
                "Total guests checked out: "
                + total);

        System.out.println(
                "========================================");
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