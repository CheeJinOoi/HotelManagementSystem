package control;

import entity.HousekeepingStatus;
import entity.Room;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * HotelBootstrap.java
 * Starts the shared hotel room inventory used by BOTH modules.
 *
 * Walk-In needs rooms that are Ready for Check-In and free.
 * Housekeeping updates cleaning status (Dirty -> ... -> Ready for Check-In).
 * Keeping one HousekeepingController instance avoids two conflicting room lists.
 *
 * @author vinsx
 */
public class HotelBootstrap {

  /** Text file that stores room statuses between runs. */
  public static final Path STATE_FILE = Path.of("data", "housekeeping_state.txt");

  /**
   * Create housekeeping control, load saved rooms if any, then ensure default rooms exist.
   */
  public static HousekeepingController create() {
    HousekeepingController controller = new HousekeepingController(20);
    try {
      if (Files.exists(STATE_FILE)) {
        controller.loadState(STATE_FILE.toString());
      }
    } catch (Exception ex) {
      System.out.println("Could not load housekeeping state. " + ex.getMessage());
    }
    ensureDefaultRooms(controller);
    return controller;
  }

  /** Save current room cleaning statuses to disk. */
  public static void save(HousekeepingController controller) {
    try {
      controller.saveState(STATE_FILE.toString());
    } catch (Exception ex) {
      System.err.println("Could not save housekeeping state: " + ex.getMessage());
    }
  }

  /**
   * Add the standard demo rooms if they are missing after load.
   * Some start Ready so Walk-In can assign immediately for demos.
   */
  private static void ensureDefaultRooms(HousekeepingController controller) {
    addIfMissing(controller, "101", "Standard", HousekeepingStatus.READY_FOR_CHECKIN);
    addIfMissing(controller, "102", "Standard", HousekeepingStatus.DIRTY);
    addIfMissing(controller, "103", "Standard", HousekeepingStatus.CLEANING_IN_PROGRESS);
    addIfMissing(controller, "201", "Deluxe", HousekeepingStatus.READY_FOR_CHECKIN);
    addIfMissing(controller, "202", "Deluxe", HousekeepingStatus.INSPECTED);
    addIfMissing(controller, "301", "Suite", HousekeepingStatus.READY_FOR_CHECKIN);
  }

  private static void addIfMissing(HousekeepingController controller, String roomId,
      String roomType, HousekeepingStatus status) {
    if (controller.findRoomById(roomId) == null) {
      controller.addRoom(new Room(roomId, roomType, status));
    }
  }
}
