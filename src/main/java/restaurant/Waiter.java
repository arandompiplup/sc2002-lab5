package restaurant;

public final class Waiter {
    private boolean used;
    public void beginTurn() { used = false; }
    public boolean available() { return !used; }
    private void use() { if (used) throw new IllegalStateException("Waiter already acted this turn."); used = true; }
    public void seat(Customer customer, Table table) { use(); table.seat(customer); }
    public void takeOrder(Customer customer, MenuItem item) { use(); customer.placeOrder(item); }
    public void serve(Customer customer, int turn) { use(); customer.serve(turn); }
}
