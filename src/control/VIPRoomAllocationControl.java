package control;

import adt.HeapPriorityQueue;
import adt.PriorityQueueInterface;
import entity.HousekeepingStatus;
import entity.Room;
import entity.VIPGuest;

/**
 * VIPRoomAllocationControl.java
 * CONTROL layer for VIP & Loyalty Tier Priority Room Allocation.
 *
 * Responsibilities:
 * - Manage VIP guests using a custom Heap Priority Queue ADT
 * - Prioritize room allocation based on VIP membership tier
 * - Allocate rooms based on availability and preferred room type
 * - Search VIP guests by phone or confirmation number
 * - Generate VIP queue and room allocation reports
 *
 * Works with other modules through the shared Room objects.
 *
 * @author Chong
 */
public class VIPRoomAllocationControl {

    private PriorityQueueInterface<VIPGuest> queue;

    private Room[] rooms;
    private int roomCount;

    private VIPGuest[] allVIPGuests;
    private int guestCount;

    private String[] bookingHistory;
    private int bookingCount;

    private int bookingCounter = 50000000;

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
            System.out.println(
                "VIP Module connected to " + roomCount + " shared rooms"
            );
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

            rooms[roomCount++] = new Room(
                "101",
                "Suite",
                HousekeepingStatus.READY_FOR_CHECKIN
            );

            rooms[roomCount++] = new Room(
                "102",
                "Suite",
                HousekeepingStatus.READY_FOR_CHECKIN
            );

            rooms[roomCount++] = new Room(
                "201",
                "Deluxe",
                HousekeepingStatus.READY_FOR_CHECKIN
            );

            rooms[roomCount++] = new Room(
                "202",
                "Deluxe",
                HousekeepingStatus.READY_FOR_CHECKIN
            );

            rooms[roomCount++] = new Room(
                "301",
                "Standard",
                HousekeepingStatus.READY_FOR_CHECKIN
            );

            rooms[roomCount++] = new Room(
                "302",
                "Standard",
                HousekeepingStatus.READY_FOR_CHECKIN
            );

            rooms[roomCount++] = new Room(
                "401",
                "Executive",
                HousekeepingStatus.READY_FOR_CHECKIN
            );

            System.out.println(
                roomCount + " rooms created for VIP module."
            );
        }

        if (guestCount == 0) {
            System.out.println(
                "No default VIP guests. Please use 'Add VIP Guest' to add guests."
            );
        }
    }

    // ===== Add VIP =====
    public void addVIPGuest(VIPGuest guest) {

        if (guestCount >= allVIPGuests.length) {

            VIPGuest[] newArray =
                new VIPGuest[allVIPGuests.length * 2 + 1];

            System.arraycopy(
                allVIPGuests,
                0,
                newArray,
                0,
                allVIPGuests.length
            );

            allVIPGuests = newArray;
        }

        allVIPGuests[guestCount++] = guest;
        queue.enqueue(guest);

        System.out.println(
            "VIP Added (waiting): " + guest
        );
    }

    public void addVIPGuest(
        String name,
        String identityNumber,
        String phone,
        VIPGuest.MembershipTier tier,
        String preferredRoomType
    ) {

        VIPGuest guest = new VIPGuest(
            name,
            identityNumber,
            phone,
            tier,
            preferredRoomType
        );

        addVIPGuest(guest);
    }

    // ===== Delete Waiting VIP Guest =====
