package boundary;

import entity.Room;
import entity.StatusEntry;

public class Reporter {
    public static void printRoomDetails(Room room) {
        if (room == null) {
            System.out.println("Room not found.");
            return;
        }

        System.out.println("Room ID: " + room.getRoomId());
        System.out.println("Room Type: " + room.getRoomType());
        System.out.println("Current Status: " + room.getCurrentStatus());
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
