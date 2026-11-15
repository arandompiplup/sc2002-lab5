package restaurant.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import restaurant.model.*;

public final class RestaurantGame implements RestaurantGameInterface {
    public static final int MAX_TURNS = 18;
    private int turn;
    private int happyHourLastTurn;
    private TurnPhase phase = TurnPhase.BETWEEN_TURNS;
    private int revenueCents;
    private int servedCount;
    private int departures;
    private final Host host = new Host();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Table> tables = List.of(new Table(1), new Table(2));
    private final List<MenuItemInterface> menu = createMenu();
    private final List<Customer> eatingAtTurnStart = new ArrayList<>();

    private static List<MenuItemInterface> createMenu() {
        List<MenuItem> dishes = List.of(
                new MenuItem("Salad", 900, 1),
                new MenuItem("Burger", 1500, 1),
                new MenuItem("Pasta", 1800, 2));

        List<MenuItemInterface> catalogue = new ArrayList<>(dishes);
        for (int first = 0; first < dishes.size(); first++) {
            for (int second = first + 1; second < dishes.size(); second++) {
                catalogue.add(new ComboMeal(dishes.get(first), dishes.get(second)));
            }
        }

        return List.copyOf(catalogue);
    }

    @Override
    public void startTurn(boolean activateHappyHour) {
        requirePhase(TurnPhase.BETWEEN_TURNS);
        if (activateHappyHour && !canActivateHappyHour()) {
            throw new IllegalStateException("Happy Hour has already been used.");
        }

        turn++;

        if (activateHappyHour) {
            happyHourLastTurn = turn + 1;
        }

        host.beginTurn();
        waiter.beginTurn();
        chef.beginTurn();
        cashier.beginTurn();

        eatingAtTurnStart.clear();
        for (Customer customer : customers) {
            if (customer.getState() == Customer.State.EATING) {
                eatingAtTurnStart.add(customer);
            }
        }

        if ((turn - 1) % 3 == 0) {
            int customerId = customers.size() + 1;
            customers.add(createArrivalCustomer(customerId));
        }

        phase = TurnPhase.HOST;
        if (happyHourTurnsRemaining() > 0) {
            customers.forEach(Customer::recoverDuringHappyHour);
        }
    }

    @Override
    public boolean canActivateHappyHour() {
        return phase == TurnPhase.BETWEEN_TURNS && happyHourLastTurn == 0;
    }

    @Override
    public int happyHourTurnsRemaining() {
        if (happyHourLastTurn == 0 || phase == TurnPhase.FINISHED) {
            return 0;
        }

        int activeTurn = phase == TurnPhase.BETWEEN_TURNS ? turn + 1 : turn;
        return Math.max(0, happyHourLastTurn - activeTurn + 1);
    }

    @Override
    public void waitHost() {
        requirePhase(TurnPhase.HOST);
        phase = TurnPhase.WAITER;
    }

    private Customer createArrivalCustomer(int customerId) {
        switch (customerId) {
            case 2:
                return new VIPCustomer(customerId);
            case 4:
                return new CriticCustomer(customerId);
            default:
                return new RegularCustomer(customerId);
        }
    }

    @Override
    public void seat(int customerId, int tableNumber) {
        Seating staff = switch (phase) {
            case HOST -> host;
            case WAITER -> waiter;
            default -> throw new IllegalStateException(
                    "Seating is only available during the Host or Waiter phase.");
        };

        staff.seat(findCustomer(customerId), findTable(tableNumber));
        phase = phase == TurnPhase.HOST ? TurnPhase.WAITER : TurnPhase.CHEF;
    }

    @Override
    public void order(int customerId) {
        requirePhase(TurnPhase.WAITER);

        Customer customer = findCustomer(customerId);
        if (!customer.canOrder()) {
            throw new IllegalStateException("Customer cannot order now.");
        }

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

        for (Customer customer : eatingAtTurnStart)
            customer.finishEating(turn);

        revenueCents += cashier.collectPayment(customers, turn);

        for (Customer customer : customers) {
            Customer.State previous = customer.getState();
            customer.waitAtTurnEnd();
            if (previous != Customer.State.LEFT && customer.getState() == Customer.State.LEFT) {
                departures++;
            }
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
        if (phase != TurnPhase.WAITER && !(phase == TurnPhase.HOST && task == WaiterTask.SEAT)) {
            return List.of();
        }

        return customers.stream().filter(c -> switch (task) {
            case SEAT ->
                c.getState() == Customer.State.WAITING && tables.stream().anyMatch(Table::isFree);
            case ORDER -> c.canOrder();
            case SERVE -> c.canBeServed(turn);
        }).map(CustomerView::new).toList();
    }

    @Override
    public List<CustomerView> eligibleForPreparation() {
        if (phase != TurnPhase.CHEF) {
            return List.of();
        }

        return customers.stream().filter(
                c -> c.getOrder() != null && c.getOrder().getStatus() == Order.Status.PREPARING)
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

    @Override
    public int turn() {
        return turn;
    }

    @Override
    public TurnPhase phase() {
        return phase;
    }

    @Override
    public int revenueCents() {
        return revenueCents;
    }

    @Override
    public int servedCount() {
        return servedCount;
    }

    @Override
    public int departures() {
        return departures;
    }

    private int calculateBill(Customer customer, MenuItemInterface item) {
        int billCents = customer.billAfterDiscount(item.getPriceCents());
        return happyHourTurnsRemaining() > 0 ? (int) ((billCents * 8L + 5) / 10) : billCents;
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
        if (phase != expected) {
            throw new IllegalStateException(
                    "Expected phase " + expected + ", but was " + phase + ".");
        }
    }
}
