package berlin.tu.ssas.group8.model.rules;

import berlin.tu.ssas.group8.model.intersection.StatesContainer;
import berlin.tu.ssas.group8.model.trafficLight.*;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ForbiddenStateRuleTest {

    @Test
    void testIsValid(){
        double lo = 56.00020;
        double la = 57.00020;

        {
            //vehicleTrafficLight cannot be green (only for test)
            VehicleTrafficLight vtl1 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
            ForbiddenStateRule forbiddenStateRule = new ForbiddenStateRule(vtl1.getId().getUuid(), TrafficLightState.GREEN);
            StatesContainer statesContainer1 = new StatesContainer();
            statesContainer1.addState(vtl1.getId().getUuid(), TrafficLightState.GREEN);
            assertFalse(forbiddenStateRule.isValid(statesContainer1));
        }
        {
            //vehicleTrafficLight can be green
            VehicleTrafficLight vtl2 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
            ForbiddenStateRule forbiddenStateRule2 = new ForbiddenStateRule(vtl2.getId().getUuid(), TrafficLightState.GREEN);
            StatesContainer statesContainer2 = new StatesContainer();
            statesContainer2.addState(vtl2.getId().getUuid(), TrafficLightState.RED);
            assertTrue(forbiddenStateRule2.isValid(statesContainer2));
        }
        {
            //pedestrianTrafficLight cannot be yellow
            PedestrianTrafficLight ptl1 = new PedestrianTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.YELLOW, TrafficLightPositionTag.SOUTH);
            ForbiddenStateRule forbiddenStateRule3 = new ForbiddenStateRule(ptl1.getId().getUuid(), TrafficLightState.YELLOW);
            StatesContainer statesContainer3 = new StatesContainer();
            statesContainer3.addState(ptl1.getId().getUuid(), TrafficLightState.YELLOW);
            assertFalse(forbiddenStateRule3.isValid(statesContainer3));
        }
        {
            //pedestrianTrafficLight cannot be red_yellow
            PedestrianTrafficLight ptl2 = new PedestrianTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.YELLOW, TrafficLightPositionTag.SOUTH);
            ForbiddenStateRule forbiddenStateRule4 = new ForbiddenStateRule(ptl2.getId().getUuid(), TrafficLightState.RED_YELLOW);
            StatesContainer statesContainer4 = new StatesContainer();
            statesContainer4.addState(ptl2.getId().getUuid(), TrafficLightState.RED_YELLOW);
            assertFalse(forbiddenStateRule4.isValid(statesContainer4));
        }
    }

    @Test
    void getDescription(){
        double lo = 56.00020;
        double la = 57.00020;
        VehicleTrafficLight vtl1 = new VehicleTrafficLight(new Location(la+0.00001, lo+0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        ForbiddenStateRule forbiddenStateRule = new ForbiddenStateRule(vtl1.getId().getUuid(), vtl1.getState());
        assertEquals("This rule validates, that traffic light " +forbiddenStateRule.getTl() + " does not have state " +forbiddenStateRule.getForbiddenState(),forbiddenStateRule.getDescription());
    }

    @Test
    void getShortDescription(){
        double lo = 56.00020;
        double la = 57.00020;
        VehicleTrafficLight vtl1 = new VehicleTrafficLight(new Location(la+0.00001, lo+0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        ForbiddenStateRule forbiddenStateRule = new ForbiddenStateRule(vtl1.getId().getUuid(), vtl1.getState());
        assertEquals("Forbidden state " + forbiddenStateRule.getForbiddenState() + " for " + forbiddenStateRule.getTl(), forbiddenStateRule.getShortDescription());
    }

}