package berlin.tu.ssas.group8.client;

import berlin.tu.ssas.group8.model.intersection.TrafficLightStateChangeRequest;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLight;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightStatus;
import io.vertx.core.json.JsonObject;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface IntersectionClient {

    JsonObject getInfo();


    Response checkStatus();


    List<TrafficLight> getTrafficLights();


    TrafficLight getTrafficLightByUuid(String uuid);


    TrafficLightStatus getTrafficLightStatus(String uuid);


    TrafficLightState getTrafficLightState(String uuid);


    LocalDateTime requestGreenLight(String uuid);


    LocalDateTime changeState(TrafficLightStateChangeRequest request);


    TrafficLightStateChangeRequest statusAsRequest();
}
