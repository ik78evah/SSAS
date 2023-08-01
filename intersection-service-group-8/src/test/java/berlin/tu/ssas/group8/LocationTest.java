package berlin.tu.ssas.group8;

import berlin.tu.ssas.group8.model.trafficLight.Location;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class LocationTest {

    @Test
    void testLocationCreate() {
        assertDoesNotThrow(() -> new Location(90, 180));
    }

    @Test
    void testLocationGet() {
        Location l = new Location(90, 180);
        assertEquals(90, l.getLatitude());
        assertEquals(180, l.getLongitude());

        // -180 longitude should be mapped to 180
        Location l2 = new Location(90, -180);
        assertEquals(180, l2.getLongitude());
    }


    @Test
    void testLocationInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> new Location(90.1f, 0));
        assertThrows(IllegalArgumentException.class, () -> new Location(-90.1f, 0));
        assertThrows(IllegalArgumentException.class, () -> new Location(0, 180.1f));
        assertThrows(IllegalArgumentException.class, () -> new Location(0, -180.1f));
    }


    @Test
    void TestLocationDistanceTo() {
        Location l1 = new Location(52.51213f, 13.32708f); //TU Hauptgebäude
        Location l2 = new Location(52.51392f, 13.32621f); //TU Mathegebäude

        Location l3 = new Location(0, 179.999f);
        Location l4 = new Location(0, -179.999f);
        assertEquals(207, l1.distanceTo(l2));
        assertEquals(0, l1.distanceTo(l1));
        //wrap around test
        assertEquals(223, l3.distanceTo(l4));
    }

    @Test
    void TestLocationDistanceInvalidArgument() {
        Location l = new Location(1, 1);
        assertThrows(NullPointerException.class, () -> l.distanceTo(null));
    }

    @Test
    void TestLocationRangeOf() {
        Location l1 = new Location(52.51213f, 13.32708f); //TU Hauptgebäude
        Location l2 = new Location(52.51392f, 13.32621f); //TU Mathegebäude
        Location l3 = new Location(52.51661f, 13.32355f); //TU MAR

        assertTrue(l1.inRangeOf(l2, 300)); //Mathegebäude is in range of 300m
        assertFalse(l1.inRangeOf(l3, 300)); // Mar is not in range of 300m
    }

    @Test
    void TestLocationRangeOfInvalidArguments() {
        Location l = new Location(1, 1);
        assertThrows(NullPointerException.class, () -> l.inRangeOf(null, 5));
        assertThrows(IllegalArgumentException.class, () -> l.inRangeOf(l, -1));
    }

}
