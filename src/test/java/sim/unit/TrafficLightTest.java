package sim.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.Config;
import sim.TrafficLight;
import sim.TrafficLightState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TrafficLight} verifying state transitions.
 */
public class TrafficLightTest {
    @BeforeEach
    public void resetConfig() {
        Config.current = new Config();
    }

    /**
     * After the yellow period expires the light should turn red.
     */
    @Test
    public void testYellowExpiresToRed() {
        TrafficLight light = new TrafficLight();
        light.setGreen();
        light.setYellow();
        Assertions.assertEquals(TrafficLightState.YELLOW, light.getState());
        light.step();
        assertEquals(TrafficLightState.RED, light.getState());
    }
}