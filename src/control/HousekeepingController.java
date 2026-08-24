package control;

import adt.ArrayStack;
import adt.StackInterface;
import entity.HousekeepingStatus;
import entity.Room;
import entity.StatusEntry;
import entity.UndoRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * HousekeepingController.java
 * CONTROL layer for Housekeeping and Task Log.
 *
 * Responsibilities:
 * - Store hotel rooms in an array
 * - Update cleaning status with valid transitions only
 * - Support undo / redo using a custom Stack ADT
 * - Save / load room status history to a text file
 *
 * Works with Walk-In through the shared Room objects from HotelBootstrap.
 */
public class HousekeepingController {
    /** Fixed-capacity room list for this module. */
    private final Room[] rooms;
    private int roomCount;
    /** Stack of past actions so the supervisor can undo mistakes. */
    private final StackInterface<UndoRecord> undoStack;
    /** Stack of undone actions so redo can re-apply them. */
    private final StackInterface<UndoRecord> redoStack;

    public HousekeepingController(int capacity) {
        this.rooms = new Room[capacity];
        this.roomCount = 0;
        this.undoStack = new ArrayStack<>();
        this.redoStack = new ArrayStack<>();
    }

    /** Add a room into the hotel inventory. */
    public boolean addRoom(Room room) {
        if (room == null || roomCount >= rooms.length) {
            return false;
        }
        rooms[roomCount++] = room;
        return true;
    }

    /** Look up one room by id (case-insensitive). */
    public Room findRoomById(String roomId) {
        for (int i = 0; i < roomCount; i++) {
            if (rooms[i].getRoomId().equalsIgnoreCase(roomId)) {
                return rooms[i];
            }
        }
        return null;
    }

    /**
     * Move a room to the next allowed housekeeping status.
     * Invalid jumps (e.g. Dirty -> Ready) are rejected.
     * Each successful update is pushed onto the undo stack.
     */
    public String updateRoomStatus(String roomId, HousekeepingStatus newStatus, String updatedBy, String note) {
        Room room = findRoomById(roomId);
        if (room == null) {
            return "Room not found.";
        }
        HousekeepingStatus currentStatus = room.getCurrentStatus();
        if (currentStatus == newStatus) {
            return "Room is already " + newStatus + ".";
        }
        if (!currentStatus.canTransitionTo(newStatus)) {
            return "Invalid transition from " + currentStatus + " to " + newStatus + ".";
        }
        UndoRecord record = new UndoRecord(roomId, currentStatus, newStatus, LocalDateTime.now(), updatedBy, note);
        StatusEntry entry = new StatusEntry(newStatus, record.getTimestamp(), updatedBy, note);
        room.recordStatus(entry);
        undoStack.push(record);
        clearRedoStack();
        return "Status updated successfully.";
    }

    public String stepStatusForward(String roomId) {
        Room room = findRoomById(roomId);
        if (room == null) {
            return "Room not found.";
        }
        HousekeepingStatus next = room.getCurrentStatus().next();
        if (next == null) {
            return "Room " + roomId + " is already Clean.";
        }
        return updateRoomStatus(roomId, next, "Housekeeping", "Next status");
    }

    public String stepStatusBack(String roomId) {
        Room room = findRoomById(roomId);
        if (room == null) {
            return "Room not found.";
        }
        HousekeepingStatus previous = room.getCurrentStatus().previous();
        if (previous == null) {
            return "Room " + roomId + " is already Dirty.";
        }
        return updateRoomStatus(roomId, previous, "Housekeeping", "Previous status");
    }

    /** Undo the latest status change (LIFO). */
    public String undoLastAction() {
        if (undoStack.isEmpty()) {
            return "Nothing to undo.";
        }
        UndoRecord record = undoStack.pop();
        Room room = findRoomById(record.getRoomId());
        if (room == null) {
            return "Room not found for undo record.";
        }
        room.rollbackLastStatus();
        redoStack.push(record);
        return "Undo successful: " + record.getRoomId() + " reverted to " + record.getFromStatus() + ".";
    }

