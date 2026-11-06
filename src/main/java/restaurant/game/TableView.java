package restaurant.game;

import restaurant.model.Table;

public final class TableView {
    private final int number;
    private final CustomerView occupant;

    TableView(Table table) {
        this.number = table.getNumber();
        this.occupant = table.getOccupant() == null ? null : new CustomerView(table.getOccupant());
    }

    public boolean isFree() { return this.occupant == null; }
    public int getNumber() { return number; }
    public CustomerView getOccupant() { return occupant; }
}
