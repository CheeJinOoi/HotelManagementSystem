
package control;

import entity.Reservation;
import entity.ReservationStatus;
import entity.Room;
import hashing.HashedDictionary;

/**
 * FrontDeskController.java
 *
 * CONTROL class for Front Desk operations.
 *
 * Responsibilities:
 * - Search reservation using confirmation number
 * - Check room availability
 * - Check billing information
 * - Check out guest
 * - Generate front desk reports
 *
 * @author Tan Jun Ren
 */
public class FrontDeskController {

    private WalkInBookingControl walkIn;

 
    private HashedDictionary<String, Reservation> reservationHash;

 
    public FrontDeskController(WalkInBookingControl walkIn) {

        this.walkIn = walkIn;

        reservationHash =
                new HashedDictionary<String, Reservation>();

        loadReservations();
    }


    private void loadReservations() {

        Reservation[] reservations =
                walkIn.getAllReservations();

        if (reservations == null) {
            return;
        }

        for (int i = 0;
             i < reservations.length;
             i++) {

            Reservation reservation =
                    reservations[i];

            if (reservation != null) {

                String confirmation =
                        reservation.getConfirmationNumber();

                if (confirmation != null) {

                    reservationHash.add(
                            confirmation,
                            reservation);
                }
            }
        }
    }


    public void refreshReservations() {

        reservationHash.clear();

        loadReservations();
    }


    public Reservation findReservation(
            String confirmationNumber) {

        if (confirmationNumber == null) {
            return null;
        }

        confirmationNumber =
                confirmationNumber.trim();

        if (confirmationNumber.isEmpty()) {
            return null;
        }

        /*
         * Fast O(1) average search.
         */
        return reservationHash.getValue(
                confirmationNumber);
    }


    public String formatReservationDetails(
            Reservation reservation) {

        if (reservation == null) {

            return "Reservation not found.";
        }

        StringBuilder output =
                new StringBuilder();

        output.append(
                "\n========================================\n");

        output.append(
                "       GUEST / RESERVATION DETAILS\n");

        output.append(
                "========================================\n");

        output.append(
                "Confirmation : ")
                .append(
                        reservation
                                .getConfirmationNumber())
                .append("\n");

        output.append(
                "Status       : ")
                .append(
                        reservation.getStatus())
                .append("\n");

        output.append(
                "Booking Type : ")
                .append(
                        reservation.getBookingType())
                .append("\n");

        output.append(
                "Room Type    : ")
                .append(
                        reservation.getRoomType())
                .append("\n");

        output.append(
                "Room         : ")
                .append(
                        reservation.getAssignedRoomId()
                                == null
                        ? "-"
                        : reservation
                                .getAssignedRoomId())
                .append("\n");

        output.append(
                "Check-in     : ")
                .append(
                        reservation.getCheckInDate())
                .append("\n");

        output.append(
                "Check-out    : ")
                .append(
                        reservation.getCheckOutDate())
                .append("\n");

        if (reservation.getGuest() != null) {

            output.append(
                    "Guest Name   : ")
                    .append(
                            reservation
                                    .getGuest()
                                    .getName())
                    .append("\n");

            output.append(
                    "IC/Passport  : ")
                    .append(
                            reservation
                                    .getGuest()
                                    .getIdentityNumber())
                    .append("\n");

            output.append(
                    "Phone        : ")
                    .append(
                            reservation
                                    .getGuest()
                                    .getPhone())
                    .append("\n");
        }

        output.append(
                "========================================\n");

        return output.toString();
    }


