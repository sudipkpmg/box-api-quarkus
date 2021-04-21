package gov.tn.dhs.quarkus.box;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class StatusResourceTest {

    @Test
    public void testStatusEndpoint() {
        given()
                .when().get("/ecm-api/v1/status")
                .then()
                .statusCode(200)
                .body(is("ecm-api-quarkus service is running"));
    }

}