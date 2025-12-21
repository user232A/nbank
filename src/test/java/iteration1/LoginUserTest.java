package iteration1;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginUserTest extends BaseTest {

    @BeforeAll
    public static void setUp() {
        configureLoggingFilters();
    }

    @Test
    public void adminCanGetAuthTokenTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "admin",
                          "password": "admin"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", notNullValue())
                .body("role", equalTo("ADMIN"))
                .body("username", equalTo("admin"));
    }

    @Test
    public void adminCanNotGetAuthTokenWithIncorrectUsernameTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "user",
                          "password": "admin"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("error", equalTo("Invalid username or password"));
    }

    @Test
    public void adminCanNotGetAuthTokenWithIncorrectPasswordTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "admin",
                          "password": "password"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("error", equalTo("Invalid username or password"));
    }

    @Test
    public void userCanGetAuthTokenTest() {

        given()
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "alex05",
                          "password": "verysFd55$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);


        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "alex05",
                          "password": "verysFd55$"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", notNullValue());

    }
}
