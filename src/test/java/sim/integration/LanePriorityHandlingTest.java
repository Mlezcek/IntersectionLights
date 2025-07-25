package sim.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.*;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LanePriorityHandlingTest {
    @BeforeEach
    public void setupConfig() {
        // Configure East Straight with high priority
        Config.current.lanePriorities.get(Direction.EAST).put(LaneType.STRAIGHT, 5.0);
    }

    @Test
    public void testLanePriorityHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> input = mapper.readValue(new File("src/test/java/sim/integration/resources/lane_priority_input.json"), Map.class);
        List<Map<String, Object>> commands = (List<Map<String, Object>>) input.get("commands");

        // Setup
        Intersection intersection = new Intersection(null);
        intersection.applyLanePriorities(Config.current.lanePriorities);
        TrafficLightController controller = new ActuatedController(intersection.getRoads());
        intersection.setController(controller);
        SimulationEngine engine = new SimulationEngine(intersection);

        engine.executeCommands(commands);

        // Verify East straight phase got more attention
        Map<String, Object> stats = engine.getStats();
        Map<String, Object> phases = (Map<String, Object>) stats.get("phases");
        Map<String, Object> phase1 = (Map<String, Object>) phases.get("Phase1"); // East-West straight
        
        assertTrue((int) phase1.get("timesActivated") > 0);
        assertTrue((int) phase1.get("totalDuration") > 0);
    }
}