package sim;

import java.util.*;

/**
 * Core engine that drives the simulation by coordinating the intersection and
 * tracking statistics. Each command supplied to the engine represents an action
 * such as adding a vehicle or advancing the simulation by one step.
 */
public class SimulationEngine {
    private final Intersection intersection;
    private final List<Map<String, Object>> stepStatuses = new ArrayList<>();

    private int currentStep = 0;
    private int totalVehicles = 0;
    private int vehiclesLeft = 0;
    private long totalWaitTimeLeft = 0;
    private int maxWaitTimeLeft = 0;
    private int maxWaitTimeRemaining = 0;
    private final Map<Direction, Integer> vehiclesPerDirection = new EnumMap<>(Direction.class);
    private final Map<String, Vehicle> vehicles = new HashMap<>();

    /**
     * Create a new engine bound to the given intersection.
     *
     * @param intersection intersection to be simulated
     */
    public SimulationEngine(Intersection intersection) {
        this.intersection = intersection;
        for (Direction d : Direction.values()) {
            vehiclesPerDirection.put(d, 0);
        }
    }


    /**
     * Execute a sequence of simulation commands. Supported commands include
     * adding vehicles and advancing the simulation clock with {@code step}
     * operations.
     *
     * @param commands list of command maps describing the simulation input
     */
    public void executeCommands(List<Map<String, Object>> commands) {
        for (Map<String, Object> command : commands) {
            String type = (String) command.get("type");
            switch (type) {
                case "addVehicle": {
                    String id = (String) command.get("vehicleId");
                    Direction start = Direction.valueOf(((String) command.get("startRoad")).toUpperCase());
                    Direction end = Direction.valueOf(((String) command.get("endRoad")).toUpperCase());
                    VehicleType vType = VehicleType.NORMAL;
                    Object vtObj = command.get("vehicleType");
                    if (vtObj instanceof String vtStr) {
                        vType = VehicleType.valueOf(vtStr.toUpperCase());
                    }
                    Vehicle v = new Vehicle(id, start, end, currentStep, vType);
                    intersection.addVehicle(v);
                    vehicles.put(id, v);
                    totalVehicles++;
                    vehiclesPerDirection.put(start, vehiclesPerDirection.get(start) + 1);
                    break;
                }
                case "step": {
                    List<Vehicle> leftVehicles = intersection.step();
                    int phaseIndex = -1;
                    TrafficLightController ctrl = intersection.getController();
                    if (ctrl instanceof ActuatedController ac) {
                        phaseIndex = ac.getCurrentPhaseIndex();
                    }

                    Map<String, Object> status = new HashMap<>();
                    List<String> ids = new ArrayList<>();
                    for (Vehicle v : leftVehicles) {
                        ids.add(v.getId());
                        vehiclesLeft++;
                        int wait = (currentStep + 1) - v.getArrivalStep();
                        totalWaitTimeLeft += wait;
                        if (wait > maxWaitTimeLeft) maxWaitTimeLeft = wait;
                        vehicles.remove(v.getId());
                        if (phaseIndex >= 0 && ctrl instanceof ActuatedController ac) {
                            ac.getPhases().get(phaseIndex).recordWaitTime(wait);

                        }
                    }
                    status.put("leftVehicles", ids);
                    stepStatuses.add(status);
                    currentStep++;
                    break;
                }
                default:
                    System.err.println("Unknown command type: " + type);
            }
        }

        // after processing all commands, finalise stats
        TrafficLightController ctrl = intersection.getController();
        if (ctrl instanceof ActuatedController ac) {
            ac.finalizeCurrentPhase();
        }

        // compute remaining vehicles wait times
        for (Vehicle v : vehicles.values()) {
            int wait = currentStep - v.getArrivalStep();
            if (wait > maxWaitTimeRemaining) maxWaitTimeRemaining = wait;
        }
    }


    /**
     * Return the raw step-by-step output of the simulation.
     * @return map containing status information for each executed step
     */
    public Map<String, Object> getResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("stepStatuses", stepStatuses);
        return result;
    }

    /**
     * Aggregate statistics collected during the run such as average wait times
     * and phase metrics used by integration tests.
     *
     * @return statistics map keyed by descriptive labels
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalVehicles", totalVehicles);
        stats.put("vehiclesLeft", vehiclesLeft);
        stats.put("vehiclesRemaining", totalVehicles - vehiclesLeft);
        stats.put("averageWaitTime", vehiclesLeft == 0 ? 0.0 : (double) totalWaitTimeLeft / vehiclesLeft);
        Map<String, Integer> maxWait = new LinkedHashMap<>();
        maxWait.put("left", maxWaitTimeLeft);
        maxWait.put("remaining", maxWaitTimeRemaining);
        stats.put("maxWaitTime", maxWait);
        stats.put("totalSteps", currentStep);

        Map<String, Object> phasesStats = new LinkedHashMap<>();
        TrafficLightController ctrl = intersection.getController();
        if (ctrl instanceof ActuatedController ac) {
            int idx = 0;
            for (Phase p : ac.getPhases()) {
                Map<String, Object> pMap = new LinkedHashMap<>();
                pMap.put("timesActivated", p.getTimesActivated());
                pMap.put("totalDuration", p.getTotalDuration());
                pMap.put("avgDuration", p.getAvgDuration());
                pMap.put("avgWaitTime", p.getAvgWaitTime());
                phasesStats.put("Phase" + idx, pMap);
                idx++;
            }
        }
        stats.put("phases", phasesStats);

        Map<String, Integer> dirMap = new LinkedHashMap<>();
        for (Direction d : Direction.values()) {
            dirMap.put(d.name(), vehiclesPerDirection.get(d));
        }
        stats.put("vehiclesPerDirection", dirMap);

        return stats;
    }
}