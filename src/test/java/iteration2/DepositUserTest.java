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
import java.util.Map;

import static io.restassured.RestAssured.given;

public class DepositUserTest extends BaseIteration2Test {

    public static final String USER1_TOKEN = "Basic dXNlcjQ6cGFzc0QyMyE=";
    public static final String USER2_TOKEN = "Basic dXNlcjU6cGFzc0QyMzExIQ==";

    @BeforeAll
    public static void setUp() {
        configureSetUp();
    }

    @Test
    public void userCanCreateAccountWithValidData() {
        double expectedBalance = 0.0;

        double actualBalance = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", Matchers.notNullValue())
                .body("accountNumber", Matchers.notNullValue())
                .extract()
                .jsonPath().getDouble("balance");

        Assertions.assertEquals(expectedBalance, actualBalance);
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


        Assertions.assertEquals(amount, balance);
        Assertions.assertEquals(amount, actualBalance);
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

        Assertions.assertEquals(expectedMessage, actualMessage);
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

        Assertions.assertEquals(expectedMessage, actualMessage);
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
    }

    @Test
    public void userCanNotDepositToAnotherAccount() {

        String expectedMessage = "Unauthorized access to account";

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

        Map<String, Number> requestBody = Map.of("id", accountId, "balance", 125);

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

        Assertions.assertEquals(expectedMessage, actualMessage);
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

        Map<String, Number> requestBody = Map.of("id", accountId, "balance", 125);

        // Make a deposit
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        // Можно убедиться что баланс не поменялся
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

        Map<String, Number> requestBody = Map.of("id", accountId, "balance", 125);

        // Make a deposit
        given()
                .header("Authorization", "incorrectToken")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        // Можно убедиться что баланс не поменялся
    }

    @Test
    public void userCanNotDepositToNonExistentAccount() {

        Map<String, Number> requestBody = Map.of("id", Integer.MAX_VALUE, "balance", 125);

        // Make a deposit
        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }
}
