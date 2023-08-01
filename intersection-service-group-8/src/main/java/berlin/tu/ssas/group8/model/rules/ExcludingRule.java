package berlin.tu.ssas.group8.model.rules;

import berlin.tu.ssas.group8.model.intersection.StatesContainer;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * This class states and validates that the two specified TrafficLight do not have the same TrafficLightState at the same time.
 */

@Getter
public class ExcludingRule implements TrafficLightRule {
    private UUID tl1;
    private UUID tl2;
    private TrafficLightState excludedState;


    /**
     * Creates a ExcludingRule
     *
     * @param t1    First TrafficLight
     * @param t2    Second TrafficLight
     * @param state TrafficLightState which the first and second TrafficLight can not have at the same time.
     */

    public ExcludingRule(@NonNull UUID t1, @NonNull UUID t2, @NonNull TrafficLightState state) {

        this.tl1 = t1;
        this.tl2 = t2;
        this.excludedState = state;
    }


    @Override
    public boolean isValid(@NonNull StatesContainer sc) {
        return !(sc.getState(tl1).equals(this.excludedState) &&
                sc.getState(tl2).equals(this.excludedState));
    }

    @Override
    public String getDescription() {
        return "This rule validates, that traffic lights " + tl1.toString() + " and " + tl2.toString()
                + " does not have state: " + this.excludedState + " at the same time";
    }

    @Override
    public String getShortDescription() {
        return "Exclude: " + tl1.toString() + " and " + tl2.toString() + " are " + excludedState;
    }


}
