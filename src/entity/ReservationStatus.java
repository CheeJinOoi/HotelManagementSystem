package entity;

/**
 * ReservationStatus.java
 * Lifecycle of a booking in the Walk-In module.
 *
 * WAITING      -> in pending queue, not assigned yet
 * ASSIGNED     -> room reserved for a future check-in
 * CHECKED_IN   -> guest is staying in the room
 * CANCELLED    -> removed from waiting / cancelled booking
 * CHECKED_OUT  -> stay finished; room returned to housekeeping as Dirty
 *
 * @author vinsx
 */
public enum ReservationStatus {
  WAITING("Waiting"),
  ASSIGNED("Assigned"),
  CHECKED_IN("Checked-In"),
  CANCELLED("Cancelled"),
  CHECKED_OUT("Checked-Out");

  private final String displayName;

  private ReservationStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
