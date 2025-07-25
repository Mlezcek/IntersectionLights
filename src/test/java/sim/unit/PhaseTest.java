package sim.unit;

import org.junit.jupiter.api.Test;
import sim.Direction;
import sim.Lane;
import sim.LaneType;
import sim.Phase;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

/**
 * Unit tests for {@link Phase} verifying timing logic and statistics
 * collection.
 */
class PhaseTest {

    /**
     * Phase timers should properly track minimum and maximum durations.
     */
    @Test
    void testPhaseTiming() {
        Lane lane1 = new Lane(Direction.NORTH, LaneType.STRAIGHT);
        Lane lane2 = new Lane(Direction.SOUTH, LaneType.STRAIGHT);
        Phase phase = new Phase(Set.of(lane1, lane2), 3, 5);

        assertFalse(phase.isMinTimeReached());
        assertFalse(phase.isMaxTimeExceeded());

        phase.incrementTimer();
        phase.incrementTimer();
        phase.incrementTimer(); // 3 step

        assertTrue(phase.isMinTimeReached());
        assertFalse(phase.isMaxTimeExceeded());

        phase.incrementTimer();
        phase.incrementTimer(); // 5 step

        assertTrue(phase.isMaxTimeExceeded());
    }

    /**
     * Duration and wait times should accumulate into statistics.
     */
    @Test
    void testStatisticsRecording() {
        Lane lane = new Lane(Direction.EAST, LaneType.LEFT);
        Phase phase = new Phase(Set.of(lane), 2, 4);

        phase.incrementActivations();
        phase.addDuration(10);
        phase.recordWaitTime(5);
        phase.recordWaitTime(7);

        assertEquals(1, phase.getTimesActivated());
        assertEquals(10, phase.getTotalDuration());
        assertEquals(6.0, phase.getAvgWaitTime()); // (5+7)/2
    }
}