package quarkus.accounts;

import io.quarkus.mongodb.panache.common.MongoEntity;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;

/**
 * Account db entity.
 *
 * <li>
 *   <ul>Db entity prepared for use with Panache MongoDB repositories.</ul>
 * </li>
 */
@MongoEntity(collection = "account")
@Data
@NoArgsConstructor
public class AccountNoSqlPanache {

  private ObjectId id;

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
