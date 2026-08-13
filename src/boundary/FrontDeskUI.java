package boundary;

import control.FrontDeskController;
import control.FrontDeskReports;
import entity.Guest;
import entity.Reservation;
import entity.Room;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * FrontDeskUI
 *
 * BOUNDARY class for Front Desk module.
 *
 * Handles user input and output only.
 */
public class FrontDeskUI {

    private Scanner scanner;

    private FrontDeskController controller;

    private FrontDeskReports reports;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FrontDeskUI() {

        scanner = new Scanner(System.in);

        controller =
                new FrontDeskController();

        reports =
                new FrontDeskReports(controller);
    }


    // =====================================================
    // MAIN
    // =====================================================

    public static void main(String[] args) {

        FrontDeskUI ui =
                new FrontDeskUI();

        ui.run();
    }


    // =====================================================
    // RUN
    // =====================================================

    public void run() {

        boolean running = true;

        while (running) {

            displayMenu();

            int choice =
                    readInt("Enter your choice: ");

            System.out.println();

            switch (choice) {

                case 1:
                    addGuest();
                    break;

                case 2:
                    searchGuest();
                    break;

                case 3:
                    updateGuest();
                    break;

                case 4:
                    deleteGuest();
                    break;

                case 5:
                    addReservation();
                    break;

                case 6:
                    searchReservation();
                    break;

                case 7:
                    addRoom();
                    break;

                case 8:
                    assignRoom();
                    break;

                case 9:
                    checkIn();
                    break;

                case 10:
                    checkOut();
                    break;

                case 11:
                    cancelReservation();
                    break;

                case 12:
                    showSummaryReport();
                    break;

                case 13:
                    showGuestReport();
                    break;

                case 14:
                    showReservationReport();
                    break;

                case 15:
                    showRoomReport();
                    break;

                case 0:
                    running = false;
                    System.out.println(
                            "Thank you for using "
                            + "Front Desk System.");
                    break;

                default:
                    System.out.println(
                            "Invalid choice.");
            }

            System.out.println();
        }

        scanner.close();
    }


    // =====================================================
    // MENU
    // =====================================================

    private void displayMenu() {

        System.out.println(
                "\n========================================");

        System.out.println(
                "          HOTEL FRONT DESK SYSTEM");

        System.out.println(
                "========================================");

        System.out.println(
                "1.  Add Guest");

        System.out.println(
                "2.  Search Guest");

        System.out.println(
                "3.  Update Guest");

        System.out.println(
                "4.  Delete Guest");

        System.out.println(
                "5.  Add Reservation");

        System.out.println(
                "6.  Search Reservation");

        System.out.println(
                "7.  Add Room");

        System.out.println(
                "8.  Assign Room");

        System.out.println(
                "9.  Check In");

        System.out.println(
                "10. Check Out");

        System.out.println(
                "11. Cancel Reservation");

        System.out.println(
                "12. Summary Report");

        System.out.println(
                "13. Guest Report");

        System.out.println(
                "14. Reservation Report");

        System.out.println(
                "15. Room Report");

        System.out.println(
                "0.  Exit");

        System.out.println(
                "========================================");
    }


    // =====================================================
    // GUEST
    // =====================================================

    private void addGuest() {

        System.out.println(
                "===== ADD GUEST =====");

        String name =
                readString("Enter name: ");

        String identityNumber =
                readString(
                        "Enter IC / Passport: ");

        String phone =
                readString("Enter phone: ");

        Guest guest =
                new Guest(
                        name,
                        identityNumber,
                        phone);

        if (controller.addGuest(guest)) {

            System.out.println(
                    "Guest added successfully.");

        } else {

            System.out.println(
                    "Failed to add guest.");
            System.out.println(
                    "IC / Passport may already exist.");
        }
    }


    private void searchGuest() {

        System.out.println(
                "===== SEARCH GUEST =====");

        String identityNumber =
                readString(
                        "Enter IC / Passport: ");

        Guest guest =
                controller.findGuest(
                        identityNumber);

        if (guest == null) {

            System.out.println(
                    "Guest not found.");

        } else {

            System.out.println(
                    "Guest found:");

            System.out.println(
                    "Name     : "
                    + guest.getName());

            System.out.println(
                    "IC/Pass  : "
                    + guest.getIdentityNumber());

            System.out.println(
                    "Phone    : "
                    + guest.getPhone());
        }
    }


    private void updateGuest() {

        System.out.println(
                "===== UPDATE GUEST =====");

        String identityNumber =
                readString(
                        "Enter IC / Passport: ");

        Guest existing =
                controller.findGuest(
                        identityNumber);

        if (existing == null) {

            System.out.println(
                    "Guest not found.");

            return;
        }

        String name =
                readString("Enter new name: ");

        String phone =
                readString("Enter new phone: ");

        existing.setName(name);

        existing.setPhone(phone);

        if (controller.updateGuest(existing)) {

            System.out.println(
                    "Guest updated successfully.");

        } else {

            System.out.println(
                    "Failed to update guest.");
        }
    }


