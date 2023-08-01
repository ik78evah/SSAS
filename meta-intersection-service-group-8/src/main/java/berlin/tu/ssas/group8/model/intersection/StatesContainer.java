package berlin.tu.ssas.group8.model.intersection;

import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import lombok.NonNull;

import java.util.*;

/**
 * This class contains a map of trafficLight UUIDs with their respective, predetermined TrafficLightstates
 */
public class StatesContainer {
    HashMap<UUID, TrafficLightState> states;

    /**
     * creates a StatesContainer
     */
    public StatesContainer() {
        this.states = new HashMap<>();
    }

    /**
     * adds a trafficLight UUID and a respective state to the StatesContainer
     *
     * @param id    contains the UUID of the specified trafficLight
     * @param state contains the trafficLightState of the specified trafficLight
     */
    public void addState(@NonNull UUID id, @NonNull TrafficLightState state) {
        this.states.put(id, state);
    }

    /**
     * gets the state of a specified trafficLight UUID
     *
     * @param id contains the UUID of the trafficLight for which the state should be retrieved
     * @return returns the TrafficLightState of the specified trafficLight UUID
     * @throws NoSuchElementException if the passed UUID does not match any UUID in the StatesContainer
     */
    public TrafficLightState getState(@NonNull UUID id) {
        if (states.containsKey(id))
            return states.get(id);
        else
            throw new NoSuchElementException("This container does not contain any traffic light with id " + id);
    }

    /**
     * retrieves all the trafficLight UUIDs with their respective TrafficLightstate
     *
     * @return a HashMap containing all the trafficLight UUIDs with their respective trafficLightState
     */
    public HashMap<UUID, TrafficLightState> getStates() {
        return (HashMap<UUID, TrafficLightState>) states.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatesContainer that = (StatesContainer) o;
        return Objects.equals(states, that.states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(states);
    }

    public StatesContainer copy() {
        StatesContainer sc = new StatesContainer();
        for (Map.Entry e : states.entrySet()) {
            sc.addState((UUID) e.getKey(), (TrafficLightState) e.getValue());
        }
        return sc;
    }
}
