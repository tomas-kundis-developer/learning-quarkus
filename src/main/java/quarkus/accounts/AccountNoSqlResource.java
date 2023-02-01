package quarkus.accounts;

import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST endpoint for {@link AccountNoSql} db entity.
 */
@Path("/accounts-nosql")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountNoSqlResource {
  @Inject
  AccountNoSqlRepository accountNoSqlRepository;

  @GET
  public List<AccountNoSql> allAccounts() {
    return accountNoSqlRepository.findAll();
  }

  @GET
  @Path("/{accountNumber}")
  public AccountNoSql getAccount(@PathParam("accountNumber") Long accountNumber) {
    AccountNoSql account = accountNoSqlRepository.findByAccountNumber(accountNumber);

    if (account == null) {
      throw new WebApplicationException("Account with " + accountNumber + " does not exist.", 404);
    }

    return account;
  }

  @POST
  public Response createAccount(AccountNoSql account) {
    if (account.get_id() != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 400);
    }

    accountNoSqlRepository.insert(account);

    return Response.status(201).entity(account).build();
  }

  @PUT
  @Path("{accountNumber}/withdrawal")
  public AccountNoSql withdrawal(@PathParam("accountNumber") Long accountNumber, String amount) {
    AccountNoSql account = accountNoSqlRepository.findByAccountNumber(accountNumber);

    if (account == null) {
      throw new WebApplicationException("Account with " + accountNumber + " does not exist.", 404);
    }

    if (account.getAccountStatus().equals(AccountStatus.OVERDRAWN)) {
      throw new WebApplicationException(
          "Account is overdrawn, no further withdrawals permitted",
          409);
    }

    account.withdrawFunds(new BigDecimal(amount));

    accountNoSqlRepository.replace(account);

    return account;
  }

  @PUT
  @Path("{accountNumber}/deposit")
  public AccountNoSql deposit(@PathParam("accountNumber") Long accountNumber, String amount) {
    AccountNoSql account = accountNoSqlRepository.findByAccountNumber(accountNumber);

    if (account == null) {
      throw new WebApplicationException("Account with " + accountNumber + " does not exist.", 404);
    }

    account.addFunds(new BigDecimal(amount));

    accountNoSqlRepository.replace(account);

    return account;
  }

  @DELETE
  @Path("{accountNumber}")
  public Response closeAccount(@PathParam("accountNumber") Long accountNumber) {
    AccountNoSql account = accountNoSqlRepository.findByAccountNumber(accountNumber);

    if (account == null) {
      throw new WebApplicationException("Account with " + accountNumber + " does not exist.", 404);
    }

    account.close();

    accountNoSqlRepository.replace(account);

    return Response.noContent().build();
  }
}
