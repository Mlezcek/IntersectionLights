package sim;

import java.util.Map;

/**
 * Monitor that checks for conflicting green lights on every step.
 * If a conflict is detected the whole intersection is put into
 * blinking mode for one step and then reset to all red.
 */
public class ConflictMonitor {
    private boolean blinking = false;

    /**
     * Should be called at the beginning of every step to reset the
     * intersection after blinking.
     */
    public void beforeStep(Intersection intersection) {
        if (blinking) {
            for (Road r : intersection.getRoads().values()) {
                for (Lane l : r.getLanes()) {
                    l.getTrafficLight().setRed();
                }
            }
            blinking = false;
        }
    }

    /**
     * Called after the lights have been updated to detect conflicts.
     */
    public void afterStep(Intersection intersection) {
        if (blinking) {
            return;
        }
        if (hasConflict(intersection)) {
            blinking = true;
            for (Road r : intersection.getRoads().values()) {
                for (Lane l : r.getLanes()) {
                    l.getTrafficLight().setBlinking();
                }
            }
        }
    }

    private boolean hasConflict(Intersection intersection) {
        boolean nsStraight = false;
        boolean ewStraight = false;
        boolean nsLeft = false;
        boolean ewLeft = false;
        for (Map.Entry<Direction, Road> e : intersection.getRoads().entrySet()) {
            Direction dir = e.getKey();
            Road road = e.getValue();
            for (Lane lane : road.getLanes()) {
                if (lane.getTrafficLight().getState() == TrafficLightState.GREEN) {
                    boolean vertical = dir == Direction.NORTH || dir == Direction.SOUTH;
                    if (lane.getType() == LaneType.STRAIGHT) {
                        if (vertical) nsStraight = true; else ewStraight = true;
                    } else {
                        if (vertical) nsLeft = true; else ewLeft = true;
                    }
                }
            }
        }
        int active = 0;
        if (nsStraight) active++;
        if (ewStraight) active++;
        if (nsLeft) active++;
        if (ewLeft) active++;
        return active > 1;
    }
}