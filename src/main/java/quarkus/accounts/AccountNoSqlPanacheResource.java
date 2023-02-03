package quarkus.accounts;

import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
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
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * REST endpoint for {@link AccountNoSqlPanacheRepository}.
 *
 * <ul>
 *   <li>This endpoint use methods provided OOB by Panache MongoDB on {@link AccountNoSqlPanacheRepository}</li>
 *   <li>{@code AccountNoSqlPanacheRepository} it's an account db entity
 *     prepared for use with Panache MongoDB repositories.</li>
 * </ul>
 */
@Path("/accounts-nosql-panache-repository")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountNoSqlPanacheResource {

  @Inject
  AccountNoSqlPanacheRepository repository;

  @GET
  public List<AccountNoSqlPanache> allAccounts() {
    return repository.listAll();
  }

  @GET
  @Path("/{accountNumber}")
  public AccountNoSqlPanache getAccount(@PathParam("accountNumber") Long accountNumber) {
    return repository
        .findByAccountNumber(accountNumber)
        .orElseThrow(() -> new WebApplicationException("Account with " + accountNumber + " does not exist.", 404));
  }

  @POST
  public Response createAccount(AccountNoSqlPanache account) {
    // Be careful - Panache's .persist() will persist account with our custom document's id if provided.
    // For new accounts we want to generate a new fresh document's id.
    if (account.getId() != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 400);
    }

    repository.persist(account);

    return Response.status(201).entity(account).build();
  }

  @PUT
  @Path("{accountNumber}/withdrawal")
  public AccountNoSqlPanache withdrawal(@PathParam("accountNumber") Long accountNumber, String amount) {
    AccountNoSqlPanache account = repository
        .findByAccountNumber(accountNumber)
        .orElseThrow(() -> new WebApplicationException("Account with " + accountNumber + " does not exist.", 404));

    if (account.getAccountStatus().equals(AccountStatus.OVERDRAWN)) {
      throw new WebApplicationException(
          "Account is overdrawn, no further withdrawals permitted",
          409);
    }

    account.withdrawFunds(new BigDecimal(amount));

    repository.update(account);

    return account;
  }

  @PUT
  @Path("{accountNumber}/deposit")
  public AccountNoSqlPanache deposit(@PathParam("accountNumber") Long accountNumber, String amount) {
    AccountNoSqlPanache account = repository
        .findByAccountNumber(accountNumber)
        .orElseThrow(() -> new WebApplicationException("Account with " + accountNumber + " does not exist.", 404));

    account.addFunds(new BigDecimal(amount));

    repository.update(account);

    return account;
  }

  @DELETE
  @Path("{accountNumber}")
  public Response closeAccount(@PathParam("accountNumber") Long accountNumber) {
    AccountNoSqlPanache account = repository
        .findByAccountNumber(accountNumber)
        .orElseThrow(() -> new WebApplicationException("Account with " + accountNumber + " does not exist.", 404));

    account.close();

    repository.update(account);

    return Response.noContent().build();
  }

  /**
   * Implements ExceptionMapper for all Exception types.
   */
  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
      int code = 500;
      if (exception instanceof WebApplicationException webAppException) {
        code = webAppException.getResponse().getStatus();
      }

      JsonObjectBuilder entityBuilder =
          Json.createObjectBuilder().add("exceptionType", exception.getClass().getName())
              .add("code", code);

      if (exception.getMessage() != null) {
        entityBuilder.add("error", exception.getMessage());
      }

      return Response.status(code).entity(entityBuilder.build()).build();
    }
  }
}
