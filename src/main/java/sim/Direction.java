package sim;

/** Directions used to reference roads and vehicle headings. */
public enum Direction {
    NORTH, SOUTH, EAST, WEST;

    public Direction left() {
        switch (this) {
            case NORTH: return WEST;
            case WEST: return SOUTH;
            case SOUTH: return EAST;
            case EAST: return NORTH;
        }
        throw new IllegalStateException();
    }

    public Direction right() {
        switch (this) {
            case NORTH: return EAST;
            case EAST: return SOUTH;
            case SOUTH: return WEST;
            case WEST: return NORTH;
        }
        throw new IllegalStateException();
    }

    public Direction opposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
        }
        throw new IllegalStateException();
    }
}

