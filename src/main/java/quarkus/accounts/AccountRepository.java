package quarkus.accounts;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import javax.enterprise.context.ApplicationScoped;

// Tells the container that only one instance should exist.
@ApplicationScoped
// Implements PanacheRepository for all the data access methods.
public class AccountRepository implements PanacheRepository<Account> {

  // Defines a custom data access method.
  public Account findByAccountNumber(Long accountNumber) {
    return find("accountNumber = ?1", accountNumber).firstResult();
  }
}
