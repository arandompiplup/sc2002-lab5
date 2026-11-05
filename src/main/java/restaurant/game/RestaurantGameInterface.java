package restaurant.game;

import java.util.List;

/**
 * Boundary-facing use-case contract, rather than an interface for every class.
 * DIP: the console accepts this abstraction through its constructor.
 * ISP: it contains the console's queries/actions, not staff reset or entity setters.
 * Contract: rejected actions leave state/phase unchanged; actions must follow phase().
 */
public interface RestaurantGameInterface {
    int turn();
    int maxTurns();
    TurnPhase phase();
    int revenueCents();
    int servedCount();
    int departures();
    int paidCount();
    double averagePaidSatisfaction();
    List<CustomerView> customers();
    List<CustomerView> waiting();
    List<TableView> tables();
    List<CustomerView> eligibleFor(WaiterTask task);
    List<CustomerView> eligibleForPreparation();
    void startTurn();
    void seat(int customerId, int tableNumber);
    void order(int customerId);
    void serve(int customerId);
    void waitWaiter();
    void prepare(int customerId);
    void waitChef();
    void finishTurn();
    boolean victory();
}
