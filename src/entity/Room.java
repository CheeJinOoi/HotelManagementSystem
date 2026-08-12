package entity;

/**
 * Room.java
 * Shared hotel room used by Walk-In and Housekeeping.
 *
 * Housekeeping cares about cleaning status (Dirty -> Ready for Check-In).
 * Walk-In cares about whether the room is free (occupied flag) and Ready.
 * A room can be assigned only when isReadyForAssignment() is true.
 */
public class Room {
    private final String roomId;
    private final String roomType;
    /** Current cleaning / inspection status. */
    private HousekeepingStatus currentStatus;
    /** History of status changes for this room. */
    private final TaskLog taskLog;
    /** True when a guest is assigned / checked in to this room. */
    private boolean occupied;
    /** Confirmation number of the guest currently using the room (if any). */
    private String assignedConfirmationNumber;

    public Room(String roomId, String roomType, HousekeepingStatus initialStatus) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.currentStatus = initialStatus;
        this.taskLog = new TaskLog();
        this.occupied = false;
        taskLog.add(new StatusEntry(initialStatus, java.time.LocalDateTime.now(), "System", "Initial status"));
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public HousekeepingStatus getCurrentStatus() {
        return currentStatus;
    }

    public TaskLog getTaskLog() {
        return taskLog;
    }

    /** Apply a new housekeeping status entry and append it to the task log. */
    public void recordStatus(StatusEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("StatusEntry cannot be null");
        }
        currentStatus = entry.getStatus();
        taskLog.add(entry);
    }

    /** Roll back the latest status change (used by undo). */
    public StatusEntry rollbackLastStatus() {
        if (taskLog.size() <= 1) {
            return null;
        }
        taskLog.removeLast();
        StatusEntry last = taskLog.peekLast();
        currentStatus = last.getStatus();
        return last;
    }

    public String getLastUpdatedBy() {
        StatusEntry last = taskLog.peekLast();
        return last == null ? "Unknown" : last.getUpdatedBy();
    }

    public java.time.LocalDateTime getLastUpdatedTime() {
        StatusEntry last = taskLog.peekLast();
        return last == null ? null : last.getTimestamp();
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getAssignedConfirmationNumber() {
        return assignedConfirmationNumber;
    }

    /**
     * Walk-In can assign this room only if housekeeping finished cleaning
     * and no guest is currently occupying it.
     */
    public boolean isReadyForAssignment() {
        return currentStatus == HousekeepingStatus.READY_FOR_CHECKIN && !occupied;
    }

    /** Mark room as taken by a reservation (after assign / check-in). */
    public void occupy(String confirmationNumber) {
        this.occupied = true;
        this.assignedConfirmationNumber = confirmationNumber;
    }

    /** Clear guest occupancy without changing housekeeping status. */
    public void clearOccupancy() {
        this.occupied = false;
        this.assignedConfirmationNumber = null;
    }

    /**
     * After check-out: free the room and set status Dirty
     * so housekeeping must clean it again before the next guest.
     */
    public void vacateAfterCheckout() {
        clearOccupancy();
        recordStatus(new StatusEntry(
                HousekeepingStatus.DIRTY,
                java.time.LocalDateTime.now(),
                "Front Desk",
                "Guest checked out"));
    }
}
