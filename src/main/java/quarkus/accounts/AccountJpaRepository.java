package quarkus.accounts;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AccountJpaRepository implements PanacheRepository<AccountJpa> {
  public AccountJpa findByAccountNumber(Long accountNumber) {
    return find("accountNumber = ?1", accountNumber).firstResult();
  }
}
