package restaurant;

public interface ChefInterface {
    void beginTurn();
    boolean available();
    void prepare(Customer customer);
}
