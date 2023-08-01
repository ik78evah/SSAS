package berlin.tu.ssas.group8.model.trafficLight;

import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * This class represents a traffic light characterized by an ID and
 * Traffic Light State
 */
public abstract class TrafficLight {

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
     * @param t                         Type of this traffic light (i.e. pedestrian/vehicle) [needed for (de-)serialization purposes]
     * @param trafficLightPositionTag   position tag of the traffic light (north, east, south, west)
     */
    public TrafficLight(@NonNull TrafficLightId id, @NonNull TrafficLightState trafficLightState, @NonNull Type t, @NonNull TrafficLightPositionTag trafficLightPositionTag) {
        this.id = id;
        this.trafficLightState = trafficLightState;
        this.type = t;
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
     * Sets the color state of this traffic light's light
     *
     * @param state TrafficLightState Object representing the light state to which to change to
     */
    public void setState(@NonNull TrafficLightState state) {
        this.trafficLightState = state;
    }

    /**
     * Sets the color state of this traffic light's light to green
     */
    public void setStateToGreen() {
        setState(TrafficLightState.GREEN);
    }

    /**
     * Sets the color state of this traffic light's light to the emergency mode (hardware dependent)
     */
    public void setStateToEmergency() {
        setState(TrafficLightState.EMERGENCY);
    }

    /**
     * Sets the color state of this traffic light's light to red
     */
    public void setStateToRed() {
        setState(TrafficLightState.RED);
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
