package entity;

import java.time.LocalDateTime;

/**
 * UndoRecord.java
 * Snapshot of one housekeeping status change, stored on the undo/redo stacks.
 *
 * fromStatus = status before the update
 * toStatus   = status after the update
 * Used so undo can roll back, and redo can re-apply the same change.
 */
public class UndoRecord {
    private final String roomId;
    private final HousekeepingStatus fromStatus;
    private final HousekeepingStatus toStatus;
    private final LocalDateTime timestamp;
    private final String updatedBy;
    private final String note;

    public UndoRecord(String roomId, HousekeepingStatus fromStatus, HousekeepingStatus toStatus,
                      LocalDateTime timestamp, String updatedBy, String note) {
        this.roomId = roomId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.timestamp = timestamp;
        this.updatedBy = updatedBy;
        this.note = note;
    }

    public String getRoomId() {
        return roomId;
    }

    public HousekeepingStatus getFromStatus() {
        return fromStatus;
    }

    public HousekeepingStatus getToStatus() {
        return toStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getNote() {
        return note;
    }
}
