package berlin.tu.ssas.group8.model.trafficLight;

/**
 * This class represents a traffic light for pedestrians by inheriting from the traffic light super class
 */

public class PedestrianTrafficLight extends TrafficLight {

    /**
     * Creates a pedestrian traffic light with the specified Location and initial traffic-light light state
     *
     * @param location          Location representing the geo-location which the traffic light will have
     * @param trafficLightState Initial color state which the traffic-light's light will be set to
     */
    public PedestrianTrafficLight(Location location, TrafficLightState trafficLightState, TrafficLightPositionTag trafficLightPositionTag) {
        super(location, trafficLightState, Type.PEDESTRIAN, trafficLightPositionTag);
    }

    /**
     * Sets the traffic light's light state
     *
     * @param state Traffic light state to which the traffic light's light will be set to. Can't be yellow since
     *              it is a pedestrian traffic light
     * @throws IllegalArgumentException If the given {@link TrafficLightState} is yellow
     */
    @Override
    public void setState(TrafficLightState state) {
        if (state.equals(TrafficLightState.YELLOW)) {
            throw new IllegalArgumentException("YELLOW state is not possible for pedestrian lights");
        } else {
            super.setState(state);
        }
    }

}
