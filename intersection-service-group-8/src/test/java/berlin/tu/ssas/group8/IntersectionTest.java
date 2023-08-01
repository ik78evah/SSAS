package berlin.tu.ssas.group8;

import berlin.tu.ssas.group8.model.intersection.Intersection;
import berlin.tu.ssas.group8.model.intersection.StatesContainer;
import berlin.tu.ssas.group8.model.intersection.TrafficLightStateChangeRequest;
import berlin.tu.ssas.group8.model.trafficLight.PedestrianTrafficLight;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLight;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import berlin.tu.ssas.group8.model.trafficLight.VehicleTrafficLight;
import berlin.tu.ssas.group8.model.utilities.IntersectionDataProvider;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@QuarkusTest
public class IntersectionTest {

    @Test
    void providerConstructorTest() {
        IntersectionDataProvider idp = new IntersectionDataProvider(10.12345, 10.12345);
        assertDoesNotThrow(() -> new Intersection(idp));
        Intersection intersection = new Intersection(idp);
        assertNotNull(intersection);
        assertFalse(intersection.getAllTrafficLights().isEmpty());
    }

    @Test
    void getTrafficLightsTest() {
        IntersectionDataProvider idp = new IntersectionDataProvider(1.0, 1.0);
        Intersection intersection = new Intersection(idp);
        List<TrafficLight> lights = intersection.getAllTrafficLights();

        for (TrafficLight tl : lights) {
            assertSame(tl, intersection.getTrafficLight(tl.getId().getUuid()));
        }
    }

    @Test
    void trafficLightStateIntegrityTest() {
        IntersectionDataProvider idp = new IntersectionDataProvider(1.0, 1.0);
        Intersection intersection = new Intersection(idp);
        List<TrafficLight> lights = intersection.getAllTrafficLights();
        StatesContainer sc = intersection.getCurrentState();

        assertEquals(sc.getStates().size(), lights.size());

        for (TrafficLight tl : lights) {
            assertSame(tl.getState(), intersection.getTrafficLight(tl.getId().getUuid()).getState());
            assertSame(tl.getState(), sc.getState(tl.getId().getUuid()));
        }
    }


    @Test
    void setGreenLightIllegalArgumentsTest() {
        IntersectionDataProvider idp = new IntersectionDataProvider(1.0, 1.0);
        Intersection intersection = new Intersection(idp);
        UUID pedestrian = null;
        UUID valid = null;

        //Selecting 2 random traffic lights: pedestrian and red vehicle traffic light (supposed to be valid)
        for (TrafficLight tl : intersection.getAllTrafficLights()) {
            if (tl instanceof PedestrianTrafficLight)
                pedestrian = tl.getId().getUuid();
            else if (tl.getState() == TrafficLightState.RED)
                valid = tl.getId().getUuid();
        }

        assertNotNull(pedestrian);
        assertNotNull(valid);
        final UUID fPedestrian = pedestrian;
        final UUID fValid = valid;

        //not existing traffic light
        assertThrows(IllegalArgumentException.class, () -> intersection.setGreenLight(UUID.randomUUID(), "mayor"));
        //not known authority
        assertThrows(IllegalArgumentException.class, () -> intersection.setGreenLight(fValid, "somebody"));
        //pedestrian traffic light
        assertThrows(IllegalArgumentException.class, () -> intersection.setGreenLight(fPedestrian, "mayor"));
    }

    @Test
    void setGreenLightAtEmergencyLightTest() {
        IntersectionDataProvider idp = new IntersectionDataProvider(1.0, 1.0);
        Intersection intersection = new Intersection(idp);
        UUID valid = null;

        //Selecting red vehicle traffic light (supposed to be valid)
        for (TrafficLight tl : intersection.getAllTrafficLights()) {
            if (tl instanceof VehicleTrafficLight && tl.getState() == TrafficLightState.RED)
                valid = tl.getId().getUuid();
        }

        assertNotNull(valid);
        final UUID fValid = valid;
        intersection.getTrafficLight(fValid).setState(TrafficLightState.EMERGENCY);
        assertThrows(IllegalArgumentException.class, () -> intersection.setGreenLight(fValid, "emergency-vehicle"));
    }

    @Test
    void setGreenLightAtBlockedIntersectionTest() {
        IntersectionDataProvider idp = new IntersectionDataProvider(1.0, 1.0);
        Intersection intersection = new Intersection(idp);
        UUID valid = null;

        //Selecting red vehicle traffic light (supposed to be valid)
        for (TrafficLight tl : intersection.getAllTrafficLights()) {
            if (tl instanceof VehicleTrafficLight && tl.getState() == TrafficLightState.RED)
                valid = tl.getId().getUuid();
        }

        assertNotNull(valid);
        final UUID fValid = valid;

        //valid
        assertDoesNotThrow(() -> intersection.setGreenLight(fValid, "mayor"));
        //intersection blocked due previous
        assertThrows(IllegalStateException.class, () -> intersection.setGreenLight(fValid, "mayor"));
        //intersection blocked, but interrupted because of priority
        assertDoesNotThrow(() -> intersection.setGreenLight(fValid, "emergency-vehicle"));
        //intersection blocked at the highest prirotiy, no interruption
        assertThrows(IllegalStateException.class, () -> intersection.setGreenLight(fValid, "emergency-vehicle"));
    }

