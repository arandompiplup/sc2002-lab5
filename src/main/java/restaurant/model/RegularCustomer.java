package restaurant.model;

/** Stage 1's only customer type. No VIP or Critic behaviour is included. */
public final class RegularCustomer extends Customer {
    public RegularCustomer(int id) { super(id); }

    @Override
    protected int satisfactionLoss() { return 8; }

    @Override
    protected MenuItemInterface chooseMenuItem() {
        // Deterministic and reproducible: Salad, Burger, Pasta, then repeat.
        MenuItem[] menu = MenuItem.values();
        return menu[(id() - 1) % menu.length];
    }
}
