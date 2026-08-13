package control;

import adt.HeapPriorityQueue;
import adt.PriorityQueueInterface;
import entity.HousekeepingStatus;
import entity.Room;
import entity.VIPGuest;
import java.util.*;

public class VIPRoomAllocationControl {

    private PriorityQueueInterface<VIPGuest> queue;
    private List<Room> rooms;
    private List<VIPGuest> allVIPGuests;
    private List<String> bookingHistory;
    private int bookingCounter = 0;

    public VIPRoomAllocationControl() {
        queue = new HeapPriorityQueue<>();
        rooms = new ArrayList<>();
        allVIPGuests = new ArrayList<>();
        bookingHistory = new ArrayList<>();
    }

    // ===== 设置共享房间 =====
    public void setRooms(List<Room> rooms) {
        if (rooms != null) {
            this.rooms = rooms;
            System.out.println("✅ VIP Module connected to " + rooms.size() + " shared rooms");
        }
    }

    public List<Room> getRooms() {
        return rooms;
    }

    // ===== 添加测试数据 =====
    public void addTestData() {
        if (rooms.isEmpty()) {
            rooms.add(new Room("101", "Suite", HousekeepingStatus.READY_FOR_CHECKIN));
            rooms.add(new Room("102", "Suite", HousekeepingStatus.READY_FOR_CHECKIN));
            rooms.add(new Room("201", "Deluxe", HousekeepingStatus.READY_FOR_CHECKIN));
            rooms.add(new Room("202", "Deluxe", HousekeepingStatus.READY_FOR_CHECKIN));
            rooms.add(new Room("301", "Standard", HousekeepingStatus.READY_FOR_CHECKIN));
            rooms.add(new Room("302", "Standard", HousekeepingStatus.READY_FOR_CHECKIN));
        }

        if (allVIPGuests.isEmpty()) {
            VIPGuest g1 = new VIPGuest("John Smith", "S1234567A", "012-345-6789",
                    "VIP001", VIPGuest.MembershipTier.ELITE, 5000, "john@email.com");
            VIPGuest g2 = new VIPGuest("Maria Garcia", "S7654321B", "012-987-6543",
                    "VIP002", VIPGuest.MembershipTier.DIAMOND, 3000, "maria@email.com");
            VIPGuest g3 = new VIPGuest("David Lee", "S5555555C", "016-555-1234",
                    "VIP003", VIPGuest.MembershipTier.PLATINUM, 1500, "david@email.com");
            VIPGuest g4 = new VIPGuest("Sarah Tan", "S7777777D", "019-777-8888",
                    "VIP004", VIPGuest.MembershipTier.DIAMOND, 2500, "sarah@email.com");
            VIPGuest g5 = new VIPGuest("James Wong", "S3333333E", "017-333-4444",
                    "VIP005", VIPGuest.MembershipTier.ELITE, 8000, "james@email.com");

            queue.enqueue(g1);
            queue.enqueue(g2);
            queue.enqueue(g3);
            queue.enqueue(g4);
            queue.enqueue(g5);

            allVIPGuests.add(g1);
            allVIPGuests.add(g2);
            allVIPGuests.add(g3);
            allVIPGuests.add(g4);
            allVIPGuests.add(g5);
        }
    }

    // ===== 1. 添加 VIP =====
    public void addVIPGuest(String name, String identityNumber, String phone,
                            String membershipId, VIPGuest.MembershipTier tier) {
        VIPGuest guest = new VIPGuest(name, identityNumber, phone, membershipId, tier);
        queue.enqueue(guest);
        allVIPGuests.add(guest);
        System.out.println("✅ VIP Added: " + guest);
    }

    public void addVIPGuest(VIPGuest guest) {
        queue.enqueue(guest);
        allVIPGuests.add(guest);
        System.out.println("✅ VIP Added: " + guest);
    }

    // ===== 2. 分配房间 =====
    public void allocateRoom() {
        if (queue.isEmpty()) {
            System.out.println("⚠️ No VIP guests waiting.");
            return;
        }

        Room availableRoom = null;
        for (Room room : rooms) {
            if (room.isReadyForAssignment() && !room.isOccupied()) {
                availableRoom = room;
                break;
            }
        }

        if (availableRoom == null) {
            System.out.println("⚠️ No rooms available.");
            return;
        }

        VIPGuest guest = queue.dequeue();
        String confirmationNumber = "VIP" + String.format("%08d", ++bookingCounter);
        availableRoom.occupy(confirmationNumber);
        guest.setAssignedRoom(availableRoom);

        String booking = "Room " + availableRoom.getRoomId() +
                         " -> " + guest.getName() + " [" + guest.getTier().getDisplay() +
                         "] (Conf: " + confirmationNumber + ")";
        bookingHistory.add(booking);

        System.out.println("🏠 Allocated: " + booking);
    }

    // ===== 3. 查看队列 =====
    public void viewQueue() {
        if (queue.isEmpty()) {
            System.out.println("No VIP guests waiting.");
            return;
        }
        System.out.println("\n=== VIP Waiting Queue ===");
        System.out.printf("%-5s %-15s %-12s %-10s %-8s%n", "#", "Name", "ID", "Tier", "Points");
        System.out.println("-".repeat(60));

        List<VIPGuest> tempList = new ArrayList<>();
        while (!queue.isEmpty()) {
            tempList.add(queue.dequeue());
        }
        for (VIPGuest g : tempList) {
            queue.enqueue(g);
        }

        int i = 1;
        for (VIPGuest g : tempList) {
            System.out.printf("%-5d %-15s %-12s %-10s %-8d%n",
                i++, g.getName(), g.getMembershipId(),
                g.getTier().getDisplay(), g.getLoyaltyPoints());
        }
        System.out.println("-".repeat(60));
        System.out.println("Total: " + tempList.size() + " VIP guests waiting");
    }

