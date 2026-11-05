package restaurant.model;

public final class Chef extends Staff {

    public Chef() {
        super();
    }

    public void prepare(Order order, int turn) {
        requireAvailable();

        if (order == null) {
            throw new IllegalArgumentException("There is no order to prepare.");
        }

        order.prepareOneUnit(turn);

        recordSuccessfulTask();
    }
}