package berlin.tu.ssas.group8.model.trafficLight;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This ENUM represents the different light colors states a traffic light can have
 */
public enum TrafficLightState {
    RED,
    RED_YELLOW,
    YELLOW,
    GREEN,
    EMERGENCY
}
