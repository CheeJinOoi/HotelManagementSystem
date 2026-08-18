package control;

import entity.Reservation;
import entity.ReservationStatus;
import entity.Room;
import hashing.HashedDictionary;


public class FrontDeskController {


    private WalkInBookingControl walkIn;

 
    private HashedDictionary<String, Reservation> reservationHash;

  
    private HousekeepingController housekeeping;

    public FrontDeskController(
            WalkInBookingControl walkIn) {

        this.walkIn = walkIn;

        this.housekeeping =
                walkIn.getHousekeepingController();

        reservationHash =
                new HashedDictionary<String, Reservation>();

        buildReservationHash();
    }

    public final void buildReservationHash() {

        reservationHash.clear();

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

            if (reservation == null) {
                continue;
            }

            String confirmation =
                    reservation.getConfirmationNumber();

            if (confirmation == null) {
                continue;
            }

            reservationHash.add(
                    confirmation,
                    reservation);
        }
    }

    public Reservation findReservation(
            String confirmationNumber) {

        if (confirmationNumber == null) {
            return null;
        }

        String key =
                confirmationNumber.trim();

        if (!isValidConfirmationNumber(key)) {
            return null;
        }

      
        return reservationHash.getValue(key);
    }



    public Reservation searchGuest(
            String confirmationNumber) {

        return findReservation(
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
                "\n========================================================\n");

        output.append(
                "                 GUEST INFORMATION\n");

        output.append(
                "========================================================\n");

        output.append(
                "Confirmation Number : ")
              .append(
                reservation.getConfirmationNumber())
              .append("\n");

        if (reservation.getGuest() != null) {

            output.append(
                    "Guest Name           : ")
                  .append(
                    reservation.getGuest().getName())
                  .append("\n");

            output.append(
                    "IC / Passport        : ")
                  .append(
                    reservation.getGuest()
                               .getIdentityNumber())
                  .append("\n");

            output.append(
                    "Phone                : ")
                  .append(
                    reservation.getGuest().getPhone())
                  .append("\n");
        }

        output.append(
                "Room Type            : ")
              .append(
                reservation.getRoomType())
              .append("\n");

        output.append(
                "Room Number          : ")
              .append(
                reservation.getAssignedRoomId() == null
                    ? "-"
                    : reservation.getAssignedRoomId())
              .append("\n");

        output.append(
                "Check-in Date        : ")
              .append(
                reservation.getCheckInDate())
              .append("\n");

        output.append(
                "Check-out Date       : ")
              .append(
                reservation.getCheckOutDate())
              .append("\n");

        output.append(
                "Booking Type         : ")
              .append(
                reservation.getBookingType())
              .append("\n");

        output.append(
                "Reservation Status   : ")
              .append(
                reservation.getStatus())
              .append("\n");

        output.append(
                "========================================================\n");

        return output.toString();
    }


    public Reservation[] getAllReservations() {

        return walkIn.getAllReservations();
    }


    public void refreshReservationHash() {

        buildReservationHash();
    }


    public int getAvailableRoomCount(
            String roomType) {

        if (roomType == null) {
            return 0;
        }

        Room[] rooms =
                housekeeping.getAllRooms();

        int count = 0;

        for (int i = 0;
             i < rooms.length;
             i++) {

            if (rooms[i] == null) {
                continue;
            }

            if (roomType.equalsIgnoreCase(
                    rooms[i].getRoomType())
                    && rooms[i].isReadyForAssignment()) {

                count++;
            }
        }

        return count;
    }


    public int getTotalRoomCount(
            String roomType) {

        if (roomType == null) {
            return 0;
        }

        Room[] rooms =
                housekeeping.getAllRooms();

        int count = 0;

        for (int i = 0;
             i < rooms.length;
             i++) {

            if (rooms[i] == null) {
                continue;
            }

            if (roomType.equalsIgnoreCase(
                    rooms[i].getRoomType())) {

                count++;
            }
        }

        return count;
    }


    public String searchRoomAvailability(
            String roomType) {

        if (roomType == null
                || roomType.trim().isEmpty()) {

            return "Room type is required.";
        }

        int total =
                getTotalRoomCount(roomType);

        int available =
                getAvailableRoomCount(roomType);

        int occupied =
                total - available;

        StringBuilder output =
                new StringBuilder();

        output.append(
                "\n========================================================\n");

        output.append(
                "              ROOM AVAILABILITY SEARCH\n");

        output.append(
                "========================================================\n");

        output.append(
                "Room Type           : ")
              .append(roomType)
              .append("\n");

        output.append(
                "Total Rooms         : ")
              .append(total)
              .append("\n");

        output.append(
                "Occupied / Unavailable : ")
              .append(occupied)
              .append("\n");

        output.append(
                "Available Rooms     : ")
              .append(available)
              .append("\n");

        output.append(
                "--------------------------------------------------------\n");

        if (available > 0) {

            output.append(
                    "Available Room Numbers:\n");

            Room[] rooms =
                    housekeeping.getAllRooms();

            for (int i = 0;
                 i < rooms.length;
                 i++) {

                if (rooms[i] == null) {
                    continue;
                }

                if (roomType.equalsIgnoreCase(
                        rooms[i].getRoomType())
                        && rooms[i].isReadyForAssignment()) {

                    output.append(
                            "- ")
                          .append(
                            rooms[i].getRoomId())
                          .append("\n");
                }
            }

            output.append(
                    "\nRoom is AVAILABLE.\n");

        } else {

            output.append(
                    "Room is NOT AVAILABLE.\n");
        }

        output.append(
                "========================================================\n");

        return output.toString();
    }


    public String getGuestBill(
            String confirmationNumber) {

        Reservation reservation =
                findReservation(
                        confirmationNumber);

        if (reservation == null) {

            return "Reservation not found.";
        }

        if (reservation.getGuest() == null) {

            return "Guest information not available.";
        }

        if (reservation.getCheckInDate() == null
                || reservation.getCheckOutDate() == null) {

            return "Check-in / Check-out date unavailable.";
        }


        if (reservation.getStatus()
                != ReservationStatus.CHECKED_IN
                && reservation.getStatus()
                != ReservationStatus.CHECKED_OUT) {

            return "Bill cannot be generated yet.\n"
                    + "Guest status: "
                    + reservation.getStatus();
        }

        long nights =
                java.time.temporal.ChronoUnit.DAYS.between(
                        reservation.getCheckInDate(),
                        reservation.getCheckOutDate());

        if (nights < 1) {
            nights = 1;
        }

        double pricePerNight =
                getRoomPrice(
                        reservation.getRoomType());

        double roomCharge =
                pricePerNight * nights;


        double serviceCharge = 50.00;

        double foodCharge = 80.00;

        double subtotal =
                roomCharge
                + serviceCharge
                + foodCharge;

        double tax =
                subtotal * 0.06;

        double total =
                subtotal + tax;

        StringBuilder output =
                new StringBuilder();

        output.append(
                "\n========================================================\n");

        output.append(
                "                    GUEST BILL\n");

        output.append(
                "========================================================\n");

        output.append(
                "Guest Name       : ")
              .append(
                reservation.getGuest().getName())
              .append("\n");

        output.append(
                "Confirmation No. : ")
              .append(
                reservation.getConfirmationNumber())
              .append("\n");

        output.append(
                "Room Number      : ")
              .append(
                reservation.getAssignedRoomId() == null
                    ? "-"
                    : reservation.getAssignedRoomId())
              .append("\n");

        output.append(
                "Room Type        : ")
              .append(
                reservation.getRoomType())
              .append("\n");

        output.append(
                "Check-in         : ")
              .append(
                reservation.getCheckInDate())
              .append("\n");

        output.append(
                "Check-out        : ")
              .append(
                reservation.getCheckOutDate())
              .append("\n");

        output.append(
                "Number of Nights : ")
              .append(nights)
              .append("\n");

        output.append(
                "--------------------------------------------------------\n");

        output.append(
                String.format(
                    "Room Charge      : RM %.2f\n",
                    roomCharge));

        output.append(
                String.format(
                    "Service Charge   : RM %.2f\n",
                    serviceCharge));

        output.append(
                String.format(
                    "Food & Beverage  : RM %.2f\n",
                    foodCharge));

        output.append(
                "--------------------------------------------------------\n");

        output.append(
                String.format(
                    "Subtotal         : RM %.2f\n",
                    subtotal));

        output.append(
                String.format(
                    "Tax (6%%)         : RM %.2f\n",
                    tax));

        output.append(
                "--------------------------------------------------------\n");

        output.append(
                String.format(
                    "TOTAL BILL       : RM %.2f\n",
                    total));

        output.append(
                "========================================================\n");

        return output.toString();
    }

    private double getRoomPrice(
            String roomType) {

        if (roomType == null) {
            return 0.0;
        }

        if (roomType.equalsIgnoreCase(
                "Standard")) {

            return 200.00;
        }

        if (roomType.equalsIgnoreCase(
                "Deluxe")) {

            return 300.00;
        }

        if (roomType.equalsIgnoreCase(
                "Suite")) {

            return 500.00;
        }

        return 200.00;
    }


    public boolean isValidConfirmationNumber(
            String confirmationNumber) {

        if (confirmationNumber == null) {
            return false;
        }

        if (confirmationNumber.length() != 8) {
            return false;
        }

        for (int i = 0;
             i < confirmationNumber.length();
             i++) {

            char c =
                    confirmationNumber.charAt(i);

            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    public int getReservationHashSize() {

        return reservationHash.getSize();
    }


    public HousekeepingController
            getHousekeepingController() {

        return housekeeping;
    }

    public void runFrontDesk() {

        boundary.FrontDeskUI ui =
                new boundary.FrontDeskUI(this);

        ui.run();
    }
}