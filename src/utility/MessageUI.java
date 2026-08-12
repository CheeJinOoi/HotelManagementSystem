package utility;

/**
 * MessageUI.java
 * UTILITY class: shared console messages only.
 * Contains static methods — call as MessageUI.displayExitMessage().
 *
 * @author vinsx
 */
public class MessageUI {

  public static void displayInvalidChoiceMessage() {
    System.out.println("\nInvalid choice");
  }

  /** Shown when leaving the Walk-In console submenu. */
  public static void displayExitMessage() {
    System.out.println("\nReturning to hotel menu.");
  }

  /** Shown when quitting the whole hotel program. */
  public static void displayHotelExitMessage() {
    System.out.println("\nExiting TARUMT Resorts Hotel Management System");
  }

  public static void displayEmptyQueueMessage() {
    System.out.println("\nThe queue is empty.");
  }
}
