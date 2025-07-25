package sim.unit;

import org.junit.jupiter.api.Test;
import sim.*;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;


/**
 * Unit tests for {@link SimulationEngine} verifying command execution and
 * resulting statistics.
 */
class SimulationEngineTest {

    /**
     * A sequence of commands should be processed in order and update
     * statistics accordingly.
     */
    @Test
    void testCommandProcessing() {
        Config.current = new Config();
        Intersection intersection = new Intersection(null);
        TrafficLightController controller = new ActuatedController(intersection.getRoads());
        intersection.setController(controller);
        SimulationEngine engine = new SimulationEngine(intersection);

        List<Map<String, Object>> commands = List.of(
                Map.of(
                        "type", "addVehicle",
                        "vehicleId", "car1",
                        "startRoad", "NORTH",
                        "endRoad", "SOUTH",
                        "vehicleType", "NORMAL"
                ),
                Map.of("type", "step"),
                Map.of("type", "step")
        );

        engine.executeCommands(commands);

        Map<String, Object> result = engine.getResult();
        List<Map<String, Object>> stepStatuses = (List<Map<String, Object>>) result.get("stepStatuses");
        assertEquals(2, stepStatuses.size());

        Map<String, Object> stats = engine.getStats();
        assertEquals(1, stats.get("totalVehicles"));
        assertEquals(1, stats.get("vehiclesLeft"));
    }
}