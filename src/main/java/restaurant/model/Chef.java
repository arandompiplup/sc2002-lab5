package restaurant.model;

public final class Chef implements ChefInterface {
    private boolean used;
    public void beginTurn() { used = false; }
    public boolean available() { return !used; }
    public void prepare(Customer customer) {
        if (used) throw new IllegalStateException("Chef already worked this turn.");
        if (customer.order() == null || customer.order().status() != Order.Status.PREPARING)
            throw new IllegalStateException("Customer has no order being prepared.");
        customer.order().prepareOneUnit();
        used = true;
    }
}
