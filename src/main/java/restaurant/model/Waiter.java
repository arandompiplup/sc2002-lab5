package restaurant.model;

public final class Waiter extends Staff {

    public void seat(Customer customer, Table table) {
        requireAvailable();
        table.seat(customer);
        recordSuccessfulTask();
    }

    public void takeOrder(Customer customer, MenuItemInterface item, int billCents) {
        requireAvailable();
        customer.placeOrder(item, billCents);
        recordSuccessfulTask();
    }

    public void serve(Customer customer, int turn) {
        requireAvailable();
        customer.receiveFood(turn);
        recordSuccessfulTask();
    }
}