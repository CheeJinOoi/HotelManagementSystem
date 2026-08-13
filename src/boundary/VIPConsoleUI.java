package boundary;

import control.VIPRoomAllocationControl;
import entity.VIPGuest;
import java.util.Scanner;

/**
 * VIPConsoleUI.java
 * BOUNDARY (console): VIP Room Allocation text menu.
 *
 * @author chong
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
            String option = scanner.nextLine().trim();

            switch (option) {
                case "1":
                    addVIPGuest();
                    break;
                case "2":
                    allocateRoom();
                    break;
                case "3":
                    control.viewQueue();
                    break;
                case "4":
                    control.viewRooms();
                    break;
                case "5":
                    releaseRoom();
                    break;
                case "6":
                    searchVIP();
                    break;
                case "7":
                    control.generateQueueReport();
                    break;
                case "8":
                    control.generateAllocationReport();
                    break;
                case "0":
                    System.out.println("Returning to hotel menu...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n==============================================");
        System.out.println(" ⭐ VIP Room Allocation System");
        System.out.println("==============================================");
        System.out.println("1. Add VIP Guest");
        System.out.println("2. Allocate Room (Highest Priority)");
        System.out.println("3. View VIP Waiting Queue");
        System.out.println("4. View Room Status");
        System.out.println("5. Release Room");
        System.out.println("6. Search VIP by ID");
        System.out.println("7. Generate VIP Queue Report");
        System.out.println("8. Generate Allocation Report");
        System.out.println("0. Return to hotel menu");
        System.out.print("Choose an option: ");
    }

    private void addVIPGuest() {
        System.out.println("\n--- Add VIP Guest ---");

        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter IC/Passport: ");
        String ic = scanner.nextLine().trim();

        System.out.print("Enter phone: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter membership ID (e.g., VIP001): ");
        String membershipId = scanner.nextLine().trim();

        System.out.println("Select tier:");
        VIPGuest.MembershipTier[] tiers = VIPGuest.MembershipTier.values();
        for (int i = 0; i < tiers.length; i++) {
            System.out.println("  " + (i + 1) + ". " + tiers[i].getDisplay());
        }
        System.out.print("Enter choice (1-" + tiers.length + "): ");
        int tierChoice = -1;
        try {
            tierChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Defaulting to PLATINUM.");
            tierChoice = 2;
        }
        if (tierChoice < 0 || tierChoice >= tiers.length) {
            System.out.println("Invalid choice. Defaulting to PLATINUM.");
            tierChoice = 2;
        }
        VIPGuest.MembershipTier tier = tiers[tierChoice];

        System.out.print("Enter loyalty points: ");
        int points = 0;
        try {
            points = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Setting points to 0.");
        }

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        VIPGuest guest = new VIPGuest(name, ic, phone, membershipId, tier, points, email);
        control.addVIPGuest(guest);
        System.out.println("✅ VIP Guest added successfully!");
    }

    private void allocateRoom() {
        if (control.getQueueSize() == 0) {
            System.out.println("⚠️ No VIP guests waiting.");
            return;
        }

        System.out.print("Allocate room to highest priority VIP? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("y") || confirm.equals("yes")) {
            control.allocateRoom();
        } else {
            System.out.println("Allocation cancelled.");
        }
    }

    private void releaseRoom() {
        control.viewRooms();
        System.out.print("Enter room ID to release: ");
        String roomId = scanner.nextLine().trim();
        if (roomId.isEmpty()) {
            System.out.println("Room ID cannot be empty.");
            return;
        }

        System.out.print("Release room " + roomId + "? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("y") || confirm.equals("yes")) {
            control.releaseRoom(roomId);
        } else {
            System.out.println("Release cancelled.");
        }
    }

    private void searchVIP() {
        System.out.print("Enter membership ID to search: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("Membership ID cannot be empty.");
            return;
        }

        VIPGuest guest = control.searchByMembershipId(id);
        if (guest != null) {
            System.out.println("\n✅ VIP Guest Found:");
            System.out.println("   Name        : " + guest.getName());
            System.out.println("   IC/Passport : " + guest.getIdentityNumber());
            System.out.println("   Phone       : " + guest.getPhone());
            System.out.println("   Membership  : " + guest.getMembershipId());
            System.out.println("   Tier        : " + guest.getTier().getDisplay());
            System.out.println("   Points      : " + guest.getLoyaltyPoints());
            System.out.println("   Email       : " + guest.getEmail());
        } else {
            System.out.println("❌ VIP not found with ID: " + id);
        }
    }
}