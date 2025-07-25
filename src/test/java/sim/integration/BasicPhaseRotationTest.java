package sim.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import sim.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BasicPhaseRotationTest {
    @Test
    public void testBasicPhaseRotation() throws Exception {
        // Load json commands
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> input = mapper.readValue(new File("src/test/java/sim/integration/resources/phase_rotation_input.json"), Map.class);
        List<Map<String, Object>> commands = (List<Map<String, Object>>) input.get("commands");

        // Setup
        Intersection intersection = new Intersection(null);
        intersection.applyLanePriorities(Config.current.lanePriorities);
        TrafficLightController controller = new ActuatedController(intersection.getRoads());
        intersection.setController(controller);
        SimulationEngine engine = new SimulationEngine(intersection);

        engine.executeCommands(commands);

        // Verify phase switching occurred
        Map<String, Object> stats = engine.getStats();
        Map<String, Object> phases = (Map<String, Object>) stats.get("phases");
        assertTrue(phases.size() >= 2);
    }
}