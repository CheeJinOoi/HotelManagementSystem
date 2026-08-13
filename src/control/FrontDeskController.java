package control;

import dao.GuestDAO;
import entity.Guest;
import entity.Reservation;
import entity.ReservationStatus;
import entity.Room;
import hashing.HashedDictionary;

/**
 * FrontDeskController
 *
 * CONTROL class for the Front Desk module.
 *
 * Responsibilities:
 * - Manage Guest through GuestDAO
 * - Manage Reservation using HashedDictionary
 * - Manage Room using HashedDictionary
 * - Assign available rooms
 * - Check-in and check-out guests
 * - Manage reservation status
 *
 */
public class FrontDeskController {

    // =====================================================
    // ATTRIBUTES
    // =====================================================

    private GuestDAO guestDAO;

    private HashedDictionary<String, Reservation>
            reservationDictionary;

    private HashedDictionary<String, Room>
            roomDictionary;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FrontDeskController() {

        guestDAO = new GuestDAO();

        reservationDictionary =
                new HashedDictionary<String, Reservation>();

        roomDictionary =
                new HashedDictionary<String, Room>();
    }


    // =====================================================
    // GUEST MANAGEMENT
    // =====================================================

    /**
     * Add a new guest.
     *
     * Identity number is used as the
     * search key in GuestDAO.
     */
    public boolean addGuest(Guest guest) {

        return guestDAO.addGuest(guest);
    }


    /**
     * Find guest using IC / passport number.
     */
    public Guest findGuest(String identityNumber) {

        return guestDAO.getGuest(identityNumber);
    }


    /**
     * Update guest information.
     */
    public boolean updateGuest(Guest guest) {

        return guestDAO.updateGuest(guest);
    }


    /**
     * Remove guest using IC / passport number.
     */
    public Guest removeGuest(String identityNumber) {

        return guestDAO.deleteGuest(identityNumber);
    }


    /**
     * Check whether guest exists.
     */
    public boolean guestExists(
            String identityNumber) {

        return guestDAO.containsGuest(
                identityNumber);
    }


    /**
     * Return total number of guests.
     */
    public int getGuestCount() {

        return guestDAO.getGuestCount();
    }


    /**
     * Return all guests.
     */
    public Object[] getAllGuests() {

        return guestDAO.getAllGuests();
    }


    // =====================================================
    // RESERVATION MANAGEMENT
    // =====================================================

    /**
     * Add a new reservation.
     *
     * Confirmation number is used as the
     * search key.
     *
     * New reservation starts with WAITING status.
     */
    public boolean addReservation(
            Reservation reservation) {

        if (reservation == null) {
            return false;
        }

        String confirmationNumber =
                reservation.getConfirmationNumber();

        if (confirmationNumber == null
                || confirmationNumber.isEmpty()) {

            return false;
        }

        // Prevent duplicate confirmation number
        if (reservationDictionary.contains(
                confirmationNumber)) {

            return false;
        }

        // New reservation starts as WAITING
        reservation.setStatus(
                ReservationStatus.WAITING);

        reservationDictionary.add(
                confirmationNumber,
                reservation);

        return true;
    }


    /**
     * Find reservation using confirmation number.
     */
    public Reservation findReservation(
            String confirmationNumber) {

        if (confirmationNumber == null) {
            return null;
        }

        return reservationDictionary.getValue(
                confirmationNumber);
    }


    /**
     * Update existing reservation.
     */
    public boolean updateReservation(
            Reservation reservation) {

        if (reservation == null) {
            return false;
        }

        String confirmationNumber =
                reservation.getConfirmationNumber();

        if (confirmationNumber == null
                || confirmationNumber.isEmpty()) {

            return false;
        }

        if (!reservationDictionary.contains(
                confirmationNumber)) {

            return false;
        }

        reservationDictionary.add(
                confirmationNumber,
                reservation);

        return true;
    }


    /**
     * Remove reservation from dictionary.
     */
    public Reservation removeReservation(
            String confirmationNumber) {

        if (confirmationNumber == null) {
            return null;
        }

        return reservationDictionary.remove(
                confirmationNumber);
    }


    /**
     * Check whether reservation exists.
     */
    public boolean reservationExists(
            String confirmationNumber) {

        return reservationDictionary.contains(
                confirmationNumber);
    }


    /**
     * Return total number of reservations.
     */
    public int getReservationCount() {

        return reservationDictionary.getSize();
    }


    /**
     * Return all reservations.
     */
    public Object[] getAllReservations() {

        return reservationDictionary.getAllValues();
    }


