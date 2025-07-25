package sim;

import java.util.Map;
import java.util.Set;

/**
 * Defines how an intersection decides which lanes receive green lights.
 */
public interface TrafficLightController {

    /**
     * Determine the set of lanes that should be granted a green signal on this
     * step.
     *
     * @param roads mapping of all roads feeding the intersection
     * @return lanes that are allowed to proceed
     */
    Set<Lane> getGreenLanes(Map<Direction, Road> roads);

    /**
     * Notification that an emergency vehicle has entered a lane. Controllers may
     * override to provide priority handling.
     */
    default void emergencyVehicleArrived(Lane lane) {}


}