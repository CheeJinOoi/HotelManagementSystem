package app;

import boundary.HotelGUI;
import control.HotelBootstrap;
import control.HousekeepingController;
import control.VIPRoomAllocationControl; 
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

    // ===== VIP Module =====
    VIPRoomAllocationControl vipControl = new VIPRoomAllocationControl();
    // share room to vip
    vipControl.setRooms(housekeeping.getAllRoomsList());

    // Persist housekeeping state on JVM exit
    Runtime.getRuntime().addShutdownHook(new Thread(() -> HotelBootstrap.save(housekeeping)));

    // Open the tabbed GUI on the Swing event thread (Walk-In + Housekeeping + VIP)
    HotelGUI.open(walkIn, housekeeping, vipControl);  // ← 添加 vipControl
  }
}