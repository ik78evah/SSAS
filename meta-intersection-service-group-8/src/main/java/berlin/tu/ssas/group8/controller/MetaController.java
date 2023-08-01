package berlin.tu.ssas.group8.controller;

import berlin.tu.ssas.group8.client.*;
import berlin.tu.ssas.group8.model.intersection.TrafficLightStateChangeRequest;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLight;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightPositionTag;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.*;

import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/meta")
public class MetaController {

    @Inject
    @RestClient
    IntersectionClient1 intersectionClient1;

    @Inject
    @RestClient
    IntersectionClient2 intersectionClient2;

    @Inject
    @RestClient
    IntersectionClient3 intersectionClient3;

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    UUID metaIntersectionID = UUID.randomUUID();


    private static final Logger LOG = Logger.getLogger("Meta-Controller");

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfo() {
        Map<String, String> intersections = checkStatus();
        JsonObject info = new JsonObject()
                .put("Info", "This service serves as an intermediary between the three intersection services " +
                        "and the rest of the application")
                .put("Status Intersection1", intersections.get("Intersection1"))
                .put("Status Intersection2", intersections.get("Intersection2"))
                .put("Status Intersection3", intersections.get("Intersection3"));
        LOG.info("Meta Intersection Info requested");
        return Response.ok(info).build();
    }

    /**
     * This method will check if all intersection services are running and reachable
     *
     * @return A map with a specific intersection and its status (connected/disconnected)
     */
    public Map<String, String> checkStatus() {
        HashMap<String, String> intersections = new HashMap<>();
        List<IntersectionClient> intersectionClients = Arrays.asList
                (intersectionClient1, intersectionClient2, intersectionClient3);
        for (int i = 0; i < intersectionClients.size(); i++) {
            try {
                if (intersectionClients.get(i).checkStatus().getStatus() == 200) {
                    intersections.put("Intersection" + (i + 1), "connected");
                }
            } catch (ProcessingException e) {
                intersections.put("Intersection" + (i + 1), "disconnected");
            }
        }
        return intersections;
    }

    //Get info from intersect
    @GET
    @Path("/intersection/{intersectionNr}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfoIntersection(@PathParam("intersectionNr") int intersectionNr) {
        LOG.info("Request Info from Intersection " + intersectionNr);
        switch(intersectionNr){
            case 1: return Response.ok(intersectionClient1.getInfo()).build();
            case 2: return Response.ok(intersectionClient2.getInfo()).build();
            case 3: return Response.ok(intersectionClient3.getInfo()).build();
            default:{
                LOG.info("The requested intersection does not exist");
                return Response.status(404).build();
            }
        }
    }

    //get all traffic lights from intersection
    @GET
    @Path("/intersection/{intersectionNr}/trafficLights")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTrafficLights(@PathParam("intersectionNr") int intersectionNr) {
        LOG.info("Request all Traffic Lights from Intersection "+intersectionNr);
        switch(intersectionNr){
            case 1: return Response.ok(intersectionClient1.getTrafficLights()).build();
            case 2: return Response.ok(intersectionClient2.getTrafficLights()).build();
            case 3: return Response.ok(intersectionClient3.getTrafficLights()).build();
            default:{
                LOG.info("The requested intersection does not exist");
                return Response.status(404).build();
            }
        }
    }

    //get specific traffic light from intersection
    @GET
    @Path("/intersection/{intersectionNr}/trafficLights/{trafficLightId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTrafficLights(@PathParam("intersectionNr") int intersectionNr,@PathParam("trafficLightId") String uuid) {
        LOG.info("Request Traffic Light: " + uuid + " from Intersection "+intersectionNr);
        switch(intersectionNr){
            case 1: return Response.ok(intersectionClient1.getTrafficLightByUuid(uuid)).build();
            case 2: return Response.ok(intersectionClient2.getTrafficLightByUuid(uuid)).build();
            case 3: return Response.ok(intersectionClient3.getTrafficLightByUuid(uuid)).build();
            default:{
                LOG.info("The requested intersection does not exist");
                return Response.status(404).build();
            }
        }
    }

    //get status from specific traffic light
    @GET
    @Path("/intersection/{intersectionNr}/trafficLights/{trafficLightId}/getStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTrafficLightStatus(@PathParam("intersectionNr") int intersectionNr,@PathParam("trafficLightId") String uuid) {
        System.out.println("intersectionNR " + intersectionNr);
        System.out.println("uuid " + uuid);
        LOG.info("Request Status from Traffic Light: " + uuid + " from Intersection "+ intersectionNr);
        switch(intersectionNr){
            case 1: return Response.ok(intersectionClient1.getTrafficLightStatus(uuid)).build();
            case 2: return Response.ok(intersectionClient2.getTrafficLightStatus(uuid)).build();
            case 3: return Response.ok(intersectionClient3.getTrafficLightStatus(uuid)).build();
            default:{
                LOG.info("The requested intersection does not exist");
                return Response.status(404).build();
            }
        }
    }