    // =====================================================
    // ROOM MANAGEMENT
    // =====================================================

    /**
     * Add a new room.
     *
     * Room ID is used as the search key.
     */
    public boolean addRoom(Room room) {

        if (room == null) {
            return false;
        }

        String roomId =
                room.getRoomId();

        if (roomId == null
                || roomId.isEmpty()) {

            return false;
        }

        // Prevent duplicate room ID
        if (roomDictionary.contains(roomId)) {

            return false;
        }

        roomDictionary.add(
                roomId,
                room);

        return true;
    }


    /**
     * Find room using room ID.
     */
    public Room findRoom(String roomId) {

        if (roomId == null) {
            return null;
        }

        return roomDictionary.getValue(roomId);
    }


    /**
     * Check whether room exists.
     */
    public boolean roomExists(String roomId) {

        return roomDictionary.contains(roomId);
    }


    /**
     * Remove room.
     */
    public Room removeRoom(String roomId) {

        if (roomId == null) {
            return null;
        }

        return roomDictionary.remove(roomId);
    }


    /**
     * Return total number of rooms.
     */
    public int getRoomCount() {

        return roomDictionary.getSize();
    }


    /**
     * Return all rooms.
     */
    public Object[] getAllRooms() {

        return roomDictionary.getAllValues();
    }


    // =====================================================
    // FIND AVAILABLE ROOM
    // =====================================================

    /**
     * Find a room that:
     *
     * 1. Has the requested room type
     * 2. Is READY_FOR_CHECKIN
     * 3. Is not occupied
     *
     * Uses Object[] returned by custom
     * HashedDictionary.
     */
    public Room findAvailableRoom(
            String roomType) {

        if (roomType == null) {
            return null;
        }

        Object[] rooms =
                roomDictionary.getAllValues();

        for (int i = 0;
             i < rooms.length;
             i++) {

            Room room =
                    (Room) rooms[i];

            if (room != null
                    && roomType.equalsIgnoreCase(
                            room.getRoomType())
                    && room.isReadyForAssignment()) {

                return room;
            }
        }

        return null;
    }


    // =====================================================
    // ASSIGN ROOM
    // =====================================================

    /**
     * Assign a specific room to a reservation.
     *
     * WAITING -> ASSIGNED
     */
    public boolean assignRoom(
            String confirmationNumber,
            String roomId) {

        Reservation reservation =
                findReservation(
                        confirmationNumber);

        if (reservation == null) {
            return false;
        }

        Room room =
                findRoom(roomId);

        if (room == null) {
            return false;
        }

        // Only WAITING reservation can be assigned
        if (reservation.getStatus()
                != ReservationStatus.WAITING) {

            return false;
        }

        // Reservation cannot already have a room
        if (reservation.getAssignedRoomId()
                != null) {

            return false;
        }

        // Room must be ready and not occupied
        if (!room.isReadyForAssignment()) {

            return false;
        }

        // Room type must match reservation
        if (!room.getRoomType()
                .equalsIgnoreCase(
                        reservation.getRoomType())) {

            return false;
        }

        // Assign room to reservation
        reservation.setAssignedRoomId(
                roomId);

        // Occupy room
        room.occupy(
                confirmationNumber);

        // Update status
        reservation.setStatus(
                ReservationStatus.ASSIGNED);

        return true;
    }


    // =====================================================
    // AUTOMATIC ROOM ASSIGNMENT
    // =====================================================

    /**
     * Automatically find a suitable room
     * and assign it to the reservation.
     *
     * WAITING -> ASSIGNED
     */
    public boolean autoAssignRoom(
            String confirmationNumber) {

        Reservation reservation =
                findReservation(
                        confirmationNumber);

        if (reservation == null) {
            return false;
        }

        if (reservation.getStatus()
                != ReservationStatus.WAITING) {

            return false;
        }

        Room room =
                findAvailableRoom(
                        reservation.getRoomType());

        if (room == null) {
            return false;
        }

        return assignRoom(
                confirmationNumber,
                room.getRoomId());
    }


    // =====================================================
    // CHECK IN
    // =====================================================

