package berlin.tu.ssas.group8.model.trafficLight;

import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This class represents the status of a traffic light by containing the id, time and light state
 */
@Getter
public class TrafficLightStatus {

    private final UUID id;
    private final LocalDateTime time;
    private final TrafficLightState state;

    /**
     * creates a traffic light status containing the id, light state and present time of the specified traffic light
     *
     * @param trafficLight The respective traffic light to which the status will be created, can't be null
     */
    public TrafficLightStatus(@NonNull TrafficLight trafficLight) {
        this.id = trafficLight.getId().getUuid();
        this.state = trafficLight.getState();
        this.time = LocalDateTime.now();
    }

}
