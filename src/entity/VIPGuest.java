package entity;

import java.io.Serializable;

/**
 * VIPGuest.java
 * ENTITY: VIP guest with membership tier, preferred room, and confirmation number.
 * Extends Guest to reuse name, identityNumber, and phone.
 * 
 * @author [Your Name]
 * Module: VIP & Loyalty Tier Priority Room Allocation
 */
public class VIPGuest extends Guest implements Comparable<VIPGuest>, Serializable {

    // ===== Member Enum =====
    public enum MembershipTier {
        ELITE(3, "Elite"),
        DIAMOND(2, "Diamond"),
        PLATINUM(1, "Platinum");

        private final int priority;
        private final String display;

        MembershipTier(int priority, String display) {
            this.priority = priority;
            this.display = display;
        }

        public int getPriority() {
            return priority;
        }

        public String getDisplay() {
            return display;
        }
    }

    // ===== Fields =====
    private MembershipTier tier;
    private String preferredRoomType;      // Preferred room type (Standard, Deluxe, Suite, Executive)
    private String confirmationNumber;      // 8-digit confirmation number
    private Room assignedRoom;              // Assigned room

    // ===== Constructors =====

    /**
     * Constructor with all required fields.
     * Phone number is inherited from Guest.
     */
    public VIPGuest(String name, String identityNumber, String phone,
                    MembershipTier tier, String preferredRoomType) {
        super(name, identityNumber, phone);
        this.tier = tier;
        this.preferredRoomType = preferredRoomType;
        this.confirmationNumber = null;
        this.assignedRoom = null;
    }

    /**
     * Constructor with preferred room type defaulting to "Standard".
     */
    public VIPGuest(String name, String identityNumber, String phone,
                    MembershipTier tier) {
        this(name, identityNumber, phone, tier, "Standard");
    }

    // ===== Getters =====
    public MembershipTier getTier() {
        return tier;
    }

    public String getPreferredRoomType() {
        return preferredRoomType;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public Room getAssignedRoom() {
        return assignedRoom;
    }

    // ===== Setters =====
    public void setTier(MembershipTier tier) {
        this.tier = tier;
    }

    public void setPreferredRoomType(String preferredRoomType) {
        this.preferredRoomType = preferredRoomType;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public void setAssignedRoom(Room assignedRoom) {
        this.assignedRoom = assignedRoom;
    }

    /**
     * Higher membership tier is greater so the max-heap keeps Elite at the front.
     * Order is Elite > Diamond > Platinum. Arrival time is not used.
     */
    @Override
    public int compareTo(VIPGuest other) {
        if (other == null) {
            return 1;
        }
        return Integer.compare(this.tier.getPriority(), other.tier.getPriority());
    }

    // ===== equals/hashCode using phone number (since membershipId removed) =====
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VIPGuest other = (VIPGuest) obj;
        // Use phone number from Guest
        return getPhone() != null && getPhone().equals(other.getPhone());
    }

    @Override
    public int hashCode() {
        return getPhone() != null ? getPhone().hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("%-20s [%-10s] Phone: %-12s Preferred: %-10s Conf: %s",
            getName(),
            tier.getDisplay(),
            getPhone(),
            preferredRoomType,
            confirmationNumber != null ? confirmationNumber : "Not assigned");
    }
}