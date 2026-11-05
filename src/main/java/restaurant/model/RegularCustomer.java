package restaurant.model;

import java.util.List;

public final class RegularCustomer extends Customer {

    public RegularCustomer(int id) {
        super(id);
    }

    @Override
    protected int satisfactionLoss() {
        return 8;
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