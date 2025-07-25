package sim;


/**
 * State machine representing a single traffic light with green, yellow, blinking
 * and red states. The light transitions from yellow to red automatically after
 * the configured duration.
 */
public class TrafficLight {
    private TrafficLightState state = TrafficLightState.RED;
    private int yellowTimer = 0;

    public TrafficLightState getState() {
        return state;
    }

    public void setGreen() {
        state = TrafficLightState.GREEN;
        yellowTimer = 0;
    }

    public void setYellow() {
        state = TrafficLightState.YELLOW;
        yellowTimer = Config.current.yellowDuration;
    }

    public void setRed() {
        state = TrafficLightState.RED;
        yellowTimer = 0;
    }

    public void setBlinking() {
        state = TrafficLightState.BLINKING;
        yellowTimer = 0;
    }

    public boolean isYellowExpired() {
        return state == TrafficLightState.YELLOW && yellowTimer <= 0;
    }

    public void step() {
        if (state == TrafficLightState.YELLOW) {
            yellowTimer--;
            if (yellowTimer <= 0) {
                setRed();
            }
        }
    }
}