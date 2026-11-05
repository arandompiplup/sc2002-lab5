package restaurant.model;

public abstract class Staff {
    private boolean used;

    public final void beginTurn() { throw new UnsupportedOperationException("Not implemented"); }

    public final boolean available() { throw new UnsupportedOperationException("Not implemented"); }

    protected final void requireAvailable() { throw new UnsupportedOperationException("Not implemented"); }

    protected final void recordSuccessfulTask() { throw new UnsupportedOperationException("Not implemented"); }
}
