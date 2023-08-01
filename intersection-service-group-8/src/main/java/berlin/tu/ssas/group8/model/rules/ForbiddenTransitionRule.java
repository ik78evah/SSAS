package berlin.tu.ssas.group8.model.rules;

import berlin.tu.ssas.group8.model.intersection.StatesContainer;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLight;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class states and validates that the specified TrafficLight does not transition from one specified state to another
 * i.e. for a vehicleTrafficLight from green to red without a yellow phase
 */

@Getter
public class ForbiddenTransitionRule {
    private UUID tl;
    private TrafficLightState stateBefore;
    private TrafficLightState stateAfter;

    /**
     * Creates the ForbiddenTransitionRule
     *
     * @param id UUID of the respective TrafficLight
     * @param s1 forbidden starting TrafficLightState of the transition
     * @param s2 forbidden ending TrafficLightState of the transition
     */

    public ForbiddenTransitionRule(@NonNull UUID id, @NonNull TrafficLightState s1, @NonNull TrafficLightState s2) {

        this.tl = id;
        this.stateBefore = s1;
        this.stateAfter = s2;
    }

    /**
     * validates if the rule is adhered to by checking that the
     * starting TrafficLightState of the specified TrafficLight is not equal to the one in the registry
     * ending TrafficLightState of the specified TrafficLight is not equal to the one in the StatesContainer
     *
     * @param sc       StatesContainer containing the TrafficLightState constellation of an intersection
     * @param registry HashMap of UUID and their respective TrafficLights
     * @return true if transition is valid, false if transition id forbidden
     */

    public boolean isValid(@NonNull StatesContainer sc, @NonNull HashMap<UUID, @NonNull TrafficLight> registry) { //can we make it better?

        if (sc.getState(tl) == this.stateAfter
                && registry.get(tl).getState().equals(this.stateBefore))
            return false;
        else
            return true;
    }

    /**
     * retrieves the description of the rule
     *
     * @return String containing the description of the rule
     */
    public String getDescription() {
        return "This rule validates, that traffic light " + tl.toString() + " does not execute forbidden transition from " + stateBefore + " to " + stateAfter;
    }

    /**
     * retrieves the short description of the rule
     *
     * @return String containing short the description of the rule
     */
    public String getShortDescription() {
        return "Forbidden transition from " + stateBefore + " to " + stateAfter + " for " + tl.toString();
    }
}
