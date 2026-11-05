package restaurant.model;

public final class Table {
    private int number;
    private Customer occupant;

    public Table(int number) { throw new UnsupportedOperationException("Not implemented"); }

    public int getNumber() { return this.number; }

    public Customer getOccupant() { return this.occupant; }

    public boolean isFree() { this.occupant == null; }

    public void seat(Customer customer) { this.occupant = customer; }

    void release(Customer customer) { this.occupant = null; }
}
