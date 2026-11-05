package restaurant.model;

import java.util.Objects;

/** Entity: preparation and payment status belong here, not in the CLI. */
public final class Order {
    public enum Status { PREPARING, READY, SERVED, PAID, CANCELLED }

    // DIP/OCP: preparation needs an item's contract, not a particular enum value.
    private final MenuItemInterface item;
    private final int billCents;
    private int unitsPrepared;
    private int readyTurn = -1;
    private Status status = Status.PREPARING;

    public Order(MenuItemInterface item) {
        this.item = Objects.requireNonNull(item, "Menu item is required.");
        if (item.priceCents() <= 0 || item.preparationUnits() <= 0)
            throw new IllegalArgumentException("Price and preparation units must be positive.");
        billCents = item.priceCents();
    }

    public MenuItemInterface item() { return item; }
    public int unitsPrepared() { return unitsPrepared; }
    public Status status() { return status; }
    public int billCents() { return billCents; }
    public boolean canBeServed(int turn) { return status == Status.READY && readyTurn < turn; }

    public void prepareOneUnit(int turn) {
        if (turn < 1) throw new IllegalArgumentException("Turn must be positive.");
        requireStatus(Status.PREPARING, "Order is not being prepared.");
        unitsPrepared++;
        if (unitsPrepared == item.preparationUnits()) {
            status = Status.READY;
            readyTurn = turn;
        }
    }

    public void serve(int turn) {
        if (!canBeServed(turn)) throw new IllegalStateException("Order is not ready from an earlier turn.");
        status = Status.SERVED;
    }

    public void markPaid() {
        requireStatus(Status.SERVED, "Order has not been served.");
        status = Status.PAID;
    }

    public void cancel() {
        if (status != Status.PREPARING && status != Status.READY)
            throw new IllegalStateException("Only unfinished orders can be cancelled.");
        status = Status.CANCELLED;
    }

    private void requireStatus(Status expected, String message) {
        if (status != expected) throw new IllegalStateException(message);
    }
}
