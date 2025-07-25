package sim;

import java.util.*;


/**
 * Traffic light controller that dynamically selects phases based on
 * lane pressure, waiting vehicles and fairness constraints.
 */
public class ActuatedController implements TrafficLightController {
    private final List<Phase> phases;
    private int currentPhaseIndex = 0;
    private int nextPhaseIndex = -1;
    private int yellowTimer = 0;
    private final int yellowDuration;
    private final Map<Direction, Road> roads;
    private final int[] stepsSinceActivation;
    private int currentStep = 0;
    private final double alpha; //Pressure
    private final double beta; //Vehicles waiting
    private final double gamma; //Fairness
    private final int fairnessCap;
    private final Deque<Lane> emergencyQueue = new ArrayDeque<>();
    private Lane currentEmergencyLane = null;


    /**
     * Create a controller using the global {@link Config} parameters and the
     * supplied road map.
     *
     * @param roads mapping of directions to their corresponding road objects
     */
    public ActuatedController(Map<Direction, Road> roads) {
        this.roads = roads;

        Config cfg = Config.current;
        this.yellowDuration = cfg.yellowDuration;
        this.alpha = cfg.alpha;
        this.beta = cfg.beta;
        this.gamma = cfg.gamma;
        this.fairnessCap = cfg.fairnessCap;

        this.phases = List.of(
                new Phase(
                        Set.of(
                                roads.get(Direction.NORTH).getLane(LaneType.STRAIGHT),
                                roads.get(Direction.SOUTH).getLane(LaneType.STRAIGHT)
                        ),
                        cfg.phases.get(0).minGreen,
                        cfg.phases.get(0).maxGreen
                ),
                new Phase(
                        Set.of(
                                roads.get(Direction.EAST).getLane(LaneType.STRAIGHT),
                                roads.get(Direction.WEST).getLane(LaneType.STRAIGHT)
                        ),
                        cfg.phases.get(1).minGreen,
                        cfg.phases.get(1).maxGreen
                ),
                new Phase(
                        Set.of(
                                roads.get(Direction.NORTH).getLane(LaneType.LEFT),
                                roads.get(Direction.SOUTH).getLane(LaneType.LEFT)
                        ),
                        cfg.phases.get(2).minGreen,
                        cfg.phases.get(2).maxGreen
                ),
                new Phase(
                        Set.of(
                                roads.get(Direction.EAST).getLane(LaneType.LEFT),
                                roads.get(Direction.WEST).getLane(LaneType.LEFT)
                        ),
                        cfg.phases.get(3).minGreen,
                        cfg.phases.get(3).maxGreen
                )
        );

        this.stepsSinceActivation = new int[phases.size()];

        // initial phase is active at start
        phases.get(currentPhaseIndex).incrementActivations();
    }


    /**
     * Queue a lane for immediate service when an emergency vehicle arrives.
     * Duplicate notifications are ignored.
     */
    @Override
    public void emergencyVehicleArrived(Lane lane) {
        if (!emergencyQueue.contains(lane)) {
            emergencyQueue.offer(lane);
        }
    }

