package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

import java.util.List;

public class BaseIteration2Test {

    public static void configureSetUp() {
        RestAssured.baseURI = "http://localhost:4111";
        RestAssured.filters(
                List.of(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter()
                ));
    }
}
