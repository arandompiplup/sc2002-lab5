package restaurant.model;

/** One successful action advances exactly one preparation unit. */
public final class Chef extends Staff {
    public void prepare(Customer customer, int turn) {
        requireAvailable();
        customer.prepareFood(turn);
        recordSuccessfulTask();
    }
}
