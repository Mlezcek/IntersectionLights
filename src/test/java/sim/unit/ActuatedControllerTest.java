package sim.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.*;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.Set;


/**
 * Unit tests for {@link ActuatedController} covering phase initialization and
 * emergency vehicle handling.
 */

class ActuatedControllerTest {

    private ActuatedController controller;
    private Map<Direction, Road> roads;

    @BeforeEach
    void setUp() {
        Config.current = new Config();
        Intersection intersection = new Intersection(null);
        roads = intersection.getRoads();
        controller = new ActuatedController(roads);
    }

    /**
     * Upon creation the controller should start with phase index 0 and allow
     * north-south straight traffic.
     */
    @Test
    void testInitialPhase() {
        assertEquals(0, controller.getCurrentPhaseIndex());
        Set<Lane> greenLanes = controller.getGreenLanes(roads);
        assertTrue(greenLanes.contains(roads.get(Direction.NORTH).getLane(LaneType.STRAIGHT)));
        assertTrue(greenLanes.contains(roads.get(Direction.SOUTH).getLane(LaneType.STRAIGHT)));
    }

    /**
     * When an emergency vehicle arrives its lane should be given an exclusive green light.
     **/
    @Test
    void testEmergencyVehicleHandling() {
        Lane northStraight = roads.get(Direction.NORTH).getLane(LaneType.STRAIGHT);
        Vehicle ambulance = new Vehicle("amb1", Direction.NORTH, Direction.SOUTH, 0, VehicleType.EMERGENCY);

        northStraight.addVehicle(ambulance);
        controller.emergencyVehicleArrived(northStraight);

        Set<Lane> greenLanes = controller.getGreenLanes(roads);
        assertEquals(1, greenLanes.size());
        assertTrue(greenLanes.contains(northStraight));
    }
}