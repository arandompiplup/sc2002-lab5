package restaurant;

import java.util.List;

public interface RestaurantGameInterface {
    int turn();
    int revenueCents();
    int servedCount();
    int departures();
    List<Table> tables();
    List<Customer> customers();
    List<Customer> waiting();
    Waiter waiter();
    Chef chef();
    double averagePaidSatisfaction();
    void startTurn(int turn);
    Customer waitingCustomer(int oneBasedIndex);
    Table table(int number);
    Customer customer(int id);
    void seat(Customer customer, Table table);
    void order(Customer customer);
    void serve(Customer customer);
    void prepare(Customer customer);
    void finishTurn();
    boolean victory();
}
