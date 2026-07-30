package entity;

import java.time.LocalDateTime;

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
    public String toString() {
        return String.format("[%s] %s by %s%s",
                timestamp,
                status,
                updatedBy == null || updatedBy.isEmpty() ? "Unknown" : updatedBy,
                note == null || note.isEmpty() ? "" : ": " + note);
    }
}
