package control;

import entity.HousekeepingStatus;
import entity.Room;
import entity.Stack;
import entity.StatusEntry;
import entity.UndoRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HousekeepingController {
    private final Room[] rooms;
    private int roomCount;
    private final Stack<UndoRecord> undoStack;
    private final Stack<UndoRecord> redoStack;

    public HousekeepingController(int capacity) {
        this.rooms = new Room[capacity];
        this.roomCount = 0;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    public boolean addRoom(Room room) {
        if (room == null || roomCount >= rooms.length) {
            return false;
        }
        rooms[roomCount++] = room;
        return true;
    }

    public Room findRoomById(String roomId) {
        for (int i = 0; i < roomCount; i++) {
            if (rooms[i].getRoomId().equalsIgnoreCase(roomId)) {
                return rooms[i];
            }
        }
        return null;
    }

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
                    .append(room.getCurrentStatus()).append(System.lineSeparator());

            StatusEntry[] history = room.getTaskLog().toArray();
            for (int j = 1; j < history.length; j++) {
                StatusEntry entry = history[j];
                builder.append("ENTRY|")
                        .append(entry.getStatus()).append("|")
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
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        Room currentRoom = null;

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            String[] parts = line.split("\\|", -1);
            if (parts[0].equals("ROOM")) {
                if (currentRoom != null) {
                    addRoom(currentRoom);
                }
                currentRoom = new Room(parts[1], parts[2], HousekeepingStatus.valueOf(parts[3]));
            } else if (parts[0].equals("ENTRY") && currentRoom != null) {
                StatusEntry entry = new StatusEntry(
                        HousekeepingStatus.valueOf(parts[1]),
                        LocalDateTime.parse(parts[2], DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        parts[3],
                        parts[4]
                );
                currentRoom.recordStatus(entry);
            }
        }

        if (currentRoom != null) {
            addRoom(currentRoom);
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
}
