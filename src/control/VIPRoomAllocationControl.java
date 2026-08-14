package control;

import adt.HeapPriorityQueue;
import adt.PriorityQueueInterface;
import entity.HousekeepingStatus;
import entity.Room;
import entity.VIPGuest;

/**
 * VIPRoomAllocationControl.java
 * CONTROL layer for VIP Room Allocation.
 * ✅ Fully compliant: No JCF classes used!
 */
public class VIPRoomAllocationControl {

    private PriorityQueueInterface<VIPGuest> queue;

    private Room[] rooms;
    private int roomCount;

    private VIPGuest[] allVIPGuests;
    private int guestCount;

    private String[] bookingHistory;
    private int bookingCount;

    private int bookingCounter = 10000000;

    public VIPRoomAllocationControl() {
        queue = new HeapPriorityQueue<>();
        rooms = new Room[0];
        roomCount = 0;
        allVIPGuests = new VIPGuest[0];
        guestCount = 0;
        bookingHistory = new String[0];
        bookingCount = 0;
    }

    // ===== Share rooms from main system =====
    public void setRooms(Room[] rooms) {
        if (rooms != null) {
            this.rooms = rooms;
            this.roomCount = rooms.length;
            System.out.println("✅ VIP Module connected to " + roomCount + " shared rooms");
        }
    }

    public Room[] getRooms() {
        return rooms;
    }

    // ===== Add test data =====
    public void addTestData() {
        if (roomCount == 0) {
            rooms = new Room[7];
            roomCount = 0;
            rooms[roomCount++] = new Room("101", "Suite", HousekeepingStatus.READY_FOR_CHECKIN);
            rooms[roomCount++] = new Room("102", "Suite", HousekeepingStatus.READY_FOR_CHECKIN);
            rooms[roomCount++] = new Room("201", "Deluxe", HousekeepingStatus.READY_FOR_CHECKIN);
            rooms[roomCount++] = new Room("202", "Deluxe", HousekeepingStatus.READY_FOR_CHECKIN);
            rooms[roomCount++] = new Room("301", "Standard", HousekeepingStatus.READY_FOR_CHECKIN);
            rooms[roomCount++] = new Room("302", "Standard", HousekeepingStatus.READY_FOR_CHECKIN);
            rooms[roomCount++] = new Room("401", "Executive", HousekeepingStatus.READY_FOR_CHECKIN);
        }

        if (guestCount == 0) {
            allVIPGuests = new VIPGuest[5];
            guestCount = 0;
            allVIPGuests[guestCount++] = new VIPGuest("John Smith", "S1234567A", "012-345-6789",
                    "VIP001", VIPGuest.MembershipTier.ELITE, 5000, "john@email.com");
            allVIPGuests[guestCount++] = new VIPGuest("Maria Garcia", "S7654321B", "012-987-6543",
                    "VIP002", VIPGuest.MembershipTier.DIAMOND, 3000, "maria@email.com");
            allVIPGuests[guestCount++] = new VIPGuest("David Lee", "S5555555C", "016-555-1234",
                    "VIP003", VIPGuest.MembershipTier.PLATINUM, 1500, "david@email.com");
            allVIPGuests[guestCount++] = new VIPGuest("Sarah Tan", "S7777777D", "019-777-8888",
                    "VIP004", VIPGuest.MembershipTier.DIAMOND, 2500, "sarah@email.com");
            allVIPGuests[guestCount++] = new VIPGuest("James Wong", "S3333333E", "017-333-4444",
                    "VIP005", VIPGuest.MembershipTier.ELITE, 8000, "james@email.com");

            for (int i = 0; i < guestCount; i++) {
                queue.enqueue(allVIPGuests[i]);
            }
        }
    }

    // ===== Add VIP =====
    public void addVIPGuest(VIPGuest guest) {
        if (guestCount >= allVIPGuests.length) {
            VIPGuest[] newArray = new VIPGuest[allVIPGuests.length * 2 + 1];
            System.arraycopy(allVIPGuests, 0, newArray, 0, allVIPGuests.length);
            allVIPGuests = newArray;
        }
        allVIPGuests[guestCount++] = guest;
        queue.enqueue(guest);
        System.out.println("✅ VIP Added: " + guest);
    }