    // ===== 4. 查看房间 =====
    public void viewRooms() {
        if (rooms == null || rooms.isEmpty()) {
            System.out.println("No rooms available.");
            return;
        }
        System.out.println("\n=== Room Status ===");
        System.out.printf("%-10s %-12s %-15s %-25s%n", "Room", "Type", "Status", "Assignment");
        System.out.println("-".repeat(65));
        for (Room room : rooms) {
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

    // ===== 5. 释放房间 =====
    public void releaseRoom(String roomId) {
        for (Room room : rooms) {
            if (room.getRoomId().equals(roomId) && room.isOccupied()) {
                room.clearOccupancy();
                for (VIPGuest guest : allVIPGuests) {
                    if (guest.getAssignedRoom() != null &&
                        guest.getAssignedRoom().getRoomId().equals(roomId)) {
                        guest.setAssignedRoom(null);
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

    // ===== 6. 搜索 VIP =====
    public VIPGuest searchByMembershipId(String membershipId) {
        if (membershipId == null || membershipId.trim().isEmpty()) {
            return null;
        }
        for (VIPGuest guest : allVIPGuests) {
            if (guest.getMembershipId().equalsIgnoreCase(membershipId.trim())) {
                return guest;
            }
        }
        return null;
    }

    // ===== 7. 队列报告 =====
    public void generateQueueReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📋 VIP QUEUE REPORT");
        System.out.println("=".repeat(60));

        List<VIPGuest> temp = new ArrayList<>();
        while (!queue.isEmpty()) {
            temp.add(queue.dequeue());
        }

        quickSortByTier(temp, 0, temp.size() - 1);

        System.out.printf("%-5s %-15s %-12s %-12s %-10s %-12s%n",
            "#", "Name", "ID", "Tier", "Points", "Phone");
        System.out.println("-".repeat(70));

        int i = 1;
        for (VIPGuest g : temp) {
            System.out.printf("%-5d %-15s %-12s %-12s %-10d %-12s%n",
                i++, g.getName(), g.getMembershipId(),
                g.getTier().getDisplay(), g.getLoyaltyPoints(),
                g.getPhone());
            queue.enqueue(g);
        }
        System.out.println("-".repeat(70));
        System.out.println("📊 Total VIP guests waiting: " + temp.size());

        int elite = 0, diamond = 0, platinum = 0;
        for (VIPGuest g : temp) {
            switch (g.getTier()) {
                case ELITE: elite++; break;
                case DIAMOND: diamond++; break;
                case PLATINUM: platinum++; break;
            }
        }
        System.out.println("   ├─ Elite: " + elite);
        System.out.println("   ├─ Diamond: " + diamond);
        System.out.println("   └─ Platinum: " + platinum);
        System.out.println("=".repeat(60));
    }

    // ===== 8. 分配报告 =====
    public void generateAllocationReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 ROOM ALLOCATION PERFORMANCE REPORT");
        System.out.println("=".repeat(60));

        int totalRooms = rooms.size();
        int occupied = 0;
        for (Room room : rooms) {
            if (room.isOccupied()) occupied++;
        }
        int available = 0;
        for (Room room : rooms) {
            if (room.isReadyForAssignment() && !room.isOccupied()) available++;
        }

        System.out.println("🏨 ROOM STATISTICS:");
        System.out.println("   ├─ Total Rooms: " + totalRooms);
        System.out.println("   ├─ Occupied: " + occupied);
        System.out.println("   ├─ Available: " + available);
        System.out.printf("   └─ Occupancy Rate: %.2f%%%n",
            (double) occupied / totalRooms * 100);

        System.out.println("\n📂 ROOM TYPE BREAKDOWN:");
        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, Integer> typeOccupied = new HashMap<>();

        for (Room room : rooms) {
            typeCount.put(room.getRoomType(), typeCount.getOrDefault(room.getRoomType(), 0) + 1);
            if (room.isOccupied()) {
                typeOccupied.put(room.getRoomType(), typeOccupied.getOrDefault(room.getRoomType(), 0) + 1);
            }
        }

        System.out.printf("%-12s %-10s %-10s %-10s%n", "Room Type", "Total", "Occupied", "Available");
        System.out.println("-".repeat(45));
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            String type = entry.getKey();
            int total = entry.getValue();
            int occ = typeOccupied.getOrDefault(type, 0);
            System.out.printf("%-12s %-10d %-10d %-10d%n", type, total, occ, total - occ);
        }

        System.out.println("\n📝 BOOKING HISTORY (" + bookingHistory.size() + " records):");
        if (bookingHistory.isEmpty()) {
            System.out.println("   └─ No bookings yet.");
        } else {
            for (String booking : bookingHistory) {
                System.out.println("   - " + booking);
            }
        }
        System.out.println("=".repeat(60));
    }

    // ===== 排序算法 =====
    public void quickSortByTier(List<VIPGuest> list, int low, int high) {
        if (low < high) {
            int pi = partition(list, low, high);
            quickSortByTier(list, low, pi - 1);
            quickSortByTier(list, pi + 1, high);
        }
    }

    private int partition(List<VIPGuest> list, int low, int high) {
        VIPGuest pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (list.get(j).compareTo(pivot) > 0) {
                i++;
                VIPGuest temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }
        VIPGuest temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);
        return i + 1;
    }

    // ===== Getters =====
    public List<VIPGuest> getAllVIPGuests() {
        return allVIPGuests;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public List<String> getBookingHistory() {
        return bookingHistory;
    }
}