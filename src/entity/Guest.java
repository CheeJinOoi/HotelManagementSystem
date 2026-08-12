package entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Guest.java
 * ENTITY: one hotel guest (name, IC/passport, phone).
 * POJO only — no Scanner / System.out (ECB rule for entity classes).
 *
 * @author vinsx
 */
public class Guest implements Serializable {

  private String name;
  private String identityNumber;
  private String phone;

  public Guest() {
  }

  public Guest(String name, String identityNumber, String phone) {
    this.name = name;
    this.identityNumber = identityNumber;
    this.phone = phone;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIdentityNumber() {
    return identityNumber;
  }

  public void setIdentityNumber(String identityNumber) {
    this.identityNumber = identityNumber;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Override
  public int hashCode() {
    return Objects.hash(identityNumber);
  }

  /** Two guests are equal if their IC / passport numbers match. */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Guest other = (Guest) obj;
    return Objects.equals(this.identityNumber, other.identityNumber);
  }

  @Override
  public String toString() {
    return String.format("%-20s %-16s %-12s", name, identityNumber, phone);
  }
}
