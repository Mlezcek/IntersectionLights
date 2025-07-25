package sim.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import sim.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BasicTrafficFlowTest {
    @Test
    public void testBasicTrafficFlow() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> input = mapper.readValue(new File("src/test/java/sim/integration/resources/basic_flow_input.json"), Map.class);
        List<Map<String, Object>> commands = (List<Map<String, Object>>) input.get("commands");

        // Setup
        Intersection intersection = new Intersection(null);
        intersection.applyLanePriorities(Config.current.lanePriorities);
        TrafficLightController controller = new ActuatedController(intersection.getRoads());
        intersection.setController(controller);
        SimulationEngine engine = new SimulationEngine(intersection);

        engine.executeCommands(commands);

        // Verify
        Map<String, Object> stats = engine.getStats();
        assertEquals(1, stats.get("totalVehicles"));
        assertEquals(1, stats.get("vehiclesLeft"));
        assertEquals(0, stats.get("vehiclesRemaining"));
        assertEquals(1.0, stats.get("averageWaitTime"));
    }
}