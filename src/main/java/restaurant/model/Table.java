package restaurant.model;

public final class Table {
    private int number;
    private Customer occupant;

    public Table(int number) { throw new UnsupportedOperationException("Not implemented"); }

    public boolean isFree() { throw new UnsupportedOperationException("Not implemented"); }

    public void seat(Customer customer) { throw new UnsupportedOperationException("Not implemented"); }

    void release(Customer customer) { throw new UnsupportedOperationException("Not implemented"); }
}
