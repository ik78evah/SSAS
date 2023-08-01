package berlin.tu.ssas.group8;

import berlin.tu.ssas.group8.model.trafficLight.*;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TrafficLightTest {

    @Test
    void testPedestrianTrafficLightState() {
        Location location = new Location(54.3123f, 12.34534f);
        PedestrianTrafficLight trafficLight = new PedestrianTrafficLight(location, TrafficLightState.RED, TrafficLightPositionTag.NORTH);
        assertEquals(TrafficLightState.RED, trafficLight.getState());
    }

    @Test
    void testPedestrianTrafficLightSetState() {
        Location location = new Location(54.3123f, 12.34534f);
        PedestrianTrafficLight trafficLight = new PedestrianTrafficLight(location, TrafficLightState.RED, TrafficLightPositionTag.EAST);
        trafficLight.setState(TrafficLightState.GREEN);
        assertEquals(TrafficLightState.GREEN, trafficLight.getState());
    }

    @Test
    void testPedestrianTrafficLightSetStateToGreen() {
        Location location = new Location(54.3123f, 12.34534f);
        PedestrianTrafficLight trafficLight = new PedestrianTrafficLight(location, TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
        trafficLight.setStateToGreen();
        assertEquals(TrafficLightState.GREEN, trafficLight.getState());
    }

    @Test
    void testPedestrianTrafficLightSetStateToRed() {
        Location location = new Location(54.3123f, 12.34534f);
        PedestrianTrafficLight trafficLight = new PedestrianTrafficLight(location, TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
        trafficLight.setStateToRed();
        assertEquals(TrafficLightState.RED, trafficLight.getState());
    }

    @Test
    void testPedestrianTrafficLightSetEmergency() {
        Location location = new Location(54.3123f, 12.34534f);
        PedestrianTrafficLight trafficLight = new PedestrianTrafficLight(location, TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
        trafficLight.setStateToEmergency();
        assertEquals(TrafficLightState.EMERGENCY, trafficLight.getState());
    }


    @Test
    void testPedestrianTrafficLightSetStateException() {
        Location location = new Location(54.3123f, 12.34534f);
        PedestrianTrafficLight trafficLight = new PedestrianTrafficLight(location, TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            trafficLight.setState(TrafficLightState.YELLOW);
        });
        assertEquals("YELLOW state is not possible for pedestrian lights", exception.getMessage());
    }

    @Test
    void testVehicleTrafficLightState() {
        Location location = new Location(54.3123f, 12.34534f);
        VehicleTrafficLight trafficLight = new VehicleTrafficLight(location, TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        assertEquals(TrafficLightState.GREEN, trafficLight.getState());
    }

    @Test
    void testVehicleTrafficLightSetState() {
        Location location = new Location(54.3123f, 12.34534f);
        UUID uuid = UUID.randomUUID();
        TrafficLightId trafficLightId = new TrafficLightId(uuid, location);
        VehicleTrafficLight trafficLight = new VehicleTrafficLight(trafficLightId, TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        trafficLight.setState(TrafficLightState.YELLOW);
        assertEquals(TrafficLightState.YELLOW, trafficLight.getState());
    }

    @Test
    void testVehicleTrafficLightSetStateToRedYellow() {
        Location location = new Location(54.3123f, 12.34534f);
        VehicleTrafficLight trafficLight = new VehicleTrafficLight(location, TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
        trafficLight.setStateToRedYellow();
        assertEquals(TrafficLightState.RED_YELLOW, trafficLight.getState());
    }

    @Test
    void testVehicleTrafficLightSetStateYellow() {
        Location location = new Location(54.3123f, 12.34534f);
        VehicleTrafficLight trafficLight = new VehicleTrafficLight(location, TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
        trafficLight.setStateToYellow();
        assertEquals(TrafficLightState.YELLOW, trafficLight.getState());
    }

    @Test
    void testStatus() {
        Location location = new Location(54.3123f, 12.34534f);
        TrafficLight trafficLight = new VehicleTrafficLight(location, TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        LocalDateTime before = LocalDateTime.now();
        TrafficLightStatus trafficLightStatus = trafficLight.checkStatus();
        LocalDateTime after = LocalDateTime.now();
        assertEquals(trafficLight.getState(), trafficLightStatus.getState());
        assertEquals(trafficLight.getId().getUuid(), trafficLightStatus.getId());
        assertTrue(before.isBefore(trafficLightStatus.getTime()) || before.isEqual(trafficLightStatus.getTime()));
        assertTrue(after.isAfter(trafficLightStatus.getTime()) || after.isEqual(trafficLightStatus.getTime()));

        trafficLight.setState(TrafficLightState.YELLOW);
        assertNotEquals(trafficLight.getState(), trafficLightStatus.getState());
        assertEquals(trafficLight.getState(), trafficLight.checkStatus().getState());
    }

    @Test
    void testPedestrianTrafficLightInvalidArguments() {
        Location l = new Location(1, 1);
        assertThrows(NullPointerException.class, () -> new PedestrianTrafficLight(null, TrafficLightState.RED, TrafficLightPositionTag.SOUTH));
        assertThrows(NullPointerException.class, () -> new PedestrianTrafficLight(l, null, TrafficLightPositionTag.SOUTH));

        PedestrianTrafficLight ptl = new PedestrianTrafficLight(new Location(0, 0), TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
        assertThrows(NullPointerException.class, () -> ptl.setState(null));
    }

    @Test
    void testVehicleTrafficLightInvalidArguments() {
        Location l = new Location(1, 1);
        assertThrows(NullPointerException.class, () -> new VehicleTrafficLight((Location) null, TrafficLightState.RED, TrafficLightPositionTag.SOUTH));
        assertThrows(NullPointerException.class, () -> new VehicleTrafficLight(l, null, TrafficLightPositionTag.SOUTH));

        VehicleTrafficLight vtl = new VehicleTrafficLight(new Location(0, 0), TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
        assertThrows(NullPointerException.class, () -> vtl.setState(null));
    }

    @Test
    void getStateAndChangeIt() {
        Location location = new Location(54.3123f, 12.34534f);
        TrafficLight vehicleTrafficLight = new VehicleTrafficLight(location, TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        TrafficLight pedestrianTrafficLight = new PedestrianTrafficLight(location, TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        TrafficLight[] trafficLights = new TrafficLight[]{vehicleTrafficLight, pedestrianTrafficLight};

        for (TrafficLight trafficLight : trafficLights) {
            // get the status object of the traffic light
            TrafficLightState t_l_state = trafficLight.getState();
            t_l_state = TrafficLightState.RED;
            // the internal state of the traffic light should not have changed
            assertEquals(TrafficLightState.GREEN, trafficLight.getState());
        }

    }

}