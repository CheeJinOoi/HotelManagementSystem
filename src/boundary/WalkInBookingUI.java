package boundary;

import entity.BookingType;
import entity.Guest;
import entity.Reservation;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * WalkInBookingUI.java
 * BOUNDARY (console): menus and input prompts for Walk-In / standard booking.
 * No business logic here — control class decides what to do with the input.
 *
 * @author vinsx
 */
public class WalkInBookingUI {

  private Scanner scanner = new Scanner(System.in);

  public int getMenuChoice() {
    ConsoleStyle.header("WALK-IN & BOOKING", "Register · assign · rooms · check-out · reports");
    ConsoleStyle.menuItem(1, "Register walk-in guest");
    ConsoleStyle.menuItem(2, "Create standard booking");
    ConsoleStyle.menuItem(3, "Assign next guest to a room");
    ConsoleStyle.menuItem(4, "Cancel waiting reservation");
    ConsoleStyle.menuItem(5, "List pending queue");
    ConsoleStyle.menuItem(6, "Check-out guest");
    ConsoleStyle.menuItem(7, "Generate reports");
    ConsoleStyle.menuItem(8, "View all room status");
    ConsoleStyle.menuExit(0, "Return to hotel menu");
    ConsoleStyle.prompt("Enter choice: ");
    return readIntAllowInvalid();
  }

  public int getReportMenuChoice() {
    ConsoleStyle.header("REPORTS", "Walk-In & Standard Booking");
    ConsoleStyle.menuItem(1, "Walk-in vs standard arrivals by date");
    ConsoleStyle.menuItem(2, "Unassigned demand vs available rooms");
    ConsoleStyle.menuExit(0, "Back");
    ConsoleStyle.prompt("Enter choice: ");
    return readIntAllowInvalid();
  }

  public Guest inputGuestDetails() {
    ConsoleStyle.clear();
    ConsoleStyle.section("Guest details");
    String name = inputNonEmpty("Enter guest name: ");
    String identityNumber = inputNonEmpty("Enter IC / passport: ");
    String phone = inputNonEmpty("Enter phone number: ");
    ConsoleStyle.blank();
    return new Guest(name, identityNumber, phone);
  }

  public String inputRoomType() {
    ConsoleStyle.section("Room type");
    ConsoleStyle.menuItem(1, "Standard");
    ConsoleStyle.menuItem(2, "Deluxe");
    ConsoleStyle.menuItem(3, "Suite");
    int choice = readInt("Enter choice: ");
    switch (choice) {
      case 1:
        return "Standard";
      case 2:
        return "Deluxe";
      case 3:
        return "Suite";
      default:
        return null;
    }
  }

  public String inputRoomTypeFilter() {
    ConsoleStyle.section("Filter by room type");
    ConsoleStyle.menuItem(0, "All types");
    ConsoleStyle.menuItem(1, "Standard");
    ConsoleStyle.menuItem(2, "Deluxe");
    ConsoleStyle.menuItem(3, "Suite");
    int choice = readInt("Enter choice: ");
    switch (choice) {
      case 1:
        return "Standard";
      case 2:
        return "Deluxe";
      case 3:
        return "Suite";
      default:
        return null;
    }
  }

  public BookingType inputBookingTypeFilter() {
    ConsoleStyle.section("Filter by booking type");
    ConsoleStyle.menuItem(0, "All");
    ConsoleStyle.menuItem(1, "Walk-In");
    ConsoleStyle.menuItem(2, "Standard");
    int choice = readInt("Enter choice: ");
    switch (choice) {
      case 1:
        return BookingType.WALK_IN;
      case 2:
        return BookingType.STANDARD;
      default:
        return null;
    }
  }

  public int inputNights() {
    int nights = readInt("Enter number of nights: ");
    return nights;
  }

  public LocalDate inputCheckInDate() {
    return inputDate("Enter check-in date");
  }

  public LocalDate inputCheckOutDate() {
    return inputDate("Enter check-out date");
  }

  public LocalDate inputStartDate() {
    return inputDate("Enter start date");
  }

