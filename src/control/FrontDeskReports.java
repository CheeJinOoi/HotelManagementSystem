package control;

import entity.Guest;
import entity.Reservation;
import entity.ReservationStatus;

/**
 * FrontDeskReports.java
 *
 * REPORT class for Front Desk module.
 *
 * @author Tan Jun Ren
 */
public class FrontDeskReports {

    private FrontDeskController controller;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FrontDeskReports(
            FrontDeskController controller) {

        this.controller = controller;
    }


    // =====================================================
    // REPORT 1
    // VIP GUEST REPORT
    // =====================================================

    /**
     * Generates VIP guest report.
     *
     * VIP criteria:
     * - Guest has more than one reservation
     * OR
     * - Guest has stayed / checked in
     *
     * The report searches all reservations,
     * filters VIP-related records,
     * then sorts by guest name.
     */
    public Reservation[] getVIPReservations() {

        Reservation[] all =
                controller.getAllReservations();

        if (all == null || all.length == 0) {
            return new Reservation[0];
        }

        int count = 0;
        for (int i = 0; i < all.length; i++) {
            if (isVipReservation(all[i], all)) {
                count++;
            }
        }

        Reservation[] vipReservations = new Reservation[count];
        int index = 0;
        for (int i = 0; i < all.length; i++) {
            if (isVipReservation(all[i], all)) {
                vipReservations[index] = all[i];
                index++;
            }
        }
        insertionSortByGuestName(vipReservations);
        return vipReservations;
    }

