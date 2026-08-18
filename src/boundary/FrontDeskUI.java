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

        this.reports =
                new FrontDeskReports(controller);
    }


    public static void main(String[] args) {

        FrontDeskController controller =
                new FrontDeskController();

        FrontDeskUI ui =
                new FrontDeskUI(controller);

        ui.run();
    }


    public void run() {

        boolean running = true;

        while (running) {

            displayMenu();

            int choice =
                    readInt("Enter your choice: ");

            System.out.println();

            switch (choice) {

                case 1:
                    searchGuest();
                    break;

                case 2:
                    searchReservation();
                    break;

                case 3:
                    searchRoomAvailability();
                    break;

                case 4:
                    checkBill();
                    break;

                case 5:
                    showVIPGuestReport();
                    break;

                case 6:
                    showFrontDeskReport();
                    break;

                case 0:

                    running = false;

                    System.out.println(
                            "Returning to Hotel System...");

                    break;

                default:

                    System.out.println(
                            "Invalid choice.");
            }

            System.out.println();
        }
    }


    private void displayMenu() {

        System.out.println(
                "\n==============================================");

        System.out.println(
                "           FRONT DESK SERVICE");

        System.out.println(
                "==============================================");

        System.out.println(
                "1. Search Guest");

        System.out.println(
                "2. Search Reservation");

        System.out.println(
                "3. Search Room Availability");

        System.out.println(
                "4. Check Guest Bill");

        System.out.println(
                "5. View VIP Guest Report");

        System.out.println(
                "6. View Front Desk Report");

        System.out.println(
                "0. Exit");

        System.out.println(
                "==============================================");
    }


    private void searchGuest() {

        System.out.println(
                "\n===== SEARCH GUEST =====");

        System.out.println(
                "Search using 8-digit confirmation number.");

        String confirmationNumber =
                readString(
                        "Confirmation number: ");

        Reservation reservation =
                controller.findReservation(
                        confirmationNumber);

        if (reservation == null) {

            System.out.println(
                    "Reservation not found.");

            return;
        }

        Guest guest =
                reservation.getGuest();

        if (guest == null) {

            System.out.println(
                    "Guest information not available.");

            return;
        }

        System.out.println(
                "\n========== GUEST INFORMATION ==========");

        System.out.println(
                "Confirmation : "
                + reservation.getConfirmationNumber());

        System.out.println(
                "Name         : "
                + guest.getName());

        System.out.println(
                "IC/Passport  : "
                + guest.getIdentityNumber());

        System.out.println(
                "Phone        : "
                + guest.getPhone());

        System.out.println(
                "Room Type    : "
                + reservation.getRoomType());

        System.out.println(
                "Room         : "
                + getRoomNumber(reservation));

        System.out.println(
                "Status       : "
                + reservation.getStatus());

        System.out.println(
                "Check-in     : "
                + reservation.getCheckInDate());

        System.out.println(
                "Check-out    : "
                + reservation.getCheckOutDate());

        System.out.println(
                "========================================");
    }


    private void searchReservation() {

        System.out.println(
                "\n===== SEARCH RESERVATION =====");

        String confirmationNumber =
                readString(
                        "Enter 8-digit confirmation number: ");

        Reservation reservation =
                controller.findReservation(
                        confirmationNumber);

        if (reservation == null) {

            System.out.println(
                    "Reservation not found.");

            return;
        }

        System.out.println(
                controller.formatReservationDetails(
                        reservation));
    }


    private void searchRoomAvailability() {

        System.out.println(
                "\n===== ROOM AVAILABILITY =====");

        String roomType =
                readString(
                        "Enter room type "
                        + "(Standard/Deluxe/Suite): ");

        System.out.println();

        String result =
                controller.searchAvailableRooms(
                        roomType);

        System.out.println(result);
    }


    private void checkBill() {

        System.out.println(
                "\n===== CHECK GUEST BILL =====");

        String confirmationNumber =
                readString(
                        "Enter 8-digit confirmation number: ");

        String bill =
                controller.checkBill(
                        confirmationNumber);

        System.out.println();

        System.out.println(bill);
    }


    private void showVIPGuestReport() {

        System.out.println(
                "\n===== VIP GUEST REPORT =====");

        System.out.println(
                reports.generateVIPGuestReport());
    }



    private void showFrontDeskReport() {

        System.out.println(
                "\n===== FRONT DESK REPORT =====");

        System.out.println(
                reports.generateFrontDeskReport());
    }


    private String getRoomNumber(
            Reservation reservation) {

        if (reservation == null) {
            return "-";
        }

        if (reservation.getAssignedRoomId()
                == null) {

            return "-";
        }

        return reservation.getAssignedRoomId();
    }



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
                        scanner.nextLine();

                return Integer.parseInt(
                        input.trim());

            } catch (NumberFormatException e) {

                System.out.println(
                        "Please enter a valid number.");
            }
        }
    }
}