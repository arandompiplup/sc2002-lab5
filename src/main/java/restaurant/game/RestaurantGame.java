package restaurant.game;

import java.util.List;
import restaurant.model.*;

public final class RestaurantGame implements RestaurantGameInterface {
    public static final int MAX_TURNS = 18;
    private int turn = 0;
    private TurnPhase phase = TurnPhase.BETWEEN_TURNS;
    private int revenueCents = 0;
    private int servedCount = 0;
    private int departures = 0;
    private Waiter waiter;
    private Chef chef;
    private Cashier cashier;
    private List<Customer> customers;
    private List<Table> tables;

    public void startTurn() { throw new UnsupportedOperationException("Not implemented"); }

    public void seat(int customerId, int tableNumber) { throw new UnsupportedOperationException("Not implemented"); }

    public void order(int customerId) { throw new UnsupportedOperationException("Not implemented"); }

    public void serve(int customerId) { throw new UnsupportedOperationException("Not implemented"); }

    public void prepare(int customerId) { throw new UnsupportedOperationException("Not implemented"); }

    public void waitWaiter() { throw new UnsupportedOperationException("Not implemented"); }

    public void waitChef() { throw new UnsupportedOperationException("Not implemented"); }

    public void finishTurn() { throw new UnsupportedOperationException("Not implemented"); }

    public int paidCount() { throw new UnsupportedOperationException("Not implemented"); }

    public double averagePaidSatisfaction() { throw new UnsupportedOperationException("Not implemented"); }

    public boolean victory() { throw new UnsupportedOperationException("Not implemented"); }

    public List<CustomerView> eligibleFor(WaiterTask task) { throw new UnsupportedOperationException("Not implemented"); }

    public List<CustomerView> eligibleForPreparation() { throw new UnsupportedOperationException("Not implemented"); }

    public List<CustomerView> customers() { throw new UnsupportedOperationException("Not implemented"); }

    public List<CustomerView> waiting() { throw new UnsupportedOperationException("Not implemented"); }

    public List<TableView> tables() { throw new UnsupportedOperationException("Not implemented"); }

    private void requirePhase(TurnPhase expected) { throw new UnsupportedOperationException("Not implemented"); }
}
