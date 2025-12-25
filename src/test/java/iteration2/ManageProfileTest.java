package iteration2;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ManageProfileTest extends BaseIteration2Test {

    public static final String USER1_TOKEN = "Basic dXNlcjY6cGFzc0QyMzEyIQ==";

    @BeforeAll
    public static void setUp() {
        configureSetUp();
    }

    @Test
    public void userCanUpdateTheirName() {

        String name = "Bob Miller";

        Map<String, String> requestBody = Map.of("name", name);

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Profile updated successfully"))
                .body("customer.name", Matchers.equalTo(name));

        String actualName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        Assertions.assertEquals(name, actualName);
    }

    @Test
    public void userUnauthorizedCanNotUpdateName() {

        String name = "Bob Miller";

        Map<String, String> requestBody = Map.of("name", name);

        String expectedName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        String actualName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        Assertions.assertEquals(name, actualName);
    }

    @Test
    public void userCanNotUpdateNameWithIncorrectToken() {

        String name = "Bob Miller";

        Map<String, String> requestBody = Map.of("name", name);

        String expectedName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        given()
                .header("Authorization", "incorrectToken")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        String actualName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        Assertions.assertEquals(name, actualName);
    }

    @Test
    public void userCanNotUpdateTheirNameWithTwoSpaces() {

        String name = "Anna von Stern";
        String expectedMessage = "Name must contain two words with letters only";

        Map<String, String> requestBody = Map.of("name", name);

        String expectedName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        String actualMessage = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response().asString();

        String actualName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        Assertions.assertEquals(expectedMessage, actualMessage);
        Assertions.assertEquals(expectedName, actualName);
    }

    @Test
    public void userCanNotUpdateTheirNameWithNullValue() {

        Map<String, String> requestBody = new HashMap<>() {{
            put("name", null);
        }};

        String expectedName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        String actualName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        Assertions.assertEquals(expectedName, actualName);
    }

    @Test
    public void userCanNotUpdateTheirNameWithIntegerValue() {

        String expectedMessage = "Name must contain two words with letters only";

        Map<String, Integer> requestBody = new HashMap<>() {{
            put("name", 5);
        }};

        String expectedName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        String actualMessage = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response().asString();

        String actualName = given()
                .header("Authorization", USER1_TOKEN)
                .contentType(ContentType.JSON)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        Assertions.assertEquals(expectedMessage, actualMessage);
        Assertions.assertEquals(expectedName, actualName);
    }
}
