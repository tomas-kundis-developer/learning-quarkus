package quarkus.accounts;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

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
// Defines the test execution order.
@TestMethodOrder(OrderAnnotation.class)
class AccountResourceTest {

  // Defines the test execution order.
  @Order(1)
  @Test
  void testRetrieveAll() {
    // With JUnit 5, test methods don’t need to be public.
    // Issues an HTTP GET request to /accounts URL
    Response result = given()
        .when()
        .get("/accounts")
        .then()
        // Verifies the response had a 200 status code
        .statusCode(200)
        .body(
            // Verifies the body contains all customer names
            containsString("George Baird"),
            containsString("Mary Taylor"),
            containsString("Diana Rigg")
        )
        // Extracts the response
        .extract()
        .response();

    // Extracts the JSON array and converts it to a list of Account objects
    List<Account> accounts = result.jsonPath().getList("$");

    // Asserts
    assertThat(accounts, not(empty()));
    assertThat(accounts, hasSize(3));
  }

  @Order(2)
  @Test
  void testGetAccount() {
    Account account = given()
        .when()
        .get("/accounts/{accountNumber}", 545454545)
        .then()
        .statusCode(200)
        .extract()
        .as(Account.class);

    assertThat(account.getAccountNumber(), equalTo(545454545L));
    assertThat(account.getCustomerName(), equalTo("Diana Rigg"));
    assertThat(account.getBalance(), equalTo(new BigDecimal("422.00")));
    assertThat(account.getAccountStatus(), equalTo(AccountStatus.OPEN));
  }

  @Order(3)
  @Test
  void testCreateAccount() {

    Account newAccount = new Account(324324L, 112244L, "Sandy Holmes", new BigDecimal("154.55"));

    // Sets the new account object into the body of the HTTP POST.
    Account returnedAccount = given()
        .contentType(ContentType.JSON).body(newAccount)
        .when()
        .post("/accounts")
        .then()
        // 201, indicating it was created successfully.
        .statusCode(201)
        .extract()
        .as(Account.class);

    assertThat(returnedAccount, notNullValue());
    assertThat(returnedAccount, equalTo(newAccount));

    Response response = given()
        .when()
        .get("/accounts/")
        .then()
        .statusCode(200)
        .body(
            containsString("George Baird"),
            containsString("Mary Taylor"),
            containsString("Diana Rigg"),
            containsString("Sandy Holmes")
        )
        .extract()
        .response();

    List<Account> accounts = response.jsonPath().getList("$");

    assertThat(accounts, not(empty()));
    assertThat(accounts, hasSize(4));
  }

  @Test
  @Order(4)
  void testAccountWithdraw() {
    Account beforeWithdraw = given()
        .when()
        .get("/accounts/{accountNumber}", 545454545)
        .then()
        .statusCode(200)
        .extract()
        .as(Account.class);

    assertThat(beforeWithdraw.getAccountNumber(), equalTo(545454545L));
    assertThat(beforeWithdraw.getCustomerName(), equalTo("Diana Rigg"));
    assertThat(beforeWithdraw.getBalance(), equalTo(new BigDecimal("422.00")));
    assertThat(beforeWithdraw.getAccountStatus(), equalTo(AccountStatus.OPEN));

    BigDecimal withdraw = new BigDecimal("56.21");

    Account afterWithdraw = given()
        .body(withdraw.toString())
        .when()
        .put("/accounts/{accountNumber}/withdraval", 545454545)
        .then()
        .statusCode(200)
        .extract()
        .as(Account.class);

    assertThat(afterWithdraw.getAccountNumber(), equalTo(545454545L));
    assertThat(afterWithdraw.getCustomerName(), equalTo("Diana Rigg"));
    assertThat(afterWithdraw.getBalance(), equalTo(
        beforeWithdraw.getBalance().subtract(withdraw)));
    assertThat(afterWithdraw.getAccountStatus(), equalTo(AccountStatus.OPEN));
  }

  @Test
  @Order(4)
  void testAccountDeposit() {
    Account beforeDeposit = given()
        .when()
        .get("/accounts/{accountNumber}", 123456789)
        .then()
        .statusCode(200)
        .extract()
        .as(Account.class);

    assertThat(beforeDeposit.getAccountNumber(), equalTo(123456789L));
    assertThat(beforeDeposit.getCustomerName(), equalTo("George Baird"));
    assertThat(beforeDeposit.getBalance(), equalTo(new BigDecimal("354.23")));
    assertThat(beforeDeposit.getAccountStatus(), equalTo(AccountStatus.OPEN));

    BigDecimal deposit = new BigDecimal("28.42");

    Account afterDeposit = given()
        .body(deposit.toString())
        .when()
        .put("/accounts/{accountNumber}/deposit", 123456789)
        .then()
        .statusCode(200)
        .extract()
        .as(Account.class);

    assertThat(afterDeposit.getAccountNumber(), equalTo(123456789L));
    assertThat(afterDeposit.getCustomerName(), equalTo("George Baird"));
    assertThat(afterDeposit.getBalance(), equalTo(beforeDeposit.getBalance().add(deposit)));
    assertThat(afterDeposit.getAccountStatus(), equalTo(AccountStatus.OPEN));
  }
}

