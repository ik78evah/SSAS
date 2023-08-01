package berlin.tu.ssas.group8;

import berlin.tu.ssas.group8.model.intersection.Intersection;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLight;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import io.quarkus.test.junit.QuarkusTest;

import javax.inject.Inject;
import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.core.Response;

import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.path.json.config.JsonPathConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class IntersectionAPITest {

    @Inject
    Intersection intersection;

    @BeforeAll
    public static void setup() {
        // from https://stackoverflow.com/questions/56114915/how-to-compare-assert-double-values-in-rest-assured
        // is needed to configure rest assured to handle the double values like we want
        // without this, the values are rounded and the tests will break
        JsonConfig jsonConfig = JsonConfig.jsonConfig()
                .numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE);
        RestAssured.config = RestAssured.config()
                .jsonConfig(jsonConfig);
    }

    @Test
    public void testStatusEndpointWithUntrustedCert() {

        RestAssured.keyStore("untrusted.p12", "password");
        RestAssured.trustStore("truststore.jks", "password");

        Exception exception = assertThrows(SSLHandshakeException.class, () -> {
            given().when().get("/intersection").then().statusCode(200)
            ;
        });

        String expectedMessage = "bad_certificate";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testGetAllTrafficLights() {
        RestAssured.keyStore("meta.p12", "password");
        RestAssured.trustStore("truststore.jks", "password");

        List<TrafficLight> trafficLights = intersection.getAllTrafficLights();
        for (int i = 0; i < trafficLights.size(); i++) {
            given().
                    when().get("/intersection/trafficLights")
                    .then()
                    .statusCode(200)
                    .body("$.size()", is(12),
                            "[" + i + "].id.uuid", is(trafficLights.get(i).getId().getUuid().toString()),
                            "[" + i + "].id.location.latitude", is(trafficLights.get(i).getId().getLocation().getLatitude()),
                            "[" + i + "].id.location.longitude", is(trafficLights.get(i).getId().getLocation().getLongitude()),
                            "[" + i + "].state", is(trafficLights.get(i).getState().name()));
        }
    }

    @Test
    void testTrafficLightGetByID() {
        RestAssured.keyStore("meta.p12", "password");
        RestAssured.trustStore("truststore.jks", "password");
        List<TrafficLight> trafficLights = intersection.getAllTrafficLights();
        for (TrafficLight tl : trafficLights) {
            given().
                    when().get("/intersection/trafficLights/" + tl.getId().getUuid().toString())
                    .then()
                    .statusCode(200)
                    .body("id.uuid", is(tl.getId().getUuid().toString()),
                            "id.location.latitude", is(tl.getId().getLocation().getLatitude()),
                            "id.location.longitude", is(tl.getId().getLocation().getLongitude()),
                            "state", is(tl.getState().name()));
        }
    }

    @Test
    void testTrafficLightGetStatusByID() {
        RestAssured.keyStore("meta.p12", "password");
        RestAssured.trustStore("truststore.jks", "password");
        List<TrafficLight> trafficLights = intersection.getAllTrafficLights();
        for (TrafficLight tl : trafficLights) {
            given().
                    when().get("/intersection/trafficLights/" + tl.getId().getUuid().toString() + "/getStatus")
                    .then()
                    .statusCode(200)
                    .body("id", is(tl.getId().getUuid().toString()),
                            "state", is(tl.getState().name()));
        }
    }

    @Test
    void testTrafficLightGetStatusByInvalidID() {
        RestAssured.keyStore("meta.p12", "password");
        RestAssured.trustStore("truststore.jks", "password");
        given().
                when().get("/intersection/trafficLights/" + UUID.randomUUID().toString()) //assuming that random is invalid
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testGetInvalidIdStatus() {
        RestAssured.keyStore("meta.p12", "password");
        RestAssured.trustStore("truststore.jks", "password");
        given().
                when().get("/intersection/trafficLights/" + UUID.randomUUID().toString() + "/getStatus")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testGetIntersectionInfo() {
        RestAssured.keyStore("meta.p12", "password");
        RestAssured.trustStore("truststore.jks", "password");
        given().
                when().get("/intersection/")
                .then()
                .statusCode(200)
                .body(
                        "Info", is("Intersection of Main Street and First Avenue"),
                        "Latitude", is(intersection.getLatitude()),
                        "Longitude", is(intersection.getLongitude()),
                        "ID", is(intersection.getIntersectionId()),
                        "trafficLightsConnected", is(12),
                        "blockedUntil", is(intersection.getBlockedUntil())
                );
    }

    @Test
    void testGetIntersectionStatus() {
        RestAssured.keyStore("meta.p12", "password");
        RestAssured.trustStore("truststore.jks", "password");
        given().when().get("/intersection/status")
                .then()
                .statusCode(200)
                .body(containsString("connected")
                ).extract().response();
    }

    @Test
    void testGetCurrentTrafficLightStateFromId() {
        RestAssured.keyStore("meta.p12", "password");
        RestAssured.trustStore("truststore.jks", "password");
        List<TrafficLight> trafficLights = intersection.getAllTrafficLights();
        for (TrafficLight trafficLight : trafficLights) {
            given().when().get("/intersection/trafficLights/" + trafficLight.getId().getUuid().toString() + "/getState")
                    .then().statusCode(200)
                    .body(containsString(trafficLight.getState().toString()))
                    .extract()
                    .response();
        }
    }

//    @Test
//    void testRequestGreenLightMayor(){
//        List<TrafficLight> trafficLights = intersection1.getAllTrafficLights();
//        Optional<TrafficLight> trafficLight = trafficLights.stream().
//                filter(tl -> tl.getState() != TrafficLightState.GREEN).findFirst().filter(vtl -> vtl.getType() != TrafficLight.Type.PEDESTRIAN);
//
//        System.out.println(trafficLight.get().getId().getUuid());
//
//                given().when().get("/intersection/requestGreen/" + trafficLight.get().getId().getUuid().toString() + "/MAYOR")
//                        .then().statusCode(200);
//    }
//
//    @Test
//    void testRequestGreenLightEmergency(){
//        List<TrafficLight> trafficLights = intersection1.getAllTrafficLights();
//        Optional<TrafficLight> trafficLight = trafficLights.stream().
//                filter(tl -> tl.getState() != TrafficLightState.GREEN).findFirst().filter(vtl -> vtl.getType() != TrafficLight.Type.PEDESTRIAN);
//
//        System.out.println(trafficLight.get().getId().getUuid());
//        given().when().get("/intersection/requestGreen/" + trafficLight.get().getId().getUuid().toString() + "/EMERGENCY")
//                .then().statusCode(200);
//    }

}
