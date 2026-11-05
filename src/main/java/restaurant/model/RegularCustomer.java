package restaurant.model;

public final class RegularCustomer extends Customer {
    public RegularCustomer(int id) { super(id); }

    protected int satisfactionLoss() { throw new UnsupportedOperationException("Not implemented"); }

    protected MenuItemInterface chooseMenuItem() { throw new UnsupportedOperationException("Not implemented"); }
}
