package restaurant.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import restaurant.model.*;

public final class RestaurantGame implements RestaurantGameInterface {
    public static final int MAX_TURNS = 18;
    private int turn;
    private TurnPhase phase = TurnPhase.BETWEEN_TURNS;
    private int revenueCents;
    private int servedCount;
    private int departures;
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Table> tables = List.of(new Table(1), new Table(2));
    private final List<MenuItemInterface> menu = List.of(
            new MenuItem("Salad", 900, 1),
            new MenuItem("Burger", 1500, 1),
            new MenuItem("Pasta", 1800, 2));
    private final List<Customer> eatingAtTurnStart = new ArrayList<>();

    @Override
    public void startTurn() {
        requirePhase(TurnPhase.BETWEEN_TURNS);
        turn++;
        waiter.beginTurn();
        chef.beginTurn();
        cashier.beginTurn();
        eatingAtTurnStart.clear();
        for (Customer customer : customers) {
            if (customer.getState() == Customer.State.EATING) eatingAtTurnStart.add(customer);
        }
        if ((turn - 1) % 3 == 0) {
            customers.add(new RegularCustomer(customers.size() + 1));
        }
        phase = TurnPhase.WAITER;
    }

    @Override
    public void seat(int customerId, int tableNumber) {
        requirePhase(TurnPhase.WAITER);
        waiter.seat(findCustomer(customerId), findTable(tableNumber));
        phase = TurnPhase.CHEF;
    }

    @Override
    public void order(int customerId) {
        requirePhase(TurnPhase.WAITER);
        Customer customer = findCustomer(customerId);
        if (!customer.canOrder()) throw new IllegalStateException("Customer cannot order now.");
        MenuItemInterface item = customer.selectItem(menu);
        waiter.takeOrder(customer, item, calculateBill(customer, item));
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
    public void prepare(int customerId) {
        requirePhase(TurnPhase.CHEF);
        chef.prepare(findCustomer(customerId), turn);
        phase = TurnPhase.END_OF_TURN;
    }

    @Override
    public void waitWaiter() {
        requirePhase(TurnPhase.WAITER);
        phase = TurnPhase.CHEF;
    }

    @Override
    public void waitChef() {
        requirePhase(TurnPhase.CHEF);
        phase = TurnPhase.END_OF_TURN;
    }

    @Override
    public void finishTurn() {
        requirePhase(TurnPhase.END_OF_TURN);
        // Only customers already eating when this turn began have eaten a full turn.
        for (Customer customer : eatingAtTurnStart) customer.finishEating(turn);
        revenueCents += cashier.collectPayment(customers, turn);
        for (Customer customer : customers) {
            Customer.State previous = customer.getState();
            customer.waitAtTurnEnd();
            if (previous != Customer.State.LEFT && customer.getState() == Customer.State.LEFT) departures++;
        }
        phase = turn == MAX_TURNS ? TurnPhase.FINISHED : TurnPhase.BETWEEN_TURNS;
    }

    @Override
    public int paidCount() {
        return (int) customers.stream().filter(c -> c.getState() == Customer.State.PAID).count();
    }

    @Override
    public double averagePaidSatisfaction() {
        return customers.stream().filter(c -> c.getState() == Customer.State.PAID)
                .mapToInt(Customer::getSatisfactionAtPayment).average().orElse(0.0);
    }

    @Override
    public boolean victory() {
        requirePhase(TurnPhase.FINISHED);
        return paidCount() >= 4 && revenueCents >= 4500 && averagePaidSatisfaction() >= 60;
    }

    @Override
    public List<CustomerView> eligibleFor(WaiterTask task) {
        Objects.requireNonNull(task, "Task cannot be null.");
        if (phase != TurnPhase.WAITER) return List.of();
        return customers.stream().filter(c -> switch (task) {
            case SEAT -> c.getState() == Customer.State.WAITING && tables.stream().anyMatch(Table::isFree);
            case ORDER -> c.canOrder();
            case SERVE -> c.canBeServed(turn);
        }).map(CustomerView::new).toList();
    }

    @Override
    public List<CustomerView> eligibleForPreparation() {
        if (phase != TurnPhase.CHEF) return List.of();
        return customers.stream().filter(c -> c.getOrder() != null
                && c.getOrder().getStatus() == Order.Status.PREPARING)
                .map(CustomerView::new).toList();
    }

    @Override
    public List<CustomerView> customers() {
        return customers.stream().map(CustomerView::new).toList();
    }

    @Override
    public List<CustomerView> waiting() {
        return customers.stream().filter(c -> c.getState() == Customer.State.WAITING)
                .map(CustomerView::new).toList();
    }

    @Override
    public List<TableView> tables() {
        return tables.stream().map(TableView::new).toList();
    }

    @Override public int turn() { return turn; }
    @Override public TurnPhase phase() { return phase; }
    @Override public int revenueCents() { return revenueCents; }
    @Override public int servedCount() { return servedCount; }
    @Override public int departures() { return departures; }

    private int calculateBill(Customer customer, MenuItemInterface item) {
        return customer.billAfterDiscount(item.getPriceCents());
    }

    private Customer findCustomer(int id) {
        return customers.stream().filter(c -> c.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No customer with that ID."));
    }

    private Table findTable(int number) {
        return tables.stream().filter(t -> t.getNumber() == number).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No table with that number."));
    }

    private void requirePhase(TurnPhase expected) {
        if (phase != expected) throw new IllegalStateException("Expected phase " + expected + ", but was " + phase + ".");
    }
}
