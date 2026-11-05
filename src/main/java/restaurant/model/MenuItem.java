package restaurant.model;

public enum MenuItem implements MenuItemInterface {
    SALAD, BURGER, PASTA;
    private String displayName;
    private int priceCents;
    private int preparationUnits;

    public String displayName() { throw new UnsupportedOperationException("Not implemented"); }

    public int priceCents() { throw new UnsupportedOperationException("Not implemented"); }

    public int preparationUnits() { throw new UnsupportedOperationException("Not implemented"); }
}
