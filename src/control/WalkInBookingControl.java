package control;

import adt.CircularQueue;
import adt.QueueInterface;
import boundary.WalkInBookingUI;
import dao.BookingStore;
import dao.ReservationDAO;
import entity.BookingType;
import entity.Guest;
import entity.Reservation;
import entity.ReservationStatus;
import entity.Room;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import utility.MessageUI;
/**
 * WalkInBookingControl.java
 * CONTROL layer for Walk-In Registrations & Standard Booking (Linear ADT module).
 *
 * Responsibilities:
 * - Keep waiting guests in pendingQueue (FIFO circular queue)
 * - Keep all bookings in historyQueue (for reports / lookup)
 * - Register walk-ins and standard bookings
 * - Assign the front guest to a Ready + free room from Housekeeping
 * - Cancel waiting bookings, check out guests, generate 2 reports
 *
 * Uses QueueInterface (not java.util.Queue) as required by the assignment.
 *
 * @author vinsx
 */
public class WalkInBookingControl {

  /** Guests still waiting for a room (front = next to assign). */
  private QueueInterface<Reservation> pendingQueue = new CircularQueue<>();
  /** Every reservation created (waiting, assigned, cancelled, checked out). */
  private QueueInterface<Reservation> historyQueue = new CircularQueue<>();
  /** Shared rooms from housekeeping (same object as the Housekeeping module). */
  private HousekeepingController housekeeping;
  /** Saves / loads both queues to data/reservations.dat. */
  private ReservationDAO reservationDAO = new ReservationDAO();
  /** Console boundary used by runWalkInBooking(). */
  private WalkInBookingUI bookingUI = new WalkInBookingUI();

  public WalkInBookingControl() {
    this(HotelBootstrap.create());
  }

  public WalkInBookingControl(HousekeepingController housekeeping) {
    this.housekeeping = housekeeping;
    BookingStore store = reservationDAO.retrieveFromFile();
    if (store != null && store.getPendingQueue() != null && store.getHistoryQueue() != null) {
      pendingQueue = store.getPendingQueue();
      historyQueue = store.getHistoryQueue();
      restoreRoomOccupancy();
    }
    seedSampleDataIfEmpty();
  }

  public HousekeepingController getHousekeepingController() {
    return housekeeping;
  }

