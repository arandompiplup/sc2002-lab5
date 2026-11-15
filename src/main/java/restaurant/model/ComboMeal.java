package restaurant.model;

import java.util.Objects;

public final class ComboMeal implements MenuItemInterface {
    private final MenuItemInterface first;
    private final MenuItemInterface second;

    public ComboMeal(MenuItemInterface first, MenuItemInterface second) {
        this.first = Objects.requireNonNull(first, "First dish is required.");
        this.second = Objects.requireNonNull(second, "Second dish is required.");
    }

    @Override
    public String getDisplayName() {
        return first.getDisplayName() + " + " + second.getDisplayName() + " Combo";
    }

    @Override
    public int getPriceCents() {
        long combinedCents = (long) first.getPriceCents() + second.getPriceCents();
        return Math.toIntExact(Math.round(combinedCents * 0.9));
    }

    @Override
    public int getPreparationUnits() {
        return Math.addExact(first.getPreparationUnits(), second.getPreparationUnits());
    }
}
