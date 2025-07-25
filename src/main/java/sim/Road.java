package sim;

import java.util.ArrayList;
import java.util.List;

/**
 * A single incoming road to the intersection. Each road owns two lanes: one for
 * left turns and one for going straight or right. Helper methods manage vehicle
 * placement into the correct lane.
 */
public class Road {
    private final Direction direction;
    private final List<Lane> lanes = new ArrayList<>();

    /**
     * Create a road oriented in the specified direction.
     *
     * @param direction compass direction that vehicles travel from
     */
    public Road(Direction direction) {
        this.direction = direction;
        lanes.add(new Lane(direction, LaneType.LEFT));
        lanes.add(new Lane(direction, LaneType.STRAIGHT));
    }

    /**
     * Place a vehicle onto the appropriate lane based on its destination.
     *
     * @param vehicle vehicle entering the road
     * @return lane that the vehicle was added to
     */
    public Lane addVehicle(Vehicle vehicle) {
        LaneType type = determineLaneType(vehicle.getEnd());
        Lane lane = getLane(type);
        lane.addVehicle(vehicle);
        return lane;
    }

    /**
     * Remove the next vehicle from the given lane, if any.
     */
    public Vehicle pollVehicle(Lane lane) {
        return lane.pollVehicle();
    }

    /**
     * Decide which lane a vehicle should use based on its exit direction.
     */
    private LaneType determineLaneType(Direction end) {
        if (end == direction.left()) {
            return LaneType.LEFT;
        }
        return LaneType.STRAIGHT;
    }

    /**
     * Retrieve one of the road's lanes.
     *
     * @param type desired lane type
     * @return lane matching the type
     */
    public Lane getLane(LaneType type) {
        for (Lane l : lanes) {
            if (l.getType() == type) return l;
        }
        throw new IllegalArgumentException("No lane of type " + type);
    }

    /**
     * Total number of vehicles waiting on this road across both lanes.
     */
    public int size() {
        int total = 0;
        for (Lane l : lanes) {
            total += l.size();
        }
        return total;
    }

    public List<Lane> getLanes() {
        return lanes;
    }

    public Direction getDirection() {
        return direction;
    }
}