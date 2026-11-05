package restaurant.model;


/** Focused item contract used by Order (DIP), with room for later implementations (OCP).
 * Implementations must be immutable with a positive price and preparation units.
 */

public interface MenuItemInterface {
    String displayName();

    int priceCents();

    int preparationUnits();
}
