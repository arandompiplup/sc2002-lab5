package restaurant.model;

public final class Table {
    private int number;
    private Customer occupant;

    public Table(int number) { this.number = number; }

    public int getNumber() { return this.number; }

    public Customer getOccupant() { return this.occupant; }

    public boolean isFree() { return this.occupant == null; }

    public void seat(Customer customer) {
        if (!isFree()) throw new IllegalStateException("Table is already occupied.");

        customer.seatAtTable(this);
        this.occupant = customer;
    }

    void release(Customer customer) { this.occupant = null; }
}