    public void addVIPGuest(String name, String identityNumber, String phone,
                            String membershipId, VIPGuest.MembershipTier tier) {
        VIPGuest guest = new VIPGuest(name, identityNumber, phone, membershipId, tier);
        addVIPGuest(guest);
    }

    // ===== Allocate Room =====
    public void allocateRoom() {
        if (queue.isEmpty()) {
            System.out.println("⚠️ No VIP guests waiting.");
            return;
        }

        Room availableRoom = null;
        for (int i = 0; i < roomCount; i++) {
            if (rooms[i].isReadyForAssignment() && !rooms[i].isOccupied()) {
                availableRoom = rooms[i];
                break;
            }
        }

        if (availableRoom == null) {
            System.out.println("⚠️ No rooms available.");
            return;
        }

        VIPGuest guest = queue.dequeue();
        bookingCounter++;
        String confirmationNumber = String.format("%08d", bookingCounter);
        guest.setConfirmationNumber(confirmationNumber);
        availableRoom.occupy(confirmationNumber);
        guest.setAssignedRoom(availableRoom);

        if (bookingCount >= bookingHistory.length) {
            String[] newArray = new String[bookingHistory.length * 2 + 1];
            System.arraycopy(bookingHistory, 0, newArray, 0, bookingHistory.length);
            bookingHistory = newArray;
        }
        bookingHistory[bookingCount++] = "Room " + availableRoom.getRoomId() +
                " -> " + guest.getName() + " [" + guest.getTier().getDisplay() +
                "] (Conf: " + confirmationNumber + ")";

        System.out.println("🏠 Allocated: Room " + availableRoom.getRoomId() +
                " to " + guest.getName() + " (Conf: " + confirmationNumber + ")");
    }

    // ===== Release Room =====
    public void releaseRoom(String roomId) {
        for (int i = 0; i < roomCount; i++) {
            if (rooms[i].getRoomId().equals(roomId) && rooms[i].isOccupied()) {
                rooms[i].clearOccupancy();
                for (int j = 0; j < guestCount; j++) {
                    if (allVIPGuests[j].getAssignedRoom() != null &&
                        allVIPGuests[j].getAssignedRoom().getRoomId().equals(roomId)) {
                        allVIPGuests[j].setAssignedRoom(null);
                        break;
                    }
                }
                System.out.println("🔓 Room " + roomId + " released.");
                if (!queue.isEmpty()) {
                    System.out.println("🔄 Auto-allocating next VIP...");
                    allocateRoom();
                }
                return;
            }
        }
        System.out.println("❌ Room " + roomId + " not found or already available.");
    }

    // ===== View Queue =====
    public void viewQueue() {
        if (queue.isEmpty()) {
            System.out.println("No VIP guests waiting.");
            return;
        }

        System.out.println("\n=== VIP Waiting Queue ===");
        System.out.printf("%-5s %-15s %-12s %-10s %-8s%n", "#", "Name", "ID", "Tier", "Points");
        System.out.println("-".repeat(60));

        VIPGuest[] temp = new VIPGuest[queue.size()];
        int tempCount = 0;
        while (!queue.isEmpty()) {
            temp[tempCount++] = queue.dequeue();
        }
        for (int i = 0; i < tempCount; i++) {
            queue.enqueue(temp[i]);
        }

        for (int i = 0; i < tempCount; i++) {
            VIPGuest g = temp[i];
            System.out.printf("%-5d %-15s %-12s %-10s %-8d%n",
                (i + 1), g.getName(), g.getMembershipId(),
                g.getTier().getDisplay(), g.getLoyaltyPoints());
        }
        System.out.println("-".repeat(60));
        System.out.println("Total: " + tempCount + " VIP guests waiting");
    }

