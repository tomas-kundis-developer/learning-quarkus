package quarkus.accounts;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * {@link Account} Lombok's alternative.
 */
@Entity
@Data
@NoArgsConstructor
public class AccountLombok {

  // Constructing instances directly is not needed when using JPA.

  // When using JPA, the fields can be marked private instead of public.

  @Id
  @GeneratedValue
  private Long id;

  @NonNull
  private Long accountNumber;

  @NonNull
  private Long customerNumber;

  @NonNull
  private String customerName;

  @NonNull
  private BigDecimal balance;

  @NonNull
  private AccountStatus accountStatus = AccountStatus.OPEN;

  public void markOverdrawn() {
    accountStatus = AccountStatus.OVERDRAWN;
  }

  public void removeOverdrawnStatus() {
    accountStatus = AccountStatus.OPEN;
  }

  public void close() {
    accountStatus = AccountStatus.CLOSED;
    balance = BigDecimal.ZERO;
  }

  public void withdrawFunds(BigDecimal amount) {
    balance = balance.subtract(amount);
  }

  public void addFunds(BigDecimal amount) {
    balance = balance.add(amount);
  }
}