    //get state from specific traffic light
    @GET
    @Path("/intersection/{intersectionNr}/trafficLights/{trafficLightId}/getState")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTrafficLightState(@PathParam("intersectionNr") int intersectionNr,@PathParam("trafficLightId") String uuid) {
        LOG.info("Request State from Traffic Light: " + uuid + " from Intersection "+intersectionNr);
        switch(intersectionNr){
            case 1: return Response.ok(intersectionClient1.getTrafficLightState(uuid)).build();
            case 2: return Response.ok(intersectionClient2.getTrafficLightState(uuid)).build();
            case 3: return Response.ok(intersectionClient3.getTrafficLightState(uuid)).build();
            default:{
                LOG.info("The requested intersection does not exist");
                return Response.status(404).build();
            }
        }
    }

    //change status on a given intersection
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("intersection/{intersectionNr}/requestState/")
    @RolesAllowed({"tmc"})
    public Response changeStateIntersection(@PathParam("intersectionNr") int intersectionNr,TrafficLightStateChangeRequest request) {
        LOG.info("Request for a state change for the intersection " +intersectionNr);
        switch(intersectionNr){
            case 1: return Response.ok(intersectionClient1.changeState(request)).build();
            case 2: return Response.ok(intersectionClient2.changeState(request)).build();
            case 3: return Response.ok(intersectionClient3.changeState(request)).build();
            default:{
                LOG.info("The requested intersection does not exist");
                return Response.status(404).build();
            }
        }
    }

    //Request green light at a given intersection
    @GET
    @Path("/intersection/{intersectionNr}/requestGreen/{trafficLightId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"emergency-vehicle", "mayor"})
    public Response requestGreenAtIntersection(@PathParam("intersectionNr") int intersectionNr,@PathParam("trafficLightId") String uuid) {
        LOG.info("Request green for Traffic Light: " + uuid + " from Intersection "+intersectionNr);
        switch(intersectionNr){
            case 1: return Response.ok(intersectionClient1.requestGreenLight(uuid)).build();
            case 2: return Response.ok(intersectionClient2.requestGreenLight(uuid)).build();
            case 3: return Response.ok(intersectionClient3.requestGreenLight(uuid)).build();
            default:{
                LOG.info("The requested intersection does not exist");
                return Response.status(404).build();
            }
        }
    }

    //getting status for request
    @GET
    @Path("/intersection/{intersectionNr}/statusAsRequest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response statusAsRequest(@PathParam("intersectionNr") int intersectionNr) {
        LOG.info("Request status as request from Intersection "+intersectionNr);
        switch(intersectionNr){
            case 1: return Response.ok(intersectionClient1.statusAsRequest()).build();
            case 2: return Response.ok(intersectionClient2.statusAsRequest()).build();
            case 3: return Response.ok(intersectionClient3.statusAsRequest()).build();
            default:{
                LOG.info("The requested intersection does not exist");
                return Response.status(404).build();
            }
        }
    }

    @GET
    @Path("/greenWaveEW")
    public Response triggerGreenWaveEastWest(){
        LOG.info("Green Wave for the east-west requested.");
        List<TrafficLight> trafficLightsIntersection1 = intersectionClient1.getTrafficLights();
        List<TrafficLight> trafficLightsIntersection2 = intersectionClient2.getTrafficLights();
        List<TrafficLight> trafficLightsIntersection3 = intersectionClient3.getTrafficLights();
        int minDuration = 10;

        List<TrafficLightStateChangeRequest> requests = Arrays.asList(
                new TrafficLightStateChangeRequest(new HashMap<>(), metaIntersectionID, minDuration),
                new TrafficLightStateChangeRequest(new HashMap<>(), metaIntersectionID, minDuration),
                new TrafficLightStateChangeRequest(new HashMap<>(), metaIntersectionID, minDuration)
        );
        List<List<TrafficLight>> intersections = Arrays.asList(trafficLightsIntersection1, trafficLightsIntersection2, trafficLightsIntersection3);
        List<IntersectionClient> clients = Arrays.asList(intersectionClient1, intersectionClient2, intersectionClient3);

        for(int i = 0; i < intersections.size(); i++){
            for(TrafficLight tfl : intersections.get(i)){
                if((tfl.getType() == TrafficLight.Type.VEHICLE)&& (tfl.getTrafficLightPositionTag() == TrafficLightPositionTag.EAST || tfl.getTrafficLightPositionTag() == TrafficLightPositionTag.WEST)){
                    requests.get(i).addState(tfl.getId().getUuid(), TrafficLightState.GREEN);
                }else{
                    requests.get(i).addState(tfl.getId().getUuid(), TrafficLightState.RED);
                }
            }
            try {
                clients.get(i).changeState(requests.get(i));
            }catch(Exception e){
                Log.info("Call to intersection "+i+" failed. Error-msg: "+e.getMessage());
                return Response.status(Response.Status.CONFLICT.getStatusCode(), "Request to intersection failed.").build();
            }
        }
        return Response.ok().build();
    }

}
