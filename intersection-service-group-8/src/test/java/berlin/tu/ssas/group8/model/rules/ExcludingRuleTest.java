package berlin.tu.ssas.group8.model.rules;

import berlin.tu.ssas.group8.model.intersection.Intersection;
import berlin.tu.ssas.group8.model.intersection.StatesContainer;
import berlin.tu.ssas.group8.model.trafficLight.*;
import berlin.tu.ssas.group8.model.utilities.IntersectionDataProvider;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.validation.constraints.AssertFalse;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
class ExcludingRuleTest {

    @Test
    void testIsValid() {
        double lo = 56.00020;
        double la = 57.00020;
        {
            //assertFalse: both trafficLight cannot be green at the same time
            VehicleTrafficLight vtl1 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
            VehicleTrafficLight vtl2 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.NORTH);
            StatesContainer statesContainer1 = new StatesContainer();
            statesContainer1.addState(vtl1.getId().getUuid(), TrafficLightState.GREEN);
            statesContainer1.addState(vtl2.getId().getUuid(), TrafficLightState.GREEN);
            ExcludingRule excludingRule = new ExcludingRule(vtl1.getId().getUuid(), vtl2.getId().getUuid(), TrafficLightState.GREEN);
            assertFalse(excludingRule.isValid(statesContainer1));
        }
        {
            //assertTrue: both trafficLight can be Red at the same time with active excluding green rule
            VehicleTrafficLight vtl3 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
            VehicleTrafficLight vtl4 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
            StatesContainer statesContainer2 = new StatesContainer();
            statesContainer2.addState(vtl3.getId().getUuid(), TrafficLightState.GREEN);
            statesContainer2.addState(vtl4.getId().getUuid(), TrafficLightState.GREEN);
            ExcludingRule excludingRule2 = new ExcludingRule(vtl3.getId().getUuid(), vtl4.getId().getUuid(), TrafficLightState.GREEN);
            assertFalse(excludingRule2.isValid(statesContainer2));
        }
        {
            //assertFalse: vehicleTrafficLight and pedestrianTrafficLight cannot be green at the same time
            VehicleTrafficLight vtl5 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
            PedestrianTrafficLight ptl1 = new PedestrianTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
            StatesContainer statesContainer3 = new StatesContainer();
            statesContainer3.addState(vtl5.getId().getUuid(), TrafficLightState.GREEN);
            statesContainer3.addState(ptl1.getId().getUuid(), TrafficLightState.GREEN);
            ExcludingRule excludingRule3 = new ExcludingRule(vtl5.getId().getUuid(), ptl1.getId().getUuid(), TrafficLightState.GREEN);
            assertFalse(excludingRule3.isValid(statesContainer3));
        }
        {
            //assertTrue: pedestrianTrafficLight and vehicleTrafficLight can be Red at the same time with active excluding green rule
            VehicleTrafficLight vtl6 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
            PedestrianTrafficLight ptl2 = new PedestrianTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
            StatesContainer statesContainer4 = new StatesContainer();
            statesContainer4.addState(vtl6.getId().getUuid(), TrafficLightState.RED);
            statesContainer4.addState(ptl2.getId().getUuid(), TrafficLightState.RED);
            ExcludingRule excludingRule3 = new ExcludingRule(vtl6.getId().getUuid(), ptl2.getId().getUuid(), TrafficLightState.GREEN);
            assertTrue(excludingRule3.isValid(statesContainer4));
        }
    }

    @Test
    void testGetDescription(){
        double lo = 56.00020;
        double la = 57.00020;
        VehicleTrafficLight vtl1 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        VehicleTrafficLight vtl2 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        StatesContainer statesContainer1 = new StatesContainer();
        statesContainer1.addState(vtl1.getId().getUuid(), TrafficLightState.GREEN);
        statesContainer1.addState(vtl2.getId().getUuid(), TrafficLightState.GREEN);
        ExcludingRule excludingRule = new ExcludingRule(vtl1.getId().getUuid(), vtl2.getId().getUuid(), TrafficLightState.GREEN);
        assertEquals("This rule validates, that traffic lights " + excludingRule.getTl1() + " and " + excludingRule.getTl2() + " does not have state: "
                + excludingRule.getExcludedState() + " at the same time", excludingRule.getDescription());
    }

    @Test
    void testGetShortDescription(){
        double lo = 56.00020;
        double la = 57.00020;
        VehicleTrafficLight vtl1 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        VehicleTrafficLight vtl2 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        StatesContainer statesContainer1 = new StatesContainer();
        statesContainer1.addState(vtl1.getId().getUuid(), TrafficLightState.GREEN);
        statesContainer1.addState(vtl2.getId().getUuid(), TrafficLightState.GREEN);
        ExcludingRule excludingRule = new ExcludingRule(vtl1.getId().getUuid(), vtl2.getId().getUuid(), TrafficLightState.GREEN);
        assertEquals("Exclude: " + excludingRule.getTl1() + " and " + excludingRule.getTl2() + " are " + excludingRule.getExcludedState(), excludingRule.getShortDescription());
    }
}