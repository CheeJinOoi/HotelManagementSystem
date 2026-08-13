package control;

import entity.Reservation;
import entity.ReservationStatus;

/**
 * FrontDeskController
 *
 * Front Desk uses the SAME reservation data
 * managed by WalkInBookingControl.
 *
 * No Java Collection Framework is used.
 */
public class FrontDeskController {

    private WalkInBookingControl walkIn;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FrontDeskController(WalkInBookingControl walkIn) {

        this.walkIn = walkIn;
    }

    // =====================================================
    // SEARCH RESERVATION
    // =====================================================

    /**
     * Search reservation using the unique
     * 8-digit confirmation number.
     *
     * The actual search is performed by
     * WalkInBookingControl.
     */
    public Reservation findReservation(String confirmationNumber) {

        if (confirmationNumber == null
                || confirmationNumber.trim().isEmpty()) {

            return null;
        }

        return walkIn.findReservation(
                confirmationNumber.trim());
    }

    // =====================================================
    // FORMAT RESERVATION
    // =====================================================

    /**
     * Displays complete guest and reservation information.
     */
    public String formatReservationDetails(
            Reservation reservation) {

        if (reservation == null) {

            return "Reservation not found.";
        }

        return walkIn.formatReservationDetails(
                reservation);
    }

    // =====================================================
    // GET ALL RESERVATIONS
    // =====================================================

    /**
     * Gets all reservations from the shared
     * WalkInBookingControl data.
     *
     * Uses custom ADT / array.
     * No ArrayList or Java Collection Framework.
     */
    public Reservation[] getAllReservations() {

        return walkIn.getAllReservations();
    }

    // =====================================================
    // FRONT DESK REPORT
    // =====================================================

    /**
     * Generates a simple Front Desk report.
     */
    public String generateFrontDeskReport() {

        Reservation[] reservations =
                getAllReservations();

        if (reservations == null
                || reservations.length == 0) {

            return "\n====================================\n"
                    + "      FRONT DESK REPORT\n"
                    + "====================================\n"
                    + "No reservation records found.\n";
        }

        int total = reservations.length;

        int waiting = 0;
        int assigned = 0;
        int checkedIn = 0;
        int checkedOut = 0;
        int cancelled = 0;

        for (int i = 0;
                i < reservations.length;
                i++) {

            Reservation r = reservations[i];

            if (r == null) {
                continue;
            }

            ReservationStatus status =
                    r.getStatus();

            if (status == ReservationStatus.WAITING) {

                waiting++;

            } else if (
                    status == ReservationStatus.ASSIGNED) {

                assigned++;

            } else if (
                    status == ReservationStatus.CHECKED_IN) {

                checkedIn++;

            } else if (
                    status == ReservationStatus.CHECKED_OUT) {

                checkedOut++;

            } else if (
                    status == ReservationStatus.CANCELLED) {

                cancelled++;
            }
        }

        StringBuilder report =
                new StringBuilder();

        report.append(
                "\n====================================================\n");

        report.append(
                "              FRONT DESK REPORT\n");

        report.append(
                "====================================================\n");

        report.append(
                "Total Reservations : ")
                .append(total)
                .append("\n");

        report.append(
                "Waiting            : ")
                .append(waiting)
                .append("\n");

        report.append(
                "Assigned           : ")
                .append(assigned)
                .append("\n");

        report.append(
                "Checked-In         : ")
                .append(checkedIn)
                .append("\n");

        report.append(
                "Checked-Out        : ")
                .append(checkedOut)
                .append("\n");

        report.append(
                "Cancelled          : ")
                .append(cancelled)
                .append("\n");

        report.append(
                "====================================================\n");

        return report.toString();
    }

    // =====================================================
    // CONSOLE FRONT DESK
    // =====================================================

    /**
     * Runs Front Desk console UI.
     */
    public void runFrontDesk() {

        boundary.FrontDeskUI ui =
                new boundary.FrontDeskUI(this);

        ui.run();
    }
}