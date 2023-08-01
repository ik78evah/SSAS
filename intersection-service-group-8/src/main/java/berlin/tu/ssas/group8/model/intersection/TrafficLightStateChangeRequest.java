package berlin.tu.ssas.group8.model.intersection;

import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

/**
 * This class extends the StatesContainer and holds all trafficLights and the respective TrafficLightState constellation
 * to which an intersection should switch to
 */
@Getter
public class TrafficLightStateChangeRequest extends StatesContainer {

    private LocalDateTime timestamp;

    private int minDuration;

    private UUID issuer;

    /**
     * creates a TrafficLightStateChangeRequest
     *
     * @param s        HashMap containing trafficLight UUIDs and their respective TrafficLightState
     * @param issuer   id of the subject, how sent the request
     * @param duration duration in minutes of how long this trafficLightState constellation should be active
     */
    @JsonCreator
    public TrafficLightStateChangeRequest(@NonNull @JsonProperty("states") HashMap<UUID, TrafficLightState> s,
                                          @JsonProperty("issuer") @NonNull UUID issuer,
                                          @JsonProperty("duration") int duration) {
        super();
        this.states = (HashMap<UUID, TrafficLightState>) s.clone();
        this.issuer = issuer;
        this.minDuration = duration;
        timestamp = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrafficLightStateChangeRequest that = (TrafficLightStateChangeRequest) o;
        return Objects.equals(issuer, that.issuer) && Objects.equals(states, that.states) &&
                Objects.equals(minDuration, that.minDuration);
    }

}
