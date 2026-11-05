package restaurant.model;

public final class Order {

    public enum Status { PREPARING, READY, SERVED, PAID, CANCELLED }
    private final MenuItemInterface item;
    private final int billCents;
    private int unitsPrepared = 0;
    private int readyTurn = -1;
    private Status status = Status.PREPARING;

    public Order(MenuItemInterface item) {
        this.item = item;
        this.billCents = item.getPriceCents();
    }

    public MenuItemInterface getItem() {
        return item;
    }

    public int getUnitsPrepared() {
        return unitsPrepared;
    }

    public Status getStatus() {
        return status;
    }

    public int getBillCents() {
        return billCents;
    }
    
    public boolean canBeServed(int turn) {
        return status == Status.READY && readyTurn < turn;
    }

    public void prepareOneUnit(int turn) {
        if (status != Status.PREPARING)
            throw new IllegalStateException("Order cannot be prepared now.");

        unitsPrepared++;
        if (unitsPrepared >= item.getPreparationUnits()) {
            status = Status.READY;
            readyTurn = turn;
        }
    }
    
    public void serve(int turn) {
        if (status != Status.READY || readyTurn >= turn)
            throw new IllegalStateException("Order cannot be served now.");

        status = Status.SERVED;
    }

    public void markPaid() {
        if (status != Status.SERVED)
            throw new IllegalStateException("Order cannot be marked paid now.");

        status = Status.PAID;
    }

    public void cancel() {
        if (status != Status.PREPARING && status != Status.READY)
            throw new IllegalStateException("Order cannot be cancelled now.");

        status = Status.CANCELLED;
    }
}
