package restaurant.model;

public interface ChefInterface {
    void beginTurn();
    boolean available();
    void prepare(Customer customer);
}