    // ===== View Rooms =====
    public void viewRooms() {
        if (roomCount == 0) {
            System.out.println("No rooms available.");
            return;
        }
        System.out.println("\n=== Room Status ===");
        System.out.printf("%-10s %-12s %-15s %-25s%n", "Room", "Type", "Status", "Assignment");
        System.out.println("-".repeat(65));
        for (int i = 0; i < roomCount; i++) {
            Room room = rooms[i];
            String status;
            String assigned = "-";
            if (room.isOccupied()) {
                status = "❌ Occupied";
                assigned = room.getAssignedConfirmationNumber();
            } else if (room.isReadyForAssignment()) {
                status = "✅ Available";
            } else {
                status = "🔧 " + room.getCurrentStatus();
            }
            System.out.printf("%-10s %-12s %-15s %-25s%n",
                room.getRoomId(), room.getRoomType(), status, assigned);
        }
    }

    // ===== Search VIP =====
    public VIPGuest searchByMembershipId(String membershipId) {
        if (membershipId == null || membershipId.trim().isEmpty()) {
            return null;
        }
        for (int i = 0; i < guestCount; i++) {
            if (allVIPGuests[i].getMembershipId().equalsIgnoreCase(membershipId.trim())) {
                return allVIPGuests[i];
            }
        }
        return null;
    }

    // ===== Queue Report =====
    public void generateQueueReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📋 VIP QUEUE REPORT");
        System.out.println("=".repeat(60));

        VIPGuest[] temp = new VIPGuest[queue.size()];
        int tempCount = 0;
        while (!queue.isEmpty()) {
            temp[tempCount++] = queue.dequeue();
        }
        for (int i = 0; i < tempCount; i++) {
            queue.enqueue(temp[i]);
        }

        quickSortByTier(temp, 0, tempCount - 1);

        System.out.printf("%-5s %-15s %-12s %-12s %-10s %-12s%n",
            "#", "Name", "ID", "Tier", "Points", "Phone");
        System.out.println("-".repeat(70));

        for (int i = 0; i < tempCount; i++) {
            VIPGuest g = temp[i];
            System.out.printf("%-5d %-15s %-12s %-12s %-10d %-12s%n",
                (i + 1), g.getName(), g.getMembershipId(),
                g.getTier().getDisplay(), g.getLoyaltyPoints(),
                g.getPhone());
        }
        System.out.println("-".repeat(70));
        System.out.println("📊 Total VIP guests waiting: " + tempCount);

