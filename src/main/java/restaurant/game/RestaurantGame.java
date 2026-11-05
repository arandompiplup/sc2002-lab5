package restaurant;

import java.util.*;

/** Stage 1 game rules and state. The console class only gathers choices and renders this state. */
public final class RestaurantGame implements RestaurantGameInterface {
    public static final int MAX_TURNS = 18;
    private final List<Table> tables = List.of(new Table(1), new Table(2));
    private final List<Customer> customers = new ArrayList<>();
    private final Deque<Customer> waiting = new ArrayDeque<>();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private int revenueCents;
    private int servedCount;
    private int departures;
    private int turn;

    public int turn() { return turn; }
    public int revenueCents() { return revenueCents; }
    public int servedCount() { return servedCount; }
    public int departures() { return departures; }
    public List<Table> tables() { return Collections.unmodifiableList(tables); }
    public List<Customer> customers() { return Collections.unmodifiableList(customers); }
    public List<Customer> waiting() { return List.copyOf(waiting); }
    public Waiter waiter() { return waiter; }
    public Chef chef() { return chef; }
    public double averagePaidSatisfaction() {
        return customers.stream().filter(Customer::isPaid).mapToInt(Customer::satisfactionAtPayment).average().orElse(-1);
    }

    public void startTurn(int turn) {
        this.turn = turn;
        waiter.beginTurn(); chef.beginTurn();
        if (List.of(1, 4, 7, 10, 13, 16).contains(turn)) {
            Customer customer = new Customer(customers.size() + 1);
            customers.add(customer); waiting.addLast(customer);
        }
    }
    public Customer waitingCustomer(int oneBasedIndex) {
        if (oneBasedIndex < 1 || oneBasedIndex > waiting.size()) return null;
        return waiting.stream().skip(oneBasedIndex - 1L).findFirst().orElse(null);
    }
    public Table table(int number) { return number < 1 || number > tables.size() ? null : tables.get(number - 1); }
    public Customer customer(int id) { return customers.stream().filter(c -> c.id() == id).findFirst().orElse(null); }
    public void seat(Customer customer, Table table) {
        if (!waiting.contains(customer)) throw new IllegalArgumentException("Customer is not waiting.");
        waiter.seat(customer, table); waiting.remove(customer);
    }
    public void order(Customer customer) { waiter.takeOrder(customer); }
    public void serve(Customer customer) { waiter.serve(customer, turn); servedCount++; }
    public void prepare(Customer customer) { chef.prepare(customer); }

    public void finishTurn() {
        for (Customer c : customers) {
            if (c.state() == Customer.State.EATING && c.servedTurn() < turn) c.finishEating(turn);
        }
        Customer payer = cashier.collectOldest(customers, turn);
        if (payer != null) { revenueCents += payer.order().billCents(); payer.pay(); }
        for (Customer c : new ArrayList<>(customers)) {
            if (c.state() == Customer.State.WAITING || c.state() == Customer.State.SEATED) {
                c.loseSatisfaction();
                if (c.satisfaction() == 0) {
                    if (c.state() == Customer.State.WAITING) waiting.remove(c);
                    c.leave(); departures++;
                }
            }
        }
    }
    public boolean victory() {
        return customers.stream().filter(Customer::isPaid).count() >= 4
                && revenueCents >= 4500 && averagePaidSatisfaction() >= 60;
    }
}
