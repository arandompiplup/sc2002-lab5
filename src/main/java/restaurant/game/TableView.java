package restaurant.game;

import restaurant.model.Table;

/** Read-only table snapshot; neither it nor its occupant exposes a live entity. */
public final class TableView {
    private final int number;
    private final CustomerView occupant;

    TableView(Table table) {
        number = table.number();
        occupant = table.isFree() ? null : new CustomerView(table.occupant());
    }

    public int number() { return number; }
    public CustomerView occupant() { return occupant; }
    public boolean isFree() { return occupant == null; }
}
