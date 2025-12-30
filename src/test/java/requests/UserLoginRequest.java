package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.UserLoginModelRequest;

import static io.restassured.RestAssured.given;

public class UserLoginRequest extends Request<UserLoginModelRequest> {

    public UserLoginRequest(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(UserLoginModelRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/auth/login")
                .then()
                .spec(responseSpecification);
    }
}