    /**
     * Check in a guest.
     *
     * ASSIGNED -> CHECKED_IN
     */
    public boolean checkIn(
            String confirmationNumber) {

        Reservation reservation =
                findReservation(
                        confirmationNumber);

        if (reservation == null) {
            return false;
        }

        // Only ASSIGNED reservation can check in
        if (reservation.getStatus()
                != ReservationStatus.ASSIGNED) {

            return false;
        }

        String roomId =
                reservation.getAssignedRoomId();

        if (roomId == null) {
            return false;
        }

        Room room =
                findRoom(roomId);

        if (room == null) {
            return false;
        }

        /*
         * Room should already be occupied
         * after assignment.
         */
        if (!room.isOccupied()) {

            room.occupy(
                    confirmationNumber);
        }

        // Update reservation status
        reservation.setStatus(
                ReservationStatus.CHECKED_IN);

        return true;
    }


    // =====================================================
    // CHECK OUT
    // =====================================================

    /**
     * Check out a guest.
     *
     * CHECKED_IN -> CHECKED_OUT
     *
     * Room becomes DIRTY after checkout.
     */
    public boolean checkOut(
            String confirmationNumber) {

        Reservation reservation =
                findReservation(
                        confirmationNumber);

        if (reservation == null) {
            return false;
        }

        // Only CHECKED_IN can check out
        if (reservation.getStatus()
                != ReservationStatus.CHECKED_IN) {

            return false;
        }

        String roomId =
                reservation.getAssignedRoomId();

        if (roomId == null) {
            return false;
        }

        Room room =
                findRoom(roomId);

        if (room == null) {
            return false;
        }

        if (!room.isOccupied()) {
            return false;
        }

        /*
         * This:
         * 1. Clears occupancy
         * 2. Changes room status to DIRTY
         * 3. Adds status history
         */
        room.vacateAfterCheckout();

        // Update reservation status
        reservation.setStatus(
                ReservationStatus.CHECKED_OUT);

        return true;
    }


    // =====================================================
    // CANCEL RESERVATION
    // =====================================================

    /**
     * Cancel a reservation.
     *
     * WAITING   -> CANCELLED
     * ASSIGNED  -> CANCELLED
     *
     * CHECKED_IN and CHECKED_OUT
     * cannot be cancelled.
     */
    public boolean cancelReservation(
            String confirmationNumber) {

        Reservation reservation =
                findReservation(
                        confirmationNumber);

        if (reservation == null) {
            return false;
        }

        ReservationStatus status =
                reservation.getStatus();

        // Cannot cancel checked-in
        if (status
                == ReservationStatus.CHECKED_IN) {

            return false;
        }

        // Cannot cancel checked-out
        if (status
                == ReservationStatus.CHECKED_OUT) {

            return false;
        }

        /*
         * If room has been assigned,
         * release the room.
         */
        String roomId =
                reservation.getAssignedRoomId();

        if (roomId != null) {

            Room room =
                    findRoom(roomId);

            if (room != null) {

                room.clearOccupancy();
            }

            reservation.setAssignedRoomId(
                    null);
        }

        // Update reservation status
        reservation.setStatus(
                ReservationStatus.CANCELLED);

        return true;
    }


    // =====================================================
    // ROOM STATISTICS
    // =====================================================

    /**
     * Count available rooms.
     */
    public int getAvailableRoomCount() {

        int count = 0;

        Object[] rooms =
                roomDictionary.getAllValues();

        for (int i = 0;
             i < rooms.length;
             i++) {

            Room room =
                    (Room) rooms[i];

            if (room != null
                    && room.isReadyForAssignment()) {

                count++;
            }
        }

        return count;
    }


    /**
     * Count occupied rooms.
     */
    public int getOccupiedRoomCount() {

        int count = 0;

        Object[] rooms =
                roomDictionary.getAllValues();

        for (int i = 0;
             i < rooms.length;
             i++) {

            Room room =
                    (Room) rooms[i];

            if (room != null
                    && room.isOccupied()) {

                count++;
            }
        }

        return count;
    }


    // =====================================================
    // RESERVATION STATISTICS
    // =====================================================

    /**
     * Count reservations by status.
     */
    public int countReservationsByStatus(
            ReservationStatus targetStatus) {

        if (targetStatus == null) {
            return 0;
        }

        int count = 0;

        Object[] reservations =
                reservationDictionary
                        .getAllValues();

        for (int i = 0;
             i < reservations.length;
             i++) {

            Reservation reservation =
                    (Reservation) reservations[i];

            if (reservation != null
                    && reservation.getStatus()
                            == targetStatus) {

                count++;
            }
        }

        return count;
    }


    // =====================================================
    // CLEAR
    // =====================================================

    /**
     * Clear all Front Desk data.
     */
    public void clearAll() {

        guestDAO.clear();

        reservationDictionary.clear();

        roomDictionary.clear();
    }
}