public String deleteVIPGuest(String phone) {

    if (phone == null || phone.trim().isEmpty()) {
        return "Phone number is required.";
    }

    phone = phone.trim();

    VIPGuest targetGuest = searchByPhone(phone);

    if (targetGuest == null) {
        return "VIP Guest not found with phone: " + phone;
    }

    // Guest with assigned room should use checkout/release instead
    if (targetGuest.getAssignedRoom() != null) {
        return "VIP Guest cannot be deleted because a room has already been assigned.";
    }

    // Remove from allVIPGuests array
    int removeIndex = -1;

    for (int i = 0; i < guestCount; i++) {
        if (allVIPGuests[i] == targetGuest) {
            removeIndex = i;
            break;
        }
    }

    if (removeIndex == -1) {
        return "VIP Guest could not be deleted.";
    }

    for (int i = removeIndex; i < guestCount - 1; i++) {
        allVIPGuests[i] = allVIPGuests[i + 1];
    }

    allVIPGuests[guestCount - 1] = null;
    guestCount--;

    // Rebuild Priority Queue without the deleted guest
    int originalSize = queue.size();

    VIPGuest[] temp = new VIPGuest[originalSize];
    int tempCount = 0;

    while (!queue.isEmpty()) {

        VIPGuest currentGuest = queue.dequeue();

        if (currentGuest != targetGuest) {
            temp[tempCount++] = currentGuest;
        }
    }

    // Reinsert remaining VIP guests
    for (int i = 0; i < tempCount; i++) {
        queue.enqueue(temp[i]);
    }

    return "VIP Guest "
            + targetGuest.getName()
            + " has been deleted successfully.";
}

    /**
     * Assign a room to the highest-tier waiting VIP (heap front).
     * Called only when the user chooses Allocate Room,
     * not after add or release.
     */
    public String allocateRoom() {

    if (queue.isEmpty()) {
        return "No VIP guests waiting.";
    }

    VIPGuest guest = queue.peek();
    String preferredType = guest.getPreferredRoomType();

    Room availableRoom = null;

    boolean roomTypeExists = false;
    boolean hasOccupiedRoom = false;
    boolean hasNotReadyRoom = false;

    // Search only for the preferred room type
    for (int i = 0; i < roomCount; i++) {

        if (rooms[i].getRoomType().equalsIgnoreCase(preferredType)) {

            roomTypeExists = true;

            if (rooms[i].isOccupied()) {
                hasOccupiedRoom = true;
                continue;
            }

            if (!rooms[i].isReadyForAssignment()) {
                hasNotReadyRoom = true;
                continue;
            }

            availableRoom = rooms[i];
            break;
        }
    }

    if (!roomTypeExists) {
        return "No " + preferredType
                + " room exists in the hotel.";
    }

    if (availableRoom == null) {

        if (hasOccupiedRoom && hasNotReadyRoom) {
            return "No " + preferredType
                    + " room is currently available. "
                    + "The rooms are occupied or not ready for check-in.";
        }

        if (hasNotReadyRoom) {
            return "The " + preferredType
                    + " room is not ready for check-in yet.";
        }

        return "No " + preferredType
                + " room is currently available. "
                + "All rooms are occupied.";
    }

    // Remove guest only after suitable room is found
    guest = queue.dequeue();

    bookingCounter++;

    String confirmationNumber =
            String.format("%08d", bookingCounter);

    guest.setConfirmationNumber(confirmationNumber);

    availableRoom.occupy(confirmationNumber);

    guest.setAssignedRoom(availableRoom);

    if (bookingCount >= bookingHistory.length) {

        String[] newArray =
                new String[bookingHistory.length * 2 + 1];

        System.arraycopy(
                bookingHistory,
                0,
                newArray,
                0,
                bookingHistory.length
        );

        bookingHistory = newArray;
    }

    bookingHistory[bookingCount++] =
            "Room "
            + availableRoom.getRoomId()
            + " -> "
            + guest.getName()
            + " ["
            + guest.getTier().getDisplay()
            + "] (Conf: "
            + confirmationNumber
            + ")";

    return "Allocated room "
            + availableRoom.getRoomId()
            + " ("
            + availableRoom.getRoomType()
            + ") to "
            + guest.getName()
            + " ["
            + guest.getTier().getDisplay()
            + "] (Conf: "
            + confirmationNumber
            + ").";
}

    // ===== Release Room =====
    public void releaseRoom(String roomId) {

        for (int i = 0; i < roomCount; i++) {

            if (rooms[i].getRoomId().equals(roomId)
                    && rooms[i].isOccupied()) {

                String confNumber =
                    rooms[i].getAssignedConfirmationNumber();

                rooms[i].vacateAfterCheckout();

                // Remove VIP guest from system
                for (int j = 0; j < guestCount; j++) {

                    if (allVIPGuests[j]
                            .getConfirmationNumber() != null
                            && allVIPGuests[j]
                            .getConfirmationNumber()
                            .equals(confNumber)) {

                        String removedName =
                            allVIPGuests[j].getName();

                        for (
                            int k = j;
                            k < guestCount - 1;
                            k++
                        ) {
                            allVIPGuests[k] =
                                allVIPGuests[k + 1];
                        }

                        allVIPGuests[
                            guestCount - 1
                        ] = null;

                        guestCount--;

                        if (
                            guestCount > 0
                            && guestCount
                            < allVIPGuests.length / 2
                        ) {

                            VIPGuest[] newArray =
                                new VIPGuest[
                                    allVIPGuests.length / 2 + 1
                                ];

                            System.arraycopy(
                                allVIPGuests,
                                0,
                                newArray,
                                0,
                                guestCount
                            );

                            allVIPGuests = newArray;
                        }

                        System.out.println(
                            "VIP Guest "
                            + removedName
                            + " removed from system."
                        );

                        break;
                    }
                }

                System.out.println(
                    "Room "
                    + roomId
                    + " released and set to DIRTY."
                );

                return;
            }
        }

        System.out.println(
            "Room "
            + roomId
            + " not found or already available."
        );
    }

    // ===== View Queue =====
    public void viewQueue() {

        if (queue.isEmpty()) {
            System.out.println(
                "No VIP guests waiting."
            );
            return;
        }

        System.out.println(
            "\n=== VIP Waiting Queue ==="
        );

        System.out.printf(
            "%-5s %-15s %-12s %-15s %-15s%n",
            "#",
            "Name",
            "Tier",
            "Phone",
            "Preferred Room"
        );

        System.out.println(
            "-".repeat(65)
        );

        VIPGuest[] temp =
            new VIPGuest[queue.size()];

        int tempCount = 0;

        while (!queue.isEmpty()) {
            temp[tempCount++] =
                queue.dequeue();
        }

        for (int i = 0; i < tempCount; i++) {
            queue.enqueue(temp[i]);
        }

        for (int i = 0; i < tempCount; i++) {

            VIPGuest g = temp[i];

            System.out.printf(
                "%-5d %-15s %-12s %-15s %-15s%n",
                (i + 1),
                g.getName(),
                g.getTier().getDisplay(),
                g.getPhone(),
                g.getPreferredRoomType()
            );
        }

        System.out.println(
            "-".repeat(65)
        );

        System.out.println(
            "Total: "
            + tempCount
            + " VIP guests waiting"
        );
    }

    // ===== View Rooms =====
    public void viewRooms() {

        if (roomCount == 0) {
            System.out.println(
                "No rooms available."
            );
            return;
        }

        System.out.println(
            "\n=== Room Status ==="
        );

        System.out.printf(
            "%-10s %-12s %-15s %-30s%n",
            "Room",
            "Type",
            "Status",
            "Assigned To"
        );

        System.out.println(
            "-".repeat(70)
        );

        for (int i = 0; i < roomCount; i++) {

            Room room = rooms[i];

            String status;
            String assigned = "-";

            if (room.isOccupied()) {

                status = "Occupied";

                String confNumber =
                    room.getAssignedConfirmationNumber();

                VIPGuest vipGuest =
                    findVIPByConfirmationNumber(
                        confNumber
                    );

                if (vipGuest != null) {

                    assigned =
                        vipGuest.getName()
                        + " (VIP)";

                } else {

                    assigned =
                        confNumber
                        + " (Walk-In)";
                }

            } else if (
                room.isReadyForAssignment()
            ) {

                status = "Available";

            } else {

                status =
                    room.getCurrentStatus().toString();
            }

            System.out.printf(
                "%-10s %-12s %-15s %-30s%n",
                room.getRoomId(),
                room.getRoomType(),
                status,
                assigned
            );
        }
    }

    // ===== Helper: Find VIP by confirmation number =====
    private VIPGuest findVIPByConfirmationNumber(
        String confirmationNumber
    ) {

        if (confirmationNumber == null) {
            return null;
        }

        for (int i = 0; i < guestCount; i++) {

            if (
                allVIPGuests[i]
                    .getConfirmationNumber() != null
                && allVIPGuests[i]
                    .getConfirmationNumber()
                    .equals(confirmationNumber)
            ) {

                return allVIPGuests[i];
            }
        }

        return null;
    }

    // ===== Search VIP by Phone Number =====
    public VIPGuest searchByPhone(
        String phone
    ) {

        if (
            phone == null
            || phone.trim().isEmpty()
        ) {
            return null;
        }

        for (int i = 0; i < guestCount; i++) {

            if (
                allVIPGuests[i].getPhone() != null
                && allVIPGuests[i]
                    .getPhone()
                    .equals(phone.trim())
            ) {

                return allVIPGuests[i];
            }
        }

        return null;
    }

    // ===== Search VIP by Confirmation Number =====
    public VIPGuest searchByConfirmationNumber(
        String confirmationNumber
    ) {

        if (
            confirmationNumber == null
            || confirmationNumber
                .trim()
                .isEmpty()
        ) {
            return null;
        }

        for (int i = 0; i < guestCount; i++) {

            if (
                allVIPGuests[i]
                    .getConfirmationNumber() != null
                && allVIPGuests[i]
                    .getConfirmationNumber()
                    .equals(
                        confirmationNumber.trim()
                    )
            ) {

                return allVIPGuests[i];
            }
        }

        return null;
    }

    // ===== Queue Report =====
    public String generateQueueReport() {

        StringBuilder report =
            new StringBuilder();

        report.append(
            "============================================================\n"
        );

        report.append(
            "VIP QUEUE REPORT\n"
        );

        report.append(
            "============================================================\n"
        );

        VIPGuest[] temp =
            new VIPGuest[queue.size()];

        int tempCount = 0;

        while (!queue.isEmpty()) {
            temp[tempCount++] =
                queue.dequeue();
        }

        for (int i = 0; i < tempCount; i++) {
            queue.enqueue(temp[i]);
        }

        quickSortByTier(
            temp,
            0,
            tempCount - 1
        );

        report.append(
            String.format(
                "%-5s %-15s %-12s %-15s %-15s%n",
                "#",
                "Name",
                "Tier",
                "Phone",
                "Preferred Room"
            )
        );

        report.append(
            "-------------------------------------------------------------\n"
        );

        for (int i = 0; i < tempCount; i++) {

            VIPGuest g = temp[i];

            report.append(
                String.format(
                    "%-5d %-15s %-12s %-15s %-15s%n",
                    (i + 1),
                    g.getName(),
                    g.getTier().getDisplay(),
                    g.getPhone(),
                    g.getPreferredRoomType()
                )
            );
        }

        report.append(
            "-------------------------------------------------------------\n"
        );

        report.append(
            "Total VIP guests waiting: "
        ).append(
            tempCount
        ).append(
            '\n'
        );

        int elite = 0;
        int diamond = 0;
        int platinum = 0;

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

        report.append(
            "  Elite: "
        ).append(
            elite
        ).append(
            '\n'
        );

        report.append(
            "  Diamond: "
        ).append(
            diamond
        ).append(
            '\n'
        );

        report.append(
            "  Platinum: "
        ).append(
            platinum
        ).append(
            '\n'
        );

        report.append(
            "============================================================\n"
        );

        String text =
            report.toString();

        System.out.println(text);

        return text;
    }

    // ===== Allocation Report =====
    public String generateAllocationReport() {

        StringBuilder report =
            new StringBuilder();

        report.append(
            "============================================================\n"
        );

        report.append(
            "ROOM ALLOCATION PERFORMANCE REPORT\n"
        );

        report.append(
            "============================================================\n"
        );

        if (roomCount == 0) {

            report.append(
                "No rooms available.\n"
            );

            String text =
                report.toString();

            System.out.println(text);

            return text;
        }

        int occupied = 0;
        int available = 0;

        for (int i = 0; i < roomCount; i++) {

            if (rooms[i].isOccupied()) {
                occupied++;
            }

            if (
                rooms[i].isReadyForAssignment()
                && !rooms[i].isOccupied()
            ) {
                available++;
            }
        }

        report.append(
            "ROOM STATISTICS:\n"
        );

        report.append(
            "  Total Rooms: "
        ).append(
            roomCount
        ).append(
            '\n'
        );

        report.append(
            "  Occupied: "
        ).append(
            occupied
        ).append(
            '\n'
        );

        report.append(
            "  Available: "
        ).append(
            available
        ).append(
            '\n'
        );

        report.append(
            String.format(
                "  Occupancy Rate: %.2f%%%n",
                (double) occupied
                / roomCount
                * 100
            )
        );

        report.append(
            "\nROOM TYPE BREAKDOWN:\n"
        );

        String[] types =
            new String[roomCount];

        int[] typeCounts =
            new int[roomCount];

        int[] typeOccupied =
            new int[roomCount];

        int typeCount = 0;

        for (int i = 0; i < roomCount; i++) {

            String type =
                rooms[i].getRoomType();

            boolean found = false;

            for (
                int j = 0;
                j < typeCount;
                j++
            ) {

                if (
                    types[j].equals(type)
                ) {

                    typeCounts[j]++;

                    if (
                        rooms[i].isOccupied()
                    ) {
                        typeOccupied[j]++;
                    }

                    found = true;
                    break;
                }
            }

            if (!found) {

                types[typeCount] =
                    type;

                typeCounts[typeCount] =
                    1;

                typeOccupied[typeCount] =
                    rooms[i].isOccupied()
                    ? 1
                    : 0;

                typeCount++;
            }
        }

        report.append(
            String.format(
                "%-12s %-10s %-10s %-10s%n",
                "Room Type",
                "Total",
                "Occupied",
                "Available"
            )
        );

        report.append(
            "---------------------------------------------\n"
        );

        for (
            int i = 0;
            i < typeCount;
            i++
        ) {

            report.append(
                String.format(
                    "%-12s %-10d %-10d %-10d%n",
                    types[i],
                    typeCounts[i],
                    typeOccupied[i],
                    typeCounts[i]
                    - typeOccupied[i]
                )
            );
        }

        report.append(
            "\nBOOKING HISTORY ("
        ).append(
            bookingCount
        ).append(
            " records):\n"
        );

        if (bookingCount == 0) {

            report.append(
                "  No bookings yet.\n"
            );

        } else {

            for (
                int i = 0;
                i < bookingCount;
                i++
            ) {

                report.append(
                    "  "
                ).append(
                    i + 1
                ).append(
                    ". "
                ).append(
                    bookingHistory[i]
                ).append(
                    '\n'
                );
            }
        }

        report.append(
            "============================================================\n"
        );

        String text =
            report.toString();

        System.out.println(text);

        return text;
    }

    // ===== Quick Sort =====
    public void quickSortByTier(
        VIPGuest[] list,
        int low,
        int high
    ) {

        if (low < high) {

            int pi =
                partition(
                    list,
                    low,
                    high
                );

            quickSortByTier(
                list,
                low,
                pi - 1
            );

            quickSortByTier(
                list,
                pi + 1,
                high
            );
        }
    }

    private int partition(
        VIPGuest[] list,
        int low,
        int high
    ) {

        VIPGuest pivot =
            list[high];

        int i =
            low - 1;

        for (
            int j = low;
            j < high;
            j++
        ) {

            if (
                list[j]
                    .compareTo(pivot) > 0
            ) {

                i++;

                VIPGuest temp =
                    list[i];

                list[i] =
                    list[j];

                list[j] =
                    temp;
            }
        }

        VIPGuest temp =
            list[i + 1];

        list[i + 1] =
            list[high];

        list[high] =
            temp;

        return i + 1;
    }

    // ===== Getters =====
    public VIPGuest[] getAllVIPGuests() {

        VIPGuest[] result =
            new VIPGuest[guestCount];

        System.arraycopy(
            allVIPGuests,
            0,
            result,
            0,
            guestCount
        );

        return result;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public String[] getBookingHistory() {

        String[] result =
            new String[bookingCount];

        System.arraycopy(
            bookingHistory,
            0,
            result,
            0,
            bookingCount
        );

        return result;
    }

    // ===== View VIP Details =====
    public void viewVIPDetails(
        String phone
    ) {

        VIPGuest guest =
            searchByPhone(phone);

        if (guest == null) {

            System.out.println(
                "VIP Guest not found with phone: "
                + phone
            );

            return;
        }

        System.out.println(
            "\n=============================================="
        );

        System.out.println(
            "              VIP GUEST DETAILS"
        );

        System.out.println(
            "=============================================="
        );

        System.out.printf(
            "%-16s: %-20s%n",
            "Name",
            guest.getName()
        );

        System.out.printf(
            "%-16s: %-20s%n",
            "IC/Passport",
            guest.getIdentityNumber()
        );

        System.out.printf(
            "%-16s: %-20s%n",
            "Phone",
            guest.getPhone()
        );

        System.out.println(
            "----------------------------------------------"
        );

        System.out.printf(
            "%-16s: %-20s%n",
            "Tier",
            guest.getTier().getDisplay()
        );

        System.out.println(
            "----------------------------------------------"
        );

        String roomInfo =
            guest.getAssignedRoom() != null
            ? guest.getAssignedRoom()
                .getRoomId()
                + " ("
                + guest.getAssignedRoom()
                    .getRoomType()
                + ")"
            : "Not assigned";

        System.out.printf(
            "%-16s: %-20s%n",
            "Assigned Room",
            roomInfo
        );

        System.out.printf(
            "%-16s: %-20s%n",
            "Preferred Room",
            guest.getPreferredRoomType()
        );

        String confirm =
            guest.getConfirmationNumber() != null
            ? guest.getConfirmationNumber()
            : "Not generated";

        System.out.printf(
            "%-16s: %-20s%n",
            "Confirmation #",
            confirm
        );

        String status;

        if (
            guest.getAssignedRoom() != null
        ) {

            status = "Checked-In";

        } else if (
            guest.getConfirmationNumber() != null
        ) {

            status = "Waiting";

        } else {

            status = "Registered";
        }

        System.out.printf(
            "%-16s: %-20s%n",
            "Status",
            status
        );

        System.out.println(
            "=============================================="
        );
    }
}