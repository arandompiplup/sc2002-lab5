package restaurant.model;

import java.util.Objects;

/** Entity: keeps the table/customer association consistent; no general setter. */
public final class Table {
    private final int number;
    private Customer occupant;

    public Table(int number) {
        if (number < 1) throw new IllegalArgumentException("Table number must be positive.");
        this.number = number;
    }

    public int number() { return number; }
    public Customer occupant() { return occupant; }
    public boolean isFree() { return occupant == null; }

    public void seat(Customer customer) {
        Objects.requireNonNull(customer, "Customer is required.");
        // Validate before changing either side: rejected actions leave state intact.
        if (!isFree()) throw new IllegalStateException("That table is unavailable.");
        if (!customer.canBeSeated()) throw new IllegalStateException("Customer is not waiting.");
        customer.seatAt(this);
        occupant = customer;
    }

    void release(Customer customer) {
        if (occupant != customer) throw new IllegalStateException("Customer is not at this table.");
        occupant = null;
    }
}