        int elite = 0, diamond = 0, platinum = 0;
        for (int i = 0; i < tempCount; i++) {
            switch (temp[i].getTier()) {
                case ELITE:
                    elite++;
                    break;
                case DIAMOND:
                    diamond++;
                    break;
                case PLATINUM:
                    platinum++;
                    break;
                default:
                    break;
            }
        }
        System.out.println("   ├─ Elite: " + elite);
        System.out.println("   ├─ Diamond: " + diamond);
        System.out.println("   └─ Platinum: " + platinum);
        System.out.println("=".repeat(60));
    }

    // ===== Allocation Report =====
    public void generateAllocationReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 ROOM ALLOCATION PERFORMANCE REPORT");
        System.out.println("=".repeat(60));

        if (roomCount == 0) {
            System.out.println("No rooms available.");
            return;
        }

        int occupied = 0;
        int available = 0;
        for (int i = 0; i < roomCount; i++) {
            if (rooms[i].isOccupied()) {
                occupied++;
            }
            if (rooms[i].isReadyForAssignment() && !rooms[i].isOccupied()) {
                available++;
            }
        }

        System.out.println("🏨 ROOM STATISTICS:");
        System.out.println("   ├─ Total Rooms: " + roomCount);
        System.out.println("   ├─ Occupied: " + occupied);
        System.out.println("   ├─ Available: " + available);
        System.out.printf("   └─ Occupancy Rate: %.2f%%%n",
            (double) occupied / roomCount * 100);

        System.out.println("\n📂 ROOM TYPE BREAKDOWN:");
        String[] types = new String[roomCount];
        int[] typeCounts = new int[roomCount];
        int[] typeOccupied = new int[roomCount];
        int typeCount = 0;

        for (int i = 0; i < roomCount; i++) {
            String type = rooms[i].getRoomType();
            boolean found = false;
            for (int j = 0; j < typeCount; j++) {
                if (types[j].equals(type)) {
                    typeCounts[j]++;
                    if (rooms[i].isOccupied()) {
                        typeOccupied[j]++;
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                types[typeCount] = type;
                typeCounts[typeCount] = 1;
                typeOccupied[typeCount] = rooms[i].isOccupied() ? 1 : 0;
                typeCount++;
            }
        }

        System.out.printf("%-12s %-10s %-10s %-10s%n", "Room Type", "Total", "Occupied", "Available");
        System.out.println("-".repeat(45));
        for (int i = 0; i < typeCount; i++) {
            System.out.printf("%-12s %-10d %-10d %-10d%n",
                types[i], typeCounts[i], typeOccupied[i], typeCounts[i] - typeOccupied[i]);
        }

        System.out.println("\n📝 BOOKING HISTORY (" + bookingCount + " records):");
        if (bookingCount == 0) {
            System.out.println("   └─ No bookings yet.");
        } else {
            for (int i = 0; i < bookingCount; i++) {
                System.out.println("   " + (i + 1) + ". " + bookingHistory[i]);
            }
        }
        System.out.println("=".repeat(60));
    }

    // ===== Quick Sort =====
    public void quickSortByTier(VIPGuest[] list, int low, int high) {
        if (low < high) {
            int pi = partition(list, low, high);
            quickSortByTier(list, low, pi - 1);
            quickSortByTier(list, pi + 1, high);
        }
    }

    private int partition(VIPGuest[] list, int low, int high) {
        VIPGuest pivot = list[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (list[j].compareTo(pivot) > 0) {
                i++;
                VIPGuest temp = list[i];
                list[i] = list[j];
                list[j] = temp;
            }
        }
        VIPGuest temp = list[i + 1];
        list[i + 1] = list[high];
        list[high] = temp;
        return i + 1;
    }

    // ===== Getters =====
    public VIPGuest[] getAllVIPGuests() {
        VIPGuest[] result = new VIPGuest[guestCount];
        System.arraycopy(allVIPGuests, 0, result, 0, guestCount);
        return result;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public String[] getBookingHistory() {
        String[] result = new String[bookingCount];
        System.arraycopy(bookingHistory, 0, result, 0, bookingCount);
        return result;
    }

    // ===== Additional helper: View VIP details =====
    public void viewVIPDetails(String membershipId) {
        VIPGuest guest = searchByMembershipId(membershipId);
        if (guest == null) {
            System.out.println("❌ VIP Guest not found with ID: " + membershipId);
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║         ⭐ VIP GUEST DETAILS                ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.printf("║ %-16s: %-20s ║\n", "Name", guest.getName());
        System.out.printf("║ %-16s: %-20s ║\n", "IC/Passport", guest.getIdentityNumber());
        System.out.printf("║ %-16s: %-20s ║\n", "Phone", guest.getPhone());
        System.out.println("╠──────────────────────────────────────────────╣");
        System.out.printf("║ %-16s: %-20s ║\n", "Membership ID", guest.getMembershipId());
        System.out.printf("║ %-16s: %-20s ║\n", "Tier", guest.getTier().getDisplay());
        System.out.printf("║ %-16s: %-20d ║\n", "Points", guest.getLoyaltyPoints());
        System.out.println("╠──────────────────────────────────────────────╣");
        String roomInfo = guest.getAssignedRoom() != null ?
            guest.getAssignedRoom().getRoomId() + " (" + guest.getAssignedRoom().getRoomType() + ")" :
            "Not assigned";
        System.out.printf("║ %-16s: %-20s ║\n", "Assigned Room", roomInfo);
        String preferred = guest.getPreferredRoomType() != null ?
            guest.getPreferredRoomType() : "Standard";
        System.out.printf("║ %-16s: %-20s ║\n", "Preferred Room", preferred);
        String confirm = guest.getConfirmationNumber() != null ?
            guest.getConfirmationNumber() : "Not generated";
        System.out.printf("║ %-16s: %-20s ║\n", "Confirmation #", confirm);
        String status;
        if (guest.getAssignedRoom() != null) {
            status = "✅ Checked-In";
        } else if (guest.getConfirmationNumber() != null) {
            status = "⏳ Waiting";
        } else {
            status = "📋 Registered";
        }
        System.out.printf("║ %-16s: %-20s ║\n", "Status", status);
        System.out.println("╚══════════════════════════════════════════════╝");
    }
}