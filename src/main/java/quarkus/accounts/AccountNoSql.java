package quarkus.accounts;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Account db entity for persisting in MongoDB.
 */
@Data
@NoArgsConstructor
public class AccountNoSql {

  /**
   * MongoDB's default id field.
   *
   * <p>In insert operation, if not provided (null value), MongoDB will generate a new ObjectId for _id field.
   */
  @SuppressWarnings({"checkstyle:MemberName", "java:S116"})
  private String _id;

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
