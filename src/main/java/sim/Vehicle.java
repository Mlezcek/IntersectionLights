package sim;



/**
 * Representation of a vehicle travelling through the intersection.
 */
public class Vehicle {
    private final String id;
    private final Direction start;
    private final Direction end;
    private final int arrivalStep;
    private final VehicleType type;

    /**
     * Create a new vehicle with identifying information and its intended route.
     */
    public Vehicle(String id, Direction start, Direction end, int arrivalStep, VehicleType type) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.arrivalStep = arrivalStep;
        this.type = type;
    }

    public String getId() { return id; }
    public Direction getStart() { return start; }
    public Direction getEnd() { return end; }
    public int getArrivalStep() { return arrivalStep; }
    public VehicleType getType() { return type; }
}
