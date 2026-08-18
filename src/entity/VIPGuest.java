package entity;

import java.io.Serializable;

public class VIPGuest extends Guest implements Comparable<VIPGuest>, Serializable {

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

        public int getPriority() { return priority; }
        public String getDisplay() { return display; }
    }

    // ===== Fields =====
    private MembershipTier tier;
    private int loyaltyPoints;
    private String email;
    private String membershipId;
    private Room assignedRoom;
    private String preferredRoomType;      // ← Add this
    private String confirmationNumber;      // ← Add this

    // ===== Constructors =====
    public VIPGuest(String name, String identityNumber, String phone,
                    String membershipId, MembershipTier tier) {
        super(name, identityNumber, phone);
        this.membershipId = membershipId;
        this.tier = tier;
        this.loyaltyPoints = 0;
        this.email = "";
        this.assignedRoom = null;
        this.preferredRoomType = "Standard";   // ← Default
        this.confirmationNumber = null;
    }

    public VIPGuest(String name, String identityNumber, String phone,
                    String membershipId, MembershipTier tier,
                    int loyaltyPoints, String email) {
        super(name, identityNumber, phone);
        this.membershipId = membershipId;
        this.tier = tier;
        this.loyaltyPoints = loyaltyPoints;
        this.email = email;
        this.assignedRoom = null;
        this.preferredRoomType = "Standard";   // ← Default
        this.confirmationNumber = null;
    }

    // ===== ADD THIS CONSTRUCTOR (for preferredRoomType) =====
    public VIPGuest(String name, String identityNumber, String phone,
                    String membershipId, MembershipTier tier,
                    int loyaltyPoints, String email, String preferredRoomType) {
        super(name, identityNumber, phone);
        this.membershipId = membershipId;
        this.tier = tier;
        this.loyaltyPoints = loyaltyPoints;
        this.email = email;
        this.assignedRoom = null;
        this.preferredRoomType = preferredRoomType;
        this.confirmationNumber = null;
    }

    // ===== Getters =====
    public MembershipTier getTier() { return tier; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public String getEmail() { return email; }
    public String getMembershipId() { return membershipId; }
    public Room getAssignedRoom() { return assignedRoom; }

    // ✅ ADD THIS GETTER
    public String getPreferredRoomType() { return preferredRoomType; }

    // ✅ ADD THIS GETTER
    public String getConfirmationNumber() { return confirmationNumber; }

    // ===== Setters =====
    public void setTier(MembershipTier tier) { this.tier = tier; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public void addLoyaltyPoints(int points) { this.loyaltyPoints += points; }
    public void setEmail(String email) { this.email = email; }
    public void setMembershipId(String membershipId) { this.membershipId = membershipId; }
    public void setAssignedRoom(Room assignedRoom) { this.assignedRoom = assignedRoom; }

    // ✅ ADD THIS SETTER
    public void setPreferredRoomType(String preferredRoomType) {
        this.preferredRoomType = preferredRoomType;
    }

    // ✅ ADD THIS SETTER
    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    @Override
    public int compareTo(VIPGuest other) {
        return Integer.compare(other.tier.getPriority(), this.tier.getPriority());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VIPGuest other = (VIPGuest) obj;
        return membershipId != null && membershipId.equals(other.membershipId);
    }

    @Override
    public int hashCode() {
        return membershipId != null ? membershipId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("%-20s [%-10s] ID: %-8s Points: %d",
            getName(), tier.getDisplay(), membershipId, loyaltyPoints);
    }
}