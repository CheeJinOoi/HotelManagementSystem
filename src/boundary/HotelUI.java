package boundary;

import java.util.Scanner;

/**
 * HotelUI.java
 * BOUNDARY: top-level console menu printed by HotelMain.
 *
 * Actors (users) only talk to boundary classes in ECB.
 *
 * @author vinsx, chong
 */
public class HotelUI {

  private Scanner scanner = new Scanner(System.in);

  /**
   * Show the hotel menu and return the user's choice.
   * Invalid input returns -1 so HotelMain can show an error message.
   */
  public int getMenuChoice() {
    ConsoleStyle.header("TARUMT RESORTS", "Hotel Management System");
    ConsoleStyle.menuItem(1, "Open Operations GUI");
    ConsoleStyle.menuItem(2, "Walk-In & Standard Booking (console)");
    ConsoleStyle.menuItem(3, "Housekeeping (console)");
    ConsoleStyle.menuItem(4, "VIP Room Allocation (console)");
    ConsoleStyle.menuItem(5, "Front Desk (console)");
    ConsoleStyle.menuExit(0, "Quit");
    ConsoleStyle.prompt("Enter choice: ");
    try {
      int choice = Integer.parseInt(scanner.nextLine().trim());
      ConsoleStyle.blank();
      return choice;
    } catch (NumberFormatException ex) {
      ConsoleStyle.blank();
      return -1;
    }
  }
}
