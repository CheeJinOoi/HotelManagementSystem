package boundary;

import control.VIPRoomAllocationControl;
import entity.VIPGuest;
import java.util.Scanner;

/**
 * VIPConsoleUI.java
 * BOUNDARY (console): VIP Room Allocation text menu.
 *
 * @author Chong
 * Module: VIP & Loyalty Tier Priority Room Allocation
 */
public class VIPConsoleUI {

    private final VIPRoomAllocationControl control;
    private final Scanner scanner;

    public VIPConsoleUI(VIPRoomAllocationControl control) {
        this.control = control;
        this.scanner = new Scanner(System.in);
    }

    public void start() {

        while (true) {

            printMenu();

            String option =
                scanner.nextLine().trim();

            switch (option) {

                case "1":
                    ConsoleStyle.clear();
                    addVIPGuest();
                    ConsoleStyle.pause(scanner);
                    break;

                case "2":
                    ConsoleStyle.clear();
                    allocateRoom();
                    ConsoleStyle.pause(scanner);
                    break;

                case "3":
                    ConsoleStyle.clear();
                    control.viewQueue();
                    ConsoleStyle.pause(scanner);
                    break;

                case "4":
                    ConsoleStyle.clear();
                    control.viewRooms();
                    ConsoleStyle.pause(scanner);
                    break;

                case "5":
                    ConsoleStyle.clear();
                    releaseRoom();
                    ConsoleStyle.pause(scanner);
                    break;

                case "6":
                    ConsoleStyle.clear();
                    searchVIP();
                    ConsoleStyle.pause(scanner);
                    break;

                case "7":
                    ConsoleStyle.clear();
                    deleteVIPGuest();
                    ConsoleStyle.pause(scanner);
                    break;

                case "8":
                    ConsoleStyle.clear();
                    control.generateQueueReport();
                    ConsoleStyle.pause(scanner);
                    break;

                case "9":
                    ConsoleStyle.clear();
                    control.generateAllocationReport();
                    ConsoleStyle.pause(scanner);
                    break;

                case "0":
                    ConsoleStyle.info(
                        "Returning to hotel menu..."
                    );
                    return;

                default:
                    ConsoleStyle.error(
                        "Invalid option. Please try again."
                    );
                    ConsoleStyle.pause(scanner);
            }
        }
    }

    // ===== Main Menu =====
    private void printMenu() {

        ConsoleStyle.header(
            "VIP ROOM ALLOCATION",
            "Priority queue · loyalty tiers · rooms"
        );

        ConsoleStyle.menuItem(
            1,
            "Add VIP Guest"
        );

        ConsoleStyle.menuItem(
            2,
            "Allocate Room (Highest Priority)"
        );

        ConsoleStyle.menuItem(
            3,
            "View VIP Waiting Queue"
        );

        ConsoleStyle.menuItem(
            4,
            "View Room Status"
        );

        ConsoleStyle.menuItem(
            5,
            "Release Room"
        );

        ConsoleStyle.menuItem(
            6,
            "Search VIP by Phone"
        );

        ConsoleStyle.menuItem(
            7,
            "Delete VIP Guest"
        );

        ConsoleStyle.menuItem(
            8,
            "Generate VIP Queue Report"
        );

        ConsoleStyle.menuItem(
            9,
            "Generate Allocation Report"
        );

        ConsoleStyle.menuExit(
            0,
            "Return to hotel menu"
        );

        ConsoleStyle.prompt(
            "Choose an option: "
        );
    }

