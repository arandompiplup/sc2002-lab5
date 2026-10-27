package restaurant;

public final class Order {
    public enum Status { PREPARING, READY, SERVED, PAID, CANCELLED }

    private final MenuItem item;
    private int unitsPrepared;
    private Status status = Status.PREPARING;

    public Order(MenuItem item) { this.item = item; }
    public MenuItem item() { return item; }
    public int unitsPrepared() { return unitsPrepared; }
    public Status status() { return status; }
    public int billCents() { return item.priceCents(); }

    public void prepareOneUnit() {
        if (status != Status.PREPARING) throw new IllegalStateException("Order is not being prepared.");
        if (++unitsPrepared == item.preparationUnits()) status = Status.READY;
    }
    public void serve() {
        if (status != Status.READY) throw new IllegalStateException("Order is not ready.");
        status = Status.SERVED;
    }
    public void markPaid() {
        if (status != Status.SERVED) throw new IllegalStateException("Order has not been served.");
        status = Status.PAID;
    }
    public void cancel() {
        if (status == Status.PREPARING || status == Status.READY) status = Status.CANCELLED;
    }
}
