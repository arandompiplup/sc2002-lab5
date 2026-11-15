package restaurant.model;

import java.util.List;

/** A customer who is less patient and prefers dishes with longer preparation time. */
public final class CriticCustomer extends Customer {

    public CriticCustomer(int id) {
        super(id);
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }

    @Override
    protected int satisfactionLoss() {
        return 12;
    }

    @Override
    protected MenuItemInterface chooseMenuItem(List<MenuItemInterface> menu) {
        if (menu == null || menu.isEmpty()) {
            throw new IllegalArgumentException("Menu cannot be empty.");
        }

        MenuItemInterface longestPreparationItem = menu.get(0);
        for (int i = 1; i < menu.size(); i++) {
            MenuItemInterface candidate = menu.get(i);
            if (candidate.getPreparationUnits() > longestPreparationItem.getPreparationUnits()) {
                longestPreparationItem = candidate;
            }
        }

        return longestPreparationItem;
    }
}
