package sim;

import java.util.*;


/**
 * Representation of a four-way intersection. Each direction contains a pair of
 * lanes and the instance is governed by a {@link TrafficLightController} that
 * decides which lanes receive green lights at each step.
 */
public class Intersection {
    private final Map<Direction, Road> roads = new EnumMap<>(Direction.class);
    private TrafficLightController controller;
    private final ConflictMonitor monitor = new ConflictMonitor();

    /**
     * Construct an intersection with the provided traffic light controller.
     *
     * @param controller strategy used to determine which lanes are given right
     *                   of way
     */
    public Intersection(TrafficLightController controller) {
        this.controller = controller;
        for (Direction d : Direction.values()) {
            roads.put(d, new Road(d));
        }
    }

    /**
     * Apply priority weighting to individual lanes. These priorities influence
     * the controller's phase-selection logic.
     *
     * @param priorities mapping of directions and lane types to priority values
     */
    public void applyLanePriorities(Map<Direction, Map<LaneType, Double>> priorities) {
        for (Direction d : Direction.values()) {
            Road road = roads.get(d);
            if (road == null) continue;
            Map<LaneType, Double> map = priorities.get(d);
            if (map == null) continue;
            for (LaneType lt : LaneType.values()) {
                Double val = map.get(lt);
                if (val != null) {
                    road.getLane(lt).setPriority(val);
                }
            }
        }
    }

    /**
     * Add a vehicle to the appropriate lane. Emergency vehicles trigger an
     * immediate notification to the controller.
     *
     * @param vehicle vehicle entering the intersection
     */
    public void addVehicle(Vehicle vehicle) {
        Road road = roads.get(vehicle.getStart());
        Lane lane = road.addVehicle(vehicle);
        if (vehicle.getType() == VehicleType.EMERGENCY && controller != null) {
            controller.emergencyVehicleArrived(lane);
        }
    }

    /**
     * Advance the simulation by one time step. Traffic lights are updated and
     * vehicles with a green light are allowed to pass.
     *
     * @return list of vehicles that left the intersection during this step
     */
    public List<Vehicle> step() {
        monitor.beforeStep(this);
        // Step 1: update lights (yellow -> red)
        for (Road road : roads.values()) {
            for (Lane lane : road.getLanes()) {
                lane.getTrafficLight().step();
            }
        }
        // Step 2: controller selects lanes that will turn green
        Set<Lane> newGreenLanes = controller.getGreenLanes(roads);

        // Step 3: apply new light states
        for (Road road : roads.values()) {
            for (Lane lane : road.getLanes()) {
                TrafficLight light = lane.getTrafficLight();
                if (light.getState() == TrafficLightState.GREEN && !newGreenLanes.contains(lane)) {
                    light.setYellow();
                } else if (light.getState() == TrafficLightState.RED && newGreenLanes.contains(lane)) {
                    light.setGreen();
                }
            }
        }

        monitor.afterStep(this);

        // Step 5: vehicles move only on green
        if (Config.debug) {
            System.out.println("=== INTERSECTION STATE ===");
            for (Road road : roads.values()) {
                for (Lane lane : road.getLanes()) {
                    TrafficLight light = lane.getTrafficLight();
                    System.out.printf("[%s-%s] Light: %-6s | Queue: %d%n",
                            road.getDirection(), lane.getType(), light.getState(), lane.size());
                }
            }
        }

        // Step 6 (debug): list vehicles that left
        List<Vehicle> leftVehicles = new ArrayList<>();
        for (Road road : roads.values()) {
            for (Lane lane : road.getLanes()) {
                if (lane.getTrafficLight().getState() == TrafficLightState.GREEN) {
                    for (int i = 0; i < Config.current.vehiclesPerStep; i++) {
                        Vehicle vehicle = lane.pollVehicle();
                        if (vehicle != null) {
                            leftVehicles.add(vehicle);
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        if (Config.debug) {
            System.out.println("Vehicles that left: " + leftVehicles.stream().map(Vehicle::getId).toList());
            System.out.println("----------------------------");
        }

        return leftVehicles;
    }


    public Map<Direction, Road> getRoads() {
        return roads;
    }

    public void setController(TrafficLightController controller) {
        this.controller = controller;
    }

    public TrafficLightController getController() {
        return controller;
    }

}