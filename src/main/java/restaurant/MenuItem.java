package restaurant;

/** The three fixed Stage 1 dishes. Prices are stored in cents. */
public enum MenuItem {
    SALAD("Salad", 900, 1), BURGER("Burger", 1500, 1), PASTA("Pasta", 1800, 2);

    private final String displayName;
    private final int priceCents;
    private final int preparationUnits;

    MenuItem(String displayName, int priceCents, int preparationUnits) {
        this.displayName = displayName;
        this.priceCents = priceCents;
        this.preparationUnits = preparationUnits;
    }

    public String displayName() { return displayName; }
    public int priceCents() { return priceCents; }
    public int preparationUnits() { return preparationUnits; }
}