    public String checkOutGuest(
            String confirmationNumber) {

        if (confirmationNumber == null
                || confirmationNumber.trim().isEmpty()) {

            return "Confirmation number is required.";
        }

        confirmationNumber =
                confirmationNumber.trim();

        Reservation reservation =
                findReservation(
                        confirmationNumber);

        if (reservation == null) {

            /*
             * Refresh in case a new reservation
             * was created after Front Desk started.
             */
            refreshReservations();

            reservation =
                    findReservation(
                            confirmationNumber);
        }

        if (reservation == null) {

            return "Reservation not found.";
        }

        if (reservation.getStatus()
                != ReservationStatus.CHECKED_IN
                && reservation.getStatus()
                != ReservationStatus.ASSIGNED) {

            return "Guest cannot check out.\n"
                    + "Current status: "
                    + reservation.getStatus();
        }

        /*
         * Let WalkInBookingControl perform
         * the actual checkout operation.
         */
        String result =
                walkIn.checkOutGuest(
                        confirmationNumber);

        /*
         * Refresh hash table.
         */
        refreshReservations();

        return result;
    }

 
    public String checkRoomAvailability(
            String roomType) {

        if (roomType == null
                || roomType.trim().isEmpty()) {

            return "Room type is required.";
        }

        roomType = roomType.trim();

        Room[] rooms =
                walkIn.getAllRooms();

        if (rooms == null
                || rooms.length == 0) {

            return "No rooms found.";
        }

        int total = 0;
        int available = 0;
        int occupied = 0;

        StringBuilder output =
                new StringBuilder();

        output.append(
                "\n========================================\n");

        output.append(
                "         ROOM AVAILABILITY\n");

        output.append(
                "========================================\n");

        output.append(
                "Room Type: ")
                .append(roomType)
                .append("\n\n");

        for (int i = 0;
             i < rooms.length;
             i++) {

            Room room = rooms[i];

            if (room == null) {
                continue;
            }

            if (room.getRoomType()
                    .equalsIgnoreCase(roomType)) {

                total++;

                if (room.isReadyForAssignment()) {

                    available++;

                    output.append(
                            room.getRoomId())
                            .append(" - AVAILABLE\n");

                } else {

                    occupied++;

                    output.append(
                            room.getRoomId())
                            .append(" - NOT AVAILABLE\n");
                }
            }
        }

        output.append(
                "\n----------------------------------------\n");

        output.append(
                "Total Rooms     : ")
                .append(total)
                .append("\n");

        output.append(
                "Available Rooms : ")
                .append(available)
                .append("\n");

        output.append(
                "Not Available   : ")
                .append(occupied)
                .append("\n");

        output.append(
                "========================================\n");

        return output.toString();
    }


    public String checkBill(
            String confirmationNumber) {

        Reservation reservation =
                findReservation(
                        confirmationNumber);

        if (reservation == null) {

            refreshReservations();

            reservation =
                    findReservation(
                            confirmationNumber);
        }

        if (reservation == null) {

            return "Reservation not found.";
        }

        StringBuilder bill =
                new StringBuilder();

        bill.append(
                "\n========================================\n");

        bill.append(
                "             BILL DETAILS\n");

        bill.append(
                "========================================\n");

        bill.append(
                "Confirmation : ")
                .append(
                        reservation
                                .getConfirmationNumber())
                .append("\n");

        if (reservation.getGuest() != null) {

            bill.append(
                    "Guest        : ")
                    .append(
                            reservation
                                    .getGuest()
                                    .getName())
                    .append("\n");
        }

        bill.append(
                "Room Type    : ")
                .append(
                        reservation.getRoomType())
                .append("\n");

        bill.append(
                "Check-in     : ")
                .append(
                        reservation.getCheckInDate())
                .append("\n");

        bill.append(
                "Check-out    : ")
                .append(
                        reservation.getCheckOutDate())
                .append("\n");

        bill.append(
                "Status       : ")
                .append(
                        reservation.getStatus())
                .append("\n");

        bill.append(
                "\nBilling information is available "
                + "from the reservation record.\n");

        bill.append(
                "========================================\n");

        return bill.toString();
    }


    public String generateFrontDeskReport() {

        Reservation[] reservations =
                walkIn.getAllReservations();

        int total = 0;
        int waiting = 0;
        int assigned = 0;
        int checkedIn = 0;
        int checkedOut = 0;
        int cancelled = 0;

        if (reservations != null) {

            for (int i = 0;
                 i < reservations.length;
                 i++) {

                Reservation reservation =
                        reservations[i];

                if (reservation == null) {
                    continue;
                }

                total++;

                ReservationStatus status =
                        reservation.getStatus();

                if (status ==
                        ReservationStatus.WAITING) {

                    waiting++;

                } else if (status ==
                        ReservationStatus.ASSIGNED) {

                    assigned++;

                } else if (status ==
                        ReservationStatus.CHECKED_IN) {

                    checkedIn++;

                } else if (status ==
                        ReservationStatus.CHECKED_OUT) {

                    checkedOut++;

                } else if (status ==
                        ReservationStatus.CANCELLED) {

                    cancelled++;
                }
            }
        }

        StringBuilder report =
                new StringBuilder();

        report.append(
                "\n============================================\n");

        report.append(
                "          FRONT DESK SERVICE REPORT\n");

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
                "Total Check-Out    : ")
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

  

    public int getTotalCheckOut() {

        Reservation[] reservations =
                walkIn.getAllReservations();

        int total = 0;

        if (reservations == null) {
            return 0;
        }

        for (int i = 0;
             i < reservations.length;
             i++) {

            Reservation reservation =
                    reservations[i];

            if (reservation != null
                    && reservation.getStatus()
                    == ReservationStatus.CHECKED_OUT) {

                total++;
            }
        }

        return total;
    }

    public Reservation[] getAllReservations() {

        return walkIn.getAllReservations();
    }

    public void runFrontDesk() {

    boundary.FrontDeskUI ui =
            new boundary.FrontDeskUI(this);

    ui.run();
}
}