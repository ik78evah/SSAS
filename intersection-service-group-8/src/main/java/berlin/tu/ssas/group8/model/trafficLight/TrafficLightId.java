package berlin.tu.ssas.group8.model.trafficLight;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.UUID;

/**
 * This class contains the UUID and the geo-location of a traffic-light
 */
@Getter
@ToString
public class TrafficLightId {

    private final UUID uuid;
    private final Location location;

    /**
     * Creates a traffic light id with the specified UUID and Location
     *
     * @param uuid     Universally Unique Identifier (UUID) the TrafficLightId will have, can't be null
     * @param location Geo-location of the respective traffic light, can't be null
     */
    public TrafficLightId(@NonNull UUID uuid, @NonNull Location location) {
        this.uuid = uuid;
        this.location = location;
    }
}
