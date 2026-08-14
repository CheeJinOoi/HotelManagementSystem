package boundary;

import entity.Room;
import entity.StatusEntry;

/**
 * Reporter.java
 * BOUNDARY helper: formats housekeeping room lists / details for console output.
 */
public class Reporter {
    public static void printAllRooms(Room[] rooms) {
        ConsoleStyle.section("All rooms");
        ConsoleStyle.tableHeader(String.format("%-8s %-10s %-22s %-12s", "Room", "Type", "Status", "Occupied"));
        if (rooms == null || rooms.length == 0) {
            ConsoleStyle.info("(no rooms)");
            ConsoleStyle.blank();
            return;
        }
        for (int i = 0; i < rooms.length; i++) {
            Room room = rooms[i];
            String occupied = room.isOccupied()
                    ? "Yes (" + room.getAssignedConfirmationNumber() + ")"
                    : "No";
            ConsoleStyle.tableRow(String.format("%-8s %-10s %-22s %-12s",
                    room.getRoomId(),
                    room.getRoomType(),
                    room.getCurrentStatus(),
                    occupied));
        }
        ConsoleStyle.blank();
    }

    public static void printRoomDetails(Room room) {
        if (room == null) {
            ConsoleStyle.error("Room not found.");
            return;
        }

        ConsoleStyle.section("Room details");
        ConsoleStyle.keyValue("Room ID", room.getRoomId());
        ConsoleStyle.keyValue("Room Type", room.getRoomType());
        ConsoleStyle.keyValue("Status", String.valueOf(room.getCurrentStatus()));
        ConsoleStyle.keyValue("Occupied", room.isOccupied()
                ? "Yes (" + room.getAssignedConfirmationNumber() + ")"
                : "No");
        ConsoleStyle.keyValue("Updated By", room.getLastUpdatedBy());
        ConsoleStyle.keyValue("Updated At", String.valueOf(room.getLastUpdatedTime()));
        ConsoleStyle.info("Task Log:");
        StatusEntry[] entries = room.getTaskLog().toArray();
        for (StatusEntry entry : entries) {
            ConsoleStyle.info("  " + entry);
        }
    }

    public static void printMessage(String message) {
        if (message == null) {
            return;
        }
        String lower = message.toLowerCase();
        if (lower.contains("success") || lower.contains("undo successful") || lower.contains("redo successful")) {
            ConsoleStyle.success(message);
        } else if (lower.contains("invalid") || lower.contains("not found") || lower.contains("nothing to")) {
            ConsoleStyle.warn(message);
        } else {
            ConsoleStyle.info(message);
        }
    }
}