    /** Redo an action that was previously undone. */
    public String redoLastAction() {
        if (redoStack.isEmpty()) {
            return "Nothing to redo.";
        }
        UndoRecord record = redoStack.pop();
        Room room = findRoomById(record.getRoomId());
        if (room == null) {
            return "Room not found for redo record.";
        }
        StatusEntry entry = new StatusEntry(record.getToStatus(), LocalDateTime.now(), record.getUpdatedBy(), record.getNote());
        room.recordStatus(entry);
        undoStack.push(record);
        return "Redo successful: " + record.getRoomId() + " set to " + record.getToStatus() + ".";
    }

    private void clearRedoStack() {
        while (!redoStack.isEmpty()) {
            redoStack.pop();
        }
    }

    public void saveState(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < roomCount; i++) {
            Room room = rooms[i];
            builder.append("ROOM|")
                    .append(room.getRoomId()).append("|")
                    .append(room.getRoomType()).append("|")
                    .append(room.getCurrentStatus().name()).append(System.lineSeparator());

            StatusEntry[] history = room.getTaskLog().toArray();
            for (int j = 1; j < history.length; j++) {
                StatusEntry entry = history[j];
                builder.append("ENTRY|")
                        .append(entry.getStatus().name()).append("|")
                        .append(entry.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("|")
                        .append(entry.getUpdatedBy() == null ? "" : entry.getUpdatedBy()).append("|")
                        .append(entry.getNote() == null ? "" : entry.getNote()).append(System.lineSeparator());
            }
        }

        Files.writeString(path, builder.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void loadState(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return;
        }

        clearRooms();
        Room currentRoom = null;

        // Read line-by-line — do not use java.util.List (Files.readAllLines) per assignment Q&A.
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts[0].equals("ROOM")) {
                    if (currentRoom != null) {
                        addRoom(currentRoom);
                    }
                    currentRoom = new Room(parts[1], parts[2], parseStatus(parts[3]));
                } else if (parts[0].equals("ENTRY") && currentRoom != null) {
                    StatusEntry entry = new StatusEntry(
                            parseStatus(parts[1]),
                            LocalDateTime.parse(parts[2], DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            parts[3],
                            parts[4]
                    );
                    currentRoom.recordStatus(entry);
                }
            }
        }

