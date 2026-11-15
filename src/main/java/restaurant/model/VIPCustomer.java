package restaurant.model;

import java.util.List;

public final class VIPCustomer extends Customer {

    public VIPCustomer(int id) {
        super(id);
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    @Override
    protected int satisfactionLoss() {
        return 5;
    }

    @Override
    public int billAfterDiscount(int baseCents) {
        int validatedBaseCents = super.billAfterDiscount(baseCents);
        return (int) ((validatedBaseCents * 9L) / 10L);
    }

    @Override
    protected MenuItemInterface chooseMenuItem(List<MenuItemInterface> menu) {
        if (menu == null || menu.isEmpty()) {
            throw new IllegalArgumentException("Menu cannot be empty.");
        }

        int menuIndex = Math.floorMod(getId() - 1, menu.size());
        return menu.get(menuIndex);
    }
}
