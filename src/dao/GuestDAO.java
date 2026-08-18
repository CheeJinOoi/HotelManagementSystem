package dao;

import entity.Guest;
import hashing.DictionaryInterface;
import hashing.HashedDictionary;

/**
 * GuestDAO
 *
 * DATA ACCESS OBJECT for Guest.
 *
 */
public class GuestDAO {

    private DictionaryInterface<String, Guest> guestDictionary;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public GuestDAO() {

        guestDictionary =
                new HashedDictionary<String, Guest>();
    }

    // =====================================================
    // CREATE
    // =====================================================

    /**
     * Adds a new guest.
     *
     * Identity number is used as the unique key.
     *
     * @return true if successfully added
     */
    public boolean addGuest(Guest guest) {

        if (guest == null) {
            return false;
        }

        if (guest.getIdentityNumber() == null
                || guest.getIdentityNumber().isEmpty()) {

            return false;
        }

        String key =
                guest.getIdentityNumber();

        // Do not allow duplicate IC / passport
        if (guestDictionary.contains(key)) {
            return false;
        }

        guestDictionary.add(key, guest);

        return true;
    }

    // =====================================================
    // READ
    // =====================================================

    /**
     * Finds a guest using IC / passport number.
     */
    public Guest getGuest(String identityNumber) {

        if (identityNumber == null) {
            return null;
        }

        return guestDictionary.getValue(
                identityNumber);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    /**
     * Updates an existing guest.
     */
    public boolean updateGuest(Guest guest) {

        if (guest == null
                || guest.getIdentityNumber() == null) {

            return false;
        }

        String key =
                guest.getIdentityNumber();

        if (!guestDictionary.contains(key)) {
            return false;
        }

        /*
         * HashedDictionary.add()
         * replaces the existing value
         * if the key already exists.
         */
        guestDictionary.add(key, guest);

        return true;
    }

    // =====================================================
    // DELETE
    // =====================================================

    /**
     * Removes a guest using IC / passport number.
     */
    public Guest deleteGuest(String identityNumber) {

        if (identityNumber == null) {
            return null;
        }

        return guestDictionary.remove(
                identityNumber);
    }

    // =====================================================
    // SEARCH
    // =====================================================

    /**
     * Checks whether a guest exists.
     */
    public boolean containsGuest(
            String identityNumber) {

        return guestDictionary.contains(
                identityNumber);
    }

    // =====================================================
    // SIZE
    // =====================================================

    /**
     * Returns total number of guests.
     */
    public int getGuestCount() {

        return guestDictionary.getSize();
    }

    // =====================================================
    // GET ALL
    // =====================================================

    /**
     * Returns all Guest objects.
     *
     * Uses Object[] because the custom
     * HashedDictionary returns Object[].
     */
    public Object[] getAllGuests() {

        return guestDictionary.getAllValues();
    }

    // =====================================================
    // CLEAR
    // =====================================================

    /**
     * Removes all guests.
     */
    public void clear() {

        guestDictionary.clear();
    }
}