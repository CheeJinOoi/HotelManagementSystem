package entity;

public class Room {
    private final String roomId;
    private final String roomType;
    private HousekeepingStatus currentStatus;
    private final TaskLog taskLog;

    public Room(String roomId, String roomType, HousekeepingStatus initialStatus) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.currentStatus = initialStatus;
        this.taskLog = new TaskLog();
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

    public void recordStatus(StatusEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("StatusEntry cannot be null");
        }
        currentStatus = entry.getStatus();
        taskLog.add(entry);
    }

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
}
