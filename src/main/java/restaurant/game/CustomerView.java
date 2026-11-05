package restaurant.game;


import restaurant.model.Customer;
import restaurant.model.Order;


public final class CustomerView {
    private final int id;
    private final int satisfaction;
    private final Customer.State state;
    private final int tableNumber;
    private final String dishName;
    private final Order.Status orderStatus;
    private final int unitsPrepared;
    private final int preparationUnits;

    CustomerView(Customer customer) { throw new UnsupportedOperationException("Not implemented"); }

    public boolean hasOrder() { throw new UnsupportedOperationException("Not implemented"); }
}
