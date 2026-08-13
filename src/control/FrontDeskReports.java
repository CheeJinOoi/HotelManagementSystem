package control;

import entity.Reservation;
import entity.ReservationStatus;
import entity.Room;

/**
 * FrontDeskReports
public class FrontDeskReports {

    private FrontDeskController controller;

    public FrontDeskReports(
            FrontDeskController controller) {

        this.controller = controller;
    }


    // =====================================================
    // REPORT 1
    // RESERVATION PERFORMANCE REPORT
    // =====================================================

    public String generateReservationReport(
            ReservationStatus statusFilter,
            String roomTypeFilter) {

        Object[] allReservations =
                controller.getAllReservations();

        /*
         * First pass:
         * Count matching reservations.
         */
        int count = 0;

        for (int i = 0;
             i < allReservations.length;
             i++) {

            Reservation reservation =
                    (Reservation) allReservations[i];

            if (matchesReservationFilter(
                    reservation,
                    statusFilter,
                    roomTypeFilter)) {

                count++;
            }
        }

        /*
         * Create an array containing
         * only matching reservations.
         */
        Reservation[] results =
                new Reservation[count];

        int index = 0;

        for (int i = 0;
             i < allReservations.length;
             i++) {

            Reservation reservation =
                    (Reservation) allReservations[i];

            if (matchesReservationFilter(
                    reservation,
                    statusFilter,
                    roomTypeFilter)) {

                results[index] = reservation;

                index++;
            }
        }

        /*
         * Sort by confirmation number.
         */
        selectionSortReservations(results);

        String output = "";

        output +=
                "\n============================================================\n";

        output +=
                "       FRONT DESK RESERVATION PERFORMANCE REPORT\n";

        output +=
                "============================================================\n";

        output +=
                String.format(
                        "Status Filter    : %s%n",
                        statusFilter == null
                                ? "ALL"
                                : statusFilter);

        output +=
                String.format(
                        "Room Type Filter  : %s%n",
                        roomTypeFilter == null
                                || roomTypeFilter.isEmpty()
                                ? "ALL"
                                : roomTypeFilter);

        output +=
                "------------------------------------------------------------\n";

        output +=
                String.format(
                        "%-12s %-20s %-12s %-15s%n",
                        "Confirm No.",
                        "Guest",
                        "Room Type",
                        "Status");

        output +=
                "------------------------------------------------------------\n";

        for (int i = 0;
             i < results.length;
             i++) {

            Reservation r =
                    results[i];

            String guestName = "-";

            if (r.getGuest() != null) {

                guestName =
                        r.getGuest().getName();
            }

            output +=
                    String.format(
                            "%-12s %-20s %-12s %-15s%n",
                            r.getConfirmationNumber(),
                            guestName,
                            r.getRoomType(),
                            r.getStatus());
        }

        output +=
                "------------------------------------------------------------\n";

        output +=
                "Total Matching Reservations : "
                + results.length
                + "\n";

        output +=
                "============================================================\n";

        return output;
    }


    // =====================================================
    // FILTER
    // =====================================================

    private boolean matchesReservationFilter(
            Reservation reservation,
            ReservationStatus statusFilter,
            String roomTypeFilter) {

        if (reservation == null) {
            return false;
        }

        /*
         * Criterion 1:
         * Reservation Status
         */
        if (statusFilter != null) {

            if (reservation.getStatus()
                    != statusFilter) {

                return false;
            }
        }

        /*
         * Criterion 2:
         * Room Type
         */
        if (roomTypeFilter != null
                && !roomTypeFilter.isEmpty()) {

            if (reservation.getRoomType() == null
                    || !reservation.getRoomType()
                            .equalsIgnoreCase(
                                    roomTypeFilter)) {

                return false;
            }
        }

        return true;
    }


    // =====================================================
    // SORT RESERVATIONS
    // SELECTION SORT
    // =====================================================

    private void selectionSortReservations(
            Reservation[] reservations) {

        for (int i = 0;
             i < reservations.length - 1;
             i++) {

            int smallest = i;

            for (int j = i + 1;
                 j < reservations.length;
                 j++) {

                String current =
                        reservations[j]
                                .getConfirmationNumber();

                String smallestValue =
                        reservations[smallest]
                                .getConfirmationNumber();

                if (current.compareToIgnoreCase(
                        smallestValue) < 0) {

                    smallest = j;
                }
            }

            Reservation temp =
                    reservations[i];

            reservations[i] =
                    reservations[smallest];

            reservations[smallest] =
                    temp;
        }
    }


    // =====================================================
    // REPORT 2
    // ROOM OCCUPANCY REPORT
    // =====================================================

    public String generateRoomOccupancyReport(
            String roomTypeFilter,
            Boolean occupiedFilter) {

        Object[] allRooms =
                controller.getAllRooms();

        /*
         * Search / filter.
         */
        int count = 0;

        for (int i = 0;
             i < allRooms.length;
             i++) {

            Room room =
                    (Room) allRooms[i];

            if (matchesRoomFilter(
                    room,
                    roomTypeFilter,
                    occupiedFilter)) {

                count++;
            }
        }

        /*
         * Store matching rooms.
         */
        Room[] results =
                new Room[count];

        int index = 0;

        for (int i = 0;
             i < allRooms.length;
             i++) {

            Room room =
                    (Room) allRooms[i];

            if (matchesRoomFilter(
                    room,
                    roomTypeFilter,
                    occupiedFilter)) {

                results[index] = room;

                index++;
            }
        }

        /*
         * Sort rooms by Room ID.
         */
        selectionSortRooms(results);

        int occupiedCount = 0;

        for (int i = 0;
             i < results.length;
             i++) {

            if (results[i].isOccupied()) {

                occupiedCount++;
            }
        }

        int availableCount =
                results.length - occupiedCount;

        double occupancyRate = 0;

        if (results.length > 0) {

            occupancyRate =
                    ((double) occupiedCount
                    / results.length)
                    * 100;
        }

        String output = "";

        output +=
                "\n============================================================\n";

        output +=
                "                 ROOM OCCUPANCY REPORT\n";

        output +=
                "============================================================\n";

        output +=
                "Room Type Filter : "
                + (roomTypeFilter == null
                        || roomTypeFilter.isEmpty()
                        ? "ALL"
                        : roomTypeFilter)
                + "\n";

        output +=
                "Occupancy Filter  : "
                + (occupiedFilter == null
                        ? "ALL"
                        : occupiedFilter
                            ? "OCCUPIED"
                            : "AVAILABLE")
                + "\n";

        output +=
                "------------------------------------------------------------\n";

        output +=
                String.format(
                        "%-12s %-15s %-12s %-15s%n",
                        "Room ID",
                        "Room Type",
                        "Occupied",
                        "Status");

        output +=
                "------------------------------------------------------------\n";

        for (int i = 0;
             i < results.length;
             i++) {

            Room room =
                    results[i];

            output +=
                    String.format(
                            "%-12s %-15s %-12s %-15s%n",
                            room.getRoomId(),
                            room.getRoomType(),
                            room.isOccupied()
                                    ? "YES"
                                    : "NO",
                            room.getCurrentStatus());
        }

        output +=
                "------------------------------------------------------------\n";

        output +=
                "Total Rooms       : "
                + results.length
                + "\n";

        output +=
                "Occupied Rooms    : "
                + occupiedCount
                + "\n";

        output +=
                "Available Rooms   : "
                + availableCount
                + "\n";

        output +=
                String.format(
                        "Occupancy Rate    : %.2f%%%n",
                        occupancyRate);

        output +=
                "============================================================\n";

        return output;
    }


    // =====================================================
    // ROOM FILTER
    // =====================================================

    private boolean matchesRoomFilter(
            Room room,
            String roomTypeFilter,
            Boolean occupiedFilter) {

        if (room == null) {
            return false;
        }

        /*
         * Criterion 1:
         * Room Type
         */
        if (roomTypeFilter != null
                && !roomTypeFilter.isEmpty()) {

            if (!room.getRoomType()
                    .equalsIgnoreCase(
                            roomTypeFilter)) {

                return false;
            }
        }

        /*
         * Criterion 2:
         * Occupancy
         */
        if (occupiedFilter != null) {

            if (room.isOccupied()
                    != occupiedFilter) {

                return false;
            }
        }

        return true;
    }


    // =====================================================
    // SORT ROOMS
    // SELECTION SORT
    // =====================================================

    private void selectionSortRooms(
            Room[] rooms) {

        for (int i = 0;
             i < rooms.length - 1;
             i++) {

            int smallest = i;

            for (int j = i + 1;
                 j < rooms.length;
                 j++) {

                if (rooms[j]
                        .getRoomId()
                        .compareToIgnoreCase(
                                rooms[smallest]
                                        .getRoomId()) < 0) {

                    smallest = j;
                }
            }

            Room temp =
                    rooms[i];

            rooms[i] =
                    rooms[smallest];

            rooms[smallest] =
                    temp;
        }
    }


    // =====================================================
    // SIMPLE SUMMARY
    // =====================================================

    public String generateSummaryReport() {

        int totalGuests =
                controller.getGuestCount();

        int totalReservations =
                controller.getReservationCount();

        int totalRooms =
                controller.getRoomCount();

        int availableRooms =
                controller.getAvailableRoomCount();

        int occupiedRooms =
                controller.getOccupiedRoomCount();

        int waiting =
                controller.countReservationsByStatus(
                        ReservationStatus.WAITING);

        int assigned =
                controller.countReservationsByStatus(
                        ReservationStatus.ASSIGNED);

        int checkedIn =
                controller.countReservationsByStatus(
                        ReservationStatus.CHECKED_IN);

        int checkedOut =
                controller.countReservationsByStatus(
                        ReservationStatus.CHECKED_OUT);

        int cancelled =
                controller.countReservationsByStatus(
                        ReservationStatus.CANCELLED);

        String output = "";

        output +=
                "\n============================================================\n";

        output +=
                "              FRONT DESK MANAGEMENT SUMMARY\n";

        output +=
                "============================================================\n";

        output +=
                "Total Guests          : "
                + totalGuests
                + "\n";

        output +=
                "Total Reservations    : "
                + totalReservations
                + "\n";

        output +=
                "Total Rooms           : "
                + totalRooms
                + "\n";

        output +=
                "Available Rooms       : "
                + availableRooms
                + "\n";

        output +=
                "Occupied Rooms        : "
                + occupiedRooms
                + "\n";

        output +=
                "------------------------------------------------------------\n";

        output +=
                "Reservation Status\n";

        output +=
                "Waiting               : "
                + waiting
                + "\n";

        output +=
                "Assigned              : "
                + assigned
                + "\n";

        output +=
                "Checked-In            : "
                + checkedIn
                + "\n";

        output +=
                "Checked-Out           : "
                + checkedOut
                + "\n";

        output +=
                "Cancelled             : "
                + cancelled
                + "\n";

        output +=
                "============================================================\n";

        return output;
    }


    // =====================================================
    // DEFAULT REPORT
    // =====================================================

    /**
     * Generates a reservation report
     * without filters.
     */
    public String generateReservationReport() {

        return generateReservationReport(
                null,
                null);
    }


    /**
     * Generates a room report
     * without filters.
     */
    public String generateRoomReport() {

        return generateRoomOccupancyReport(
                null,
                null);
    }


    /**
     * Generates guest report.
     */
    public String generateGuestReport() {

        Object[] guests =
                controller.getAllGuests();

        String output = "";

        output +=
                "\n============================================================\n";

        output +=
                "                    GUEST REPORT\n";

        output +=
                "============================================================\n";

        output +=
                String.format(
                        "%-20s %-18s %-15s%n",
                        "Name",
                        "IC / Passport",
                        "Phone");

        output +=
                "------------------------------------------------------------\n";

        for (int i = 0;
             i < guests.length;
             i++) {

            entity.Guest guest =
                    (entity.Guest) guests[i];

            output +=
                    String.format(
                            "%-20s %-18s %-15s%n",
                            guest.getName(),
                            guest.getIdentityNumber(),
                            guest.getPhone());
        }

        output +=
                "------------------------------------------------------------\n";

        output +=
                "Total Guests : "
                + guests.length
                + "\n";

        output +=
                "============================================================\n";

        return output;
    }
}