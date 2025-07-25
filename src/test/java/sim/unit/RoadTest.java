package sim.unit;

import org.junit.jupiter.api.Test;
import sim.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Road} verifying lane selection and retrieval.
 */
class RoadTest {

    /**
     * Vehicles should be placed on lanes matching their intended maneuver.
     **/
    @Test
    void testAddVehicleToCorrectLane() {
        Road road = new Road(Direction.NORTH);

        // left turn from NORTH to WEST
        Vehicle leftTurn = new Vehicle("v1", Direction.NORTH, Direction.WEST, 0, VehicleType.NORMAL);
        Lane leftLane = road.addVehicle(leftTurn);
        assertEquals(LaneType.LEFT, leftLane.getType());

        // straight movement from NORTH to SOUTH
        Vehicle straight = new Vehicle("v2", Direction.NORTH, Direction.SOUTH, 0, VehicleType.NORMAL);
        Lane straightLane = road.addVehicle(straight);
        assertEquals(LaneType.STRAIGHT, straightLane.getType());
    }

    /**
     * Roads should expose left and straight lanes for retrieval.
     **/
    @Test
    void testLaneRetrieval() {
        Road road = new Road(Direction.EAST);

        assertNotNull(road.getLane(LaneType.LEFT));
        assertNotNull(road.getLane(LaneType.STRAIGHT));
        assertEquals(2, road.getLanes().size());
    }
}