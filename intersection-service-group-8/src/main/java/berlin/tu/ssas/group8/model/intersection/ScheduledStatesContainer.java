package berlin.tu.ssas.group8.model.intersection;

import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;


/**
 * This class holds all trafficLights and the respective TrafficLightState constellation of an intersection for a given amount of time
 */
@Getter
public class ScheduledStatesContainer {

    private StatesContainer container;
    private String name;
    private int duration;
    private LocalDateTime end;
    private boolean started;

    /**
     * creates a ScheduledStatesContainer with the specified StatesContainer, name and duration in seconds
     *
     * @param sc          contains all he trafficLights and a predetermined trafficLightstate constellation for the whole intersection
     * @param name        name/description of the container
     * @param durationSec the duration in seconds this particular TrafficLightState constellation should be active
     */
    public ScheduledStatesContainer(@NonNull StatesContainer sc, @NonNull String name, int durationSec) {
        if (durationSec <= 0)
            throw new IllegalArgumentException("Container " + name + " has not acceptable duration of " + durationSec + "s");
        this.container = sc.copy();
        this.duration = durationSec;
        this.name = name;
    }

    /**
     * activates the countdown of the duration
     */
    public void start() {
        started = true;
        end = LocalDateTime.now().plusSeconds(duration);
    }

    /**
     * checks if the duration has expired
     *
     * @return a boolean indicating if the duration has expired
     */
    public boolean isExpired() {
        return end.isBefore(LocalDateTime.now());
    }

    public LocalDateTime expiresAt() {
        return end;
    }
}
