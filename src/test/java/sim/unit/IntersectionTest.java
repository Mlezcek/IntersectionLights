package sim.unit;

import org.junit.jupiter.api.Test;
import sim.*;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Intersection} verifying vehicle movement under a
 * static green-light controller.
 */
public class IntersectionTest {
    /**
     * Controller that always keeps the supplied lanes green.
     **/
    static class StaticController implements TrafficLightController {
        private final Set<Lane> lanes;
        StaticController(Set<Lane> lanes) { this.lanes = lanes; }
        @Override
        public Set<Lane> getGreenLanes(Map<Direction, Road> roads) {
            return lanes;
        }
    }

    /**
     * Vehicles waiting on lanes granted a green light should exit the
     * intersection once a simulation step is executed.
     */
    @Test
    void testVehiclesLeaveOnGreen() {
        Intersection intersection = new Intersection(null);
        Lane northLane = intersection.getRoads().get(Direction.NORTH).getLane(LaneType.STRAIGHT);
        Lane southLane = intersection.getRoads().get(Direction.SOUTH).getLane(LaneType.STRAIGHT);
        intersection.setController(new StaticController(Set.of(northLane, southLane)));

        Vehicle v1 = new Vehicle("v1", Direction.NORTH, Direction.SOUTH, 0, VehicleType.NORMAL);
        Vehicle v2 = new Vehicle("v2", Direction.NORTH, Direction.SOUTH, 0, VehicleType.NORMAL);
        intersection.addVehicle(v1);
        intersection.addVehicle(v2);

        List<Vehicle> left = intersection.step();
        assertEquals(Config.current.vehiclesPerStep, left.size());
        assertTrue(left.containsAll(List.of(v1, v2)));
    }
}