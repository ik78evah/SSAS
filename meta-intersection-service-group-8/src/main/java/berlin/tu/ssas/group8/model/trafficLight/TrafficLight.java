package berlin.tu.ssas.group8.model.trafficLight;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * This class represents a traffic light characterized by an ID and
 * Traffic Light State
 */
public /*abstract*/ class TrafficLight {

    private TrafficLightState trafficLightState;
    private final TrafficLightId id;
    private final TrafficLightPositionTag trafficLightPositionTag;
    @Getter
    Type type;

    /**
     * Creates a traffic light with the specified id and initial light state
     *
     * @param id                        id containing the UUID and Location which the traffic light will have, can't be null
     * @param trafficLightState         Initial color state which the traffic-light's light will be set to, can't be null
     * @param type                      Type of this traffic light (i.e. pedestrian/vehicle)
     * @param trafficLightPositionTag   position tag of the traffic light (north, east, south, west)
     */
    @JsonCreator
    public TrafficLight(@NonNull @JsonProperty("id") TrafficLightId id,
                        @NonNull @JsonProperty("state") TrafficLightState trafficLightState,
                        @NonNull @JsonProperty("type") Type type,
                        @NonNull @JsonProperty("trafficLightPositionTag") TrafficLightPositionTag trafficLightPositionTag) {
        this.id = id;
        this.trafficLightState = trafficLightState;
        this.type = type;
        this.trafficLightPositionTag = trafficLightPositionTag;
    }

    /**
     * Creates a traffic light with the specified Location and initial light state
     *
     * @param location          Location representing the geo-location which the traffic light will have, can't be null
     * @param trafficLightState Initial color state which the traffic-light's light will be set to, can't be null
     */
    public TrafficLight(@NonNull Location location, @NonNull TrafficLightState trafficLightState, @NonNull Type t, @NonNull TrafficLightPositionTag trafficLightPositionTag) {
        UUID uuid = UUID.randomUUID();
        this.id = new TrafficLightId(uuid, location);
        this.trafficLightState = trafficLightState;
        this.type = t;
        this.trafficLightPositionTag = trafficLightPositionTag;
    }


    /**
     * Gets the id of this traffic light
     *
     * @return An id object containing the traffic lights UUID and geo-location
     */
    public TrafficLightId getId() {
        return this.id;
    }

    /**
     * Retrieves the status information of this traffic light
     *
     * @return A TrafficLightStatus Object containing the status information of this traffic light
     */
    public TrafficLightStatus checkStatus() {
        return new TrafficLightStatus(this);
    }

    /**
     * Gets the color state of this traffic light's light
     *
     * @return TrafficLightState Object representing the light state of this traffic light
     */
    public TrafficLightState getState() {
        return this.trafficLightState;
    }

    /**
     * @return Value of the traffic-light-positon-tag
     */
    public TrafficLightPositionTag getTrafficLightPositionTag(){
        return this.trafficLightPositionTag;
    }

    public enum Type {
        VEHICLE,
        PEDESTRIAN
    }

}
