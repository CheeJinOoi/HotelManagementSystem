package app;

import boundary.ConsoleUI;
import boundary.HotelGUI;
import boundary.HotelUI;
import control.HotelBootstrap;
import control.HousekeepingController;
import control.WalkInBookingControl;
import utility.MessageUI;

/**
 * HotelMain.java
 * Main program entry for the combined TARUMT Resorts system.
 *
 * Flow:
 * 1. Create shared rooms (HotelBootstrap) used by both modules
 * 2. Create Walk-In booking control (uses circular queue + shared rooms)
 * 3. Show a simple console menu:
 *    1 = Combined GUI (Walk-In tab + Housekeeping tab)
 *    2 = Walk-In console only
 *    3 = Housekeeping console only
 *    0 = Quit
 *
 * ECB: this class is the application launcher; it talks to boundary + control only.
 *
 * @author vinsx
 */
public class HotelMain {

  public static void main(String[] args) {
    // Shared room list so Walk-In assign/check-out and Housekeeping cleaning stay in sync
    HousekeepingController housekeeping = HotelBootstrap.create();
    WalkInBookingControl walkIn = new WalkInBookingControl(housekeeping);
    HotelUI hotelUI = new HotelUI();

    // Save housekeeping room statuses if the program closes unexpectedly
    Runtime.getRuntime().addShutdownHook(new Thread(() -> HotelBootstrap.save(housekeeping)));

    int choice = 0;
    do {
      choice = hotelUI.getMenuChoice();
      switch (choice) {
        case 0:
          // Normal exit: save then leave
          HotelBootstrap.save(housekeeping);
          MessageUI.displayHotelExitMessage();
          break;
        case 1:
          // One window with two tabs (Walk-In and Housekeeping)
          HotelGUI.open(walkIn, housekeeping);
          System.out.println("GUI opened. Close the window when finished.");
          break;
        case 2:
          // Text menu for Walk-In / standard booking only
          walkIn.runWalkInBooking();
          break;
        case 3:
          // Text menu for housekeeping status / undo / redo
          new ConsoleUI(housekeeping).start();
          break;
        default:
          MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);
  }
}
