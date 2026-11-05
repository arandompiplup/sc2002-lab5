package restaurant.model;

public final class Order {

    public enum Status { PREPARING, READY, SERVED, PAID, CANCELLED }
    private MenuItemInterface item;
    private int billCents;
    private int unitsPrepared = 0;
    private int readyTurn = -1;
    private Status status = Status.PREPARING;

    public Order(MenuItemInterface item) { throw new UnsupportedOperationException("Not implemented"); }

    public void prepareOneUnit(int turn) { throw new UnsupportedOperationException("Not implemented"); }

    public boolean canBeServed(int turn) { throw new UnsupportedOperationException("Not implemented"); }

    public void serve(int turn) { throw new UnsupportedOperationException("Not implemented"); }

    public void markPaid() { throw new UnsupportedOperationException("Not implemented"); }

    public void cancel() { throw new UnsupportedOperationException("Not implemented"); }

    public int billCents() { throw new UnsupportedOperationException("Not implemented"); }
}
