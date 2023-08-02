package berlin.tu.ssas.group8.model.rules;

import berlin.tu.ssas.group8.model.intersection.StatesContainer;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * This class states and validates that the specified TrafficLight can not have the specified TrafficLightState i.e. pedestrian traffic light has no YELLOW state
 */

@Getter
public class ForbiddenStateRule implements TrafficLightRule {
    private UUID tl;
    private TrafficLightState forbiddenState;

    /**
     * Creates a forbiddenStateRule
     *
     * @param id    UUID of the trafficLight to which the rule should apply
     * @param state TrafficLightState which should be forbidden
     */

    public ForbiddenStateRule(@NonNull UUID id, @NonNull TrafficLightState state) {

        this.tl = id;
        this.forbiddenState = state;
    }

    /**
     * validates if the rule is adhered to
     *
     * @param sc StatesContainer containing the TrafficLightState constellation of an intersection
     * @return true, if the rule is adhered to, false if not.
     */
    @Override
    public boolean isValid(@NonNull StatesContainer sc) {
        return !sc.getState(tl).equals(forbiddenState);
    }

    /**
     * retrieves the description of the rule
     *
     * @return String containing the description of the rule
     */
    @Override
    public String getDescription() {
        return "This rule validates, that traffic light " + tl.toString() + " does not have state " + this.forbiddenState;
    }

    /**
     * retrieves the short description of the rule
     *
     * @return String containing short the description of the rule
     */
    @Override
    public String getShortDescription() {
        return "Forbidden state " + this.forbiddenState + " for " + this.tl.toString();
    }


}
