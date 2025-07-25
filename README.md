# Traffic Light Simulation

This project provides a intersection simulator written in Java. It models
vehicles arriving from four directions and uses a flexible controller to
determine which lanes receive a green signal each step. The code is organized with unit and integration tests.

## Features

- Actuated traffic light controller with weighted priorities
- Support for emergency vehicles and bus preference
- Conflict monitor that detects unsafe green combinations and enforces a blinking mode
- JSON-based configuration and command input
- Detailed statistics written to `output_stats.json`
- Comprehensive unit and integration tests

## Table of Contents

- [Project Structure](#project-structure)
- [Core Components](#core-components)
- [Running with Docker](#running-with-docker)
- [Building](#building)
- [Running the Simulation](#running-the-simulation)
- [Command Format](#command-format)
- [Output](#output)
- [ActuatedController](#actuatedcontroller)
- [Configuration](#configuration)
- [Testing](#testing)

## Project Structure

- `src/main/java/sim` – core simulation classes
- `src/test/java/sim` – unit and integration tests
- `config.json` – default configuration parameters
- `input.json` – example set of simulation commands
- `pom.xml` – Maven build file

## Core Components

The core components are:

- **Intersection** – maintains roads, lanes and the active traffic light controller.
- **SimulationEngine** – executes command sequences and records results.
- **ActuatedController** – decides which lanes receive a green light each step.
- **ConflictMonitor** – checks for conflicting greens and resets lights when detected.
- **Config** – configuration for lane priorities, phase timings and controller weights.

# Running with Docker

A `Dockerfile` is provided so the simulator can be run without a local Java or Maven installation.

Build the image:

```bash
docker build -t traffic-light .
```

Run the container using the included `input.json`:

```bash
docker run --rm traffic-light
```

To run with custom files, mount them and override the command. For example:

```bash
docker run --rm -v $(pwd)/my_input.json:/app/input.json traffic-light \
  java -jar app.jar input.json my_output.json
```

## Building

The project targets **Java 21**. Use Maven to compile and package the simulator:

```bash
mvn package
```

This creates a runnable JAR in `target/`.

## Running the Simulation

The `Main` class expects an input file describing the command sequence and an
output file to store the results:

```bash
java -jar target/trafficlight-1.0-SNAPSHOT.jar input.json output.json [--config config.json] [--debug true|false]
```

If a configuration file is provided, any values missing from it fall back to the
defaults defined in `Config`.

Passing `--debug true` enables verbose console output showing each step of the
simulation. Omitting the flag or passing `false` disables this output.

### Command Format

The simulator processes a list of commands such as:

```json
{
  "commands": [
    { "type": "addVehicle", "vehicleId": "V1", "startRoad": "NORTH", "endRoad": "SOUTH" },
    { "type": "step" },
    { "type": "step" }
  ]
}
```

`addVehicle` entries create a vehicle with optional `vehicleType` (`NORMAL`,
`BUS`, or `EMERGENCY`). `step` advances the simulation by one tick.

### Output

The simulation writes `output.json` describing which vehicles left the
intersection on each step. An additional `output_stats.json` file summarises
statistics such as average wait times and phase activations.


<a id="actuatedcontroller"></a>
## ActuatedController

For a deeper explanation of the traffic light control logic and why this design was chosen, see [docs/TrafficLightAlgorithm.md](docs/TrafficLightAlgorithm.md)

`ActuatedController` implements the `TrafficLightController` interface and provides 
adaptive signal control based on lane pressure and fairness. 
The controller builds four phases on construction:

1. North–South straight
2. East–West straight
3. North–South left turns
4. East–West left turns

Each phase specifies its minimum and maximum green duration. At runtime the
controller decides when to switch phases based on three weighted factors:

- **Pressure** – total waiting time of vehicles in the phase (α)
- **Vehicles waiting** – current queue sizes (β)
- **Fairness** – how long a phase has been inactive (γ)

These weights are configured via `Config` and combined to compute a priority for
each phase. The algorithm proceeds as follows for every simulation step:

1. **Emergency handling** – If an emergency vehicle is present, its lane receives
   an exclusive green light until clear.
2. **Yellow transition** – When switching, the previous phase remains yellow for
   `yellowDuration` steps before the next phase turns green.
3. **Priority evaluation** – Once the minimum green time is satisfied, the
   controller compares priorities of all phases. 
4. **Fairness Cap** – If a phase has been inactive longer than `fairnessCap` steps and 
    vehicles are waiting, it becomes a candidate for immediate activation regardless of 
    priority.
5. **Switch or stay** – If another phase has higher priority or fairness demands
   it, the controller schedules a switch if the current phase has reached its minimum green 
   time; otherwise it continues with the current phase.

By tracking activation counts and waiting times, the controller adapts to real‑
time traffic while ensuring every direction eventually receives service.

## Configuration

Parameters controlling the simulation are loaded from an optional JSON file. Any
fields not present use the defaults provided by `Config`.

Key options include:

- `yellowDuration` – how long [steps] lights remain yellow before switching to red
- `alpha`, `beta`, `gamma` – weights for pressure, queue length and fairness
- `fairnessCap` –  maximum number of steps a phase may be skipped
- `busPriority` – extra priority for vehicles with BUS type
- `lanePriorities` –  maximum number of steps a phase may be skipped
- `phases` – list of phases with minimum and maximum green durations

Check `config.json` for an example and default values.

## Testing

Unit and integration tests are implemented with JUnit 5. Run all tests with:

```bash
mvn test
```


