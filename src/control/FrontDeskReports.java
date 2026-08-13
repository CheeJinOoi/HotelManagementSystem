package control;

import entity.BookingType;
import entity.Reservation;
import entity.ReservationStatus;

/**
 * Front Desk Reports.
 *
 * Uses:
 * - Hashing search through FrontDeskController
 * - Arrays
 * - Insertion Sort
 * - Multiple filtering criteria
 *
 * No Java Collection Framework is used.
 */
public class FrontDeskReports {

    private FrontDeskController controller;

    public FrontDeskReports(
            FrontDeskController controller) {

        this.controller = controller;
    }

    /**
     * Report 1:
     *
     * Reservation Status Report
     *
     * Filters:
     * - Booking Type
     * - Reservation Status
     *
     * Sort:
     * - Confirmation number
     */
    public String generateReservationReport() {

        Reservation[] all =
                controller.getAllReservations();

        if (all == null || all.length == 0) {

            return
                    "\nNo reservation data available.\n";
        }

        // ==========================================
        // FILTER
        // ==========================================

        int count = 0;

        for (int i = 0;
                i < all.length;
                i++) {

            if (matchesFilter(
                    all[i],
                    null,
                    null)) {

                count++;
            }
        }

        Reservation[] filtered =
                new Reservation[count];

        int index = 0;

        for (int i = 0;
                i < all.length;
                i++) {

            if (matchesFilter(
                    all[i],
                    null,
                    null)) {

                filtered[index] =
                        all[i];

                index++;
            }
        }

        // ==========================================
        // SORT
        // ==========================================

        insertionSortByConfirmation(
                filtered);

        // ==========================================
        // REPORT
        // ==========================================

        StringBuilder report =
                new StringBuilder();

        report.append(
                "\n==============================================================\n");

        report.append(
                "              FRONT DESK RESERVATION REPORT\n");

        report.append(
                "==============================================================\n");

        report.append(
                "Filter: All Booking Types | All Status\n");

        report.append(
                "Sorting: Confirmation Number (Ascending)\n");

        report.append(
                "==============================================================\n");

        report.append(
                String.format(
                        "%-10s %-12s %-20s %-10s %-12s %-12s%n",
                        "Confirm",
                        "Type",
                        "Guest",
                        "Room",
                        "Check-In",
                        "Status"));

        report.append(
                "--------------------------------------------------------------\n");

        int waiting = 0;
        int assigned = 0;
        int checkedIn = 0;
        int checkedOut = 0;
        int cancelled = 0;

        for (int i = 0;
                i < filtered.length;
                i++) {

            Reservation r =
                    filtered[i];

            String guestName =
                    r.getGuest() == null
                            ? "-"
                            : r.getGuest().getName();

            report.append(
                    String.format(
                            "%-10s %-12s %-20s %-10s %-12s %-12s%n",
                            r.getConfirmationNumber(),
                            r.getBookingType(),
                            guestName,
                            r.getRoomType(),
                            r.getCheckInDate(),
                            r.getStatus()));

            if (r.getStatus()
                    == ReservationStatus.WAITING) {

                waiting++;

            } else if (r.getStatus()
                    == ReservationStatus.ASSIGNED) {

                assigned++;

            } else if (r.getStatus()
                    == ReservationStatus.CHECKED_IN) {

                checkedIn++;

            } else if (r.getStatus()
                    == ReservationStatus.CHECKED_OUT) {

                checkedOut++;

            } else if (r.getStatus()
                    == ReservationStatus.CANCELLED) {

                cancelled++;
            }
        }

        report.append(
                "--------------------------------------------------------------\n");

        report.append(
                "Total Reservations : "
                        + filtered.length
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
                "==============================================================\n");

        return report.toString();
    }

    /**
     * Report 2:
     *
     * Guest Information Report.
     *
     * Filters:
     * - Guest exists
     * - Booking Type
     *
     * Sort:
     * - Guest name
     */
    public String generateGuestReport() {

        Reservation[] all =
                controller.getAllReservations();

        if (all == null || all.length == 0) {

            return
                    "\nNo guest data available.\n";
        }

        int count = 0;

        for (int i = 0;
                i < all.length;
                i++) {

            if (all[i] != null
                    && all[i].getGuest() != null) {

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
                    && all[i].getGuest() != null) {

                filtered[index] =
                        all[i];

                index++;
            }
        }

        insertionSortByGuestName(
                filtered);

        StringBuilder report =
                new StringBuilder();

        report.append(
                "\n==============================================================\n");

        report.append(
                "                  FRONT DESK GUEST REPORT\n");

        report.append(
                "==============================================================\n");

        report.append(
                "Filter: Guests with reservations\n");

        report.append(
                "Sorting: Guest Name (Ascending)\n");

        report.append(
                "==============================================================\n");

        report.append(
                String.format(
                        "%-20s %-18s %-15s %-10s %-12s%n",
                        "Guest Name",
                        "IC / Passport",
                        "Phone",
                        "Confirm",
                        "Status"));

        report.append(
                "--------------------------------------------------------------\n");

        for (int i = 0;
                i < filtered.length;
                i++) {

            Reservation r =
                    filtered[i];

            report.append(
                    String.format(
                            "%-20s %-18s %-15s %-10s %-12s%n",
                            r.getGuest().getName(),
                            r.getGuest().getIdentityNumber(),
                            r.getGuest().getPhone(),
                            r.getConfirmationNumber(),
                            r.getStatus()));
        }

        report.append(
                "--------------------------------------------------------------\n");

        report.append(
                "Total Guests: "
                        + filtered.length
                        + "\n");

        report.append(
                "==============================================================\n");

        return report.toString();
    }

    /**
     * Multiple filter method.
     */
    private boolean matchesFilter(
            Reservation reservation,
            BookingType bookingType,
            ReservationStatus status) {

        if (reservation == null) {
            return false;
        }

        if (bookingType != null
                && reservation.getBookingType()
                != bookingType) {

            return false;
        }

        if (status != null
                && reservation.getStatus()
                != status) {

            return false;
        }

        return true;
    }

    /**
     * Insertion Sort by confirmation number.
     */
    private void insertionSortByConfirmation(
            Reservation[] data) {

        for (int i = 1;
                i < data.length;
                i++) {

            Reservation key =
                    data[i];

            int j = i - 1;

            while (j >= 0
                    && data[j]
                    .getConfirmationNumber()
                    .compareTo(
                            key.getConfirmationNumber())
                    > 0) {

                data[j + 1] =
                        data[j];

                j--;
            }

            data[j + 1] =
                    key;
        }
    }

    /**
     * Insertion Sort by guest name.
     */
    private void insertionSortByGuestName(
            Reservation[] data) {

        for (int i = 1;
                i < data.length;
                i++) {

            Reservation key =
                    data[i];

            int j = i - 1;

            while (j >= 0
                    && data[j]
                    .getGuest()
                    .getName()
                    .compareToIgnoreCase(
                            key.getGuest()
                            .getName())
                    > 0) {

                data[j + 1] =
                        data[j];

                j--;
            }

            data[j + 1] =
                    key;
        }
    }
}