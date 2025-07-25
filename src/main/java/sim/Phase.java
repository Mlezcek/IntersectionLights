package sim;

import java.util.Set;

/**
 * Describes a group of non-conflicting lanes that can receive a green light
 * simultaneously. Tracks activation statistics used by the controller.
 */

public class Phase {
    private final Set<Lane> lanes;
    private final int minGreen;
    private final int maxGreen;
    private int timeGreen = 0;
    private int timesActivated = 0;
    private int totalDuration = 0;
    private long totalWaitTime = 0;
    private int vehiclesPassed = 0;


    /**
     * Construct a phase consisting of the given lanes and timing constraints.
     */
    public Phase(Set<Lane> lanes, int minGreen, int maxGreen) {
        this.lanes = lanes;
        this.minGreen = minGreen;
        this.maxGreen = maxGreen;
    }


    public void resetTimer() {
        timeGreen = 0;
    }

    public void incrementActivations() {
        timesActivated++;
    }

    public void addDuration(int duration) {
        totalDuration += duration;
    }

    public void recordWaitTime(long wait) {
        totalWaitTime += wait;
        vehiclesPassed++;
    }

    public void incrementTimer() {
        timeGreen++;
    }

    public boolean isMinTimeReached() {
        return timeGreen >= minGreen;
    }

    public boolean isMaxTimeExceeded() {
        return timeGreen >= maxGreen;
    }

    public Set<Lane> getLanes() {
        return lanes;
    }

    public int getTimeGreen() {
        return timeGreen;
    }

    public int getTimesActivated() {
        return timesActivated;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public double getAvgDuration() {
        return timesActivated == 0 ? 0.0 : (double) totalDuration / timesActivated;
    }

    public double getAvgWaitTime() {
        return vehiclesPassed == 0 ? 0.0 : (double) totalWaitTime / vehiclesPassed;
    }


}