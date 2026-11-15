package restaurant.model;

public abstract class Staff {
    private boolean available;

    protected Staff() {
        this.available = true;
    }

    public final void beginTurn() {
        available = true;
    }

    public final boolean isAvailable() {
        return available;
    }

    protected final void requireAvailable() {
        if (!available) {
            throw new IllegalStateException("Staff member already acted this turn.");
        }
    }

    protected final void recordSuccessfulTask() {
        available = false;
    }
}
