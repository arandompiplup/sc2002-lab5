package restaurant.model;

public class MenuItem implements MenuItemInterface {
    private final String displayName;
    private final int priceCents;
    private final int preparationUnits;

    public MenuItem(String displayName, int priceCents, int preparationUnits) {
        this.displayName = displayName;
        this.priceCents = priceCents;
        this.preparationUnits = preparationUnits;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getPriceCents() {
        return priceCents;
    }

    @Override
    public int getPreparationUnits() {
        return preparationUnits;
    }
}
