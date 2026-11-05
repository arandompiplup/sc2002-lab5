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

    boolean victory();
}
