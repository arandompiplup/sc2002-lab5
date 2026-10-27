package restaurant;

public final class Cashier {
    public Customer collectOldest(java.util.List<Customer> customers, int turn) {
        return customers.stream()
                .filter(c -> c.state() == Customer.State.READY_TO_PAY && c.readyToPayTurn() < turn)
                .min(java.util.Comparator.comparingInt(Customer::id))
                .orElse(null);
    }
}
