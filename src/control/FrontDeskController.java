package control;

import entity.Guest;
import entity.Reservation;
import entity.ReservationStatus;
import entity.Room;
import hashing.DictionaryInterface;
import hashing.HashedDictionary;

/**
 * FrontDeskController.java
 *
 * CONTROL class for Front Desk operations and reports.
 *
 * Responsibilities:
 * - Search reservation using confirmation number (hash table)
 * - Check room availability
 * - Check billing information
 * - Check out guest
 * - Generate VIP and operational front desk reports
 *
 * @author Tan Jun Ren
 */
public class FrontDeskController {

    private WalkInBookingControl walkIn;
    private DictionaryInterface<String, Reservation> reservationHash;
    private HousekeepingController housekeeping;

    public FrontDeskController(WalkInBookingControl walkIn) {
        this.walkIn = walkIn;
        this.housekeeping = walkIn.getHousekeepingController();
        reservationHash = new HashedDictionary<String, Reservation>();
        buildReservationHash();
    }

    public final void buildReservationHash() {
        reservationHash.clear();
        Reservation[] reservations = walkIn.getAllReservations();
        if (reservations == null) {
            return;
        }
        for (int i = 0; i < reservations.length; i++) {
            Reservation reservation = reservations[i];
            if (reservation == null) {
                continue;
            }
            String confirmation = reservation.getConfirmationNumber();
            if (confirmation == null) {
                continue;
            }
            reservationHash.add(confirmation, reservation);
        }
    }

    public void refreshReservations() {
        buildReservationHash();
    }

    public void refreshReservationHash() {
        buildReservationHash();
    }

    public void refreshHashTable() {
        refreshReservationHash();
    }

    public Reservation findReservation(String confirmationNumber) {
        if (confirmationNumber == null) {
            return null;
        }
        String key = confirmationNumber.trim();
        if (!isValidConfirmationNumber(key)) {
            return null;
        }
        return reservationHash.getValue(key);
    }

    public Reservation searchGuest(String confirmationNumber) {
        return findReservation(confirmationNumber);
    }

    public String formatReservationDetails(Reservation reservation) {
        if (reservation == null) {
            return "Reservation not found.";
        }

        StringBuilder output = new StringBuilder();
        output.append("\n========================================\n");
        output.append("       GUEST / RESERVATION DETAILS\n");
        output.append("========================================\n");
        output.append("Confirmation : ").append(reservation.getConfirmationNumber()).append("\n");
        output.append("Status       : ").append(reservation.getStatus()).append("\n");
        output.append("Booking Type : ").append(reservation.getBookingType()).append("\n");
        output.append("Room Type    : ").append(reservation.getRoomType()).append("\n");
        output.append("Room         : ")
                .append(reservation.getAssignedRoomId() == null ? "-" : reservation.getAssignedRoomId())
                .append("\n");
        output.append("Check-in     : ").append(reservation.getCheckInDate()).append("\n");
        output.append("Check-out    : ").append(reservation.getCheckOutDate()).append("\n");

        if (reservation.getGuest() != null) {
            output.append("Guest Name   : ").append(reservation.getGuest().getName()).append("\n");
            output.append("IC/Passport  : ").append(reservation.getGuest().getIdentityNumber()).append("\n");
            output.append("Phone        : ").append(reservation.getGuest().getPhone()).append("\n");
        }

        output.append("========================================\n");
        return output.toString();
    }

    public String checkOutGuest(String confirmationNumber) {
        if (confirmationNumber == null || confirmationNumber.trim().isEmpty()) {
            return "Confirmation number is required.";
        }

        confirmationNumber = confirmationNumber.trim();
        Reservation reservation = findReservation(confirmationNumber);
        if (reservation == null) {
            refreshReservationHash();
            reservation = findReservation(confirmationNumber);
        }
        if (reservation == null) {
            return "Reservation not found.";
        }
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN
                && reservation.getStatus() != ReservationStatus.ASSIGNED) {
            return "Guest cannot check out.\nCurrent status: " + reservation.getStatus();
        }

