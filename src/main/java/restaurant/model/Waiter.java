package restaurant.model;

/** SRP: performs waiter tasks; Customer/Table/Order enforce their own state rules. */
public final class Waiter extends Staff {
    public void seat(Customer customer, Table table) {
        requireAvailable();
        table.seat(customer);
        // Only successful actions consume the task. Exceptions keep it available.
        recordSuccessfulTask();
    }

    public void takeOrder(Customer customer) {
        requireAvailable();
        customer.placeOrder();
        recordSuccessfulTask();
    }

    public void serve(Customer customer, int turn) {
        requireAvailable();
        customer.receiveFood(turn);
        recordSuccessfulTask();
    }
}
