package berlin.tu.ssas.group8.model.intersection;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ScheduledStatesContainerTest {
    ScheduledStatesContainer scheduledStatesContainer;
    StatesContainer statesContainer;
    @BeforeEach
    public void setUp(){
        statesContainer = new StatesContainer();
        scheduledStatesContainer = new ScheduledStatesContainer(statesContainer, "name", 30);
    }

    @Test
    void testStart(){

        scheduledStatesContainer.start();
        assertTrue(scheduledStatesContainer.isStarted());
    }

    @Test
    void testIsExpired(){
        scheduledStatesContainer.start();
        assertFalse(scheduledStatesContainer.isExpired());
    }

    @Test
    void testGeneralGetter(){
        assertEquals("name", scheduledStatesContainer.getName());
        assertEquals(30, scheduledStatesContainer.getDuration());
        assertEquals(0, scheduledStatesContainer.getContainer().getStates().size());
    }
    @Test
    void testScheduledStatesContainer(){
        assertThrows(IllegalArgumentException.class, () -> new ScheduledStatesContainer(statesContainer, "name", -1) );
    }

}