    // ===== Add VIP Guest =====
    private void addVIPGuest() {

        ConsoleStyle.section(
            "Add VIP Guest"
        );

        ConsoleStyle.prompt(
            "Enter name: "
        );

        String name =
            scanner.nextLine().trim();

        ConsoleStyle.prompt(
            "Enter IC/Passport: "
        );

        String ic =
            scanner.nextLine().trim();

        ConsoleStyle.prompt(
            "Enter phone number: "
        );

        String phone =
            scanner.nextLine().trim();

        if (name.isEmpty()) {
            ConsoleStyle.error(
                "Name cannot be empty."
            );
            return;
        }

        if (phone.isEmpty()) {
            ConsoleStyle.error(
                "Phone number cannot be empty."
            );
            return;
        }

        // ===== Select Membership Tier =====
        ConsoleStyle.info(
            "Select tier:"
        );

        VIPGuest.MembershipTier[] tiers =
            VIPGuest.MembershipTier.values();

        for (int i = 0; i < tiers.length; i++) {

            ConsoleStyle.menuItem(
                i + 1,
                tiers[i].getDisplay()
            );
        }

        ConsoleStyle.prompt(
            "Enter choice (1-"
            + tiers.length
            + "): "
        );

        int tierChoice = -1;

        try {

            tierChoice =
                Integer.parseInt(
                    scanner.nextLine().trim()
                ) - 1;

        } catch (NumberFormatException e) {

            ConsoleStyle.warn(
                "Invalid number. Defaulting to PLATINUM."
            );

            tierChoice = 2;
        }

        if (
            tierChoice < 0
            || tierChoice >= tiers.length
        ) {

            ConsoleStyle.warn(
                "Invalid choice. Defaulting to PLATINUM."
            );

            tierChoice = 2;
        }

        VIPGuest.MembershipTier tier =
            tiers[tierChoice];

        // ===== Select Preferred Room =====
        ConsoleStyle.info(
            "Select preferred room type:"
        );

        ConsoleStyle.menuItem(
            1,
            "Standard"
        );

        ConsoleStyle.menuItem(
            2,
            "Deluxe"
        );

        ConsoleStyle.menuItem(
            3,
            "Suite"
        );

        ConsoleStyle.menuItem(
            4,
            "Executive"
        );

        ConsoleStyle.prompt(
            "Enter choice (1-4): "
        );

        String roomChoice =
            scanner.nextLine().trim();

        String preferredRoom;

        switch (roomChoice) {

            case "1":
                preferredRoom =
                    "Standard";
                break;

            case "2":
                preferredRoom =
                    "Deluxe";
                break;

            case "3":
                preferredRoom =
                    "Suite";
                break;

            case "4":
                preferredRoom =
                    "Executive";
                break;

            default:

                ConsoleStyle.warn(
                    "Invalid choice. Defaulting to Standard."
                );

                preferredRoom =
                    "Standard";
                break;
        }

        // ===== Create Guest =====
        VIPGuest guest =
            new VIPGuest(
                name,
                ic,
                phone,
                tier,
                preferredRoom
            );

        control.addVIPGuest(
            guest
        );

        ConsoleStyle.success(
            "VIP guest added successfully."
        );

        ConsoleStyle.keyValue(
            "Name",
            name
        );

        ConsoleStyle.keyValue(
            "Phone",
            phone
        );

        ConsoleStyle.keyValue(
            "Tier",
            tier.getDisplay()
        );

        ConsoleStyle.keyValue(
            "Preferred Room",
            preferredRoom
        );
    }

    // ===== Allocate Room =====
    private void allocateRoom() {

        if (
            control.getQueueSize() == 0
        ) {

            ConsoleStyle.warn(
                "No VIP guests waiting."
            );

            return;
        }

        ConsoleStyle.prompt(
            "Allocate room to highest priority VIP? (y/n): "
        );

        String confirm =
            scanner.nextLine()
                .trim()
                .toLowerCase();

        if (
            confirm.equals("y")
            || confirm.equals("yes")
        ) {

            String result =
                control.allocateRoom();

            ConsoleStyle.info(
                result
            );

        } else {

            ConsoleStyle.info(
                "Allocation cancelled."
            );
        }
    }