    @Test
    void setNewStateIllegalArgumentsState() {
        IntersectionDataProvider idp = new IntersectionDataProvider(1.0, 1.0);
        Intersection intersection = new Intersection(idp);
        HashMap<UUID, TrafficLightState> map = new HashMap<>();

        //Empty map
        TrafficLightStateChangeRequest emptyMap = new TrafficLightStateChangeRequest(map, UUID.randomUUID(), 1);
        assertThrows(IllegalArgumentException.class, () -> intersection.setTrafficLightState(emptyMap));

        //Bad values
        for (int i = 0; i < 12; i++)
            map.put(UUID.randomUUID(), TrafficLightState.GREEN);
        TrafficLightStateChangeRequest badValues = new TrafficLightStateChangeRequest(map, UUID.randomUUID(), 1);
        assertThrows(IllegalArgumentException.class, () -> intersection.setTrafficLightState(badValues));

        //Many values
        map.put(UUID.randomUUID(), TrafficLightState.GREEN);
        TrafficLightStateChangeRequest manyValues = new TrafficLightStateChangeRequest(map, UUID.randomUUID(), 1);
        assertThrows(IllegalArgumentException.class, () -> intersection.setTrafficLightState(manyValues));
    }

    @Test
    void setNewStateBadRulesTest() {
        IntersectionDataProvider idp = new IntersectionDataProvider(1.0, 1.0);
        Intersection intersection = new Intersection(idp);
        HashMap<UUID, TrafficLightState> currentStates = new HashMap<>();

        //Trying to make all vehicle lights green
        for (TrafficLight tl : intersection.getAllTrafficLights()) {
            if (tl instanceof VehicleTrafficLight)
                currentStates.put(tl.getId().getUuid(), TrafficLightState.GREEN);
            else
                currentStates.put(tl.getId().getUuid(), tl.getState());
        }
        TrafficLightStateChangeRequest vehicleGreen = new TrafficLightStateChangeRequest(currentStates, UUID.randomUUID(), 1);
        assertThrows(IllegalArgumentException.class, () -> intersection.setTrafficLightState(vehicleGreen));

        //Trying to make pedestrian traffic lights yellow
        currentStates = new HashMap<>();
        for (TrafficLight tl : intersection.getAllTrafficLights()) {
            if (tl instanceof PedestrianTrafficLight)
                currentStates.put(tl.getId().getUuid(), TrafficLightState.YELLOW);
            else
                currentStates.put(tl.getId().getUuid(), tl.getState());
        }
        TrafficLightStateChangeRequest pedestrianYellow = new TrafficLightStateChangeRequest(currentStates, UUID.randomUUID(), 1);
        assertThrows(IllegalArgumentException.class, () -> intersection.setTrafficLightState(pedestrianYellow));

        //Trying to make invalid transition
        currentStates = new HashMap<>();
        for (TrafficLight tl : intersection.getAllTrafficLights()) {
            if (tl instanceof VehicleTrafficLight && tl.getState() == TrafficLightState.RED)
                currentStates.put(tl.getId().getUuid(), TrafficLightState.GREEN);
            else
                currentStates.put(tl.getId().getUuid(), tl.getState());
        }
        TrafficLightStateChangeRequest invalidTransition = new TrafficLightStateChangeRequest(currentStates, UUID.randomUUID(), 1);
        assertThrows(IllegalArgumentException.class, () -> intersection.setTrafficLightState(invalidTransition));
    }

    @Test
    void setNewValidStateTest() {
        IntersectionDataProvider idp = new IntersectionDataProvider(1.0, 1.0);
        Intersection intersection = new Intersection(idp);
        HashMap<UUID, TrafficLightState> currentStates = new HashMap<>();

        //Trying to make green trafic lights yellow
        for (TrafficLight tl : intersection.getAllTrafficLights()) {
            if (tl instanceof VehicleTrafficLight && tl.getState() == TrafficLightState.GREEN)
                currentStates.put(tl.getId().getUuid(), TrafficLightState.YELLOW);
            else
                currentStates.put(tl.getId().getUuid(), tl.getState());
        }

        TrafficLightStateChangeRequest validRequest = new TrafficLightStateChangeRequest(currentStates, UUID.randomUUID(), 1);
        assertDoesNotThrow(() -> intersection.setTrafficLightState(validRequest));
    }

    @Test
    void setNewStateWithTransitionTest() {
        IntersectionDataProvider idp = new IntersectionDataProvider(1.0, 1.0);
        Intersection intersection = new Intersection(idp);
        HashMap<UUID, TrafficLightState> currentStates = new HashMap<>();

        //Trying to make green trafic lights yellow
        for (TrafficLight tl : intersection.getAllTrafficLights()) {
            if (tl instanceof VehicleTrafficLight && tl.getState() == TrafficLightState.GREEN)
                currentStates.put(tl.getId().getUuid(), TrafficLightState.RED);
            else
                currentStates.put(tl.getId().getUuid(), tl.getState());
        }

        TrafficLightStateChangeRequest validRequest = new TrafficLightStateChangeRequest(currentStates, UUID.randomUUID(), 1);
        assertDoesNotThrow(() -> intersection.setTrafficLightState(validRequest));
    }

}
