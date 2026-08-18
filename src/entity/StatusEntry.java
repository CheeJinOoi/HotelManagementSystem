package entity;

import java.time.LocalDateTime;

/**
 * StatusEntry.java
 * One housekeeping status-change event stored in a room's TaskLog.
 */
public class StatusEntry {
    private final HousekeepingStatus status;
    private final LocalDateTime timestamp;
    private final String updatedBy;
    private final String note;

    public StatusEntry(HousekeepingStatus status, LocalDateTime timestamp, String updatedBy, String note) {
        this.status = status;
        this.timestamp = timestamp;
        this.updatedBy = updatedBy;
        this.note = note;
    }

    public HousekeepingStatus getStatus() {
        return status;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StatusEntry other = (StatusEntry) obj;
        return status == other.status
                && java.util.Objects.equals(timestamp, other.timestamp)
                && java.util.Objects.equals(updatedBy, other.updatedBy);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(status, timestamp, updatedBy);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s%s",
                timestamp,
                status,
                updatedBy == null || updatedBy.isEmpty() ? "Unknown" : updatedBy,
                note == null || note.isEmpty() ? "" : ": " + note);
    }
}
