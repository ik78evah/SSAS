package berlin.tu.ssas.group8.controller;

import berlin.tu.ssas.group8.model.intersection.Intersection;
import io.quarkus.security.identity.SecurityIdentity;
import berlin.tu.ssas.group8.model.intersection.TrafficLightStateChangeRequest;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import javax.enterprise.event.Observes;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Path("/intersection")
@Produces(MediaType.APPLICATION_JSON)
public class IntersectionController {

    @Inject
    Intersection intersection;

    @Inject
    @ConfigProperty(name = "intersectionID")
    int intersectionId;

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;


    private Logger log;

    /**
     * On start-up initialize the traffic light controller with the current
     * state of lights at the intersection.
     *
     * @param ev Quarkus informs with this event that the application is being started.
     */
    public void onStart(@Observes StartupEvent ev) {
        log = Logger.getLogger("Intersection-" + intersectionId + "-Controller");
        log.info("The application is starting...");
        intersection.initializeIntersection();

    }


    @GET
    public Response getIntersectionInfo() {
        log.info("Request info from Intersection " + intersectionId);
        return Response.ok(intersection.getInfo()).build();
    }

    @GET
    @Path("/status")
    public Response getIntersectionStatus() {
        return Response.ok("connected").build();
    }


    /**
     * This method produces a StateChangeRequest in JsonFormat, which can be used for POST method.
     * (Don’t forget to change some values)
     * <p>
     * curl -X POST -H "Content-Type: application/json" \
     * -d 'here_comes_the_request' \
     * localhost:8081/intersection/requestState/
     *
     * @return current intersection state in form of StateChangeRequest
     */
    @GET
    @Path("/statusAsRequest")
    public Response getStatus() {
        TrafficLightStateChangeRequest requestState = new TrafficLightStateChangeRequest(intersection.getCurrentState()
                .getStates(), UUID.randomUUID(), 5);
        return Response.ok(requestState).build();
    }


    @GET
    @Path("/trafficLights")
    public Response getTrafficLights() {
        log.info("Request all Traffic Lights");
        return Response.ok(intersection.getAllTrafficLights()).build();
    }

    @GET
    @Path("/trafficLights/{trafficLightId}")
    public Response getTrafficLightByUuid(@PathParam("trafficLightId") String uuid) {
        log.info("Request Traffic Light with ID: " + uuid);
        try {
            return Response.ok(intersection.getTrafficLight(UUID.fromString(uuid))).build();
        } catch (NoSuchElementException e) {
            log.error("Intersection does not have any traffic light with ID: " + uuid);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/trafficLights/{trafficLightId}/getState")
    public Response getCurrentTrafficLightState(@PathParam("trafficLightId") String uuid) {
        log.info("Request State from Traffic Light with ID: " + uuid);
        return Response.ok(intersection.getCurrentState().getState(UUID.fromString(uuid))).build();
    }

    @GET
    @Path("/trafficLights/{trafficLightId}/getStatus")
    public Response getCurrentTrafficLightStatus(@PathParam("trafficLightId") String uuid) {
        log.info("Request Status from Traffic Light with ID: " + uuid);
        try {
            return Response.ok(intersection.getTrafficLight(UUID.fromString(uuid)).checkStatus()).build();
        } catch (NoSuchElementException e) {
            log.error("Intersection does not have any traffic light with ID: " + uuid);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @RolesAllowed({"emergency-vehicle", "mayor"})
    @Path("requestGreen/{trafficLightId}")
    public Response requestGreenLight(@PathParam("trafficLightId") String uuid) {
        log.info("Requested green light for traffic light: " + uuid);
        String issuer;
        Set<String> roles = identity.getRoles();
        if (roles.contains("emergency-vehicle")) {
            issuer = "emergency-vehicle";
        } else if (roles.contains("mayor")) {
            issuer = "mayor";
        } else {
            issuer = "autonomous-vehicle";
        }
        try {
            LocalDateTime blocked = intersection.setGreenLight(UUID.fromString(uuid), issuer);
            log.info("Green light request accepted");
            return Response.ok(blocked).build();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"tmc"})
    @Path("requestState/")
    public Response changeState(TrafficLightStateChangeRequest request) {
        log.info("Requested to change intersection state");
        try {
            LocalDateTime blocked = intersection.setTrafficLightState(request);
            return Response.ok(blocked).build();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }




}
