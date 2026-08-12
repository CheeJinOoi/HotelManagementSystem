package entity;

/**
 * BookingType.java
 * Distinguishes same-day walk-ins from advance standard bookings.
 *
 * @author vinsx
 */
public enum BookingType {
  WALK_IN("Walk-In"),
  STANDARD("Standard");

  private final String displayName;

  private BookingType(String displayName) {
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