        if (currentRoom != null) {
            addRoom(currentRoom);
        }
    }

    private HousekeepingStatus parseStatus(String text) {
        if (text == null) {
            return HousekeepingStatus.DIRTY;
        }
        String trimmed = text.trim();
        if (trimmed.equalsIgnoreCase("Clean")
                || trimmed.equalsIgnoreCase("Ready For Check-In")
                || trimmed.equalsIgnoreCase("Ready for Check-In")) {
            return HousekeepingStatus.READY_FOR_CHECKIN;
        }
        try {
            return HousekeepingStatus.valueOf(trimmed);
        } catch (IllegalArgumentException ignored) {
            HousekeepingStatus[] values = HousekeepingStatus.values();
            for (int i = 0; i < values.length; i++) {
                if (values[i].toString().equalsIgnoreCase(trimmed)) {
                    return values[i];
                }
            }
            return HousekeepingStatus.DIRTY;
        }
    }

    private void clearRooms() {
        for (int i = 0; i < roomCount; i++) {
            rooms[i] = null;
        }
        roomCount = 0;
    }

    public Room[] getAllRooms() {
        Room[] result = new Room[roomCount];
        for (int i = 0; i < roomCount; i++) {
            result[i] = rooms[i];
        }
        return result;
    }

    /** Linear search for rooms matching all supplied criteria. */
    public Room[] searchRooms(HousekeepingStatus statusFilter, String roomTypeFilter,
            Boolean occupiedFilter) {
        int matchCount = 0;
        for (int i = 0; i < roomCount; i++) {
            if (matchesRoom(rooms[i], statusFilter, roomTypeFilter, occupiedFilter)) {
                matchCount++;
            }
        }
        Room[] matches = new Room[matchCount];
        int index = 0;
        for (int i = 0; i < roomCount; i++) {
            if (matchesRoom(rooms[i], statusFilter, roomTypeFilter, occupiedFilter)) {
                matches[index++] = rooms[i];
            }
        }
        return matches;
    }

    /** Insertion sort used by housekeeping reports. */
    public void insertionSortByTaskCount(Room[] reportRooms) {
        for (int i = 1; i < reportRooms.length; i++) {
            Room key = reportRooms[i];
            int j = i - 1;
            while (j >= 0 && reportRooms[j].getTaskLog().size() < key.getTaskLog().size()) {
                reportRooms[j + 1] = reportRooms[j];
                j--;
            }
            reportRooms[j + 1] = key;
        }
    }

    /** Report 1: status workload filtered by status, room type, and occupancy. */
    public String generateStatusWorkloadReport(HousekeepingStatus statusFilter,
            String roomTypeFilter, Boolean occupiedFilter) {
        Room[] matches = searchRooms(statusFilter, roomTypeFilter, occupiedFilter);
        insertionSortByTaskCount(matches);
        StringBuilder report = new StringBuilder();
        report.append("\n==============================================================\n")
                .append(" HOUSEKEEPING STATUS WORKLOAD REPORT\n")
                .append(" Filters: status=").append(statusFilter == null ? "All" : statusFilter)
                .append(", type=").append(roomTypeFilter == null ? "All" : roomTypeFilter)
                .append(", occupancy=").append(occupiedFilter == null ? "All" : occupiedFilter ? "Occupied" : "Free")
                .append("\n Sorted by task-log entries (highest first)\n")
                .append("==============================================================\n")
                .append(String.format("%-8s %-12s %-24s %-10s %-10s\n",
                        "Room", "Type", "Status", "Occupied", "Tasks"));
        for (int i = 0; i < matches.length; i++) {
            Room room = matches[i];
            report.append(String.format("%-8s %-12s %-24s %-10s %-10d\n",
                    room.getRoomId(), room.getRoomType(), room.getCurrentStatus(),
                    room.isOccupied() ? "Yes" : "No", room.getTaskLog().size()));
        }
        report.append("--------------------------------------------------------------\n")
                .append("Matching rooms: ").append(matches.length).append('\n');
        return report.toString();
    }

    /** Report 2: task history filtered by room type and minimum activity. */
    public String generateTaskHistoryReport(String roomTypeFilter, int minimumTaskEntries) {
        Room[] searched = searchRooms(null, roomTypeFilter, null);
        int matchCount = 0;
        for (int i = 0; i < searched.length; i++) {
            if (searched[i].getTaskLog().size() >= minimumTaskEntries) {
                matchCount++;
            }
        }
        Room[] matches = new Room[matchCount];
        int index = 0;
        for (int i = 0; i < searched.length; i++) {
            if (searched[i].getTaskLog().size() >= minimumTaskEntries) {
                matches[index++] = searched[i];
            }
        }
        insertionSortByTaskCount(matches);
        StringBuilder report = new StringBuilder();
        report.append("\n==============================================================\n")
                .append(" HOUSEKEEPING TASK HISTORY REPORT\n")
                .append(" Filters: type=").append(roomTypeFilter == null ? "All" : roomTypeFilter)
                .append(", minimum task entries=").append(minimumTaskEntries).append('\n')
                .append(" Sorted by task-log entries (highest first)\n")
                .append("==============================================================\n")
                .append(String.format("%-8s %-12s %-24s %-10s %-24s\n",
                        "Room", "Type", "Status", "Tasks", "Last Updated By"));
        for (int i = 0; i < matches.length; i++) {
            Room room = matches[i];
            report.append(String.format("%-8s %-12s %-24s %-10d %-24s\n",
                    room.getRoomId(), room.getRoomType(), room.getCurrentStatus(),
                    room.getTaskLog().size(), room.getLastUpdatedBy()));
        }
        report.append("--------------------------------------------------------------\n")
                .append("Matching rooms: ").append(matches.length).append('\n');
        return report.toString();
    }

    private boolean matchesRoom(Room room, HousekeepingStatus statusFilter, String roomTypeFilter,
            Boolean occupiedFilter) {
        if (statusFilter != null && room.getCurrentStatus() != statusFilter) {
            return false;
        }
        if (roomTypeFilter != null && !roomTypeFilter.equalsIgnoreCase(room.getRoomType())) {
            return false;
        }
        return occupiedFilter == null || room.isOccupied() == occupiedFilter;
    }

    }
