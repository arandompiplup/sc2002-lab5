package restaurant.game;

import restaurant.model.Customer;
import restaurant.model.Order;

/**
 * Immutable copy for the boundary. An unmodifiable list of live Customers would
 * still expose mutable orders. This view copies values and exposes no commands.
 * Trade-off: a little copying/boilerplate buys a clear encapsulation boundary.
 */
public final class CustomerView {
    private final int id;
    private final int satisfaction;
    private final Customer.State state;
    private final int tableNumber;
    private final String dishName;
    private final Order.Status orderStatus;
    private final int unitsPrepared;
    private final int preparationUnits;

    // Only the game package creates snapshots; the CLI only reads them.
    CustomerView(Customer customer) {
        id = customer.id();
        satisfaction = customer.satisfaction();
        state = customer.state();
        tableNumber = customer.table() == null ? 0 : customer.table().number();
        Order order = customer.order();
        dishName = order == null ? "" : order.item().displayName();
        orderStatus = order == null ? null : order.status();
        unitsPrepared = order == null ? 0 : order.unitsPrepared();
        preparationUnits = order == null ? 0 : order.item().preparationUnits();
    }

    public int id() { return id; }
    public int satisfaction() { return satisfaction; }
    public Customer.State state() { return state; }
    public int tableNumber() { return tableNumber; }
    public boolean hasOrder() { return orderStatus != null; }
    public String dishName() { return dishName; }
    public Order.Status orderStatus() { return orderStatus; }
    public int unitsPrepared() { return unitsPrepared; }
    public int preparationUnits() { return preparationUnits; }
}