    // ===== Release Room =====
    private void releaseRoom() {

        control.viewRooms();

        ConsoleStyle.prompt(
            "Enter room ID to release: "
        );

        String roomId =
            scanner.nextLine().trim();

        if (roomId.isEmpty()) {

            ConsoleStyle.error(
                "Room ID cannot be empty."
            );

            return;
        }

        ConsoleStyle.prompt(
            "Release room "
            + roomId
            + "? (y/n): "
        );

        String confirm =
            scanner.nextLine()
                .trim()
                .toLowerCase();

        if (
            confirm.equals("y")
            || confirm.equals("yes")
        ) {

            control.releaseRoom(
                roomId
            );

        } else {

            ConsoleStyle.info(
                "Release cancelled."
            );
        }
    }

    // ===== Search VIP =====
    private void searchVIP() {

        ConsoleStyle.prompt(
            "Enter phone number to search: "
        );

        String phone =
            scanner.nextLine().trim();

        if (phone.isEmpty()) {

            ConsoleStyle.error(
                "Phone number cannot be empty."
            );

            return;
        }

        VIPGuest guest =
            control.searchByPhone(
                phone
            );

        if (guest != null) {

            ConsoleStyle.section(
                "VIP Guest Found"
            );

            ConsoleStyle.keyValue(
                "Name",
                guest.getName()
            );

            ConsoleStyle.keyValue(
                "IC/Passport",
                guest.getIdentityNumber()
            );

            ConsoleStyle.keyValue(
                "Phone",
                guest.getPhone()
            );

            ConsoleStyle.keyValue(
                "Tier",
                guest.getTier().getDisplay()
            );

            ConsoleStyle.keyValue(
                "Preferred Room",
                guest.getPreferredRoomType()
            );

            String confirmation =
                guest.getConfirmationNumber() != null
                    ? guest.getConfirmationNumber()
                    : "Not assigned yet";

            ConsoleStyle.keyValue(
                "Confirmation #",
                confirmation
            );

            String assignedRoom =
                guest.getAssignedRoom() != null
                    ? guest.getAssignedRoom()
                        .getRoomId()
                    : "Not assigned yet";

            ConsoleStyle.keyValue(
                "Assigned Room",
                assignedRoom
            );

        } else {

            ConsoleStyle.error(
                "VIP not found with phone: "
                + phone
            );
        }
    }

    // ===== Delete Waiting VIP Guest =====
    private void deleteVIPGuest() {

        ConsoleStyle.section(
            "Delete VIP Guest"
        );

        ConsoleStyle.prompt(
            "Enter phone number of VIP guest to delete: "
        );

        String phone =
            scanner.nextLine().trim();

        if (phone.isEmpty()) {

            ConsoleStyle.error(
                "Phone number cannot be empty."
            );

            return;
        }

        VIPGuest guest =
            control.searchByPhone(
                phone
            );

        if (guest == null) {

            ConsoleStyle.error(
                "VIP not found with phone: "
                + phone
            );

            return;
        }

        // Do not delete a guest that already has a room
        if (
            guest.getAssignedRoom() != null
        ) {

            ConsoleStyle.warn(
                "VIP guest cannot be deleted because "
                + "a room has already been assigned."
            );

            ConsoleStyle.info(
                "Please use Release Room / Checkout instead."
            );

            return;
        }

        // Display guest before confirmation
        ConsoleStyle.section(
            "VIP Guest Details"
        );

        ConsoleStyle.keyValue(
            "Name",
            guest.getName()
        );

        ConsoleStyle.keyValue(
            "Phone",
            guest.getPhone()
        );

        ConsoleStyle.keyValue(
            "Tier",
            guest.getTier().getDisplay()
        );

        ConsoleStyle.keyValue(
            "Preferred Room",
            guest.getPreferredRoomType()
        );

        ConsoleStyle.prompt(
            "Delete this VIP guest? (y/n): "
        );

        String confirm =
            scanner.nextLine()
                .trim()
                .toLowerCase();

        if (
            confirm.equals("y")
            || confirm.equals("yes")
        ) {

            String result =
                control.deleteVIPGuest(
                    phone
                );

            ConsoleStyle.success(
                result
            );

        } else {

            ConsoleStyle.info(
                "Delete cancelled."
            );
        }
    }
}