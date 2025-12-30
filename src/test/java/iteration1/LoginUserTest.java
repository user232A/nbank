package iteration1;

import generators.RandomData;
import models.CreateUserModelRequest;
import models.UserLoginModelRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequest;
import requests.UserLoginRequest;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGetAuthTokenTest() {

        UserLoginModelRequest userLoginModelRequest = UserLoginModelRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new UserLoginRequest(RequestSpecs.unAuthSpec(), ResponseSpecs.operationOk())
                .post(userLoginModelRequest);
    }

    @Test
    public void adminCanNotGetAuthTokenWithIncorrectUsernameTest() {

        UserLoginModelRequest userLoginModelRequest = UserLoginModelRequest.builder()
                .username(RandomData.getUsername())
                .password("admin")
                .build();

        new UserLoginRequest(RequestSpecs.unAuthSpec(), ResponseSpecs.unAuthorizedUser())
                .post(userLoginModelRequest);
    }

    @Test
    public void adminCanNotGetAuthTokenWithIncorrectPasswordTest() {

        UserLoginModelRequest userLoginModelRequest = UserLoginModelRequest.builder()
                .username("admin")
                .password(RandomData.getPassword())
                .build();

        new UserLoginRequest(RequestSpecs.unAuthSpec(), ResponseSpecs.unAuthorizedUser())
                .post(userLoginModelRequest);
    }

    @Test
    public void userCanGetAuthTokenTest() {

        CreateUserModelRequest userModelRequest = CreateUserModelRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequest(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreatedWithRoleUser())
                .post(userModelRequest);

        UserLoginModelRequest userLoginModelRequest = UserLoginModelRequest.builder()
                .username(userModelRequest.getUsername())
                .password(userModelRequest.getPassword())
                .build();

        new UserLoginRequest(RequestSpecs.unAuthSpec(), ResponseSpecs.checkToken())
                .post(userLoginModelRequest);
    }
}
