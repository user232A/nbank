package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.CreateUserModelRequest;

import static io.restassured.RestAssured.given;

public class AdminCreateUserRequest extends Request<CreateUserModelRequest>{

    public AdminCreateUserRequest(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(CreateUserModelRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("api/v1/admin/users")
                .then()
                .spec(responseSpecification);
    }
}
