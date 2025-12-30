package iteration1;

import generators.RandomData;
import io.restassured.http.ContentType;
import models.CreateUserModelRequest;
import models.CreateUserModelResponse;
import models.UserRole;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequest;
import requests.UserCreateAccountRequest;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateUserTest extends BaseTest {

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // username field validation
                Arguments.of("   ", "Password33$", "USER", "username", List.of("Username cannot be blank", "Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of("ab", "Password33$", "USER", "username", List.of("Username must be between 3 and 15 characters")),
                Arguments.of("abc$", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of("abc%", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots"))
        );

    }

    @Test
    public void adminCanCreateUserWithCorrectData() {

        CreateUserModelRequest createUserModelRequest = CreateUserModelRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserModelResponse createUserModelResponse = new AdminCreateUserRequest(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreatedWithRoleUser())
                .post(createUserModelRequest)
                .extract()
                .response().as(CreateUserModelResponse.class);

        softly.assertThat(createUserModelRequest.getUsername()).isEqualTo(createUserModelResponse.getUsername());
        softly.assertThat(createUserModelRequest.getPassword()).isNotEqualTo(createUserModelResponse.getPassword());
        softly.assertThat(createUserModelRequest.getRole()).isEqualTo(createUserModelResponse.getRole());

        // Сделать get запрос и глянуть профиль пользователя
    }

    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void adminCanNotCreateUserWithIncorrectData(String name, String password, String role, String errorKey,
                                                       List<String> errorValue) {

        CreateUserModelRequest createUserModelRequest = CreateUserModelRequest.builder()
                .username(name)
                .password(password)
                .role(role)
                .build();

        new AdminCreateUserRequest(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserModelRequest);
    }

    @Test
    public void adminCanNotCreateExistsUser() {

        CreateUserModelRequest userModelRequest = CreateUserModelRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequest(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(userModelRequest);

        String expectedErrorMessage = "Error: Username '" + userModelRequest.getUsername() + "' already exists.";

        String actualErrorMessage = new AdminCreateUserRequest(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequest())
                .post(userModelRequest)
                .extract()
                .response().asString();

        softly.assertThat(expectedErrorMessage).isEqualTo(actualErrorMessage);
    }
}
