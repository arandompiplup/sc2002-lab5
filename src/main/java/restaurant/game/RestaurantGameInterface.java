package restaurant.game;

import java.util.List;

public interface RestaurantGameInterface {
    void startTurn();
    void seat(int customerId, int tableNumber);
    void order(int customerId);
    void serve(int customerId);
    void prepare(int customerId);
    void waitWaiter();
    void waitChef();
    void finishTurn();
    List<CustomerView> eligibleFor(WaiterTask task);
    List<CustomerView> eligibleForPreparation();
    List<CustomerView> customers();
    List<CustomerView> waiting();
    List<TableView> tables();
    int turn();
    TurnPhase phase();
    int revenueCents();
    int servedCount();
    int departures();
    int paidCount();
    /** Returns zero when no customers have paid. */
    double averagePaidSatisfaction();
    /** Valid only after turn 18 has finished. */
    boolean victory();
}
