# Integration Test Scenarios

This document outlines the integration tests used to validate the traffic simulation engine. Each test drives the `SimulationEngine` with a predefined sequence of commands and verifies that the intersection controller behaves as expected.

## Running the Tests

Tests are implemented with JUnit 5 and executed via Maven. From the project root run:

```bash
mvn test
```

This will compile the application and run all integration and unit tests. The integration tests reside under `src/test/java/sim/integration`.

## Scenario 1: Basic Traffic Flow
**Description:** Validate standard vehicle progression through a single phase.

**Commands**
1. Add a normal vehicle travelling from North to South.
2. Execute one simulation step.
3. Execute a second simulation step.
4. Check the aggregated statistics.

**Expected Result**
- The vehicle waits for exactly one step before passing through the intersection.
- Statistics report one processed vehicle with an average wait time of one step.

## Scenario 2: Basic Phase Rotation
**Description:** Ensure the controller rotates through phases when vehicles are present.

**Commands**
1. Add vehicles to the North and South straight lanes.
2. Step the simulation until the phase changes.
3. Add vehicles to the East and West straight lanes.
4. Continue stepping the simulation until the phase changes again.

**Expected Result**
- The initial green phase serves the North–South straight lanes.
- After the minimum green time, the controller switches to the East–West straight phase once vehicles are detected.
- Each phase honours its configured minimum and maximum green times.

## Scenario 3: Emergency Vehicle Priority
**Description:** Emergency vehicles should obtain immediate right-of-way.

**Commands**
1. Add normal vehicles to the East straight lane and step once.
2. Add an emergency vehicle to the North straight lane.
3. Step the simulation and observe the phase change.

**Expected Result**
- The current phase is interrupted when the emergency vehicle arrives.
- The North lane receives a green light immediately so the emergency vehicle can pass.
- Once clear, the controller resumes normal operation.

## Scenario 4: Lane Priority Handling
**Description:** Verify that lane priority settings influence phase selection and duration.

**Commands**
1. Configure the East straight lane with a higher priority.
2. Add multiple vehicles to the North straight lane.
3. Add a smaller number of vehicles to the East straight lane.
4. Step the simulation through several cycles.

**Expected Result**
- The East straight phase receives a longer effective green time.
- The controller favours the higher‑priority lane while still respecting minimum green times for other phases.

## Scenario 5: Fairness Cap Enforcement
**Description:** Confirm that the fairness cap prevents starvation of less active phases.

**Commands**
1. Set a low fairness cap (for example, three steps).
2. Continuously add vehicles to the North straight lane.
3. Step through many simulation iterations.

**Expected Result**
- Even with constant North traffic, the controller eventually switches to other phases when the fairness cap is reached.
- Multiple phases become active during the test, demonstrating that no single phase can monopolize the intersection.

## Scenario 6: Mixed Traffic Simulation
**Description:** Exercise the engine with a combination of normal, bus and emergency vehicles.

**Commands**
1. Add a normal vehicle travelling North to South.
2. Add a bus travelling East to West.
3. Add an emergency vehicle travelling South to North.
4. Execute multiple simulation steps.

**Expected Result**
- All vehicle types are processed correctly.
- Emergency priority and bus weighting interact without blocking normal traffic.

---
These scenarios form the basis of the integration test suite located in `src/test/java/sim/integration`. Each scenario corresponds to a JSON command file in `src/test/java/sim/integration/resources/` and a JUnit test class that drives the simulation engine.