    public String getVipLevel(Reservation reservation) {
        Reservation[] all = controller.getAllReservations();
        if (reservation == null || reservation.getGuest() == null || all == null) {
            return "-";
        }
        int reservationCount = countGuestReservations(all, reservation.getGuest().getIdentityNumber());
        if (reservationCount >= 3) {
            return "Gold VIP";
        }
        if (reservationCount > 1) {
            return "VIP";
        }
        if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
            return "In-house VIP";
        }
        return "-";
    }

    public Reservation[] getReservationsSortedByConfirmation() {
        Reservation[] all = controller.getAllReservations();
        if (all == null || all.length == 0) {
            return new Reservation[0];
        }
        Reservation[] sorted = copyReservations(all);
        insertionSortByConfirmation(sorted);
        return sorted;
    }

    public String generateVIPGuestReport() {

        Reservation[] all =
                controller.getAllReservations();

        if (all == null || all.length == 0) {
            return "\nNo reservation data available.\n";
        }

        Reservation[] vipReservations = getVIPReservations();
        StringBuilder report = new StringBuilder();

        report.append(
                "\n==============================================================\n");
        report.append(
                "                 VIP GUEST REPORT\n");
        report.append(
                "==============================================================\n");
        report.append(
                "VIP criteria: repeat guest OR currently checked-in\n");
        report.append(
                String.format(
                        "%-20s %-12s %-18s %-12s %-15s\n",
                        "Guest",
                        "Confirmation",
                        "Room",
                        "Status",
                        "VIP Level"));
        report.append(
                "--------------------------------------------------------------\n");

        for (int i = 0; i < vipReservations.length; i++) {
            Reservation reservation = vipReservations[i];
            Guest guest = reservation.getGuest();
            String room = reservation.getAssignedRoomId();
            if (room == null) {
                room = "-";
            }
            report.append(
                    String.format(
                            "%-20s %-12s %-18s %-12s %-15s\n",
                            guest.getName(),
                            reservation.getConfirmationNumber(),
                            room,
                            reservation.getStatus(),
                            getVipLevel(reservation)));
        }

        report.append(
                "--------------------------------------------------------------\n");
        report.append(
                "Total VIP records : "
                + vipReservations.length
                + "\n");
        report.append(
                "==============================================================\n");
        return report.toString();
    }

    private boolean isVipReservation(Reservation reservation, Reservation[] all) {
        if (reservation == null || reservation.getGuest() == null) {
            return false;
        }
        int reservationCount = countGuestReservations(all, reservation.getGuest().getIdentityNumber());
        return reservationCount > 1
                || reservation.getStatus() == ReservationStatus.CHECKED_IN;
    }


    // =====================================================
    // REPORT 2
    // FRONT DESK OPERATIONAL REPORT
    // =====================================================

    /**
     * Generates operational summary.
     *
     * Multiple criteria:
     *
     * - Reservation status
     * - Booking type
     * - Room type
     *
     * Searching:
     * - Search reservations by criteria
     *
     * Sorting:
     * - Sort reservations by confirmation number
     */
    public String generateFrontDeskReport() {

        Reservation[] all =
                controller.getAllReservations();


        if (all == null
                || all.length == 0) {

            return "\nNo reservation data available.\n";
        }


        // -------------------------------------------------
        // SORT
        // -------------------------------------------------

        Reservation[] sorted =
                copyReservations(all);

        insertionSortByConfirmation(
                sorted);


        // -------------------------------------------------
        // COUNTERS
        // -------------------------------------------------

        int total = 0;

        int waiting = 0;

        int assigned = 0;

        int checkedIn = 0;

        int checkedOut = 0;

        int cancelled = 0;

        int standard = 0;

        int deluxe = 0;

        int suite = 0;


        for (int i = 0;
                i < sorted.length;
                i++) {

            Reservation reservation =
                    sorted[i];

            if (reservation == null) {
                continue;
            }

            total++;


            // Status filter

            if (reservation.getStatus()
                    == ReservationStatus.WAITING) {

                waiting++;

            } else if (reservation.getStatus()
                    == ReservationStatus.ASSIGNED) {

                assigned++;

            } else if (reservation.getStatus()
                    == ReservationStatus.CHECKED_IN) {

                checkedIn++;

            } else if (reservation.getStatus()
                    == ReservationStatus.CHECKED_OUT) {

                checkedOut++;

            } else if (reservation.getStatus()
                    == ReservationStatus.CANCELLED) {

                cancelled++;
            }


            // Room type filter

            if (reservation.getRoomType()
                    != null) {

                if (reservation.getRoomType()
                        .equalsIgnoreCase(
                                "Standard")) {

                    standard++;

                } else if (
                        reservation.getRoomType()
                        .equalsIgnoreCase(
                                "Deluxe")) {

                    deluxe++;

                } else if (
                        reservation.getRoomType()
                        .equalsIgnoreCase(
                                "Suite")) {

                    suite++;
                }
            }
        }


        // -------------------------------------------------
        // BUILD REPORT
        // -------------------------------------------------

        StringBuilder report =
                new StringBuilder();


        report.append(
                "\n====================================================\n");

        report.append(
                "             FRONT DESK OPERATIONAL REPORT\n");

        report.append(
                "====================================================\n");


        report.append(
                "\n[Reservation Summary]\n");

        report.append(
                "Total Reservations : "
                + total
                + "\n");

        report.append(
                "Waiting            : "
                + waiting
                + "\n");

        report.append(
                "Assigned           : "
                + assigned
                + "\n");

        report.append(
                "Checked-In         : "
                + checkedIn
                + "\n");

        report.append(
                "Checked-Out        : "
                + checkedOut
                + "\n");

        report.append(
                "Cancelled          : "
                + cancelled
                + "\n");


        report.append(
                "\n[Room Type Analysis]\n");

        report.append(
                "Standard           : "
                + standard
                + "\n");

        report.append(
                "Deluxe             : "
                + deluxe
                + "\n");

        report.append(
                "Suite              : "
                + suite
                + "\n");


        report.append(
                "\n[Reservation Details]\n");

        report.append(
                String.format(
                        "%-12s %-20s %-12s %-12s %-12s\n",
                        "Confirmation",
                        "Guest",
                        "Room Type",
                        "Status",
                        "Room"));

        report.append(
                "----------------------------------------------------\n");


        for (int i = 0;
                i < sorted.length;
                i++) {

            Reservation reservation =
                    sorted[i];

            if (reservation == null) {
                continue;
            }

            Guest guest =
                    reservation.getGuest();

            String guestName =
                    guest == null
                    ? "-"
                    : guest.getName();

            String room =
                    reservation.getAssignedRoomId();

            if (room == null) {
                room = "-";
            }


            report.append(
                    String.format(
                            "%-12s %-20s %-12s %-12s %-12s\n",
                            reservation.getConfirmationNumber(),
                            guestName,
                            reservation.getRoomType(),
                            reservation.getStatus(),
                            room));
        }


        report.append(
                "====================================================\n");


        return report.toString();
    }


    // =====================================================
    // SEARCH
    // =====================================================

    /**
     * Searches reservations by guest identity number.
     *
     * This is a linear search over the returned array.
     *
     * The main confirmation-number search itself
     * should be performed by HashedDictionary
     * inside FrontDeskController.
     */
    public Reservation[] searchByGuestIdentity(
            String identityNumber) {

        Reservation[] all =
                controller.getAllReservations();


        if (all == null) {

            return new Reservation[0];
        }


        int count = 0;


        for (int i = 0;
                i < all.length;
                i++) {

            if (all[i] != null
                    && all[i].getGuest() != null
                    && identityNumber != null
                    && identityNumber.equals(
                            all[i]
                            .getGuest()
                            .getIdentityNumber())) {

                count++;
            }
        }


        Reservation[] result =
                new Reservation[count];

        int index = 0;


        for (int i = 0;
                i < all.length;
                i++) {

            if (all[i] != null
                    && all[i].getGuest() != null
                    && identityNumber != null
                    && identityNumber.equals(
                            all[i]
                            .getGuest()
                            .getIdentityNumber())) {

                result[index] =
                        all[i];

                index++;
            }
        }


        return result;
    }


    // =====================================================
    // COUNT GUEST RESERVATIONS
    // =====================================================

    private int countGuestReservations(
            Reservation[] reservations,
            String identityNumber) {

        int count = 0;


        for (int i = 0;
                i < reservations.length;
                i++) {

            if (reservations[i] == null
                    || reservations[i].getGuest() == null) {

                continue;
            }


            if (identityNumber.equals(
                    reservations[i]
                    .getGuest()
                    .getIdentityNumber())) {

                count++;
            }
        }


        return count;
    }


    // =====================================================
    // COPY ARRAY
    // =====================================================

    private Reservation[] copyReservations(
            Reservation[] source) {

        Reservation[] copy =
                new Reservation[source.length];


        for (int i = 0;
                i < source.length;
                i++) {

            copy[i] =
                    source[i];
        }


        return copy;
    }


    // =====================================================
    // INSERTION SORT
    // =====================================================

    /**
     * Sort by guest name.
     */
    private void insertionSortByGuestName(
            Reservation[] reservations) {

        for (int i = 1;
                i < reservations.length;
                i++) {

            Reservation key =
                    reservations[i];

            if (key == null
                    || key.getGuest() == null) {

                continue;
            }


            String keyName =
                    key.getGuest()
                    .getName();


            int j = i - 1;


            while (j >= 0
                    && reservations[j] != null
                    && reservations[j].getGuest() != null
                    && reservations[j]
                    .getGuest()
                    .getName()
                    .compareToIgnoreCase(
                            keyName) > 0) {

                reservations[j + 1] =
                        reservations[j];

                j--;
            }


            reservations[j + 1] =
                    key;
        }
    }


    /**
     * Sort by confirmation number.
     */
    private void insertionSortByConfirmation(
            Reservation[] reservations) {

        for (int i = 1;
                i < reservations.length;
                i++) {

            Reservation key =
                    reservations[i];


            if (key == null) {
                continue;
            }


            String keyNumber =
                    key.getConfirmationNumber();


            int j = i - 1;


            while (j >= 0
                    && reservations[j] != null
                    && reservations[j]
                    .getConfirmationNumber()
                    .compareTo(
                            keyNumber) > 0) {

                reservations[j + 1] =
                        reservations[j];

                j--;
            }


            reservations[j + 1] =
                    key;
        }
    }
}