  /**
   * Console menu loop for the Walk-In module.
   * Choice 0 returns to the hotel menu (does not exit the JVM).
   */
  public void runWalkInBooking() {
    int choice = 0;
    do {
      choice = bookingUI.getMenuChoice();
      switch (choice) {
        case 0:
          MessageUI.displayExitMessage();
          break;
        case 1:
          registerWalkIn();
          break;
        case 2:
          createStandardBooking();
          break;
        case 3:
          assignNextGuest();
          break;
        case 4:
          cancelWaitingReservation();
          break;
        case 5:
          bookingUI.listPendingReservations(getAllPending());
          break;
        case 6:
          checkOutGuest();
          break;
        case 7:
          runReports();
          break;
        case 8:
          bookingUI.displayReport(formatRoomStatusBoard());
          break;
        default:
          MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);
  }

  /**
   * Console helper: collect walk-in details from UI, then register.
   */
  public void registerWalkIn() {
    Guest guest = bookingUI.inputGuestDetails();
    String roomType = bookingUI.inputRoomType();
    int nights = bookingUI.inputNights();
    bookingUI.displayMessage(registerWalkIn(guest, roomType, nights));
  }

  /**
   * Register a same-day walk-in guest into the pending FIFO queue.
   * Used by both console UI and GUI.
   */
  public String registerWalkIn(Guest guest, String roomType, int nights) {
    if (guest == null || isBlank(guest.getName()) || isBlank(guest.getIdentityNumber()) || isBlank(guest.getPhone())) {
      return "Guest name, IC/passport and phone are required.";
    }
    if (roomType == null) {
      return "Invalid room type.";
    }
    if (nights < 1) {
      return "Number of nights must be at least 1.";
    }

    LocalDate checkIn = LocalDate.now();
    LocalDate checkOut = checkIn.plusDays(nights);
    if (!hasRoomCapacity(roomType, checkIn, checkOut)) {
      return "No " + roomType + " capacity left for those dates.";
    }

    Reservation reservation = createReservation(guest, roomType, checkIn, checkOut, BookingType.WALK_IN);
    if (pendingQueue.contains(reservation) || historyQueue.contains(reservation)) {
      return "Duplicate confirmation number. Registration aborted.";
    }

    pendingQueue.enqueue(reservation);
    historyQueue.enqueue(reservation);
    saveReservations();
    return "Walk-in registered. Confirmation: " + reservation.getConfirmationNumber()
        + "\nGuest is at position " + pendingQueue.getNumberOfEntries() + " in the pending queue.";
  }

  public void createStandardBooking() {
    Guest guest = bookingUI.inputGuestDetails();
    String roomType = bookingUI.inputRoomType();
    LocalDate checkIn = bookingUI.inputCheckInDate();
    LocalDate checkOut = bookingUI.inputCheckOutDate();
    bookingUI.displayMessage(createStandardBooking(guest, roomType, checkIn, checkOut));
  }

  /**
   * Create a future-dated standard booking and enqueue it chronologically.
   */
  public String createStandardBooking(Guest guest, String roomType, LocalDate checkIn, LocalDate checkOut) {
    if (guest == null || isBlank(guest.getName()) || isBlank(guest.getIdentityNumber()) || isBlank(guest.getPhone())) {
      return "Guest name, IC/passport and phone are required.";
    }
    if (roomType == null) {
      return "Invalid room type.";
    }
    if (checkIn == null || checkOut == null) {
      return "Check-in and check-out dates are required.";
    }
    if (!checkOut.isAfter(checkIn)) {
      return "Check-out date must be after check-in date.";
    }
    if (checkIn.isBefore(LocalDate.now())) {
      return "Check-in date cannot be in the past.";
    }
    if (!hasRoomCapacity(roomType, checkIn, checkOut)) {
      return "No " + roomType + " capacity left for those dates.";
    }

    Reservation reservation = createReservation(guest, roomType, checkIn, checkOut, BookingType.STANDARD);
    if (historyQueue.contains(reservation)) {
      return "Duplicate confirmation number. Booking aborted.";
    }

    pendingQueue.enqueue(reservation);
    historyQueue.enqueue(reservation);
    saveReservations();
    return "Standard booking created. Confirmation: " + reservation.getConfirmationNumber()
        + "\nReservation is at position " + pendingQueue.getNumberOfEntries() + " in the pending queue.";
  }

  public void assignNextGuest() {
    bookingUI.displayMessage(assignNextGuestToRoom());
  }

  /**
   * Serve the front of the pending queue (FIFO).
   * Needs a free room of the requested type with housekeeping status Clean.
   */
  public String assignNextGuestToRoom() {
    if (pendingQueue.isEmpty()) {
      return "The queue is empty.";
    }

    syncRoomOccupancy();

    Reservation front = pendingQueue.getFront();
    Room room = findAvailableRoom(front.getRoomType());
    if (room == null) {
      return buildNoRoomMessage(front);
    }

    pendingQueue.dequeue();
    front.setAssignedRoomId(room.getRoomId());
    room.occupy(front.getConfirmationNumber());
    if (!front.getCheckInDate().isAfter(LocalDate.now())) {
      front.setStatus(ReservationStatus.CHECKED_IN);
    } else {
      front.setStatus(ReservationStatus.ASSIGNED);
    }
    saveReservations();
    HotelBootstrap.save(housekeeping);
    return "Assigned room " + room.getRoomId() + " to confirmation "
        + front.getConfirmationNumber() + " (" + front.getStatus() + ").";
  }

  private String buildNoRoomMessage(Reservation front) {
    StringBuilder message = new StringBuilder();
    message.append("Cannot assign yet.\n");
    message.append("Front of queue: ").append(front.getGuest().getName())
        .append(" needs a ").append(front.getRoomType()).append(" room.\n\n");
    message.append("Ready and free rooms now:\n");
    Room[] allRooms = housekeeping.getAllRooms();
    boolean foundReady = false;
    for (int i = 0; i < allRooms.length; i++) {
      Room current = allRooms[i];
      if (current.isReadyForAssignment()) {
        message.append("- ").append(current.getRoomId())
            .append(" (").append(current.getRoomType()).append(")\n");
        foundReady = true;
      }
    }
    if (!foundReady) {
      message.append("- none\n");
    }
    message.append("\nReady but still occupied:\n");
    boolean foundOccupied = false;
    for (int i = 0; i < allRooms.length; i++) {
      Room current = allRooms[i];
      if (current.getCurrentStatus() == entity.HousekeepingStatus.READY_FOR_CHECKIN
          && current.isOccupied()) {
        message.append("- ").append(current.getRoomId())
            .append(" (").append(current.getRoomType()).append(") guest ")
            .append(current.getAssignedConfirmationNumber()).append("\n");
        foundOccupied = true;
      }
    }
    if (!foundOccupied) {
      message.append("- none\n");
    }
        message.append("\nMake a FREE ").append(front.getRoomType())
        .append(" room Clean in Housekeeping,")
        .append(" or check out the guest blocking that room first.");
    return message.toString();
  }

  public void cancelWaitingReservation() {
    bookingUI.displayMessage(cancelWaitingReservation(bookingUI.inputConfirmationNumber()));
  }

  /**
   * Remove a waiting reservation even if it is not at the front (uses queue remove by position).
   */
  public String cancelWaitingReservation(String confirmationNumber) {
    if (isBlank(confirmationNumber)) {
      return "Confirmation number is required.";
    }
    Reservation key = new Reservation(confirmationNumber.trim());
    if (!pendingQueue.contains(key)) {
      return "No waiting reservation with that confirmation number.";
    }

    int position = pendingQueue.indexOf(key);
    Reservation removed = pendingQueue.remove(position);
    if (removed == null) {
      return "Unable to remove reservation from the queue.";
    }
    removed.setStatus(ReservationStatus.CANCELLED);
    saveReservations();
    return "Cancelled confirmation " + removed.getConfirmationNumber()
        + " at pending position " + position + ".";
  }

  public Reservation findReservation(String confirmationNumber) {
    if (isBlank(confirmationNumber)) {
      return null;
    }
    return findInQueue(historyQueue, confirmationNumber.trim());
  }

  public void checkOutGuest() {
    bookingUI.displayMessage(checkOutGuest(bookingUI.inputCheckoutKey()));
  }

  /**
   * End a stay: free the room and mark it Dirty for housekeeping.
   * Accepts a confirmation number or an occupied room ID.
   */
  public String checkOutGuest(String confirmationOrRoomId) {
    if (isBlank(confirmationOrRoomId)) {
      return "Confirmation number or room ID is required.";
    }
    String key = confirmationOrRoomId.trim();
    Reservation found = findInQueue(historyQueue, key);
    if (found == null) {
      Room occupiedRoom = findRoomByIdOrConfirmation(key);
      if (occupiedRoom != null && occupiedRoom.isOccupied()
          && occupiedRoom.getAssignedConfirmationNumber() != null) {
        found = findInQueue(historyQueue, occupiedRoom.getAssignedConfirmationNumber());
      }
    }
    if (found == null) {
      return "No walk-in reservation found for " + key + ".";
    }
    if (found.getStatus() != ReservationStatus.CHECKED_IN
        && found.getStatus() != ReservationStatus.ASSIGNED) {
      return "Reservation is " + found.getStatus() + " and cannot be checked out.";
    }

    Room room = findRoomById(found.getAssignedRoomId());
    if (room != null) {
      room.vacateAfterCheckout();
    }
    found.setStatus(ReservationStatus.CHECKED_OUT);
    saveReservations();
    HotelBootstrap.save(housekeeping);
    return "Checked out confirmation " + found.getConfirmationNumber()
        + ". Room " + (found.getAssignedRoomId() == null ? "-" : found.getAssignedRoomId())
        + " is now dirty and sent to housekeeping.";
  }

  public Room findRoomByIdOrConfirmation(String roomIdOrConfirmation) {
    if (isBlank(roomIdOrConfirmation)) {
      return null;
    }
    String key = roomIdOrConfirmation.trim();
    Room byId = findRoomById(key);
    if (byId != null) {
      return byId;
    }
    Room[] rooms = housekeeping.getAllRooms();
    if (rooms == null) {
      return null;
    }
    for (int i = 0; i < rooms.length; i++) {
      Room room = rooms[i];
      if (room != null && key.equals(room.getAssignedConfirmationNumber())) {
        return room;
      }
    }
    return null;
  }

  public Room[] getAllRooms() {
    return housekeeping.getAllRooms();
  }

  /**
   * Assignment board: every room with housekeeping status, occupancy,
   * and whether Walk-In can assign it right now.
   */
  public String formatRoomStatusBoard() {
    Room[] rooms = housekeeping.getAllRooms();
    StringBuilder report = new StringBuilder();
    report.append("==============================================================\n");
    report.append(" ROOM STATUS BOARD (Walk-In assignment)\n");
    report.append(" A room can be assigned only when it is Clean and free.\n");
    report.append("==============================================================\n");
    report.append(String.format("%-8s %-10s %-22s %-10s %-12s %-16s\n",
        "Room", "Type", "Housekeeping", "Occupancy", "Can Assign", "Assigned To"));

    int readyCount = 0;
    int occupiedCount = 0;
    if (rooms != null) {
      for (int i = 0; i < rooms.length; i++) {
        Room room = rooms[i];
        if (room == null) {
          continue;
        }
        boolean assignable = room.isReadyForAssignment();
        if (assignable) {
          readyCount++;
        }
        if (room.isOccupied()) {
          occupiedCount++;
        }
        String assigned = "-";
        if (room.getAssignedConfirmationNumber() != null) {
          Reservation reservation = findReservation(room.getAssignedConfirmationNumber());
          if (reservation != null && reservation.getGuest() != null) {
            assigned = reservation.getGuest().getName();
          } else {
            assigned = room.getAssignedConfirmationNumber();
          }
        }
        report.append(String.format("%-8s %-10s %-22s %-10s %-12s %-16s\n",
            room.getRoomId(),
            room.getRoomType(),
            room.getCurrentStatus(),
            room.isOccupied() ? "Occupied" : "Free",
            assignable ? "YES" : "No",
            assigned));
      }
    }

    report.append("--------------------------------------------------------------\n");
    report.append(String.format("%-10s %-10s %-12s %-12s\n", "Room Type", "Rooms", "Ready/Free", "Waiting"));
    appendTypeSummary(report, "Standard", null);
    appendTypeSummary(report, "Deluxe", null);
    appendTypeSummary(report, "Suite", null);
    report.append("--------------------------------------------------------------\n");
    report.append("Ready to assign now : ").append(readyCount).append("\n");
    report.append("Currently occupied  : ").append(occupiedCount).append("\n");
    report.append("Guests still waiting: ").append(pendingQueue.getNumberOfEntries()).append("\n");
    return report.toString();
  }

  public Reservation[] getPendingReservations() {
    return copyQueue(pendingQueue);
  }

  public Reservation[] getAllReservations() {

    int size =
            historyQueue.getNumberOfEntries();

    Reservation[] reservations =
            new Reservation[size];

    for (int i = 1;
            i <= size;
            i++) {

        reservations[i - 1] =
                historyQueue.getEntry(i);
    }

    return reservations;
}

  public String formatReservationDetails(Reservation reservation) {
    if (reservation == null) {
      return "No reservation found.";
    }
    StringBuilder details = new StringBuilder();
    details.append("Confirmation : ").append(reservation.getConfirmationNumber()).append('\n');
    details.append("Type         : ").append(reservation.getBookingType()).append('\n');
    details.append("Status       : ").append(reservation.getStatus()).append('\n');
    details.append("Room type    : ").append(reservation.getRoomType()).append('\n');
    details.append("Room         : ").append(reservation.getAssignedRoomId() == null ? "-" : reservation.getAssignedRoomId()).append('\n');
    details.append("Check-in     : ").append(reservation.getCheckInDate()).append('\n');
    details.append("Check-out    : ").append(reservation.getCheckOutDate()).append('\n');
    if (reservation.getGuest() != null) {
      details.append("Guest        : ").append(reservation.getGuest().getName()).append('\n');
      details.append("IC/Passport  : ").append(reservation.getGuest().getIdentityNumber()).append('\n');
      details.append("Phone        : ").append(reservation.getGuest().getPhone()).append('\n');
    }
    return details.toString();
  }

  public String getAllPending() {
    if (pendingQueue.isEmpty()) {
      return "Pending queue is empty.\n";
    }
    String outputStr = "";
    Iterator<Reservation> iterator = pendingQueue.getIterator();
    int position = 1;
    while (iterator.hasNext()) {
      Reservation reservation = iterator.next();
      outputStr += String.format("%-4d %s\n", position, reservation.toString());
      position++;
    }
    return outputStr;
  }

  public void runReports() {
    int choice = 0;
    do {
      choice = bookingUI.getReportMenuChoice();
      switch (choice) {
        case 0:
          break;
        case 1:
          LocalDate startDate = bookingUI.inputStartDate();
          LocalDate endDate = bookingUI.inputEndDate();
          BookingType typeFilter = bookingUI.inputBookingTypeFilter();
          bookingUI.displayReport(generateArrivalsReport(startDate, endDate, typeFilter));
          break;
        case 2:
          String roomTypeFilter = bookingUI.inputRoomTypeFilter();
          bookingUI.displayReport(generateDemandReport(roomTypeFilter));
          break;
        default:
          MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);
  }

  /**
   * Filter history by check-in date range and booking type, then sort by booked time.
   * Uses a copied array + insertion sort (not Collections.sort).
   */
  public Reservation[] getFilteredArrivals(LocalDate startDate, LocalDate endDate, BookingType typeFilter) {
    Reservation[] all = copyQueue(historyQueue);
    int matchCount = 0;
    for (int i = 0; i < all.length; i++) {
      if (matchesArrivalFilter(all[i], startDate, endDate, typeFilter)) {
        matchCount++;
      }
    }

    Reservation[] filtered = new Reservation[matchCount];
    int index = 0;
    for (int i = 0; i < all.length; i++) {
      if (matchesArrivalFilter(all[i], startDate, endDate, typeFilter)) {
        filtered[index] = all[i];
        index++;
      }
    }
    insertionSortByBookedAt(filtered);
    return filtered;
  }

  public Reservation[] getFilteredDemand(String roomTypeFilter) {
    Reservation[] pending = copyQueue(pendingQueue);
    int matchCount = 0;
    for (int i = 0; i < pending.length; i++) {
      if (matchesRoomType(pending[i], roomTypeFilter)) {
        matchCount++;
      }
    }

    Reservation[] filtered = new Reservation[matchCount];
    int index = 0;
    for (int i = 0; i < pending.length; i++) {
      if (matchesRoomType(pending[i], roomTypeFilter)) {
        filtered[index] = pending[i];
        index++;
      }
    }
    insertionSortByBookedAt(filtered);
    return filtered;
  }

  public int getPendingQueuePosition(Reservation reservation) {
    if (reservation == null) {
      return -1;
    }
    return pendingQueue.indexOf(reservation);
  }

  public String getAssignmentOutlook(Reservation reservation) {
    return describeAssignmentOutlook(reservation, getPendingQueuePosition(reservation));
  }

  public int countRoomsOfType(String roomType) {
    int count = 0;
    Room[] allRooms = housekeeping.getAllRooms();
    for (int i = 0; i < allRooms.length; i++) {
      if (allRooms[i].getRoomType().equals(roomType)) {
        count++;
      }
    }
    return count;
  }

  public int countAvailableRoomsOfType(String roomType) {
    int count = 0;
    Room[] allRooms = housekeeping.getAllRooms();
    for (int i = 0; i < allRooms.length; i++) {
      if (allRooms[i].getRoomType().equals(roomType) && allRooms[i].isReadyForAssignment()) {
        count++;
      }
    }
    return count;
  }

  public int countWaitingOfType(String roomType) {
    int count = 0;
    for (int i = 1; i <= pendingQueue.getNumberOfEntries(); i++) {
      Reservation reservation = pendingQueue.getEntry(i);
      if (reservation != null && roomType.equals(reservation.getRoomType())) {
        count++;
      }
    }
    return count;
  }

  public String generateArrivalsReport(LocalDate startDate, LocalDate endDate, BookingType typeFilter) {
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      return "\nStart date cannot be after end date.\n";
    }

    Reservation[] filtered = getFilteredArrivals(startDate, endDate, typeFilter);

    int walkInCount = 0;
    int standardCount = 0;
    int waitingCount = 0;
    int assignedCount = 0;
    StringBuilder report = new StringBuilder();
    report.append("\n==============================================================\n");
    report.append(" REPORT: Walk-In vs Standard Arrivals\n");
    report.append(" Filter: ").append(startDate).append(" to ").append(endDate);
    report.append(" | Type: ").append(typeFilter == null ? "All" : typeFilter).append("\n");
    report.append(" Sorted by booking time (earliest first)\n");
    report.append("==============================================================\n");
    report.append(String.format("%-10s %-12s %-20s %-10s %-12s %-12s\n",
        "Conf No", "Type", "Guest", "Room Type", "Check-In", "Status"));

    for (int i = 0; i < filtered.length; i++) {
      Reservation reservation = filtered[i];
      String guestName = reservation.getGuest() == null ? "-" : reservation.getGuest().getName();
      report.append(String.format("%-10s %-12s %-20s %-10s %-12s %-12s\n",
          reservation.getConfirmationNumber(),
          reservation.getBookingType(),
          guestName,
          reservation.getRoomType(),
          reservation.getCheckInDate(),
          reservation.getStatus()));
      if (reservation.getBookingType() == BookingType.WALK_IN) {
        walkInCount++;
      } else if (reservation.getBookingType() == BookingType.STANDARD) {
        standardCount++;
      }
      if (reservation.getStatus() == ReservationStatus.WAITING) {
        waitingCount++;
      } else if (reservation.getStatus() == ReservationStatus.ASSIGNED
          || reservation.getStatus() == ReservationStatus.CHECKED_IN) {
        assignedCount++;
      }
    }

    report.append("--------------------------------------------------------------\n");
    report.append("Matching records : ").append(filtered.length).append("\n");
    report.append("Walk-in          : ").append(walkInCount).append("\n");
    report.append("Standard         : ").append(standardCount).append("\n");
    report.append("Still waiting    : ").append(waitingCount).append("\n");
    report.append("Assigned/in-house: ").append(assignedCount).append("\n");
    return report.toString();
  }

  /**
   * Report 2: filter pending queue by room type, sort by waiting time, compare with free rooms.
   * Shows who is blocked because no matching Ready room is free.
   */
  public String generateDemandReport(String roomTypeFilter) {
    Reservation[] filtered = getFilteredDemand(roomTypeFilter);

    StringBuilder report = new StringBuilder();
    report.append("\n==============================================================\n");
    report.append(" REPORT: Unassigned Demand vs Available Rooms\n");
    report.append(" Filter: ").append(roomTypeFilter == null ? "All types" : roomTypeFilter).append("\n");
    report.append(" Sorted by waiting time (longest wait first)\n");
    report.append("==============================================================\n");
    report.append(String.format("%-4s %-10s %-20s %-10s %-12s %-28s\n",
        "Pos", "Conf No", "Guest", "Room Type", "Booked", "Assignment outlook"));

    for (int i = 0; i < filtered.length; i++) {
      Reservation reservation = filtered[i];
      int queuePosition = pendingQueue.indexOf(reservation);
      String outlook = describeAssignmentOutlook(reservation, queuePosition);
      String guestName = reservation.getGuest() == null ? "-" : reservation.getGuest().getName();
      String bookedDate = reservation.getBookedAt() == null ? "-" : reservation.getBookedAt().toLocalDate().toString();
      report.append(String.format("%-4d %-10s %-20s %-10s %-12s %-28s\n",
          queuePosition,
          reservation.getConfirmationNumber(),
          guestName,
          reservation.getRoomType(),
          bookedDate,
          outlook));
    }

    report.append("--------------------------------------------------------------\n");
    report.append(String.format("%-10s %-10s %-12s %-12s\n", "Room Type", "Rooms", "Available", "Waiting"));
    appendTypeSummary(report, "Standard", roomTypeFilter);
    appendTypeSummary(report, "Deluxe", roomTypeFilter);
    appendTypeSummary(report, "Suite", roomTypeFilter);
    return report.toString();
  }

  public static void main(String[] args) {
    app.HotelMain.main(args);
  }

  private Reservation createReservation(Guest guest, String roomType, LocalDate checkIn,
      LocalDate checkOut, BookingType bookingType) {
    String confirmationNumber = generateConfirmationNumber();
    return new Reservation(confirmationNumber, guest, roomType, checkIn, checkOut,
        LocalDateTime.now(), bookingType, ReservationStatus.WAITING);
  }

  private String generateConfirmationNumber() {
    int next = 10000000 + historyQueue.getNumberOfEntries() + 1;
    String confirmation = String.format("%08d", next);
    while (historyQueue.contains(new Reservation(confirmation))) {
      next++;
      confirmation = String.format("%08d", next);
    }
    return confirmation;
  }

  private boolean hasRoomCapacity(String roomType, LocalDate checkIn, LocalDate checkOut) {
    int capacity = countRoomsOfType(roomType);
    int demand = 0;
    for (int i = 1; i <= historyQueue.getNumberOfEntries(); i++) {
      Reservation reservation = historyQueue.getEntry(i);
      if (reservation == null) {
        continue;
      }
      if (!roomType.equals(reservation.getRoomType())) {
        continue;
      }
      if (reservation.getStatus() == ReservationStatus.CANCELLED
          || reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
        continue;
      }
      if (reservation.overlaps(checkIn, checkOut)) {
        demand++;
      }
    }
    return demand < capacity;
  }

  private Room findAvailableRoom(String roomType) {
    Room[] allRooms = housekeeping.getAllRooms();
    for (int i = 0; i < allRooms.length; i++) {
      if (allRooms[i].getRoomType().equalsIgnoreCase(roomType)
          && allRooms[i].isReadyForAssignment()) {
        return allRooms[i];
      }
    }
    return null;
  }

  private Room findRoomById(String roomId) {
    return housekeeping.findRoomById(roomId);
  }

  private Reservation findInQueue(QueueInterface<Reservation> queue, String confirmationNumber) {
    Reservation key = new Reservation(confirmationNumber);
    int position = queue.indexOf(key);
    if (position == -1) {
      return null;
    }
    return queue.getEntry(position);
  }

  private Reservation[] copyQueue(QueueInterface<Reservation> queue) {
    int size = queue.getNumberOfEntries();
    Reservation[] copy = new Reservation[size];
    for (int i = 1; i <= size; i++) {
      copy[i - 1] = queue.getEntry(i);
    }
    return copy;
  }

  private void insertionSortByBookedAt(Reservation[] reservations) {
    for (int i = 1; i < reservations.length; i++) {
      Reservation key = reservations[i];
      int j = i - 1;
      while (j >= 0 && bookedAtIsAfter(reservations[j], key)) {
        reservations[j + 1] = reservations[j];
        j--;
      }
      reservations[j + 1] = key;
    }
  }

  private boolean bookedAtIsAfter(Reservation left, Reservation right) {
    if (left.getBookedAt() == null) {
      return false;
    }
    if (right.getBookedAt() == null) {
      return true;
    }
    return left.getBookedAt().isAfter(right.getBookedAt());
  }

  private boolean matchesArrivalFilter(Reservation reservation, LocalDate startDate,
      LocalDate endDate, BookingType typeFilter) {
    if (reservation == null || reservation.getCheckInDate() == null) {
      return false;
    }
    if (startDate != null && reservation.getCheckInDate().isBefore(startDate)) {
      return false;
    }
    if (endDate != null && reservation.getCheckInDate().isAfter(endDate)) {
      return false;
    }
    if (typeFilter != null && reservation.getBookingType() != typeFilter) {
      return false;
    }
    return true;
  }

  private boolean matchesRoomType(Reservation reservation, String roomTypeFilter) {
    if (reservation == null) {
      return false;
    }
    if (roomTypeFilter == null) {
      return true;
    }
    return roomTypeFilter.equals(reservation.getRoomType());
  }

  private String describeAssignmentOutlook(Reservation reservation, int queuePosition) {
    if (queuePosition != 1) {
      return "Waiting behind position " + queuePosition;
    }
    Room room = findAvailableRoom(reservation.getRoomType());
    if (room == null) {
      return "Blocked: no ready " + reservation.getRoomType() + " room";
    }
    return "Ready to assign " + room.getRoomId();
  }

  private void appendTypeSummary(StringBuilder report, String roomType, String roomTypeFilter) {
    if (roomTypeFilter != null && !roomTypeFilter.equals(roomType)) {
      return;
    }
    report.append(String.format("%-10s %-10d %-12d %-12d\n",
        roomType,
        countRoomsOfType(roomType),
        countAvailableRoomsOfType(roomType),
        countWaitingOfType(roomType)));
  }

  private void restoreRoomOccupancy() {
    syncRoomOccupancy();
  }

  private void syncRoomOccupancy() {
    Room[] allRooms = housekeeping.getAllRooms();
    for (int i = 0; i < allRooms.length; i++) {
      Room room = allRooms[i];
      String currentConf = room.getAssignedConfirmationNumber();
      Reservation currentWalkIn = currentConf == null ? null : findInQueue(historyQueue, currentConf);
      if (currentWalkIn != null) {
        room.clearOccupancy();
      }
    }
    for (int i = 1; i <= historyQueue.getNumberOfEntries(); i++) {
      Reservation reservation = historyQueue.getEntry(i);
      if (reservation == null) {
        continue;
      }
      if (reservation.getStatus() == ReservationStatus.ASSIGNED
          || reservation.getStatus() == ReservationStatus.CHECKED_IN) {
        Room room = findRoomById(reservation.getAssignedRoomId());
        if (room != null) {
          room.occupy(reservation.getConfirmationNumber());
        }
      }
    }
  }

  private void seedSampleDataIfEmpty() {
    if (!historyQueue.isEmpty()) {
      return;
    }

    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();

    Reservation walkInWaiting = new Reservation("10000001",
        new Guest("Ali Rahman", "900101-14-1234", "012-1111111"),
        "Standard", today, today.plusDays(2), now.minusHours(4),
        BookingType.WALK_IN, ReservationStatus.WAITING);
    pendingQueue.enqueue(walkInWaiting);
    historyQueue.enqueue(walkInWaiting);

    Reservation deluxeWaiting = new Reservation("10000002",
        new Guest("Mei Ling", "880215-10-5678", "016-2222222"),
        "Deluxe", today, today.plusDays(1), now.minusHours(3),
        BookingType.WALK_IN, ReservationStatus.WAITING);
    pendingQueue.enqueue(deluxeWaiting);
    historyQueue.enqueue(deluxeWaiting);

    Reservation standardWaiting = new Reservation("10000003",
        new Guest("Raj Kumar", "950330-08-9012", "017-3333333"),
        "Suite", today.plusDays(1), today.plusDays(4), now.minusHours(2),
        BookingType.STANDARD, ReservationStatus.WAITING);
    pendingQueue.enqueue(standardWaiting);
    historyQueue.enqueue(standardWaiting);

    Reservation extraStandard = new Reservation("10000007",
        new Guest("Lisa Wong", "930404-08-3344", "018-7777777"),
        "Standard", today, today.plusDays(3), now.minusHours(1),
        BookingType.WALK_IN, ReservationStatus.WAITING);
    pendingQueue.enqueue(extraStandard);
    historyQueue.enqueue(extraStandard);

    Reservation extraDeluxe = new Reservation("10000008",
        new Guest("Ahmad Faiz", "910212-10-5566", "014-8888888"),
        "Deluxe", today.plusDays(2), today.plusDays(5), now.minusMinutes(40),
        BookingType.STANDARD, ReservationStatus.WAITING);
    pendingQueue.enqueue(extraDeluxe);
    historyQueue.enqueue(extraDeluxe);

    Reservation inHouse = new Reservation("10000006",
        new Guest("David Chong", "870808-14-7788", "011-5555555"),
        "Deluxe", today, today.plusDays(2), now.minusHours(8),
        BookingType.WALK_IN, ReservationStatus.CHECKED_IN);
    inHouse.setAssignedRoomId("201");
    historyQueue.enqueue(inHouse);

    Reservation checkedOut = new Reservation("10000004",
        new Guest("Sarah Tan", "920512-14-3456", "019-4444444"),
        "Standard", today.minusDays(3), today.minusDays(1), now.minusDays(3),
        BookingType.STANDARD, ReservationStatus.CHECKED_OUT);
    checkedOut.setAssignedRoomId("102");
    historyQueue.enqueue(checkedOut);

    Reservation cancelled = new Reservation("10000005",
        new Guest("Nur Aisyah", "990101-01-1122", "013-6666666"),
        "Standard", today.plusDays(2), today.plusDays(5), now.minusHours(1),
        BookingType.STANDARD, ReservationStatus.CANCELLED);
    historyQueue.enqueue(cancelled);

    saveReservations();
  }

  private boolean isBlank(String text) {
    return text == null || text.trim().isEmpty();
  }

  private void saveReservations() {
    reservationDAO.saveToFile(pendingQueue, historyQueue);
  }



}
