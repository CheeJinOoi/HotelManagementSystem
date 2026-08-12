package tests;

import control.HousekeepingController;
import entity.HousekeepingStatus;
import entity.Room;
import java.nio.file.Files;
import java.nio.file.Path;

public class HousekeepingControllerPersistenceTest {
    public static void main(String[] args) throws Exception {
        Path tempFile = Files.createTempFile("housekeeping-test-", ".txt");
        HousekeepingController controller = new HousekeepingController(5);
        controller.addRoom(new Room("201", "Deluxe", HousekeepingStatus.DIRTY));

        controller.saveState(tempFile.toString());
        HousekeepingController reloaded = new HousekeepingController(5);
        reloaded.loadState(tempFile.toString());

        if (reloaded.findRoomById("201") == null) {
            throw new AssertionError("Room should be loaded from file");
        }

        if (reloaded.findRoomById("201").getCurrentStatus() != HousekeepingStatus.DIRTY) {
            throw new AssertionError("Room status should be preserved");
        }

        Files.deleteIfExists(tempFile);
        System.out.println("Persistence test passed");
    }
}