  public LocalDate inputEndDate() {
    return inputDate("Enter end date");
  }

  public String inputConfirmationNumber() {
    ConsoleStyle.clear();
    return inputNonEmpty("Enter 8-digit confirmation number: ");
  }

  public void listPendingReservations(String outputStr) {
    ConsoleStyle.clear();
    ConsoleStyle.section("Pending queue (front = next to assign)");
    ConsoleStyle.tableHeader(getReservationHeader());
    ConsoleStyle.tableRow(outputStr.trim());
    ConsoleStyle.pause(scanner);
  }

  public void displayReservation(Reservation reservation) {
    ConsoleStyle.clear();
    ConsoleStyle.section("Reservation details");
    ConsoleStyle.tableHeader(getDetailHeader());
    ConsoleStyle.tableRow(reservation.toString());
    if (reservation.getGuest() != null) {
      ConsoleStyle.keyValue("IC/Passport", reservation.getGuest().getIdentityNumber());
      ConsoleStyle.keyValue("Phone", reservation.getGuest().getPhone());
    }
    ConsoleStyle.pause(scanner);
  }

  public void displayReport(String outputStr) {
    ConsoleStyle.clear();
    ConsoleStyle.section("Report");
    ConsoleStyle.info(outputStr);
    ConsoleStyle.pause(scanner);
  }

  public void displayMessage(String message) {
    ConsoleStyle.clear();
    ConsoleStyle.section("Result");
    ConsoleStyle.info(message);
    ConsoleStyle.pause(scanner);
  }

  public String getReservationHeader() {
    return String.format("%-4s %-10s %-12s %-20s %-10s %-12s %-12s %-10s %-12s",
        "Pos", "Conf No", "Type", "Guest", "Room Type", "Check-In", "Check-Out", "Room", "Status");
  }

  public String getDetailHeader() {
    return String.format("%-10s %-12s %-20s %-10s %-12s %-12s %-10s %-12s",
        "Conf No", "Type", "Guest", "Room Type", "Check-In", "Check-Out", "Room", "Status");
  }

  private static final DateTimeFormatter[] DATE_FORMATS = {
      DateTimeFormatter.ISO_LOCAL_DATE,
      DateTimeFormatter.ofPattern("d/M/uuuu"),
      DateTimeFormatter.ofPattern("d-M-uuuu"),
      DateTimeFormatter.ofPattern("d.M.uuuu"),
      DateTimeFormatter.ofPattern("uuuu/M/d"),
      DateTimeFormatter.ofPattern("d/M/uu"),
      DateTimeFormatter.ofPattern("d-M-uu")
  };

  private LocalDate inputDate(String prompt) {
    LocalDate today = LocalDate.now();
    String example = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    while (true) {
      ConsoleStyle.prompt(prompt + " (e.g. " + example + ", Enter = today): ");
      String text = scanner.nextLine().trim();
      if (text.isEmpty() || text.equalsIgnoreCase("today")) {
        return today;
      }
      LocalDate parsed = parseFlexibleDate(text);
      if (parsed != null) {
        return parsed;
      }
      ConsoleStyle.error("Invalid date. Try " + example + " or " + today + ".");
    }
  }

  private LocalDate parseFlexibleDate(String text) {
    for (int i = 0; i < DATE_FORMATS.length; i++) {
      try {
        return LocalDate.parse(text, DATE_FORMATS[i]);
      } catch (DateTimeParseException ex) {
        // try the next format
      }
    }
    return null;
  }

  private String inputNonEmpty(String prompt) {
    while (true) {
      ConsoleStyle.prompt(prompt);
      String text = scanner.nextLine().trim();
      if (!text.isEmpty()) {
        return text;
      }
      ConsoleStyle.warn("Value cannot be empty.");
    }
  }

  private int readInt(String prompt) {
    while (true) {
      ConsoleStyle.prompt(prompt);
      try {
        return Integer.parseInt(scanner.nextLine().trim());
      } catch (NumberFormatException ex) {
        ConsoleStyle.error("Invalid number. Try again.");
      }
    }
  }

  private int readIntAllowInvalid() {
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
