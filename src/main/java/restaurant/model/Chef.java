package restaurant.model;

public final class Chef extends Staff {

    public Chef() {
        super();
    }

    public void prepare(Customer customer, int turn) {
        requireAvailable();

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        Order order = customer.getOrder();
        if (order == null) {
            throw new IllegalArgumentException("There is no order to prepare.");
        }

        order.prepareOneUnit(turn);

        recordSuccessfulTask();
    }
}
