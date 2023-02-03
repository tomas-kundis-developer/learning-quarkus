package quarkus.accounts;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import lombok.NonNull;

/**
 * Panache MongoDB repository with OOB provided CRUD methods.
 *
 * <p>All the operations that are defined on {@link io.quarkus.mongodb.panache.PanacheMongoEntityBase}
 * are available on your repository.
 */
@ApplicationScoped
public class AccountNoSqlPanacheRepository implements PanacheMongoRepository<AccountNoSqlPanache> {

  /**
   * Find account with given account number.
   *
   * <p>Example of custom query method.
   *
   * @param accountNumber unique account number
   */
  public Optional<AccountNoSqlPanache> findByAccountNumber(@NonNull Long accountNumber) {
    return find("accountNumber", accountNumber).firstResultOptional();
  }
}
