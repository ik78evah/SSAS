package berlin.tu.ssas.group8.model.trafficLight;

/**
 * This ENUM represents the different light colors states a traffic light can have
 */

public enum TrafficLightState {
    RED,
    RED_YELLOW,
    YELLOW,
    GREEN,
    EMERGENCY;

    public static TrafficLightState getTransition(TrafficLightState from, TrafficLightState to) {
        if (from.equals(to))
            return to;
        if (from.equals(TrafficLightState.GREEN)) {
            if (to.equals(TrafficLightState.RED_YELLOW))
                throw new IllegalArgumentException("Unsupported transition from GREEN to RED_YELLOW");
            else
                return TrafficLightState.YELLOW;
        } else if (from.equals(TrafficLightState.RED)) {
            if (to.equals(TrafficLightState.YELLOW))
                throw new IllegalArgumentException("Unsupported transition from RED to YELLOW");
            else if (to.equals(TrafficLightState.EMERGENCY))
                return TrafficLightState.EMERGENCY;
            else
                return TrafficLightState.RED_YELLOW;
        } else if (from.equals(TrafficLightState.YELLOW)) {
            if (to.equals(TrafficLightState.RED_YELLOW))
                throw new IllegalArgumentException("Unsupported transition from YELLOW to RED_YELLOW");
            else
                return to; //Could we also go from YELLOW to GREEN?
        } else if (from.equals(TrafficLightState.RED_YELLOW)) {
            if (to.equals(TrafficLightState.YELLOW))
                throw new IllegalArgumentException("Unsupported transition from RED_YELLOW to YELLOW");
            else
                return to;
        } else //Emergency. What should we do?
            throw new IllegalArgumentException("Unsupported transition from EMERGENCY to " + to);
    }
}
