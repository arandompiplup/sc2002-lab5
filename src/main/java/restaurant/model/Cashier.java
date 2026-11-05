package restaurant.model;

import java.util.List;

/** SRP: selects and collects one eligible payment; the game owns total revenue. */
public final class Cashier extends Staff {
    public int collectPayment(List<Customer> customers, int turn) {
        requireAvailable();
        Customer oldest = null;
        for (Customer customer : customers) {
            if (!customer.canPay(turn)) continue;
            // "Oldest first" means longest READY_TO_PAY wait; arrival ID breaks ties.
            if (oldest == null || customer.readyToPayTurn() < oldest.readyToPayTurn()
                    || (customer.readyToPayTurn() == oldest.readyToPayTurn() && customer.id() < oldest.id())) {
                oldest = customer;
            }
        }
        if (oldest == null) return 0;
        int payment = oldest.pay(turn);
        recordSuccessfulTask();
        return payment;
    }
}
