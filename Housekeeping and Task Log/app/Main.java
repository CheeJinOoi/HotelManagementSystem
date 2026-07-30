package app;

import boundary.HousekeepingGUI;
import control.HousekeepingController;
import entity.HousekeepingStatus;
import entity.Room;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Path stateFile = Path.of("data", "housekeeping_state.txt");
        HousekeepingController controller = new HousekeepingController(20);

        try {
            if (Files.exists(stateFile)) {
                controller.loadState(stateFile.toString());
                System.out.println("Loaded saved housekeeping state from " + stateFile);
            } else {
                controller.addRoom(new Room("101", "Standard", HousekeepingStatus.DIRTY));
                controller.addRoom(new Room("102", "Suite", HousekeepingStatus.DIRTY));
                controller.addRoom(new Room("103", "Deluxe", HousekeepingStatus.CLEANING_IN_PROGRESS));
            }
        } catch (Exception ex) {
            System.out.println("Could not load saved state. Starting with default rooms. " + ex.getMessage());
            controller.addRoom(new Room("101", "Standard", HousekeepingStatus.DIRTY));
            controller.addRoom(new Room("102", "Suite", HousekeepingStatus.DIRTY));
            controller.addRoom(new Room("103", "Deluxe", HousekeepingStatus.CLEANING_IN_PROGRESS));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                controller.saveState(stateFile.toString());
            } catch (Exception ex) {
                System.err.println("Could not save housekeeping state: " + ex.getMessage());
            }
        }));

        SwingUtilities.invokeLater(() -> {
            HousekeepingGUI gui = new HousekeepingGUI(controller);
            gui.setVisible(true);
        });
    }
}