    private void deleteGuest() {

        System.out.println(
                "===== DELETE GUEST =====");

        String identityNumber =
                readString(
                        "Enter IC / Passport: ");

        Guest removed =
                controller.removeGuest(
                        identityNumber);

        if (removed == null) {

            System.out.println(
                    "Guest not found.");

        } else {

            System.out.println(
                    "Guest deleted successfully.");
        }
    }


    // =====================================================
    // RESERVATION
    // =====================================================

    private void addReservation() {

        System.out.println(
                "===== ADD RESERVATION =====");

        String confirmation =
                readString(
                        "Confirmation number: ");

        String identityNumber =
                readString(
                        "Guest IC / Passport: ");

        Guest guest =
                controller.findGuest(
                        identityNumber);

        if (guest == null) {

            System.out.println(
                    "Guest does not exist.");

            return;
        }

        String roomType =
                readString(
                        "Room type: ");

        LocalDate checkIn =
                readDate(
                        "Check-in date (YYYY-MM-DD): ");

        LocalDate checkOut =
                readDate(
                        "Check-out date (YYYY-MM-DD): ");

        Reservation reservation =
                new Reservation(
                        confirmation,
                        guest,
                        roomType,
                        checkIn,
                        checkOut,
                        LocalDateTime.now(),
                        null,
                        null);

        if (controller.addReservation(
                reservation)) {

            System.out.println(
                    "Reservation added successfully.");

        } else {

            System.out.println(
                    "Failed to add reservation.");
            System.out.println(
                    "Confirmation number may "
                    + "already exist.");
        }
    }


    private void searchReservation() {

        System.out.println(
                "===== SEARCH RESERVATION =====");

        String confirmation =
                readString(
                        "Confirmation number: ");

        Reservation reservation =
                controller.findReservation(
                        confirmation);

        if (reservation == null) {

            System.out.println(
                    "Reservation not found.");

        } else {

            System.out.println(
                    reservation.toString());
        }
    }


    // =====================================================
    // ROOM
    // =====================================================

    private void addRoom() {

        System.out.println(
                "===== ADD ROOM =====");

        String roomId =
                readString("Room ID: ");

        String roomType =
                readString("Room type: ");

        /*
         * IMPORTANT:
         * The exact enum value depends on
         * your HousekeepingStatus class.
         */
        System.out.println(
                "This method requires your "
                + "HousekeepingStatus enum.");

        System.out.println(
                "Use the appropriate initial status "
                + "from your project.");
    }


    private void assignRoom() {

        System.out.println(
                "===== ASSIGN ROOM =====");

        String confirmation =
                readString(
                        "Confirmation number: ");

        String roomId =
                readString(
                        "Room ID: ");

        if (controller.assignRoom(
                confirmation,
                roomId)) {

            System.out.println(
                    "Room assigned successfully.");

        } else {

            System.out.println(
                    "Unable to assign room.");

            System.out.println(
                    "Check reservation, room ID, "
                    + "room status and occupancy.");
        }
    }


    // =====================================================
    // CHECK-IN
    // =====================================================

    private void checkIn() {

        System.out.println(
                "===== CHECK IN =====");

        String confirmation =
                readString(
                        "Confirmation number: ");

        if (controller.checkIn(
                confirmation)) {

            System.out.println(
                    "Guest checked in successfully.");

        } else {

            System.out.println(
                    "Check-in failed.");
        }
    }


    // =====================================================
    // CHECK-OUT
    // =====================================================

    private void checkOut() {

        System.out.println(
                "===== CHECK OUT =====");

        String confirmation =
                readString(
                        "Confirmation number: ");

        if (controller.checkOut(
                confirmation)) {

            System.out.println(
                    "Guest checked out successfully.");

        } else {

            System.out.println(
                    "Check-out failed.");
        }
    }


    // =====================================================
    // CANCEL
    // =====================================================

    private void cancelReservation() {

        System.out.println(
                "===== CANCEL RESERVATION =====");

        String confirmation =
                readString(
                        "Confirmation number: ");

        if (controller.cancelReservation(
                confirmation)) {

            System.out.println(
                    "Reservation cancelled.");

        } else {

            System.out.println(
                    "Reservation not found.");
        }
    }


    // =====================================================
    // REPORTS
    // =====================================================

    private void showSummaryReport() {

        System.out.println(
                reports.generateSummaryReport());
    }


    private void showGuestReport() {

        System.out.println(
                reports.generateGuestReport());
    }


    private void showReservationReport() {

        System.out.println(
                reports.generateReservationReport());
    }


    private void showRoomReport() {

        System.out.println(
                reports.generateRoomReport());
    }


    // =====================================================
    // INPUT METHODS
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
                        scanner.nextLine();

                return Integer.parseInt(
                        input.trim());

            } catch (NumberFormatException e) {

                System.out.println(
                        "Please enter a valid number.");
            }
        }
    }


    private LocalDate readDate(String message) {

        while (true) {

            try {

                System.out.print(message);

                String input =
                        scanner.nextLine();

                return LocalDate.parse(
                        input.trim());

            } catch (Exception e) {

                System.out.println(
                        "Invalid date.");
                System.out.println(
                        "Example: 2026-08-20");
            }
        }
    }
}