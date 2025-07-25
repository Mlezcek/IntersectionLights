package sim;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Configuration parameters for the simulation. */
public class Config {
    /**
     * Default instance used by the simulation.
     */
    public static Config current = new Config();
    /**
     * Enables debug output when true.
     */
    public static boolean debug = false;

    public Config() {
        // Default priorities for every lane
        Map<LaneType, Double> north = new EnumMap<>(LaneType.class);
        north.put(LaneType.LEFT, 1.0);
        north.put(LaneType.STRAIGHT, 1.0);

        Map<LaneType, Double> south = new EnumMap<>(LaneType.class);
        south.put(LaneType.LEFT, 1.0);
        south.put(LaneType.STRAIGHT, 1.0);

        Map<LaneType, Double> east = new EnumMap<>(LaneType.class);
        east.put(LaneType.LEFT, 1.0);
        east.put(LaneType.STRAIGHT, 1.0);

        Map<LaneType, Double> west = new EnumMap<>(LaneType.class);
        west.put(LaneType.LEFT, 1.0);
        west.put(LaneType.STRAIGHT, 1.0);

        lanePriorities.put(Direction.NORTH, north);
        lanePriorities.put(Direction.SOUTH, south);
        lanePriorities.put(Direction.EAST, east);
        lanePriorities.put(Direction.WEST, west);
    }

    /**
     * how long [steps] lights remain yellow during a phase change
     */
    public int yellowDuration = 1;
    /**
     * weight for accumulated waiting time (pressure)
     */
    public double alpha = 2.0;
    /**
     * weight for number of vehicles waiting
     */
    public double beta = 1.0;
    /**
     * weight for how long a phase has been inactive
     */
    public double gamma = 1.0;
    /**
     * maximum steps a phase may be skipped before forced
     */
    public int fairnessCap = 30;
    /**
     * additional priority given to each bus in a lane
     */
    public double busPriority = 2.0;
    /**
     * base priority for every lane
     */
    public Map<Direction, Map<LaneType, Double>> lanePriorities = new EnumMap<>(Direction.class);

    /**
     * timing constraints for each defined phase
     */
    public List<PhaseTime> phases = new ArrayList<>(List.of(
            new PhaseTime(2, 5),
            new PhaseTime(2, 5),
            new PhaseTime(2, 5),
            new PhaseTime(2, 5)
    ));

    public static class PhaseTime {
        /**
         * minimum time [steps] the phase stays green
         */
        public int minGreen;
        /**
         * maximum time [steps] the phase may stay green
         */
        public int maxGreen;

        public PhaseTime() {
        }

        public PhaseTime(int minGreen, int maxGreen) {
            this.minGreen = minGreen;
            this.maxGreen = maxGreen;
        }
    }

    /**
     * Load configuration from a JSON file. Any values not present in the file
     * keep their default values.
     */
    public static Config load(String file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Config defaults = new Config();
        mapper.readerForUpdating(defaults).readValue(new File(file));
        current = defaults;
        return defaults;
    }

    public double getLanePriority(Direction dir, LaneType type) {
        Map<LaneType, Double> map = lanePriorities.get(dir);
        if (map == null) return 1.0;
        return map.getOrDefault(type, 1.0);
    }
}