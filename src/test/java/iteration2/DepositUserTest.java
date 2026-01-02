package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class DepositUserTest extends BaseIteration2Test {

    public static final String USER1_TOKEN = "Basic dXNlcjE6cGFzc0YzNSEx";
    public static final String USER2_TOKEN = "Basic dXNlcjI6cGFzc1lBNSU=";

    @BeforeAll
    public static void setUp() {
        configureSetUp();
    }

    @Test
    public void userCanCreateAccountWithValidData() {

        Account expectedAccount = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", Matchers.notNullValue())
                .body("accountNumber", Matchers.notNullValue())
                .extract()
                .response().as(Account.class);

        List<Account> actaulAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        int expectedAccountId = expectedAccount.getId();

        Account actaulAccount = actaulAccountList.stream()
                .filter(account -> account.getId() == expectedAccountId).toList().getFirst();

        Assertions.assertEquals(expectedAccount, actaulAccount);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 4999.99, 5000.00})
    public void userCanDepositWithValidAmount(double amount) {

        // Create an account
        int accountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        Map<String, Number> requestBody = Map.of("id", accountId, "balance", amount);

        // Make a deposit
        double balance = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(accountId))
                .extract()
                .jsonPath().getDouble("balance");

        double actualBalance = given()
                .pathParams("accountId", accountId)
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/accounts/{accountId}/transactions")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("[0].type", Matchers.equalTo("DEPOSIT"))
                .body("[0].relatedAccountId", Matchers.equalTo(accountId))
                .extract()
                .jsonPath().getDouble("[0].amount");

        List<Account> actaulAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        Account actaulAccount = actaulAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst();

        Assertions.assertEquals(amount, balance);
        Assertions.assertEquals(amount, actualBalance);
        Assertions.assertEquals(amount, actaulAccount.getBalance());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -50, -725, -10})
    public void userCanNotDepositWithNegativeAmount(double amount) {

        String expectedMessage = "Deposit amount must be at least 0.01";

        // Create an account
        int accountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        List<Account> expectedAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double expectedBalance = expectedAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Map<String, Number> requestBody = Map.of("id", accountId, "balance", amount);

        // Make a deposit
        String actualMessage = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response().asString();

        List<Account> actualAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double actualBalance = actualAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Assertions.assertEquals(expectedMessage, actualMessage);
        Assertions.assertEquals(expectedBalance, actualBalance);
    }

    @ParameterizedTest
    @ValueSource(doubles = {5000.01, 8000, 7252, 10000})
    public void userCanNotDepositWhenAmountExceedsLimit(double amount) {

        String expectedMessage = "Deposit amount cannot exceed 5000";

        // Create an account
        int accountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        List<Account> expectedAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double expectedBalance = expectedAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Map<String, Number> requestBody = Map.of("id", accountId, "balance", amount);

        // Make a deposit
        String actualMessage = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response().asString();

        List<Account> actualAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double actualBalance = actualAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Assertions.assertEquals(expectedMessage, actualMessage);
        Assertions.assertEquals(expectedBalance, actualBalance);
    }

    @Test
    public void userCanNotDepositWithNullValue() {

        // Create an account
        Integer accountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        List<Account> expectedAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double expectedBalance = expectedAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Map<String, Object> requestBody = new HashMap<>() {{
            put("id", accountId);
            put("balance", null);
        }};

        // Make a deposit
        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        List<Account> actualAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double actualBalance = actualAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Assertions.assertEquals(expectedBalance, actualBalance);
    }

    @Test
    public void userCanNotDepositToAnotherAccount() {

        String expectedMessage = "Unauthorized access to account";
        Integer amount = 125;

        // Create an account for user2
        Integer accountId = given()
                .header("Authorization", USER2_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        List<Account> expectedAccountList = given()
                .header("Authorization", USER2_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double expectedBalance = expectedAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Map<String, Number> requestBody = Map.of("id", accountId, "balance", amount);

        // Make a deposit
        String actualMessage = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .extract()
                .response().asString();

        List<Account> actualAccountList = given()
                .header("Authorization", USER2_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double actualBalance = actualAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Assertions.assertEquals(expectedMessage, actualMessage);
        Assertions.assertEquals(expectedBalance, actualBalance);
    }

    @Test
    public void userUnauthorizedCanNotDeposit() {

        // Create an account
        Integer accountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        List<Account> expectedAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double expectedBalance = expectedAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Map<String, Number> requestBody = Map.of("id", accountId, "balance", 125);

        // Make a deposit
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        List<Account> actualAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double actualBalance = actualAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Assertions.assertEquals(expectedBalance, actualBalance);
    }

    @Test
    public void userCanNotDepositWithIncorrectToken() {

        // Create an account
        Integer accountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        List<Account> expectedAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double expectedBalance = expectedAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Map<String, Number> requestBody = Map.of("id", accountId, "balance", 125);

        // Make a deposit
        given()
                .header("Authorization", "incorrectToken")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        List<Account> actualAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        double actualBalance = actualAccountList.stream()
                .filter(account -> account.getId() == accountId).toList().getFirst().getBalance();

        Assertions.assertEquals(expectedBalance, actualBalance);
    }

    @Test
    public void userCanNotDepositToNonExistentAccount() {

        Integer amount = 125;

        List<Account> expectedAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        Map<String, Number> requestBody = Map.of("id", Integer.MAX_VALUE, "balance", amount);

        // Make a deposit
        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        List<Account> actualAccountList = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getList("", Account.class);

        List<Account> sortedExpectedAccountList = expectedAccountList.stream()
                .sorted(Comparator.comparing(Account::getId)).toList();
        List<Account> sortedActualAccountList = actualAccountList.stream()
                .sorted(Comparator.comparing(Account::getId)).toList();

        Assertions.assertEquals(sortedExpectedAccountList, sortedActualAccountList);
    }
}
