package utility;

import boundary.ConsoleStyle;

/**
 * MessageUI.java
 * UTILITY class: shared console messages only.
 * Contains static methods — call as MessageUI.displayExitMessage().
 *
 * @author vinsx
 */
public class MessageUI {

  public static void displayInvalidChoiceMessage() {
    ConsoleStyle.error("Invalid choice.");
    ConsoleStyle.pause();
  }

  /** Shown when leaving the Walk-In console submenu. */
  public static void displayExitMessage() {
    ConsoleStyle.info("Returning to hotel menu.");
  }

  /** Shown when quitting the whole hotel program. */
  public static void displayHotelExitMessage() {
    ConsoleStyle.header("GOODBYE", "TARUMT Resorts - session closed");
  }

  public static void displayEmptyQueueMessage() {
    ConsoleStyle.warn("The queue is empty.");
  }
}
