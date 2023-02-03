package quarkus.accounts;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
// Start an H2 database prior to the tests being executed.
@QuarkusTestResource(H2DatabaseTestResource.class)
// Along with @Order defines the test execution order.
@TestMethodOrder(OrderAnnotation.class)
class AccountJpaResourceTest {

  @Test
  @Order(1)
  void testRetrieveAll() {
    // With JUnit 5, test methods don’t need to be public.

    // Issues an HTTP GET request to /accounts URL
    Response result = given()
        .when().get("/accounts")
        .then()
        // Verifies the response had a 200 status code
        .statusCode(200)
        .body(
            // Verifies the body contains all customer names
            containsString("Debbie Hall"),
            containsString("David Tennant"),
            containsString("Alex Kingston")
        )
        // Extracts the response
        .extract()
        .response();

    // Extracts the JSON array and converts it to a list of Account objects
    List<AccountJpa> accounts = result.jsonPath().getList("$");

    assertThat(accounts, not(empty()));
    assertThat(accounts, hasSize(8));
  }

  @Test
  @Order(2)
  void testGetAccount() {
    AccountJpa account = given()
        .when().get("/accounts/{accountNumber}", 444666)
        .then()
        .statusCode(200)
        .extract().as(AccountJpa.class);

    assertThat(account.getAccountNumber(), equalTo(444666L));
    assertThat(account.getCustomerName(), equalTo("Billie Piper"));
    assertThat(account.getCustomerNumber(), equalTo(332233L));
    assertThat(account.getBalance(), equalTo(new BigDecimal("3499.12")));
    assertThat(account.getAccountStatus(), equalTo(AccountStatus.OPEN));
  }

  @Test
  @Order(3)
  void testCreateAccount() {

    AccountJpa newAccount = new AccountJpa();
    newAccount.setAccountNumber(324324L);
    newAccount.setCustomerNumber(112244L);
    newAccount.setCustomerName("Sandy Holmes");
    newAccount.setBalance(new BigDecimal("154.55"));

    // Sets the new account object into the body of the HTTP POST.
    AccountJpa returnedAccount = given()
        .contentType(ContentType.JSON)
        .body(newAccount)
        .when().post("/accounts")
        .then()
        .statusCode(201) // 201 indicates it was created successfully.
        .extract().as(AccountJpa.class);

    assertThat(returnedAccount, notNullValue());

    // Set id returned from database in order to test Account equivalency.
    newAccount.setId(returnedAccount.getId());
    assertThat(returnedAccount, equalTo(newAccount));

    Response response = given()
        .when().get("/accounts/")
        .then()
        .statusCode(200)
        .body(
            containsString("Debbie Hall"),
            containsString("David Tennant"),
            containsString("Alex Kingston"),
            containsString("Sandy Holmes")
        )
        .extract()
        .response();

    List<AccountJpa> accounts = response.jsonPath().getList("$");

    assertThat(accounts, not(empty()));
    assertThat(accounts, hasSize(9));
  }

  @Test
  @Order(4)
  void testCloseAccount() {
    given()
        .when().delete("/accounts/{accountNumber}", 5465)
        .then()
        .statusCode(204);

    AccountJpa account = given()
        .when().get("/accounts/{accountNumber}", 5465)
        .then()
        .statusCode(200)
        .extract().as(AccountJpa.class);

    assertThat(account.getAccountNumber(), equalTo(5465L));
    assertThat(account.getCustomerName(), equalTo("Alex Trebek"));
    assertThat(account.getCustomerNumber(), equalTo(776868L));
    assertThat(account.getAccountStatus(), equalTo(AccountStatus.CLOSED));
    assertThat(account.getBalance(), equalTo(new BigDecimal("0.00")));
  }

