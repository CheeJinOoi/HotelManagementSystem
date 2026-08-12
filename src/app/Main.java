package app;

import boundary.HotelGUI;
import control.HotelBootstrap;
import control.HousekeepingController;
import control.WalkInBookingControl;

/**
 * Main.java
 * Shortcut launcher: skips the console hotel menu and opens the combined GUI directly.
 *
 * Use HotelMain if you also want console options.
 * Use this class (or run.bat -> app.HotelMain) for the normal team demo flow.
 */
public class Main {
  public static void main(String[] args) {
    // Build shared rooms, then Walk-In control that uses those same rooms
    HousekeepingController housekeeping = HotelBootstrap.create();
    WalkInBookingControl walkIn = new WalkInBookingControl(housekeeping);

    // Persist housekeeping state on JVM exit
    Runtime.getRuntime().addShutdownHook(new Thread(() -> HotelBootstrap.save(housekeeping)));

    // Open the tabbed GUI on the Swing event thread
    HotelGUI.open(walkIn, housekeeping);
  }
}
