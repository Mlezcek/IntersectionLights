package sim.unit;

import org.junit.jupiter.api.Test;
import sim.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for {@link Lane} covering queue operations, counters and
 * priority calculations.
 */
class LaneTest {

    /**
     * Vehicles added to the lane should be returned in FIFO order when polled.
     */
    @Test
    void testAddAndPollVehicle() {
        Lane lane = new Lane(Direction.NORTH, LaneType.STRAIGHT);
        Vehicle car = new Vehicle("car1", Direction.NORTH, Direction.SOUTH, 0, VehicleType.NORMAL);

        lane.addVehicle(car);
        assertEquals(1, lane.size());

        Vehicle polled = lane.pollVehicle();
        assertEquals(car, polled);
        assertEquals(0, lane.size());
    }

    /**
     * Bus and emergency vehicle counters should update when vehicles are added
     * and removed.
     */
    @Test
    void testVehicleCounters() {
        Lane lane = new Lane(Direction.NORTH, LaneType.STRAIGHT);
        Vehicle bus = new Vehicle("bus1", Direction.NORTH, Direction.SOUTH, 0, VehicleType.BUS);
        Vehicle ambulance = new Vehicle("amb1", Direction.NORTH, Direction.SOUTH, 0, VehicleType.EMERGENCY);

        lane.addVehicle(bus);
        lane.addVehicle(ambulance);

        assertEquals(1, lane.busCount);
        assertEquals(1, lane.emergencyCount);
        assertTrue(lane.hasEmergency());

        lane.pollVehicle();
        assertEquals(0, lane.busCount);
        assertEquals(1, lane.emergencyCount);
    }

    /**
     * Total waiting time should equal the sum of individual vehicle wait
     * durations at a given simulation step.
     */
    @Test
    void testWaitingTimeCalculation() {
        Lane lane = new Lane(Direction.NORTH, LaneType.STRAIGHT);
        Vehicle car1 = new Vehicle("car1", Direction.NORTH, Direction.SOUTH, 5, VehicleType.NORMAL);
        Vehicle car2 = new Vehicle("car2", Direction.NORTH, Direction.SOUTH, 10, VehicleType.NORMAL);

        lane.addVehicle(car1);
        lane.addVehicle(car2);

        // at step 15: car1 waited 10, car2 waited 5 -> total 15
        assertEquals(15, lane.getTotalWaitingTime(15));
    }

    /**
     * Lane priority should include bus bonus when buses are present.
     */
    @Test
    void testPriorityCalculation() {
        Config.current.busPriority = 2.0;
        Lane lane = new Lane(Direction.NORTH, LaneType.STRAIGHT);
        lane.setPriority(1.5);

        assertEquals(1.5, lane.getPriority());  // no buses yet

        Vehicle bus = new Vehicle("bus1", Direction.NORTH, Direction.SOUTH, 0, VehicleType.BUS);
        lane.addVehicle(bus);

        // 1.5 base + 1 * 2.0 bus bonus = 3.5
        assertEquals(3.5, lane.getPriority());
    }
}