package sim;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Queue of vehicles sharing the same movement (left or straight/right) from a
 * particular direction. Each lane has its own traffic light instance and tracks
 * counts of buses and emergency vehicles for priority calculations.
 */
public class Lane {
    private final Direction start;
    private final LaneType type;
    private final Queue<Vehicle> vehicles = new LinkedList<>();
    private final TrafficLight light = new TrafficLight();
    private double basePriority;
    public int busCount = 0;
    public int emergencyCount = 0;


    /**
     * Create a lane starting from the given direction.
     *
     * @param start direction vehicles enter from
     * @param type  classification of the lane (LEFT or STRAIGHT)
     */
    public Lane(Direction start, LaneType type) {
        this.start = start;
        this.type = type;
    }

    /**
     * Enqueue a vehicle onto this lane and update priority counters.
     */
    public void addVehicle(Vehicle v) {
        vehicles.offer(v);
        if (v.getType() == VehicleType.BUS) busCount++;
        if (v.getType() == VehicleType.EMERGENCY) emergencyCount++;
    }

    /**
     * Dequeue the next vehicle, adjusting priority counters accordingly.
     */
    public Vehicle pollVehicle() {
        Vehicle v = vehicles.poll();
        if (v != null) {
            if (v.getType() == VehicleType.BUS) busCount--;
            if (v.getType() == VehicleType.EMERGENCY) emergencyCount--;
        }
        return v;
    }

    /**
     * Calculate total waiting time of vehicles currently in this lane.
     *
     * @param currentStep current simulation step
     * @return sum of waiting time for all vehicles in the lane
     */
    public int getTotalWaitingTime(int currentStep) {
        int wait = 0;
        for (Vehicle v : vehicles) {
            wait += currentStep - v.getArrivalStep();
        }
        return wait;
    }

    public int size() {
        return vehicles.size();
    }

    public TrafficLight getTrafficLight() {
        return light;
    }

    public LaneType getType() {
        return type;
    }

    public Direction getStart() {
        return start;
    }

    public double getPriority() {
        return basePriority + busCount * Config.current.busPriority;
    }

    public void setPriority(double priority) {
        this.basePriority = priority;
    }

    /**
     * @return whether an emergency vehicle is currently waiting in this lane
     */
    public boolean hasEmergency() {
        return emergencyCount > 0;
    }

    @Override
    public String toString() {
        return start + "-" + type + ": " + vehicles.size() + " vehicles";
    }
}