package quarkus.accounts;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import javax.enterprise.context.ApplicationScoped;

/**
 * {@link AccountRepository} Lombok's alternative.
 */
@ApplicationScoped
public class AccountLombokRepository implements PanacheRepository<AccountLombok> {
  public AccountLombok findByAccountNumber(Long accountNumber) {
    return find("accountNumber = ?1", accountNumber).firstResult();
  }
}
