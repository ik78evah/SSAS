package berlin.tu.ssas.group8.model.intersection;

import berlin.tu.ssas.group8.model.trafficLight.Location;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightPositionTag;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightState;
import berlin.tu.ssas.group8.model.trafficLight.VehicleTrafficLight;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TrafficLightStateChangeRequestTest {
    VehicleTrafficLight vtl1;
    TrafficLightStateChangeRequest trafficLightStateChangeRequest;


    @BeforeEach
    void setup() {
        double lo = 56.00020;
        double la = 57.00020;
        vtl1 = new VehicleTrafficLight(new Location(la + 0.00001, lo + 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        HashMap<UUID,TrafficLightState> hashmap = new HashMap();
        trafficLightStateChangeRequest = new TrafficLightStateChangeRequest(hashmap, vtl1.getId().getUuid(),5);
    }

    @Test
    void testAddState(){

        trafficLightStateChangeRequest.addState(vtl1.getId().getUuid(), vtl1.getState());
        assertEquals(1, trafficLightStateChangeRequest.getStates().size());
    }

    @Test
    void testGetState(){

        trafficLightStateChangeRequest.addState(vtl1.getId().getUuid(), vtl1.getState());
        assertEquals(TrafficLightState.GREEN,trafficLightStateChangeRequest.getState(vtl1.getId().getUuid()));

        VehicleTrafficLight vtl2 = new VehicleTrafficLight(new Location(57.00020, 56.00020), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        assertThrows(NoSuchElementException.class,() -> trafficLightStateChangeRequest.getState(vtl2.getId().getUuid()));
    }

    @Test
    void testGetStates(){
        trafficLightStateChangeRequest.addState(vtl1.getId().getUuid(), vtl1.getState());
        assertEquals(TrafficLightState.GREEN,trafficLightStateChangeRequest.getStates().get(vtl1.getId().getUuid()));
    }

    @Test
    void equals(){
        StatesContainer statesContainer1 = new StatesContainer();
        StatesContainer statesContainer2 = new StatesContainer();
        statesContainer1.addState(vtl1.getId().getUuid(), vtl1.getState());
        statesContainer2.addState(vtl1.getId().getUuid(), vtl1.getState());
        assertTrue(statesContainer1.equals(statesContainer2));

        VehicleTrafficLight vtl2 = new VehicleTrafficLight(new Location(0.00001, 0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        statesContainer1.addState(vtl2.getId().getUuid(), vtl2.getState());

        assertFalse(statesContainer1.equals(statesContainer2));

    }

    @Test
    void generalGetters(){
        assertEquals(vtl1.getId().getUuid(),trafficLightStateChangeRequest.getIssuer());
        assertEquals(5, trafficLightStateChangeRequest.getMinDuration());
    }
}