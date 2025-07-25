package sim.unit;

import org.junit.jupiter.api.Test;
import sim.Direction;
import sim.Vehicle;
import sim.VehicleType;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for {@link Vehicle} ensuring correct construction of its fields.
 */
class VehicleTest {


    /**
     * Vehicles should retain the properties passed to the constructor.
     */
    @Test
    void vehicleIsCreatedWithGivenParameters() {
        Vehicle vehicle = new Vehicle("v1", Direction.NORTH, Direction.SOUTH, 10, VehicleType.BUS);

        assertEquals("v1", vehicle.getId());
        assertEquals(Direction.NORTH, vehicle.getStart());
        assertEquals(Direction.SOUTH, vehicle.getEnd());
        assertEquals(10, vehicle.getArrivalStep());
        assertEquals(VehicleType.BUS, vehicle.getType());
    }
}