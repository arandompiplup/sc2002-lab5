package restaurant.game;

import restaurant.model.*;

import java.util.ArrayList;
import java.util.List;
import restaurant.model.Cashier;
import restaurant.model.Chef;
import restaurant.model.Customer;
import restaurant.model.RegularCustomer;
import restaurant.model.Table;
import restaurant.model.Waiter;

/**
 * Control object: owns the game and coordinates the use case (Chapter 8).
 * SRP: sequencing/aggregate results live here; customer/order rules stay in entities.
 * Composition: staff, tables and customers are created and retained by this game.
 * REBUILD: implement phases first, then commands, queries, and end-of-turn work.
 */
public final class RestaurantGame implements RestaurantGameInterface {
    public static final int MAX_TURNS = 18;
    private static final int REQUIRED_PAID = 4;
    private static final int REQUIRED_REVENUE_CENTS = 4500;
    private static final int REQUIRED_SATISFACTION = 60;
    private static final List<Integer> ARRIVAL_TURNS = List.of(1, 4, 7, 10, 13, 16);

    private final List<Table> tables = List.of(new Table(1), new Table(2));
    private final List<Customer> customers = new ArrayList<>();
    private final List<Customer> waiting = new ArrayList<>();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private TurnPhase phase = TurnPhase.BETWEEN_TURNS;
    private int turn;
    private int revenueCents;
    private int servedCount;
    private int departures;

    @Override public int turn() { return turn; }
    @Override public int maxTurns() { return MAX_TURNS; }
    @Override public TurnPhase phase() { return phase; }
    @Override public int revenueCents() { return revenueCents; }
    @Override public int servedCount() { return servedCount; }
    @Override public int departures() { return departures; }

    @Override
    public int paidCount() {
        int count = 0;
        for (Customer customer : customers) if (customer.isPaid()) count++;
        return count;
    }

    @Override
    public double averagePaidSatisfaction() {
        int total = 0;
        for (Customer customer : customers) {
            if (customer.isPaid()) total += customer.satisfactionAtPayment();
        }
        int paid = paidCount();
        return paid == 0 ? -1 : (double) total / paid;
    }

    @Override public List<CustomerView> customers() { return viewsOf(customers); }
    @Override public List<CustomerView> waiting() { return viewsOf(waiting); }

    @Override
    public List<TableView> tables() {
        List<TableView> views = new ArrayList<>();
        for (Table table : tables) views.add(new TableView(table));
        return List.copyOf(views);
    }

    @Override
    public List<CustomerView> eligibleFor(WaiterTask task) {
        if (task == null) throw new IllegalArgumentException("Waiter task is required.");
        if (phase != TurnPhase.WAITER) return List.of();
        List<Customer> eligible = new ArrayList<>();
        if (task == WaiterTask.SEAT) {
            for (Table table : tables) if (table.isFree()) return viewsOf(waiting);
            return List.of();
        }
        for (Customer customer : customers) {
            if ((task == WaiterTask.ORDER && customer.canOrder())
                    || (task == WaiterTask.SERVE && customer.canBeServed(turn))) {
                eligible.add(customer);
            }
        }
        return viewsOf(eligible);
    }

    @Override
    public List<CustomerView> eligibleForPreparation() {
        if (phase != TurnPhase.CHEF) return List.of();
        List<Customer> eligible = new ArrayList<>();
        for (Customer customer : customers) if (customer.canPrepare()) eligible.add(customer);
        return viewsOf(eligible);
    }

    @Override
    public void startTurn() {
        requirePhase(TurnPhase.BETWEEN_TURNS);
        turn++;
        waiter.beginTurn();
        chef.beginTurn();
        cashier.beginTurn();
        if (ARRIVAL_TURNS.contains(turn)) {
            // Only this setup point knows the concrete type; the loop uses Customer.
            Customer customer = new RegularCustomer(customers.size() + 1);
            customers.add(customer);
            waiting.add(customer);
        }
        phase = TurnPhase.WAITER;
    }

    @Override
    public void seat(int customerId, int tableNumber) {
        requirePhase(TurnPhase.WAITER);
        Customer customer = findCustomer(customerId);
        Table table = findTable(tableNumber);
        if (!waiting.contains(customer)) throw new IllegalArgumentException("Customer is not waiting.");
        waiter.seat(customer, table);
        waiting.remove(customer);
        phase = TurnPhase.CHEF;
    }

    @Override
    public void order(int customerId) {
        requirePhase(TurnPhase.WAITER);
        waiter.takeOrder(findCustomer(customerId));
        phase = TurnPhase.CHEF;
    }

    @Override
    public void serve(int customerId) {
        requirePhase(TurnPhase.WAITER);
        waiter.serve(findCustomer(customerId), turn);
        servedCount++;
        phase = TurnPhase.CHEF;
    }

    @Override
    public void waitWaiter() {
        requirePhase(TurnPhase.WAITER);
        phase = TurnPhase.CHEF;
    }

    @Override
    public void prepare(int customerId) {
        requirePhase(TurnPhase.CHEF);
        chef.prepare(findCustomer(customerId), turn);
        phase = TurnPhase.END_OF_TURN;
    }

    @Override
    public void waitChef() {
        requirePhase(TurnPhase.CHEF);
        phase = TurnPhase.END_OF_TURN;
    }

    @Override
    public void finishTurn() {
        requirePhase(TurnPhase.END_OF_TURN);
        for (Customer customer : customers) customer.finishEating(turn);
        revenueCents += cashier.collectPayment(customers, turn);
        for (Customer customer : customers) {
            Customer.State before = customer.state();
            // Polymorphic loss is dispatched inside Customer, without type checks.
            customer.waitAtTurnEnd();
            if (before != Customer.State.LEFT && customer.state() == Customer.State.LEFT) {
                waiting.remove(customer);
                departures++;
            }
        }
        phase = turn == MAX_TURNS ? TurnPhase.FINISHED : TurnPhase.BETWEEN_TURNS;
    }

    @Override
    public boolean victory() {
        requirePhase(TurnPhase.FINISHED);
        return paidCount() >= REQUIRED_PAID && revenueCents >= REQUIRED_REVENUE_CENTS
                && averagePaidSatisfaction() >= REQUIRED_SATISFACTION;
    }

    private Customer findCustomer(int id) {
        for (Customer customer : customers) if (customer.id() == id) return customer;
        throw new IllegalArgumentException("No customer with that ID.");
    }

    private Table findTable(int number) {
        for (Table table : tables) if (table.number() == number) return table;
        throw new IllegalArgumentException("No table with that number.");
    }

    private void requirePhase(TurnPhase expected) {
        if (phase != expected) throw new IllegalStateException("Action requires " + expected + "; current phase is " + phase + ".");
    }

    private List<CustomerView> viewsOf(List<Customer> source) {
        List<CustomerView> views = new ArrayList<>();
        for (Customer customer : source) views.add(new CustomerView(customer));
        return List.copyOf(views);
    }
}
