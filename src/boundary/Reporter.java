package boundary;

import entity.Room;
import entity.StatusEntry;

/**
 * Reporter.java
 * BOUNDARY helper: formats housekeeping room lists / details for console output.
 */
public class Reporter {
    public static void printAllRooms(Room[] rooms) {
        System.out.println("\nAll rooms:");
        System.out.println(String.format("%-8s %-10s %-22s %-12s", "Room", "Type", "Status", "Occupied"));
        System.out.println("------------------------------------------------------");
        if (rooms == null || rooms.length == 0) {
            System.out.println("(no rooms)");
            System.out.println();
            return;
        }
        for (int i = 0; i < rooms.length; i++) {
            Room room = rooms[i];
            String occupied = room.isOccupied()
                    ? "Yes (" + room.getAssignedConfirmationNumber() + ")"
                    : "No";
            System.out.println(String.format("%-8s %-10s %-22s %-12s",
                    room.getRoomId(),
                    room.getRoomType(),
                    room.getCurrentStatus(),
                    occupied));
        }
        System.out.println();
    }

    public static void printRoomDetails(Room room) {
        if (room == null) {
            System.out.println("Room not found.");
            return;
        }

        System.out.println("Room ID: " + room.getRoomId());
        System.out.println("Room Type: " + room.getRoomType());
        System.out.println("Current Status: " + room.getCurrentStatus());
        System.out.println("Occupied: " + (room.isOccupied() ? "Yes (" + room.getAssignedConfirmationNumber() + ")" : "No"));
        System.out.println("Last Updated By: " + room.getLastUpdatedBy());
        System.out.println("Last Updated Time: " + room.getLastUpdatedTime());
        System.out.println("Task Log:");
        StatusEntry[] entries = room.getTaskLog().toArray();
        for (StatusEntry entry : entries) {
            System.out.println("  " + entry);
        }
    }

    public static void printMessage(String message) {
        System.out.println(message);
    }
}
