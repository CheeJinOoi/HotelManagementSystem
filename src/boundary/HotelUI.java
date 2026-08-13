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
    System.out.println("\n==============================================");
    System.out.println(" TARUMT Resorts - Hotel Management System");
    System.out.println("==============================================");
    System.out.println("1. GUI (Walk-In + Housekeeping + VIP)");
    System.out.println("2. Walk-In (console)");
    System.out.println("3. Housekeeping (console)");
    System.out.println("4. VIP Room Allocation (console)");
    System.out.println("0. Quit");
    System.out.print("Enter choice: ");
    try {
      int choice = Integer.parseInt(scanner.nextLine().trim());
      System.out.println();
      return choice;
    } catch (NumberFormatException ex) {
      System.out.println();
      return -1;
    }
  }
}