package berlin.tu.ssas.group8.model.trafficLight;

/**
 * This class represents a traffic light for vehicles by inheriting from the traffic light super class
 */
public class VehicleTrafficLight extends TrafficLight {

    /**
     * Creates a vehicle traffic light with the specified Location and initial traffic-light light state
     *
     * @param location          Location representing the geo-location which the traffic light will have
     * @param trafficLightState Initial color state which the traffic-light's light will be set to
     */
    public VehicleTrafficLight(Location location, TrafficLightState trafficLightState, TrafficLightPositionTag trafficLightPositionTag) {
        super(location, trafficLightState, Type.VEHICLE, trafficLightPositionTag);
    }

    /**
     * Creates a vehicle traffic light with the specified id and initial traffic-light light state
     *
     * @param id                id which the traffic light will have
     * @param trafficLightState Initial color state which the traffic-light's light will be set to
     */
    public VehicleTrafficLight(TrafficLightId id, TrafficLightState trafficLightState, TrafficLightPositionTag trafficLightPositionTag) {
        super(id, trafficLightState, Type.VEHICLE, trafficLightPositionTag);
    }

    /**
     * Sets the color state of this traffic light's light to yellow
     */
    public void setStateToYellow() {
        setState(TrafficLightState.YELLOW);
    }

    /**
     * Sets the color state of this traffic light's light to red_yellow
     */
    public void setStateToRedYellow() {
        setState(TrafficLightState.RED_YELLOW);
    }

}
