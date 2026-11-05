package restaurant.model;

/**
 * Shared one-task allowance, not a list of unrelated staff operations.
 * LSP: every staff subtype honours the same availability/reset contract.
 * ISP: there is no cook() or seat() here forcing unrelated roles to implement it.
 * This small inheritance relationship reuses a real shared rule.
 */
public abstract class Staff {
    private boolean used;

    public final void beginTurn() { used = false; }
    public final boolean available() { return !used; }

    protected final void requireAvailable() {
        if (used) throw new IllegalStateException("Staff member already acted this turn.");
    }

    protected final void recordSuccessfulTask() { used = true; }
}
