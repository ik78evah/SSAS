package berlin.tu.ssas.group8.model.rules;

import berlin.tu.ssas.group8.model.intersection.StatesContainer;
import lombok.NonNull;

public interface TrafficLightRule {


    /**
     * validates if the rule is adhered to
     *
     * @param sc StatesContainer containing the TrafficLightState constellation of an intersection
     * @return true, if the rule is adhered to, false if not.
     */
    boolean isValid(@NonNull StatesContainer sc);

    /**
     * retrieves the description of the rule
     *
     * @return String containing the description of the rule
     */

    String getDescription();

    /**
     * retrieves the short description of the rule
     *
     * @return String containing short the description of the rule
     */
    String getShortDescription();

}
