package restaurant.model;

public interface TableInterface {
    int number();
    Customer occupant();
    boolean isFree();
}
