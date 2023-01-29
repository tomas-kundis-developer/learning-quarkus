package quarkus.accounts;

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;

// Indicates the POJO is a JPA entity.
@Entity
// Defines a named query to retrieve all accounts, and orders the result by accountNumber.
@NamedQuery(name = "Accounts.findAll", query = "SELECT a FROM Account a ORDER BY a.accountNumber")
// Finding accounts that match accountNumber.
@NamedQuery(
    name = "Accounts.findByAccountNumber",
    query =
        "SELECT a FROM Account a WHERE a.accountNumber = :accountNumber ORDER BY a.accountNumber")
public class Account {

  // Constructing instances directly is not needed when using JPA.

  // When using JPA, the fields can be marked private instead of public.

  @Id
  // Creates a sequence generator for the id field, starting with the number 10.
  //   Starting at 10 provides space to import some records on startup for testing.
  @SequenceGenerator(
      name = "accountsSequence",
      sequenceName = "accounts_id_seq",
      allocationSize = 1,
      initialValue = 10)
  // Uses the sequence generator from the previous line to specify
  // where the generated value comes from for the primary key.
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountsSequence")
  private Long id;

  private Long accountNumber;
  private Long customerNumber;
  private String customerName;
  private BigDecimal balance;
  private AccountStatus accountStatus = AccountStatus.OPEN;

  public Long getId() {
    return id;
  }

  // @TODO 2023-01-24 TOKU: DANGEROUS!?
  public void setId(Long id) {
    this.id = id;
  }

  public void markOverdrawn() {
    accountStatus = AccountStatus.OVERDRAWN;
  }

  public void removeOverdrawnStatus() {
    accountStatus = AccountStatus.OPEN;
  }

  public void close() {
    accountStatus = AccountStatus.CLOSED;
    // @TODO 2023-01-24 TOKU: BigDecimal.ZERO?
    balance = BigDecimal.valueOf(0);
  }

  public void withdrawFunds(BigDecimal amount) {
    balance = balance.subtract(amount);
  }

  public void addFunds(BigDecimal amount) {
    balance = balance.add(amount);
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public Long getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(Long accountNumber) {
    this.accountNumber = accountNumber;
  }

  public Long getCustomerNumber() {
    return customerNumber;
  }

  public void setCustomerNumber(Long customerNumber) {
    this.customerNumber = customerNumber;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public AccountStatus getAccountStatus() {
    return accountStatus;
  }

  public void setAccountStatus(AccountStatus accountStatus) {
    this.accountStatus = accountStatus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Account account = (Account) o;

    return id.equals(account.id)
        && accountNumber.equals(account.accountNumber)
        && customerNumber.equals(account.customerNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, accountNumber, customerNumber);
  }
}