    /**
     * Determine which lanes should receive a green light for the current step.
     * The algorithm considers emergency vehicles, phase timing constraints and
     * weighted priorities.
     */
    @Override
    public Set<Lane> getGreenLanes(Map<Direction, Road> roads) {
        currentStep++;
        for (int i = 0; i < stepsSinceActivation.length; i++) {
            stepsSinceActivation[i]++;
        }
        // Handle emergency vehicles
        if (currentEmergencyLane != null && !currentEmergencyLane.hasEmergency()) {
            emergencyQueue.poll();
            currentEmergencyLane = null;
        }
        if (currentEmergencyLane == null) {
            while (!emergencyQueue.isEmpty()) {
                Lane cand = emergencyQueue.peek();
                if (cand.hasEmergency()) {
                    currentEmergencyLane = cand;
                    break;
                } else {
                    emergencyQueue.poll();
                }
            }
        }
        if (currentEmergencyLane != null) {
            return Set.of(currentEmergencyLane);
        }

        if (yellowTimer > 0) {
            yellowTimer--;
            if (yellowTimer == 0 && nextPhaseIndex >= 0) {
                currentPhaseIndex = nextPhaseIndex;
                nextPhaseIndex = -1;
                Phase newPhase = phases.get(currentPhaseIndex);
                newPhase.resetTimer();
                newPhase.incrementActivations();
                stepsSinceActivation[currentPhaseIndex] = 0;
            }
            return Set.of();
        }

        stepsSinceActivation[currentPhaseIndex] = 0;

        Phase current = phases.get(currentPhaseIndex);
        current.incrementTimer();

        double currentPriority = calculatePriority(currentPhaseIndex);
        double bestOtherPriority = currentPriority;
        int bestOtherIndex = currentPhaseIndex;

        // Fairness cap selection
        int fairnessCandidateIndex = -1;
        double maxFairnessPressure = -1;

        for (int i = 0; i < phases.size(); i++) {
            Phase phase = phases.get(i);
            int fairness = stepsSinceActivation[i];

            boolean hasVehicles = phase.getLanes().stream().anyMatch(l -> l.size() > 0);

            if (fairness >= fairnessCap && hasVehicles) {
                double pressure = calculatePressure(phase);
                if (pressure > maxFairnessPressure) {
                    maxFairnessPressure = pressure;
                    fairnessCandidateIndex = i;
                }
            }

            // Regular priority evaluation
            double priority = calculatePriority(i);
            if (i != currentPhaseIndex && priority > bestOtherPriority) {
                bestOtherPriority = priority;
                bestOtherIndex = i;
            }

            if (Config.debug) {
                System.out.printf(
                        "Phase %d (%s): priority=%.2f, timer=%d, fairness=%d%n",
                        i,
                        phase.getLanes(),
                        priority,
                        phase.getTimeGreen(),
                        fairness
                );
            }
        }

        boolean minReached = current.isMinTimeReached();
        boolean maxReached = current.isMaxTimeExceeded();
        boolean switchByPriority = bestOtherIndex != currentPhaseIndex && bestOtherPriority > currentPriority;

        int targetIndex = -1;

        if (fairnessCandidateIndex != -1) {
            targetIndex = fairnessCandidateIndex;
            if (Config.debug) {
                System.out.printf("-> Fairness cap reached by phase %d%n", targetIndex);
            }
        } else if (minReached && (maxReached || switchByPriority)) {
            targetIndex = bestOtherIndex;
        }

        if (targetIndex != -1 && targetIndex != currentPhaseIndex && minReached) {
            if (Config.debug) {
                System.out.printf("-> Switching phase from %s to %s%n",
                        current.getLanes(), phases.get(targetIndex).getLanes());
            }
            current.addDuration(current.getTimeGreen());
            nextPhaseIndex = targetIndex;
            yellowTimer = yellowDuration;
        } else {
            if (Config.debug) {
                System.out.printf("-> Staying on current phase: %s%n", current.getLanes());
            }
        }

        return phases.get(currentPhaseIndex).getLanes();
    }



    /**
     * Compute the accumulated waiting pressure for a phase. Each lane
     * contributes its total waiting time weighted by lane priority.
     */
    private double calculatePressure(Phase phase) {
        double total = 0.0;
        for (Lane lane : phase.getLanes()) {
            total += lane.getTotalWaitingTime(currentStep) * lane.getPriority();
        }
        return total;
    }


    /**
     * Overall priority used to compare phases. Combines waiting pressure,
     * number of vehicles and a fairness factor.
     */
    private double calculatePriority(int index) {
        Phase phase = phases.get(index);
        double pressure = calculatePressure(phase);
        double vehiclesWaiting = 0.0;
        for (Lane lane : phase.getLanes()) {
            vehiclesWaiting += lane.size() * lane.getPriority();
        }
        int fairness = stepsSinceActivation[index];

        return alpha * pressure + beta * vehiclesWaiting + gamma * fairness;
    }

    public int getCurrentPhaseIndex() {
        return currentPhaseIndex;
    }

    public List<Phase> getPhases() {
        return phases;
    }

    public void finalizeCurrentPhase() {
        Phase current = phases.get(currentPhaseIndex);
        current.addDuration(current.getTimeGreen());
    }
}