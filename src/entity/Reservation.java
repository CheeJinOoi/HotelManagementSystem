package entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Reservation.java
 * ENTITY: one walk-in or standard booking held in the circular queue.
 *
 * equals() uses confirmation number only so contains/indexOf can search by key.
 *
 * @author vinsx
 */
public class Reservation implements Serializable {

  /** Unique 8-digit booking code. */
  private String confirmationNumber;
  private Guest guest;
  private String roomType;
  /** Room id after assignment, or null while still waiting. */
  private String assignedRoomId;
  private LocalDate checkInDate;
  private LocalDate checkOutDate;
  /** When the booking was created (used for chronological reports). */
  private LocalDateTime bookedAt;
  private BookingType bookingType;
  private ReservationStatus status;

  public Reservation() {
  }

  /**
   * Search-key constructor. Used with queue.contains / indexOf
   * so we can find a booking by confirmation number only.
   */
  public Reservation(String confirmationNumber) {
    this.confirmationNumber = confirmationNumber;
  }

  public Reservation(String confirmationNumber, Guest guest, String roomType,
      LocalDate checkInDate, LocalDate checkOutDate, LocalDateTime bookedAt,
      BookingType bookingType, ReservationStatus status) {
    this.confirmationNumber = confirmationNumber;
    this.guest = guest;
    this.roomType = roomType;
    this.checkInDate = checkInDate;
    this.checkOutDate = checkOutDate;
    this.bookedAt = bookedAt;
    this.bookingType = bookingType;
    this.status = status;
  }

  public String getConfirmationNumber() {
    return confirmationNumber;
  }

  public void setConfirmationNumber(String confirmationNumber) {
    this.confirmationNumber = confirmationNumber;
  }

  public Guest getGuest() {
    return guest;
  }

  public void setGuest(Guest guest) {
    this.guest = guest;
  }

  public String getRoomType() {
    return roomType;
  }

  public void setRoomType(String roomType) {
    this.roomType = roomType;
  }

  public String getAssignedRoomId() {
    return assignedRoomId;
  }

  public void setAssignedRoomId(String assignedRoomId) {
    this.assignedRoomId = assignedRoomId;
  }

  public LocalDate getCheckInDate() {
    return checkInDate;
  }

  public void setCheckInDate(LocalDate checkInDate) {
    this.checkInDate = checkInDate;
  }

  public LocalDate getCheckOutDate() {
    return checkOutDate;
  }

  public void setCheckOutDate(LocalDate checkOutDate) {
    this.checkOutDate = checkOutDate;
  }

  public LocalDateTime getBookedAt() {
    return bookedAt;
  }

  public void setBookedAt(LocalDateTime bookedAt) {
    this.bookedAt = bookedAt;
  }

  public BookingType getBookingType() {
    return bookingType;
  }

  public void setBookingType(BookingType bookingType) {
    this.bookingType = bookingType;
  }

  public ReservationStatus getStatus() {
    return status;
  }

  public void setStatus(ReservationStatus status) {
    this.status = status;
  }

  /** True if this stay overlaps another date range (used for capacity checks). */
  public boolean overlaps(LocalDate otherCheckIn, LocalDate otherCheckOut) {
    if (checkInDate == null || checkOutDate == null || otherCheckIn == null || otherCheckOut == null) {
      return false;
    }
    return checkInDate.isBefore(otherCheckOut) && otherCheckIn.isBefore(checkOutDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(confirmationNumber);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Reservation other = (Reservation) obj;
    return Objects.equals(this.confirmationNumber, other.confirmationNumber);
  }

  @Override
  public String toString() {
    String guestName = guest == null ? "-" : guest.getName();
    String room = assignedRoomId == null ? "-" : assignedRoomId;
    return String.format("%-10s %-12s %-20s %-10s %-12s %-12s %-10s %-12s",
        confirmationNumber,
        bookingType,
        guestName,
        roomType,
        checkInDate,
        checkOutDate,
        room,
        status);
  }
}
