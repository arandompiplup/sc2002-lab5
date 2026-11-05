package restaurant.model;

import java.util.List;

public final class Cashier extends Staff {

    public int collectPayment(List<Customer> customers, int turn) {
        requireAvailable();

        if (customers == null) {
            throw new IllegalArgumentException("Customer list cannot be null.");
        }

        Customer oldest = null;

        // Checks for oldest customer amongst all customers eligible for payment
        for (Customer customer : customers) {
            if (customer != null && customer.canPay(turn)) {
                if (oldest == null
                        || customer.getReadyToPayTurn() < oldest.getReadyToPayTurn()
                        || (customer.getReadyToPayTurn() == oldest.getReadyToPayTurn()
                            && customer.getId() < oldest.getId())) {
                    oldest = customer;
                }
            }
        }

        if (oldest == null) {
            return 0;
        }

        int amountPaid = oldest.pay(turn);
        recordSuccessfulTask();
        return amountPaid;
    }
}