package restaurant.model;


public interface WaiterInterface {
    void beginTurn();

    boolean available();

    void seat(Customer customer, Table table);

    void takeOrder(Customer customer);

    void serve(Customer customer, int turn);
}
