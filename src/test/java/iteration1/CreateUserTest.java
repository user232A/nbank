package iteration1;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateUserTest extends BaseTest {

    @BeforeAll
    public static void setUp() {
        configureLoggingFilters();
    }

    public static Stream<Arguments> userIncorrectData() {
        return Stream.of(
                Arguments.of("""
                        {
                          "username": "  ",
                          "password": "verysFd77$",
                          "role": "USER"
                        }
                        """),
                Arguments.of("""
                        {
                          "username": "al",
                          "password": "verysFd77$",
                          "role": "USER"
                        }
                        """),
                Arguments.of("""
                        {
                          "username": "abcd!Aefgh1er432",
                          "password": "verysFd77$",
                          "role": "USER"
                        }
                        """)
        );
    }

    @Test
    public void adminCanCreateUserWithCorrectData() {

        given()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "alex07",
                          "password": "verysFd77$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("role", equalTo("USER"));

    }

    @ParameterizedTest
    @MethodSource("userIncorrectData")
    public void adminCanNotCreateUserWithIncorrectData(String body) {

        given()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("username", Matchers.hasItem("Username must be between 3 and 15 characters"));

    }

    @Test
    public void adminCanNotCreateExistsUser() {

        given()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "alex06",
                          "password": "verysFd77$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("role", equalTo("USER"))
                .body("username", equalTo("alex06"));

        String error = given()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "alex06",
                          "password": "verysFd77$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response()
                .asString();

        Assertions.assertTrue(error.contains("Error"));
    }
}
