package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class TransferUserTest extends BaseIteration2Test {

    public static final String USER1_TOKEN = "Basic dXNlcjE6cGFzc0YzNSEx";
    public static final String USER2_TOKEN = "Basic dXNlcjI6cGFzc1lBNSU=";

    @BeforeAll
    public static void setUp() {
        configureSetUp();
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 544.45, 9999.99, 10000.00})
    public void userCanTransferWithValidAmountToOwnAccount(double transferAmount) {

        double depositAmount = 5000;

        // Create accounts
        int senderAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        int receiverAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // Make deposits to senderAccount
        Map<String, Number> requestBody = Map.of("id", senderAccountId, "balance", depositAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        // Get balances before transfer
        List<Account> accountListBeforeTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        // Make transfer
        Map<String, Number> transferBody = Map.of("senderAccountId", senderAccountId,
                "receiverAccountId", receiverAccountId, "amount", transferAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(transferBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Transfer successful"));

        // Get balances after transfer
        List<Account> accountListAfterTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        Assertions.assertEquals(senderBalanceAfterTransfer, senderBalanceBeforeTransfer - transferAmount, 0.001);
        Assertions.assertEquals(receiverBalanceAfterTransfer, receiverBalanceBeforeTransfer + transferAmount);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 544.45, 9999.99, 10000.00})
    public void userCanTransferWithValidAmountToAnotherUsersAccount(double transferAmount) {

        double depositAmount = 5000;

        // Create accounts
        int senderAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        int receiverAccountId = given()
                .header("Authorization", USER2_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // Make deposits to senderAccount
        Map<String, Number> requestBody = Map.of("id", senderAccountId, "balance", depositAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        // Get balances before transfer
        List<Account> accountsUser1BeforeTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        List<Account> accountsUser2BeforeTransfer = given()
                .header("Authorization", USER2_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceBeforeTransfer = accountsUser1BeforeTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceBeforeTransfer = accountsUser2BeforeTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        // Make transfer
        Map<String, Number> transferBody = Map.of("senderAccountId", senderAccountId,
                "receiverAccountId", receiverAccountId, "amount", transferAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(transferBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Transfer successful"));

        // Get balances after transfer
        List<Account> accountsUser1AfterTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        List<Account> accountsUser2AfterTransfer = given()
                .header("Authorization", USER2_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceAfterTransfer = accountsUser1AfterTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceAfterTransfer = accountsUser2AfterTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        Assertions.assertEquals(senderBalanceAfterTransfer, senderBalanceBeforeTransfer - transferAmount, 0.001);
        Assertions.assertEquals(receiverBalanceAfterTransfer, receiverBalanceBeforeTransfer + transferAmount);
    }

    @Test
    public void userCanNotTransferMoreLimit() {

        double depositAmount = 5000;
        double transferAmount = 10000.01;
        String expectedErrorMessage = "Transfer amount cannot exceed 10000";

        // Create accounts
        int senderAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        int receiverAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // Make deposits to senderAccount
        Map<String, Number> requestBody = Map.of("id", senderAccountId, "balance", depositAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        // Get balances before transfer
        List<Account> accountListBeforeTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        // Make transfer
        Map<String, Number> transferBody = Map.of("senderAccountId", senderAccountId,
                "receiverAccountId", receiverAccountId, "amount", transferAmount);

        String actualErrorMessage = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(transferBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response().asString();

        // Get balances after transfer
        List<Account> accountListAfterTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        Assertions.assertEquals(expectedErrorMessage, actualErrorMessage);
        Assertions.assertEquals(senderBalanceAfterTransfer, senderBalanceBeforeTransfer);
        Assertions.assertEquals(receiverBalanceAfterTransfer, receiverBalanceBeforeTransfer);
    }

    @Test
    public void userCanNotTransferWithNegativeAmount() {

        double depositAmount = 100;
        double transferAmount = -1;
        String expectedErrorMessage = "Transfer amount must be at least 0.01";

        // Create accounts
        int senderAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        int receiverAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // Make deposits to senderAccount
        Map<String, Number> requestBody = Map.of("id", senderAccountId, "balance", depositAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        // Get balances before transfer
        List<Account> accountListBeforeTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        // Make transfer
        Map<String, Number> transferBody = Map.of("senderAccountId", senderAccountId,
                "receiverAccountId", receiverAccountId, "amount", transferAmount);

        String actualErrorMessage = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(transferBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response().asString();

        // Get balances after transfer
        List<Account> accountListAfterTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        Assertions.assertEquals(expectedErrorMessage, actualErrorMessage);
        Assertions.assertEquals(senderBalanceAfterTransfer, senderBalanceBeforeTransfer);
        Assertions.assertEquals(receiverBalanceAfterTransfer, receiverBalanceBeforeTransfer);
    }

    @Test
    public void userCanNotTransferWithNullValue() {

        double depositAmount = 100;
        Double transferAmount = null;

        // Create accounts
        int senderAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        int receiverAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // Make deposits to senderAccount
        Map<String, Number> requestBody = Map.of("id", senderAccountId, "balance", depositAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        // Get balances before transfer
        List<Account> accountListBeforeTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        // Make transfer
        Map<String, Object> transferBody = new HashMap<>() {{
            put("senderAccountId", senderAccountId);
            put("receiverAccountId", receiverAccountId);
            put("amount", transferAmount);
        }};

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(transferBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        // Get balances after transfer
        List<Account> accountListAfterTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        Assertions.assertEquals(senderBalanceAfterTransfer, senderBalanceBeforeTransfer);
        Assertions.assertEquals(receiverBalanceAfterTransfer, receiverBalanceBeforeTransfer);
    }

    @Test
    public void userUnauthorizedCanNotTransfer() {

        double depositAmount = 100;
        double transferAmount = 20;

        // Create accounts
        int senderAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        int receiverAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // Make deposits to senderAccount
        Map<String, Number> requestBody = Map.of("id", senderAccountId, "balance", depositAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        // Get balances before transfer
        List<Account> accountListBeforeTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        // Make transfer
        Map<String, Number> transferBody = Map.of("senderAccountId", senderAccountId,
                "receiverAccountId", receiverAccountId, "amount", transferAmount);

        given()
                .contentType(ContentType.JSON)
                .body(transferBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        // Get balances after transfer
        List<Account> accountListAfterTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        Assertions.assertEquals(senderBalanceAfterTransfer, senderBalanceBeforeTransfer);
        Assertions.assertEquals(receiverBalanceAfterTransfer, receiverBalanceBeforeTransfer);
    }

    @Test
    public void userCanNotTransferWithIncorrectToken() {

        double depositAmount = 120;
        double transferAmount = 40;

        // Create accounts
        int senderAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        int receiverAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // Make deposits to senderAccount
        Map<String, Number> requestBody = Map.of("id", senderAccountId, "balance", depositAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        // Get balances before transfer
        List<Account> accountListBeforeTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        // Make transfer
        Map<String, Number> transferBody = Map.of("senderAccountId", senderAccountId,
                "receiverAccountId", receiverAccountId, "amount", transferAmount);

        given()
                .header("Authorization", "incorrectToken")
                .contentType(ContentType.JSON)
                .body(transferBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        // Get balances after transfer
        List<Account> accountListAfterTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        Assertions.assertEquals(senderBalanceAfterTransfer, senderBalanceBeforeTransfer);
        Assertions.assertEquals(receiverBalanceAfterTransfer, receiverBalanceBeforeTransfer);
    }

    @Test
    public void userCanNotTransferAmountExceedBalance() {

        double depositAmount = 130;
        double transferAmount = 130.01;
        String expectedErrorMessage = "Invalid transfer: insufficient funds or invalid accounts";

        // Create accounts
        int senderAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        int receiverAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // Make deposits to senderAccount
        Map<String, Number> requestBody = Map.of("id", senderAccountId, "balance", depositAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        // Get balances before transfer
        List<Account> accountListBeforeTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        // Make transfer
        Map<String, Number> transferBody = Map.of("senderAccountId", senderAccountId,
                "receiverAccountId", receiverAccountId, "amount", transferAmount);

        String actualErrorMessage = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(transferBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response().asString();

        // Get balances after transfer
        List<Account> accountListAfterTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        double receiverBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == receiverAccountId)
                .mapToDouble(Account::getBalance).sum();

        Assertions.assertEquals(expectedErrorMessage, actualErrorMessage);
        Assertions.assertEquals(senderBalanceAfterTransfer, senderBalanceBeforeTransfer);
        Assertions.assertEquals(receiverBalanceAfterTransfer, receiverBalanceBeforeTransfer);
    }

    @Test
    public void userCanNotTransferToNonExistentAccount() {

        double depositAmount = 130;
        double transferAmount = 110.00;
        int receiverAccountId = Integer.MAX_VALUE;
        String expectedErrorMessage = "Invalid transfer: insufficient funds or invalid accounts";

        // Create accounts
        int senderAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // Make deposits to senderAccount
        Map<String, Number> requestBody = Map.of("id", senderAccountId, "balance", depositAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        // Get balance before transfer
        List<Account> accountListBeforeTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        // Make transfer
        Map<String, Number> transferBody = Map.of("senderAccountId", senderAccountId,
                "receiverAccountId", receiverAccountId, "amount", transferAmount);

        String actualErrorMessage = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(transferBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response().asString();

        // Get balances after transfer
        List<Account> accountListAfterTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        Assertions.assertEquals(expectedErrorMessage, actualErrorMessage);
        Assertions.assertEquals(senderBalanceAfterTransfer, senderBalanceBeforeTransfer);
    }

    @Test
    public void userCanNotTransferTheSameAccount() {

        int receiverAccountId;
        double depositAmount = 5000;
        double transferAmount = 1000;

        // Create account
        int senderAccountId = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        receiverAccountId = senderAccountId;

        // Make deposits to senderAccount
        Map<String, Number> requestBody = Map.of("id", senderAccountId, "balance", depositAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(senderAccountId))
                .body("balance", Matchers.notNullValue());

        // Get balances before transfer
        List<Account> accountListBeforeTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceBeforeTransfer = accountListBeforeTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        // Make transfer
        Map<String, Number> transferBody = Map.of("senderAccountId", senderAccountId,
                "receiverAccountId", receiverAccountId, "amount", transferAmount);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(transferBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // Get balances after transfer
        List<Account> accountListAfterTransfer = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getList("", Account.class);

        double senderBalanceAfterTransfer = accountListAfterTransfer.stream()
                .filter(account -> account.getId() == senderAccountId)
                .mapToDouble(Account::getBalance).sum();

        Assertions.assertEquals(senderBalanceAfterTransfer, senderBalanceBeforeTransfer);
    }
}