        String result = walkIn.checkOutGuest(confirmationNumber);
        refreshReservationHash();
        return result;
    }

    public String checkRoomAvailability(String roomType) {
        return searchRoomAvailability(roomType);
    }

    public String searchAvailableRooms(String roomType) {
        return searchRoomAvailability(roomType);
    }

    public String searchRoomAvailability(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) {
            return "Room type is required.";
        }

        int total = getTotalRoomCount(roomType);
        int available = getAvailableRoomCount(roomType);
        int occupied = total - available;

        StringBuilder output = new StringBuilder();
        output.append("\n========================================================\n");
        output.append("              ROOM AVAILABILITY SEARCH\n");
        output.append("========================================================\n");
        output.append("Room Type           : ").append(roomType).append("\n");
        output.append("Total Rooms         : ").append(total).append("\n");
        output.append("Occupied / Unavailable : ").append(occupied).append("\n");
        output.append("Available Rooms     : ").append(available).append("\n");
        output.append("--------------------------------------------------------\n");

        if (available > 0) {
            output.append("Available Room Numbers:\n");
            Room[] rooms = housekeeping.getAllRooms();
            for (int i = 0; i < rooms.length; i++) {
                if (rooms[i] == null) {
                    continue;
                }
                if (roomType.equalsIgnoreCase(rooms[i].getRoomType())
                        && rooms[i].isReadyForAssignment()) {
                    output.append("- ").append(rooms[i].getRoomId()).append("\n");
                }
            }
            output.append("\nRoom is AVAILABLE.\n");
        } else {
            output.append("Room is NOT AVAILABLE.\n");
        }

        output.append("========================================================\n");
        return output.toString();
    }

    public String checkBill(String confirmationNumber) {
        return getGuestBill(confirmationNumber);
    }

    public String getGuestBill(String confirmationNumber) {
        Reservation reservation = findReservation(confirmationNumber);
        if (reservation == null) {
            return "Reservation not found.";
        }
        if (reservation.getGuest() == null) {
            return "Guest information not available.";
        }
        if (reservation.getCheckInDate() == null || reservation.getCheckOutDate() == null) {
            return "Check-in / Check-out date unavailable.";
        }
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN
                && reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            return "Bill cannot be generated yet.\nGuest status: " + reservation.getStatus();
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                reservation.getCheckInDate(), reservation.getCheckOutDate());
        if (nights < 1) {
            nights = 1;
        }

        double pricePerNight = getRoomPrice(reservation.getRoomType());
        double roomCharge = pricePerNight * nights;
        double serviceCharge = 50.00;
        double foodCharge = 80.00;
        double subtotal = roomCharge + serviceCharge + foodCharge;
        double tax = subtotal * 0.06;
        double total = subtotal + tax;

        StringBuilder output = new StringBuilder();
        output.append("\n========================================================\n");
        output.append("                    GUEST BILL\n");
        output.append("========================================================\n");
        output.append("Guest Name       : ").append(reservation.getGuest().getName()).append("\n");
        output.append("Confirmation No. : ").append(reservation.getConfirmationNumber()).append("\n");
        output.append("Room Number      : ")
                .append(reservation.getAssignedRoomId() == null ? "-" : reservation.getAssignedRoomId())
                .append("\n");
        output.append("Room Type        : ").append(reservation.getRoomType()).append("\n");
        output.append("Check-in         : ").append(reservation.getCheckInDate()).append("\n");
        output.append("Check-out        : ").append(reservation.getCheckOutDate()).append("\n");
        output.append("Number of Nights : ").append(nights).append("\n");
        output.append("--------------------------------------------------------\n");
        output.append(String.format("Room Charge      : RM %.2f\n", roomCharge));
        output.append(String.format("Service Charge   : RM %.2f\n", serviceCharge));
        output.append(String.format("Food & Beverage  : RM %.2f\n", foodCharge));
        output.append("--------------------------------------------------------\n");
        output.append(String.format("Subtotal         : RM %.2f\n", subtotal));
        output.append(String.format("Tax (6%%)         : RM %.2f\n", tax));
        output.append("--------------------------------------------------------\n");
        output.append(String.format("TOTAL BILL       : RM %.2f\n", total));
        output.append("========================================================\n");
        return output.toString();
    }

    public Room[] getAvailableRooms(String roomType) {
        if (roomType == null || housekeeping == null) {
            return new Room[0];
        }
        Room[] rooms = housekeeping.getAllRooms();
        int count = getAvailableRoomCount(roomType);
        Room[] available = new Room[count];
        int index = 0;
        for (int i = 0; i < rooms.length; i++) {
            if (rooms[i] == null) {
                continue;
            }
            if (roomType.equalsIgnoreCase(rooms[i].getRoomType())
                    && rooms[i].isReadyForAssignment()) {
                available[index] = rooms[i];
                index++;
            }
        }
        return available;
    }

    public Room[] getAllRooms() {
        if (housekeeping == null) {
            return new Room[0];
        }
        return housekeeping.getAllRooms();
    }

    public int getAvailableRoomCount(String roomType) {
        if (roomType == null || housekeeping == null) {
            return 0;
        }
        Room[] rooms = housekeeping.getAllRooms();
        int count = 0;
        for (int i = 0; i < rooms.length; i++) {
            if (rooms[i] == null) {
                continue;
            }
            if (roomType.equalsIgnoreCase(rooms[i].getRoomType())
                    && rooms[i].isReadyForAssignment()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalRoomCount(String roomType) {
        if (roomType == null || housekeeping == null) {
            return 0;
        }
        Room[] rooms = housekeeping.getAllRooms();
        int count = 0;
        for (int i = 0; i < rooms.length; i++) {
            if (rooms[i] == null) {
                continue;
            }
            if (roomType.equalsIgnoreCase(rooms[i].getRoomType())) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCheckOut() {
        Reservation[] reservations = walkIn.getAllReservations();
        int total = 0;
        if (reservations == null) {
            return 0;
        }
        for (int i = 0; i < reservations.length; i++) {
            Reservation reservation = reservations[i];
            if (reservation != null
                    && reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
                total++;
            }
        }
        return total;
    }

    public Reservation[] getAllReservations() {
        return walkIn.getAllReservations();
    }

    public boolean isValidConfirmationNumber(String confirmationNumber) {
        if (confirmationNumber == null) {
            return false;
        }
        if (confirmationNumber.length() != 8) {
            return false;
        }
        for (int i = 0; i < confirmationNumber.length(); i++) {
            char c = confirmationNumber.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public int getReservationHashSize() {
        return reservationHash.getSize();
    }

    public HousekeepingController getHousekeepingController() {
        return housekeeping;
    }

    // =====================================================
    // REPORT 1 — VIP GUEST REPORT
    // =====================================================

    public Reservation[] getVIPReservations() {
        Reservation[] all = getAllReservations();
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
        Reservation[] all = getAllReservations();
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
        Reservation[] all = getAllReservations();
        if (all == null || all.length == 0) {
            return new Reservation[0];
        }
        Reservation[] sorted = copyReservations(all);
        insertionSortByConfirmation(sorted);
        return sorted;
    }

    public String generateVIPGuestReport() {
        Reservation[] all = getAllReservations();
        if (all == null || all.length == 0) {
            return "\nNo reservation data available.\n";
        }

        Reservation[] vipReservations = getVIPReservations();
        StringBuilder report = new StringBuilder();
        report.append("\n==============================================================\n");
        report.append("                 VIP GUEST REPORT\n");
        report.append("==============================================================\n");
        report.append("VIP criteria: repeat guest OR currently checked-in\n");
        report.append(String.format("%-20s %-12s %-18s %-12s %-15s\n",
                "Guest", "Confirmation", "Room", "Status", "VIP Level"));
        report.append("--------------------------------------------------------------\n");

        for (int i = 0; i < vipReservations.length; i++) {
            Reservation reservation = vipReservations[i];
            Guest guest = reservation.getGuest();
            String room = reservation.getAssignedRoomId();
            if (room == null) {
                room = "-";
            }
            report.append(String.format("%-20s %-12s %-18s %-12s %-15s\n",
                    guest.getName(),
                    reservation.getConfirmationNumber(),
                    room,
                    reservation.getStatus(),
                    getVipLevel(reservation)));
        }

        report.append("--------------------------------------------------------------\n");
        report.append("Total VIP records : " + vipReservations.length + "\n");
        report.append("==============================================================\n");
        return report.toString();
    }

    // =====================================================
    // REPORT 2 — FRONT DESK OPERATIONAL REPORT
    // =====================================================

    public String generateFrontDeskReport() {
        Reservation[] all = getAllReservations();
        if (all == null || all.length == 0) {
            return "\nNo reservation data available.\n";
        }

        Reservation[] sorted = copyReservations(all);
        insertionSortByConfirmation(sorted);

        int total = 0;
        int waiting = 0;
        int assigned = 0;
        int checkedIn = 0;
        int checkedOut = 0;
        int cancelled = 0;
        int standard = 0;
        int deluxe = 0;
        int suite = 0;

        for (int i = 0; i < sorted.length; i++) {
            Reservation reservation = sorted[i];
            if (reservation == null) {
                continue;
            }
            total++;

            if (reservation.getStatus() == ReservationStatus.WAITING) {
                waiting++;
            } else if (reservation.getStatus() == ReservationStatus.ASSIGNED) {
                assigned++;
            } else if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
                checkedIn++;
            } else if (reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
                checkedOut++;
            } else if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                cancelled++;
            }

            if (reservation.getRoomType() != null) {
                if (reservation.getRoomType().equalsIgnoreCase("Standard")) {
                    standard++;
                } else if (reservation.getRoomType().equalsIgnoreCase("Deluxe")) {
                    deluxe++;
                } else if (reservation.getRoomType().equalsIgnoreCase("Suite")) {
                    suite++;
                }
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("\n====================================================\n");
        report.append("             FRONT DESK OPERATIONAL REPORT\n");
        report.append("====================================================\n");
        report.append("\n[Reservation Summary]\n");
        report.append("Total Reservations : " + total + "\n");
        report.append("Waiting            : " + waiting + "\n");
        report.append("Assigned           : " + assigned + "\n");
        report.append("Checked-In         : " + checkedIn + "\n");
        report.append("Checked-Out        : " + checkedOut + "\n");
        report.append("Cancelled          : " + cancelled + "\n");
        report.append("\n[Room Type Analysis]\n");
        report.append("Standard           : " + standard + "\n");
        report.append("Deluxe             : " + deluxe + "\n");
        report.append("Suite              : " + suite + "\n");
        report.append("\n[Reservation Details]\n");
        report.append(String.format("%-12s %-20s %-12s %-12s %-12s\n",
                "Confirmation", "Guest", "Room Type", "Status", "Room"));
        report.append("----------------------------------------------------\n");

        for (int i = 0; i < sorted.length; i++) {
            Reservation reservation = sorted[i];
            if (reservation == null) {
                continue;
            }
            Guest guest = reservation.getGuest();
            String guestName = guest == null ? "-" : guest.getName();
            String room = reservation.getAssignedRoomId();
            if (room == null) {
                room = "-";
            }
            report.append(String.format("%-12s %-20s %-12s %-12s %-12s\n",
                    reservation.getConfirmationNumber(),
                    guestName,
                    reservation.getRoomType(),
                    reservation.getStatus(),
                    room));
        }

        report.append("====================================================\n");
        return report.toString();
    }

    public Reservation[] searchByGuestIdentity(String identityNumber) {
        Reservation[] all = getAllReservations();
        if (all == null) {
            return new Reservation[0];
        }

        int count = 0;
        for (int i = 0; i < all.length; i++) {
            if (all[i] != null
                    && all[i].getGuest() != null
                    && identityNumber != null
                    && identityNumber.equals(all[i].getGuest().getIdentityNumber())) {
                count++;
            }
        }

        Reservation[] result = new Reservation[count];
        int index = 0;
        for (int i = 0; i < all.length; i++) {
            if (all[i] != null
                    && all[i].getGuest() != null
                    && identityNumber != null
                    && identityNumber.equals(all[i].getGuest().getIdentityNumber())) {
                result[index] = all[i];
                index++;
            }
        }
        return result;
    }

    public void runFrontDesk() {
        boundary.FrontDeskUI ui = new boundary.FrontDeskUI(this);
        ui.run();
    }

    private boolean isVipReservation(Reservation reservation, Reservation[] all) {
        if (reservation == null || reservation.getGuest() == null) {
            return false;
        }
        int reservationCount = countGuestReservations(all, reservation.getGuest().getIdentityNumber());
        return reservationCount > 1
                || reservation.getStatus() == ReservationStatus.CHECKED_IN;
    }

    private int countGuestReservations(Reservation[] reservations, String identityNumber) {
        int count = 0;
        for (int i = 0; i < reservations.length; i++) {
            if (reservations[i] == null || reservations[i].getGuest() == null) {
                continue;
            }
            if (identityNumber.equals(reservations[i].getGuest().getIdentityNumber())) {
                count++;
            }
        }
        return count;
    }

    private Reservation[] copyReservations(Reservation[] source) {
        Reservation[] copy = new Reservation[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i];
        }
        return copy;
    }

    private void insertionSortByGuestName(Reservation[] reservations) {
        for (int i = 1; i < reservations.length; i++) {
            Reservation key = reservations[i];
            if (key == null || key.getGuest() == null) {
                continue;
            }
            String keyName = key.getGuest().getName();
            int j = i - 1;
            while (j >= 0
                    && reservations[j] != null
                    && reservations[j].getGuest() != null
                    && reservations[j].getGuest().getName().compareToIgnoreCase(keyName) > 0) {
                reservations[j + 1] = reservations[j];
                j--;
            }
            reservations[j + 1] = key;
        }
    }

    private void insertionSortByConfirmation(Reservation[] reservations) {
        for (int i = 1; i < reservations.length; i++) {
            Reservation key = reservations[i];
            if (key == null) {
                continue;
            }
            String keyNumber = key.getConfirmationNumber();
            int j = i - 1;
            while (j >= 0
                    && reservations[j] != null
                    && reservations[j].getConfirmationNumber().compareTo(keyNumber) > 0) {
                reservations[j + 1] = reservations[j];
                j--;
            }
            reservations[j + 1] = key;
        }
    }

    private double getRoomPrice(String roomType) {
        if (roomType == null) {
            return 0.0;
        }
        if (roomType.equalsIgnoreCase("Standard")) {
            return 200.00;
        }
        if (roomType.equalsIgnoreCase("Deluxe")) {
            return 300.00;
        }
        if (roomType.equalsIgnoreCase("Suite")) {
            return 500.00;
        }
        return 200.00;
    }
}
