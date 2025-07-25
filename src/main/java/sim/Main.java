package sim;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Command-line entry point that loads command sequences from JSON and runs the
 * simulation. Results and statistics are written to output files.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 2 || args.length % 2 != 0) {
            System.err.println("Usage: java -jar simulator.jar input.json output.json [--config config.json] [--debug true|false]");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        for (int i = 2; i < args.length; i += 2) {
            String opt = args[i];
            if (i + 1 >= args.length) {
                System.err.println("Invalid arguments");
                return;
            }
            switch (opt) {
                case "--config" -> Config.load(args[i + 1]);
                case "--debug" -> Config.debug = Boolean.parseBoolean(args[i + 1]);
                default -> {
                    System.err.println("Unknown option: " + opt);
                    return;
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> input = mapper.readValue(new File(inputFile), Map.class);
        List<Map<String, Object>> commands = (List<Map<String, Object>>) input.get("commands");

        Intersection intersection = new Intersection(null);
        intersection.applyLanePriorities(Config.current.lanePriorities);
        TrafficLightController controller = new ActuatedController(intersection.getRoads());
        intersection.setController(controller);

        SimulationEngine engine = new SimulationEngine(intersection);
        engine.executeCommands(commands);

        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFile), engine.getResult());

        // write statistics file alongside output
        File statsFile = new File("output_stats.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(statsFile, engine.getStats());
    }
}