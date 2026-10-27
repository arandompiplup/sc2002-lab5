package restaurant;

public final class Table {
    private final int number;
    private Customer occupant;
    public Table(int number) { this.number = number; }
    public int number() { return number; }
    public Customer occupant() { return occupant; }
    public boolean isFree() { return occupant == null; }
    void seat(Customer customer) {
        if (!isFree()) throw new IllegalStateException("Table is occupied.");
        occupant = customer;
        customer.seat(this);
    }
    void release(Customer customer) {
        if (occupant != customer) throw new IllegalStateException("Customer is not at this table.");
        occupant = null;
    }
}
