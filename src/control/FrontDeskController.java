package control;

import entity.Reservation;
import hashing.HashedDictionary;


public class FrontDeskController {

    private WalkInBookingControl walkIn;

    private HashedDictionary<String, Reservation>
            reservationHash;

    public FrontDeskController(
            WalkInBookingControl walkIn) {

        this.walkIn = walkIn;

        reservationHash =
                new HashedDictionary<String, Reservation>();

        buildHashTable();
    }



    private void buildHashTable() {

        Reservation[] reservations =
                walkIn.getAllReservations();

        for (int i = 0;
                i < reservations.length;
                i++) {

            Reservation reservation =
                    reservations[i];

            if (reservation == null) {
                continue;
            }

            String confirmation =
                    reservation
                            .getConfirmationNumber();

            if (confirmation != null
                    && !confirmation.isEmpty()) {

                reservationHash.add(
                        confirmation,
                        reservation);
            }
        }
    }


 
    public void refreshHashTable() {

        reservationHash.clear();

        buildHashTable();
    }


    
    public Reservation findReservation(
            String confirmationNumber) {

        if (confirmationNumber == null) {
            return null;
        }

        String key =
                confirmationNumber.trim();

        if (key.isEmpty()) {
            return null;
        }

        return reservationHash.getValue(key);
    }



    public String formatReservationDetails(
            Reservation reservation) {

        if (reservation == null) {

            return "Reservation not found.";
        }

        return walkIn.formatReservationDetails(
                reservation);
    }


    public Reservation[] getAllReservations() {

        return walkIn.getAllReservations();
    }


    public String generateFrontDeskReport() {

        Reservation[] reservations =
                getAllReservations();

        if (reservations == null
                || reservations.length == 0) {

            return
                    "\n============================================\n"
                  + "          FRONT DESK REPORT\n"
                  + "============================================\n"
                  + "No reservation records found.\n";
        }


        int total = 0;
        int waiting = 0;
        int assigned = 0;
        int checkedIn = 0;
        int checkedOut = 0;
        int cancelled = 0;


        for (int i = 0;
                i < reservations.length;
                i++) {

            Reservation reservation =
                    reservations[i];

            if (reservation == null) {
                continue;
            }

            total++;

            if (reservation.getStatus()
                    == entity.ReservationStatus.WAITING) {

                waiting++;

            } else if (
                    reservation.getStatus()
                    == entity.ReservationStatus.ASSIGNED) {

                assigned++;

            } else if (
                    reservation.getStatus()
                    == entity.ReservationStatus.CHECKED_IN) {

                checkedIn++;

            } else if (
                    reservation.getStatus()
                    == entity.ReservationStatus.CHECKED_OUT) {

                checkedOut++;

            } else if (
                    reservation.getStatus()
                    == entity.ReservationStatus.CANCELLED) {

                cancelled++;
            }
        }


        StringBuilder report =
                new StringBuilder();

        report.append(
                "\n============================================\n");

        report.append(
                "          FRONT DESK REPORT\n");

        report.append(
                "============================================\n");

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
                "============================================\n");

        return report.toString();
    }


    public void runFrontDesk() {

        boundary.FrontDeskUI ui =
                new boundary.FrontDeskUI(this);

        ui.runFrontDesk();
    }
}