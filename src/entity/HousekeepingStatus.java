package entity;

/**
 * HousekeepingStatus.java
 * Allowed cleaning states for a room.
 *
 * Normal flow (one step at a time):
 * DIRTY -> CLEANING_IN_PROGRESS -> INSPECTED -> READY_FOR_CHECKIN
 *
 * Walk-In may assign a guest only when status is READY_FOR_CHECKIN
 * and the room is not occupied.
 */
public enum HousekeepingStatus {
    DIRTY,
    CLEANING_IN_PROGRESS,
    INSPECTED,
    READY_FOR_CHECKIN;

    /** Next status in the normal cleaning workflow, or null if already Ready. */
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

    /** Previous status (used conceptually; undo uses stored UndoRecord). */
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

    /** Only allow moving one step forward in the cleaning chain. */
    public boolean canTransitionTo(HousekeepingStatus target) {
        if (target == null) {
            return false;
        }
        return target == next();
    }

    @Override
    public String toString() {
        switch (this) {
            case CLEANING_IN_PROGRESS:
                return "Cleaning In Progress";
            case READY_FOR_CHECKIN:
                return "Ready For Check-In";
            default:
                return name().replace('_', ' ');
        }
    }
}
