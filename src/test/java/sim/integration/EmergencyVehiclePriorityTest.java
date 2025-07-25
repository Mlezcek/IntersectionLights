package sim.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import sim.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EmergencyVehiclePriorityTest {
    @Test
    public void testEmergencyVehiclePriority() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> input = mapper.readValue(new File("src/test/java/sim/integration/resources/emergency_priority_input.json"), Map.class);
        List<Map<String, Object>> commands = (List<Map<String, Object>>) input.get("commands");

        // Setup
        Intersection intersection = new Intersection(null);
        intersection.applyLanePriorities(Config.current.lanePriorities);
        TrafficLightController controller = new ActuatedController(intersection.getRoads());
        intersection.setController(controller);
        SimulationEngine engine = new SimulationEngine(intersection);

        engine.executeCommands(commands);

        // Verify emergency vehicle was processed quickly
        Map<String, Object> stats = engine.getStats();
        assertEquals(2, stats.get("totalVehicles"));
        assertTrue((int) stats.get("vehiclesLeft") >= 1);
    }
}