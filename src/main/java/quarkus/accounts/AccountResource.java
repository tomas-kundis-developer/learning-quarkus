package quarkus.accounts;

import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
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

// Quarkus defaults JAX-RS resources to @Singleton
@Path("/accounts")
// Indicates the response and request are converted to JSON
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

  @Inject
  EntityManager entityManager;

  /**
   * Returns a Set of Account objects.
   */
  @GET
  public List<Account> allAccounts() {
    return entityManager
        // Tells the entityManager to use the named query "Accounts.findAll" defined on Account
        //   and that the expected results will be of the Account type.
        .createNamedQuery("Accounts.findAll", Account.class)
        // Converts the results from the database into a List of Account instances.
        .getResultList();
  }

  @GET
  // Defines the name of the parameter on the URL path
  @Path("/{accountNumber}")
  // @PathParam maps the accountNumber URL parameter into the accountNumber method parameter.
  public Account getAccount(@PathParam("accountNumber") Long accountNumber) {
    try {
      return entityManager
          // Uses the "Accounts.findByAccountNumber" named query.
          .createNamedQuery("Accounts.findByAccountNumber", Account.class)
          // Passes the parameter into the query, setting the name of the parameter in the query
          //   and passing the value.
          .setParameter("accountNumber", accountNumber)
          // For a given accountNumber, there should only be one account,
          //   so requests the return of a single Account instance.
          .getSingleResult();
    } catch (NoResultException e) {
      // When there is no account and converts it to a WebApplicationException.
      throw new WebApplicationException("Account with " + accountNumber + " does not exist.", 404);
    }
  }

  @POST
  // A transaction should be created for this operation.
  //   A transaction is necessary here because any exception from within the method needs
  //   to result in a “rollback” of any proposed database changes before they’re committed.
  @Transactional
  public Response createAccount(Account account) {
    if (account.getAccountNumber() == null) {
      throw new WebApplicationException("No Account number specified.", 400);
    }

    if (account.getId() != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 400);
    }

    // Adding it to the persistent context for committing to the database
    //   at the completion of the transaction, in this case, createAccount().
    entityManager.persist(account);

    return Response.status(201).entity(account).build();
  }

  @PUT
  @Path("{accountNumber}/withdrawal")
  @Transactional
  public Account withdrawal(@PathParam("accountNumber") Long accountNumber, String amount) {
    Account account;
    try {
      // Retrieving the account puts the instance into the persistence context as a managed object.
      account = getAccount(accountNumber);
    } catch (NoResultException e) {
      throw new WebApplicationException("Account with " + accountNumber + " does not exist.", 404);
    }

    if (account.getAccountStatus().equals(AccountStatus.OVERDRAWN)) {
      throw new WebApplicationException("Account is overdrawn, no further withdrawals permitted",
          409);
    }

    account.withdrawFunds(new BigDecimal(amount));
    return account;
  }

  @PUT
  @Path("{accountNumber}/deposit")
  @Transactional
  public Account deposit(@PathParam("accountNumber") Long accountNumber, String amount) {
    Account account;
    try {
      account = getAccount(accountNumber);
    } catch (NoResultException e) {
      throw new WebApplicationException("Account with " + accountNumber + " does not exist.", 404);
    }

    account.addFunds(new BigDecimal(amount));
    return account;
  }

  @DELETE
  @Path("{accountNumber}")
  @Transactional
  public Response closeAccount(@PathParam("accountNumber") Long accountNumber) {
    Account account;
    try {
      account = getAccount(accountNumber);
    } catch (NoResultException e) {
      throw new WebApplicationException("Account with " + accountNumber + " does not exist.", 404);
    }

    account.close();
    return Response.noContent().build();
  }

  /**
   * Implements ExceptionMapper for all Exception types.
   */
  // @Provider indicates the class is an auto-discovered JAX-RS Provider
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
