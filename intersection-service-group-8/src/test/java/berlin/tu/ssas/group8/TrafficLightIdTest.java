package berlin.tu.ssas.group8;

import berlin.tu.ssas.group8.model.trafficLight.Location;
import berlin.tu.ssas.group8.model.trafficLight.TrafficLightId;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TrafficLightIdTest {
    @Test
    void testTrafficLightCreation() {
        UUID uuid = UUID.randomUUID();
        Location l = new Location(1, 1);
        TrafficLightId testID = new TrafficLightId(uuid, l);
        assertEquals(l, testID.getLocation());
        assertEquals(uuid, testID.getUuid());
    }

    @Test
    void testTrafficLightIdInvalidArguments() {
        Location l = new Location(1, 1);
        UUID uuid = UUID.randomUUID();
        assertThrows(NullPointerException.class, () -> new TrafficLightId(null, l));
        assertThrows(NullPointerException.class, () -> new TrafficLightId(uuid, null));
    }

    @Test
    void testTrafficLightIdToString() {
        Location l = new Location(1, 1);
        UUID uuid = UUID.randomUUID();
        TrafficLightId trafficLightId = new TrafficLightId(uuid, l);
        assertEquals(trafficLightId.toString(), "TrafficLightId(uuid=" + uuid + ", location=" + l.toString() + ")");
    }

}
