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
    System.out.println("\n==============================================");
    System.out.println(" TARUMT Resorts - Walk-In & Standard Booking");
    System.out.println("==============================================");
    System.out.println("1. Register walk-in guest");
    System.out.println("2. Create standard booking");
    System.out.println("3. Assign next guest to a room");
    System.out.println("4. Cancel waiting reservation");
    System.out.println("5. List pending queue");
    System.out.println("6. Check-out guest");
    System.out.println("7. Generate reports");
    System.out.println("0. Return to hotel menu");
    System.out.print("Enter choice: ");
    return readIntAllowInvalid();
  }

  public int getReportMenuChoice() {
    System.out.println("\nREPORT MENU");
    System.out.println("1. Walk-in vs standard arrivals by date");
    System.out.println("2. Unassigned demand vs available rooms");
    System.out.println("0. Back");
    System.out.print("Enter choice: ");
    return readIntAllowInvalid();
  }

  public Guest inputGuestDetails() {
    String name = inputNonEmpty("Enter guest name: ");
    String identityNumber = inputNonEmpty("Enter IC / passport: ");
    String phone = inputNonEmpty("Enter phone number: ");
    System.out.println();
    return new Guest(name, identityNumber, phone);
  }

  public String inputRoomType() {
    System.out.println("Room type:");
    System.out.println("1. Standard");
    System.out.println("2. Deluxe");
    System.out.println("3. Suite");
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
    System.out.println("Filter by room type:");
    System.out.println("0. All types");
    System.out.println("1. Standard");
    System.out.println("2. Deluxe");
    System.out.println("3. Suite");
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
    System.out.println("Filter by booking type:");
    System.out.println("0. All");
    System.out.println("1. Walk-In");
    System.out.println("2. Standard");
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
    return inputNonEmpty("Enter 8-digit confirmation number: ");
  }

  public void listPendingReservations(String outputStr) {
    System.out.println("\nPENDING QUEUE (front = next to assign)");
    System.out.println(getReservationHeader());
    System.out.println(outputStr);
  }

  public void displayReservation(Reservation reservation) {
    System.out.println("\nReservation details");
    System.out.println(getDetailHeader());
    System.out.println(reservation);
    if (reservation.getGuest() != null) {
      System.out.println("Guest IC/Passport: " + reservation.getGuest().getIdentityNumber());
      System.out.println("Guest phone      : " + reservation.getGuest().getPhone());
    }
  }

  public void displayReport(String outputStr) {
    System.out.println(outputStr);
  }

  public void displayMessage(String message) {
    System.out.println("\n" + message);
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
      System.out.print(prompt + " (e.g. " + example + ", Enter = today): ");
      String text = scanner.nextLine().trim();
      if (text.isEmpty() || text.equalsIgnoreCase("today")) {
        return today;
      }
      LocalDate parsed = parseFlexibleDate(text);
      if (parsed != null) {
        return parsed;
      }
      System.out.println("Invalid date. Try " + example + " or " + today + ".");
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
      System.out.print(prompt);
      String text = scanner.nextLine().trim();
      if (!text.isEmpty()) {
        return text;
      }
      System.out.println("Value cannot be empty.");
    }
  }

  private int readInt(String prompt) {
    while (true) {
      System.out.print(prompt);
      try {
        return Integer.parseInt(scanner.nextLine().trim());
      } catch (NumberFormatException ex) {
        System.out.println("Invalid number. Try again.");
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
