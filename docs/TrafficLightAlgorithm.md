# Traffic Light Control Algorithm

This document describes the control algorithm used by the ActuatedController and explains why some strategies were used.

## Motivation for an Adaptive Controller

Key goals of this algorithm are:

- **Responsiveness:** adapt to changing vehicle volumes without requiring manual tuning for each scenario.
- **Fairness:** ensure all directions eventually receive service so no lane starves.
- **Support for priority traffic:** handle emergency vehicles immediately and give optional weighting to buses.

## Phase Structure

The intersection is modelled with four logical phases:

1. North–South straight lanes
2. East–West straight lanes
3. North–South left turns
4. East–West left turns

Each phase defines minimum and maximum green durations configured in `config.json`. The controller cycles through these phases based on calculated priorities. Limiting the green time prevents a single phase from monopolising the intersection, while a minimum ensures a phase remains green long enough for vehicles to move safely.

## Priority Calculation

When deciding whether to remain on the current phase or switch, the controller computes a **priority value** for each phase using three weighted components:

1. **Pressure** (`α`): total waiting time of all vehicles in the phase\'s lanes, multiplied by each lane\'s priority. This favours lanes that have accumulated long queues.
2. **Vehicles waiting** (`β`): the current number of vehicles in the phase\'s lanes, again weighted by lane priority.
3. **Fairness** (`γ`): how many steps have passed since the phase was last active. This prevents phases with infrequent traffic from being ignored indefinitely.

The final priority is `α * pressure + β * vehiclesWaiting + γ * fairness`. The weights `α`, `β` and `γ` are configurable to emphasise throughput or fairness.

## Fairness Cap

Even with the weighted priority, a heavily used phase might dominate the intersection. To guard against starvation, the controller enforces a **fairness cap**. If a phase has been inactive longer than the configured cap and vehicles are waiting, it becomes eligible for immediate activation regardless of priority comparison. The phase with the highest waiting pressure is selected when multiple phases hit the cap simultaneously.

## Emergency and Bus Handling

Emergency vehicles require immediate right-of-way. When such a vehicle appears, its lane receives a dedicated green light until the emergency vehicle clears the intersection. Other traffic waits, ensuring the fastest possible response.

Buses can optionally receive extra weight via lane priorities. By increasing the priority on bus lanes, the algorithm naturally favours those lanes in both the pressure and vehicles-waiting terms without hard-coding special behaviour.

## Yellow Transition

To model realistic signal transitions the controller inserts a yellow interval when changing phases. After the current green ends, lights remain yellow for `yellowDuration` steps before the new phase turns green. This delay simulates real-world caution time and prevents conflicting movements.

## Why This Algorithm

The combination of pressure, queue length and fairness delivers a balanced approach:

- **Adaptive:** Because priorities depend on real-time conditions, heavy traffic quickly attracts more green time, improving throughput.
- **Predictable:** Minimum and maximum green windows ensure each phase behaves consistently and avoids overly rapid switching.
- **Fair:** The fairness term and cap guarantee that even rarely used lanes eventually receive service.
- **Extensible:** Weight parameters and lane priorities make it easy to tailor behaviour for different intersections without rewriting code.
- **Safety conscious:** Emergency handling and yellow transitions model typical road rules, keeping the simulation realistic.

Alternative approaches lead to unpredictable behaviour. The weighted-actuated method provides a good compromise between performance and fairness while remaining simple enough to configure and understand.

## Conclusion

The actuated controller offers a versatile solution for traffic light management in the simulation. By monitoring lane pressure, queue sizes and fairness, it adapts to traffic demand while preventing starvation. Emergency vehicles and buses are accommodated through priority mechanisms, and configurable timing parameters allow the algorithm to be tuned for diverse scenarios. 

