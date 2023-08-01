package berlin.tu.ssas.group8.model.rules;

import berlin.tu.ssas.group8.model.intersection.StatesContainer;
import berlin.tu.ssas.group8.model.intersection.TrafficLightStateChangeRequest;
import berlin.tu.ssas.group8.model.trafficLight.*;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ForbiddenTransitionRuleTest {

    @Test
    void testIsValid(){
        double lo = 56.00020;
        double la = 57.00020;
        {
            //assertFalse: forbidden transition for vehicleTrafficLight from green to Red
            VehicleTrafficLight vtl1 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
            StatesContainer statesContainer1 = new StatesContainer();
            HashMap<UUID, TrafficLight> map1 = new HashMap<>();
            map1.put(vtl1.getId().getUuid(), vtl1);
            statesContainer1.addState(vtl1.getId().getUuid(), TrafficLightState.RED);
            ForbiddenTransitionRule forbiddenTransitionRule = new ForbiddenTransitionRule(vtl1.getId().getUuid(), TrafficLightState.GREEN, TrafficLightState.RED);
            assertFalse(forbiddenTransitionRule.isValid(statesContainer1, map1));
        }
        {
            //assertFalse: forbidden transition for vehicleTrafficLight from red to green
            VehicleTrafficLight vtl2 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
            StatesContainer statesContainer2 = new StatesContainer();
            HashMap<UUID, TrafficLight> map2 = new HashMap<>();
            map2.put(vtl2.getId().getUuid(), vtl2);
            statesContainer2.addState(vtl2.getId().getUuid(), TrafficLightState.GREEN);
            ForbiddenTransitionRule forbiddenTransitionRule2 = new ForbiddenTransitionRule(vtl2.getId().getUuid(), TrafficLightState.RED, TrafficLightState.GREEN);
            assertFalse(forbiddenTransitionRule2.isValid(statesContainer2, map2));
        }
        {
            //assertFalse: forbidden transition for pedestrianTrafficLight from green to red_yellow
            PedestrianTrafficLight ptl1 = new PedestrianTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
            StatesContainer statesContainer3 = new StatesContainer();
            HashMap<UUID, TrafficLight> map3 = new HashMap<>();
            map3.put(ptl1.getId().getUuid(), ptl1);
            statesContainer3.addState(ptl1.getId().getUuid(), TrafficLightState.RED_YELLOW);
            ForbiddenTransitionRule forbiddenTransitionRule3 = new ForbiddenTransitionRule(ptl1.getId().getUuid(), TrafficLightState.GREEN, TrafficLightState.RED_YELLOW);
            assertFalse(forbiddenTransitionRule3.isValid(statesContainer3, map3));
        }
        {
            //assertTrue: transition from green to yellow while active forbiddenTransition from green to red
            VehicleTrafficLight vtl3 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
            StatesContainer statesContainer4 = new StatesContainer();
            HashMap<UUID, TrafficLight> map4 = new HashMap<>();
            map4.put(vtl3.getId().getUuid(), vtl3);
            statesContainer4.addState(vtl3.getId().getUuid(), TrafficLightState.YELLOW);
            ForbiddenTransitionRule forbiddenTransitionRule = new ForbiddenTransitionRule(vtl3.getId().getUuid(), TrafficLightState.GREEN, TrafficLightState.RED);
            assertTrue(forbiddenTransitionRule.isValid(statesContainer4, map4));
        }

    }

    @Test
    void testGetDescription(){
        double lo = 56.00020;
        double la = 57.00020;
        VehicleTrafficLight vtl1 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        StatesContainer statesContainer1 = new StatesContainer();
        statesContainer1.addState(vtl1.getId().getUuid(), TrafficLightState.RED);
        ForbiddenTransitionRule forbiddenTransitionRule = new ForbiddenTransitionRule(vtl1.getId().getUuid(), TrafficLightState.GREEN, TrafficLightState.RED);
        assertEquals("This rule validates, that traffic light " + forbiddenTransitionRule.getTl() + " does not execute forbidden transition from "
                + forbiddenTransitionRule.getStateBefore() + " to " + forbiddenTransitionRule.getStateAfter(), forbiddenTransitionRule.getDescription());
    }

    @Test
    void testGetShortDescription(){
        double lo = 56.00020;
        double la = 57.00020;
        VehicleTrafficLight vtl1 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        StatesContainer statesContainer1 = new StatesContainer();
        statesContainer1.addState(vtl1.getId().getUuid(), TrafficLightState.RED);
        ForbiddenTransitionRule forbiddenTransitionRule = new ForbiddenTransitionRule(vtl1.getId().getUuid(), TrafficLightState.GREEN, TrafficLightState.RED);
        assertEquals("Forbidden transition from " +forbiddenTransitionRule.getStateBefore() + " to " + forbiddenTransitionRule.getStateAfter() + " for " + forbiddenTransitionRule.getTl(),
                forbiddenTransitionRule.getShortDescription());
    }

}