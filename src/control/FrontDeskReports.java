package control;

import entity.Reservation;
import entity.ReservationStatus;

/**
 * FrontDeskReports
 *
 * Generates analytical reports for Front Desk.
 *
 * Algorithms used:
 * 1. Searching
 * 2. Filtering
 * 3. Insertion Sort
 *
 * No Java Collection Framework is used.
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
    // RESERVATION REPORT
    // =====================================================

    public String generateReservationReport() {

        Reservation[] all =
                controller.getAllReservations();

        if (all == null || all.length == 0) {

            return
                    "\nNo reservation data available.\n";
        }

        /*
         * Filter:
         *
         * Only active reservations
         */
        int count = 0;

        for (int i = 0;
                i < all.length;
                i++) {

            if (all[i] != null
                    && isActive(all[i])) {

                count++;
            }
        }

        Reservation[] filtered =
                new Reservation[count];

        int index = 0;

        for (int i = 0;
                i < all.length;
                i++) {

            if (all[i] != null
                    && isActive(all[i])) {

                filtered[index] =
                        all[i];

                index++;
            }
        }

        /*
         * Sorting:
         *
         * Sort by check-in date.
         */
        insertionSortByCheckInDate(
                filtered);

        StringBuilder report =
                new StringBuilder();

        report.append(
                "\n==============================================================\n");

        report.append(
                "              FRONT DESK RESERVATION REPORT\n");

        report.append(
                "==============================================================\n");

        report.append(
                "Filter: Active reservations only\n");

        report.append(
                "Sort : Check-in date (earliest first)\n");

        report.append(
                "--------------------------------------------------------------\n");

        report.append(
                String.format(
                        "%-10s %-20s %-12s %-12s %-12s%n",
                        "Confirm",
                        "Guest",
                        "Room Type",
                        "Check-In",
                        "Status"));

        report.append(
                "--------------------------------------------------------------\n");

        for (int i = 0;
                i < filtered.length;
                i++) {

            Reservation r =
                    filtered[i];

            String guestName = "-";

            if (r.getGuest() != null) {

                guestName =
                        r.getGuest().getName();
            }

            report.append(
                    String.format(
                            "%-10s %-20s %-12s %-12s %-12s%n",

                            r.getConfirmationNumber(),

                            guestName,

                            r.getRoomType(),

                            r.getCheckInDate(),

                            r.getStatus()));
        }

        report.append(
                "--------------------------------------------------------------\n");

        report.append(
                "Total active reservations: "
                + filtered.length
                + "\n");

        return report.toString();
    }


    // =====================================================
    // REPORT 2
    // GUEST REPORT
    // =====================================================

    public String generateGuestReport() {

        Reservation[] all =
                controller.getAllReservations();

        if (all == null || all.length == 0) {

            return
                    "\nNo guest data available.\n";
        }

        StringBuilder report =
                new StringBuilder();

        report.append(
                "\n==============================================================\n");

        report.append(
                "                    GUEST REPORT\n");

        report.append(
                "==============================================================\n");

        report.append(
                "Guest information retrieved from reservation records\n");

        report.append(
                "--------------------------------------------------------------\n");

        report.append(
                String.format(
                        "%-10s %-20s %-18s %-15s%n",
                        "Confirm",
                        "Guest Name",
                        "IC/Passport",
                        "Phone"));

        report.append(
                "--------------------------------------------------------------\n");

        int count = 0;

        for (int i = 0;
                i < all.length;
                i++) {

            Reservation r =
                    all[i];

            if (r == null
                    || r.getGuest() == null) {

                continue;
            }

            report.append(
                    String.format(
                            "%-10s %-20s %-18s %-15s%n",

                            r.getConfirmationNumber(),

                            r.getGuest().getName(),

                            r.getGuest()
                                    .getIdentityNumber(),

                            r.getGuest()
                                    .getPhone()));

            count++;
        }

        report.append(
                "--------------------------------------------------------------\n");

        report.append(
                "Total guest records: "
                + count
                + "\n");

        return report.toString();
    }


    // =====================================================
    // REPORT 3
    // ROOM AVAILABILITY REPORT
    // =====================================================

    public String generateRoomAvailabilityReport() {

        Reservation[] all =
                controller.getAllReservations();

        if (all == null) {

            return
                    "\nNo reservation data available.\n";
        }

        int standard = 0;
        int deluxe = 0;
        int suite = 0;

        int waiting = 0;
        int assigned = 0;
        int checkedIn = 0;
        int cancelled = 0;
        int checkedOut = 0;

        for (int i = 0;
                i < all.length;
                i++) {

            Reservation r =
                    all[i];

            if (r == null) {
                continue;
            }

            /*
             * Multiple criteria:
             *
             * Room Type
             * AND
             * Reservation Status
             */

            if ("Standard".equalsIgnoreCase(
                    r.getRoomType())) {

                standard++;
            }

            else if ("Deluxe".equalsIgnoreCase(
                    r.getRoomType())) {

                deluxe++;
            }

            else if ("Suite".equalsIgnoreCase(
                    r.getRoomType())) {

                suite++;
            }

            ReservationStatus status =
                    r.getStatus();

            if (status ==
                    ReservationStatus.WAITING) {

                waiting++;
            }

            else if (status ==
                    ReservationStatus.ASSIGNED) {

                assigned++;
            }

            else if (status ==
                    ReservationStatus.CHECKED_IN) {

                checkedIn++;
            }

            else if (status ==
                    ReservationStatus.CANCELLED) {

                cancelled++;
            }

            else if (status ==
                    ReservationStatus.CHECKED_OUT) {

                checkedOut++;
            }
        }

        StringBuilder report =
                new StringBuilder();

        report.append(
                "\n==============================================================\n");

        report.append(
                "              ROOM / RESERVATION ANALYSIS\n");

        report.append(
                "==============================================================\n");

        report.append(
                "\nROOM TYPE SUMMARY\n");

        report.append(
                "--------------------------------------------------------------\n");

        report.append(
                String.format(
                        "%-20s %-15s%n",
                        "Room Type",
                        "Reservations"));

        report.append(
                "--------------------------------------------------------------\n");

        report.append(
                String.format(
                        "%-20s %-15d%n",
                        "Standard",
                        standard));

        report.append(
                String.format(
                        "%-20s %-15d%n",
                        "Deluxe",
                        deluxe));

        report.append(
                String.format(
                        "%-20s %-15d%n",
                        "Suite",
                        suite));

        report.append(
                "\nSTATUS SUMMARY\n");

        report.append(
                "--------------------------------------------------------------\n");

        report.append(
                String.format(
                        "%-20s %-15d%n",
                        "Waiting",
                        waiting));

        report.append(
                String.format(
                        "%-20s %-15d%n",
                        "Assigned",
                        assigned));

        report.append(
                String.format(
                        "%-20s %-15d%n",
                        "Checked-In",
                        checkedIn));

        report.append(
                String.format(
                        "%-20s %-15d%n",
                        "Cancelled",
                        cancelled));

        report.append(
                String.format(
                        "%-20s %-15d%n",
                        "Checked-Out",
                        checkedOut));

        report.append(
                "--------------------------------------------------------------\n");

        report.append(
                "Management Summary\n");

        report.append(
                "Total reservations : "
                + all.length
                + "\n");

        report.append(
                "Currently waiting  : "
                + waiting
                + "\n");

        report.append(
                "Currently in-house : "
                + checkedIn
                + "\n");

        return report.toString();
    }


    // =====================================================
    // FILTER
    // =====================================================

    private boolean isActive(
            Reservation reservation) {

        ReservationStatus status =
                reservation.getStatus();

        return status !=
                ReservationStatus.CANCELLED
                &&
                status !=
                ReservationStatus.CHECKED_OUT;
    }


    // =====================================================
    // INSERTION SORT
    // =====================================================

    private void insertionSortByCheckInDate(
            Reservation[] reservations) {

        for (int i = 1;
                i < reservations.length;
                i++) {

            Reservation key =
                    reservations[i];

            int j = i - 1;

            while (j >= 0
                    && isAfter(
                            reservations[j],
                            key)) {

                reservations[j + 1] =
                        reservations[j];

                j--;
            }

            reservations[j + 1] =
                    key;
        }
    }


    // =====================================================
    // COMPARE DATES
    // =====================================================

    private boolean isAfter(
            Reservation first,
            Reservation second) {

        if (first.getCheckInDate() == null) {

            return false;
        }

        if (second.getCheckInDate() == null) {

            return true;
        }

        return first.getCheckInDate()
                .isAfter(
                        second.getCheckInDate());
    }
}