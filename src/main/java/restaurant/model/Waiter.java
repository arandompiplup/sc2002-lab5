package restaurant.model;

public final class Waiter extends Staff {
    public void seat(Customer customer, Table table) { throw new UnsupportedOperationException("Not implemented"); }

    public void takeOrder(Customer customer) { throw new UnsupportedOperationException("Not implemented"); }

    public void serve(Customer customer, int turn) { throw new UnsupportedOperationException("Not implemented"); }
}
