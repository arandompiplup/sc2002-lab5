package restaurant.game;

import restaurant.model.Customer;
import restaurant.model.Order;

public final class CustomerView {
    private final int id;
    private final String typeName;
    private final int satisfaction;
    private final Customer.State state;
    private final int tableNumber;
    private final String dishName;
    private final Order.Status orderStatus;
    private final int unitsPrepared;
    private final int preparationUnits;
    private final int billCents;

    CustomerView(Customer customer) {
        this.id = customer.getId();
        this.typeName = customer.getTypeName();
        this.satisfaction = customer.getSatisfaction();
        this.state = customer.getState();
        this.tableNumber = customer.getTable() == null ? -1 : customer.getTable().getNumber();

        Order order = customer.getOrder();
        this.dishName = order == null ? null : order.getItem().getDisplayName();
        this.orderStatus = order == null ? null : order.getStatus();
        this.unitsPrepared = order == null ? 0 : order.getUnitsPrepared();
        this.preparationUnits = order == null ? 0 : order.getItem().getPreparationUnits();
        this.billCents = order == null ? 0 : order.getBillCents();
    }

    public int getId() {
        return id;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public Customer.State getState() {
        return state;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public boolean hasOrder() {
        return orderStatus != null;
    }

    public String getDishName() {
        return dishName;
    }

    public Order.Status getOrderStatus() {
        return orderStatus;
    }

    public int getUnitsPrepared() {
        return unitsPrepared;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }

    public int getBillCents() {
        return billCents;
    }
}
