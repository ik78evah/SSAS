package berlin.tu.ssas.group8.client;

import berlin.tu.ssas.group8.model.intersection.TrafficLightStateChangeRequest;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLight;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightStatus;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;

@Singleton
@Path("/intersection")
@RegisterRestClient(configKey = "intersection_client1") //config -> application.properties
@RegisterClientHeaders
@Produces(MediaType.APPLICATION_JSON)
public interface IntersectionClient1 extends IntersectionClient {

    @GET
    JsonObject getInfo();

    @GET
    @Path("/status")
    Response checkStatus();

    @GET
    @Path("/trafficLights")
    List<TrafficLight> getTrafficLights();

    @GET
    @Path("trafficLights/{trafficLightId}")
    TrafficLight getTrafficLightByUuid(@PathParam("trafficLightId") String uuid);

    @GET
    @Path("trafficLights/{trafficLightId}/getStatus")
    TrafficLightStatus getTrafficLightStatus(@PathParam("trafficLightId") String uuid);

    @GET
    @Path("trafficLights/{trafficLightId}/getState")
    TrafficLightState getTrafficLightState(@PathParam("trafficLightId") String uuid);

    @GET
    @Path("requestGreen/{trafficLightId}/")
    LocalDateTime requestGreenLight(@PathParam("trafficLightId") String uuid);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("requestState/")
    LocalDateTime changeState(TrafficLightStateChangeRequest request);


    @GET
    @Path("statusAsRequest")
    TrafficLightStateChangeRequest statusAsRequest();
}
