package iteration1;

import generators.RandomData;
import io.restassured.http.ContentType;
import models.CreateUserModelRequest;
import models.UserLoginModelRequest;
import models.UserRole;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequest;
import requests.UserCreateAccountRequest;
import requests.UserLoginRequest;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountWithValidData() {

        CreateUserModelRequest userModelRequest = CreateUserModelRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        UserLoginModelRequest userLoginModelRequest = UserLoginModelRequest.builder()
                .username(userModelRequest.getUsername())
                .password(userModelRequest.getPassword())
                .build();

        // create user
        new AdminCreateUserRequest(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreatedWithRoleUser())
                .post(userModelRequest);

        // get token
        String token = new UserLoginRequest(RequestSpecs.unAuthSpec(), ResponseSpecs.checkToken())
                .post(userLoginModelRequest)
                .extract().header("Authorization");

        // create account
        new UserCreateAccountRequest(RequestSpecs.userSpec(token), ResponseSpecs.entityWasCreated())
                .post(null);

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там
    }
}
