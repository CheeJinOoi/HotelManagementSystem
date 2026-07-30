package entity;

public enum HousekeepingStatus {
    DIRTY,
    CLEANING_IN_PROGRESS,
    INSPECTED,
    READY_FOR_CHECKIN;

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
