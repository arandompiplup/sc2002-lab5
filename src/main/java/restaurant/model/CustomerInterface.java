package restaurant;

public interface CustomerInterface {
    int id();
    int satisfaction();
    Customer.State state();
    Order order();
    Table table();
    int servedTurn();
    int readyToPayTurn();
    int satisfactionAtPayment();
    boolean isPaid();
}
