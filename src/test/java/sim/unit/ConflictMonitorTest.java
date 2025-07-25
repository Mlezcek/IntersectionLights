package sim.unit;


import org.junit.jupiter.api.Test;
import sim.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for {@link ConflictMonitor}
 */
public class ConflictMonitorTest {

    private static class OnceConflictingController implements TrafficLightController {
        private boolean first = true;
        @Override
        public Set<Lane> getGreenLanes(Map<Direction, Road> roads) {
            if (first) {
                first = false;
                Set<Lane> lanes = new HashSet<>();
                lanes.add(roads.get(Direction.NORTH).getLane(LaneType.STRAIGHT));
                lanes.add(roads.get(Direction.EAST).getLane(LaneType.STRAIGHT));
                return lanes;
            }
            return Set.of();
        }
    }

    @Test
    public void monitorDetectsAndResetsConflict() {
        Intersection intersection = new Intersection(new OnceConflictingController());

        intersection.step();
        for (Road road : intersection.getRoads().values()) {
            for (Lane lane : road.getLanes()) {
                assertEquals(TrafficLightState.BLINKING, lane.getTrafficLight().getState());
            }
        }

        intersection.step();
        for (Road road : intersection.getRoads().values()) {
            for (Lane lane : road.getLanes()) {
                assertEquals(TrafficLightState.RED, lane.getTrafficLight().getState());
            }
        }
    }
}