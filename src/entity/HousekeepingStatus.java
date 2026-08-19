package entity;

/**
 * HousekeepingStatus.java
 * Allowed cleaning states for a room.
 *
 * Normal flow (one step at a time):
 * DIRTY -> CLEANING_IN_PROGRESS -> INSPECTED -> READY_FOR_CHECKIN (shown as Clean)
 *
 * Walk-In may assign a guest only when status is Clean (READY_FOR_CHECKIN)
 * and the room is not occupied.
 */
public enum HousekeepingStatus {
    DIRTY,
    CLEANING_IN_PROGRESS,
    INSPECTED,
    READY_FOR_CHECKIN;

    /** Next status in the normal cleaning workflow, or null if already Clean. */
    public HousekeepingStatus next() {
        switch (this) {
            case DIRTY:
                return CLEANING_IN_PROGRESS;
            case CLEANING_IN_PROGRESS:
                return INSPECTED;
            case INSPECTED:
                return READY_FOR_CHECKIN;
            default:
                return null;
        }
    }

    /** Previous status in the cleaning workflow, or null if already Dirty. */
    public HousekeepingStatus previous() {
        switch (this) {
            case CLEANING_IN_PROGRESS:
                return DIRTY;
            case INSPECTED:
                return CLEANING_IN_PROGRESS;
            case READY_FOR_CHECKIN:
                return INSPECTED;
            default:
                return null;
        }
    }

    /** Allow moving one step forward or one step backward. */
    public boolean canTransitionTo(HousekeepingStatus target) {
        if (target == null) {
            return false;
        }
        return target == next() || target == previous();
    }

    @Override
    public String toString() {
        switch (this) {
            case CLEANING_IN_PROGRESS:
                return "Cleaning In Progress";
            case READY_FOR_CHECKIN:
                return "Clean";
            default:
                return name().replace('_', ' ');
        }
    }
}