  @Test
  @Order(5)
  void testDeposit() {
    AccountJpa beforeDeposit = given()
        .when().get("/accounts/{accountNumber}", 123456789)
        .then()
        .statusCode(200)
        .extract().as(AccountJpa.class);

    assertThat(beforeDeposit.getAccountNumber(), equalTo(123456789L));
    assertThat(beforeDeposit.getCustomerName(), equalTo("Debbie Hall"));
    assertThat(beforeDeposit.getCustomerNumber(), equalTo(12345L));
    assertThat(beforeDeposit.getAccountStatus(), equalTo(AccountStatus.OPEN));
    assertThat(beforeDeposit.getBalance(), equalTo(new BigDecimal("550.78")));

    BigDecimal deposit = new BigDecimal("154.98");

    AccountJpa afterDeposit = given()
        .contentType(ContentType.JSON)
        .body(deposit.toString())
        .when().put("/accounts/{accountNumber}/deposit", 123456789)
        .then()
        .statusCode(200)
        .extract().as(AccountJpa.class);

    assertThat(afterDeposit.getAccountNumber(), equalTo(123456789L));
    assertThat(afterDeposit.getCustomerName(), equalTo("Debbie Hall"));
    assertThat(afterDeposit.getCustomerNumber(), equalTo(12345L));
    assertThat(afterDeposit.getAccountStatus(), equalTo(AccountStatus.OPEN));
    assertThat(afterDeposit.getBalance(), equalTo(beforeDeposit.getBalance().add(deposit)));

    AccountJpa account = given()
        .when().get("/accounts/{accountNumber}", 123456789)
        .then()
        .statusCode(200)
        .extract().as(AccountJpa.class);

    assertThat(account.getAccountNumber(), equalTo(123456789L));
    assertThat(account.getCustomerName(), equalTo("Debbie Hall"));
    assertThat(account.getCustomerNumber(), equalTo(12345L));
    assertThat(account.getAccountStatus(), equalTo(AccountStatus.OPEN));
    assertThat(account.getBalance(), equalTo(beforeDeposit.getBalance().add(deposit)));
  }

  @Test
  @Order(6)
  void testWithdrawal() {
    AccountJpa beforeWithdraw = given()
        .when().get("/accounts/{accountNumber}", 78790)
        .then()
        .statusCode(200)
        .extract().as(AccountJpa.class);

    assertThat(beforeWithdraw.getAccountNumber(), equalTo(78790L));
    assertThat(beforeWithdraw.getCustomerName(), equalTo("Vanna White"));
    assertThat(beforeWithdraw.getCustomerNumber(), equalTo(444222L));
    assertThat(beforeWithdraw.getAccountStatus(), equalTo(AccountStatus.OPEN));
    assertThat(beforeWithdraw.getBalance(), equalTo(new BigDecimal("439.01")));

    BigDecimal withdrawal = new BigDecimal("23.82");

    AccountJpa afterWithdraw = given()
        .contentType(ContentType.JSON)
        .body(withdrawal.toString())
        .when().put("/accounts/{accountNumber}/withdrawal", 78790)
        .then()
        .statusCode(200)
        .extract().as(AccountJpa.class);

    assertThat(afterWithdraw.getAccountNumber(), equalTo(78790L));
    assertThat(afterWithdraw.getCustomerName(), equalTo("Vanna White"));
    assertThat(afterWithdraw.getCustomerNumber(), equalTo(444222L));
    assertThat(afterWithdraw.getAccountStatus(), equalTo(AccountStatus.OPEN));
    assertThat(afterWithdraw.getBalance(),
        equalTo(beforeWithdraw.getBalance().subtract(withdrawal)));

    AccountJpa account = given()
        .when().get("/accounts/{accountNumber}", 78790)
        .then()
        .statusCode(200)
        .extract().as(AccountJpa.class);

    assertThat(account.getAccountNumber(), equalTo(78790L));
    assertThat(account.getCustomerName(), equalTo("Vanna White"));
    assertThat(account.getCustomerNumber(), equalTo(444222L));
    assertThat(account.getAccountStatus(), equalTo(AccountStatus.OPEN));
    assertThat(account.getBalance(), equalTo(beforeWithdraw.getBalance().subtract(withdrawal)));
  }

  @Test
  void testGetAccountFailure() {
    given()
        .when().get("/accounts/{accountNumber}", 11)
        .then()
        .statusCode(404);
  }

  @Test
  void testCreateAccountFailure() {
    AccountJpa newAccount = new AccountJpa();
    newAccount.setId(12L);
    newAccount.setAccountNumber(90909L);
    newAccount.setCustomerNumber(888898L);
    newAccount.setCustomerName("Barry Mines");
    newAccount.setBalance(new BigDecimal("878.32"));

    given()
        .contentType(ContentType.JSON)
        .body(newAccount)
        .when().post("/accounts")
        .then()
        .statusCode(400);
  }
}

