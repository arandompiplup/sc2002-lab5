package restaurant.model;

public abstract class Staff {
    private boolean available;

    public final void beginTurn() { throw new UnsupportedOperationException("Not implemented"); }

    public final boolean isAvailable() { throw new UnsupportedOperationException("Not implemented"); }

    protected final void requireAvailable() { throw new UnsupportedOperationException("Not implemented"); }

    protected final void markSuccessfulTask() { throw new UnsupportedOperationException("Not implemented"); }
}
