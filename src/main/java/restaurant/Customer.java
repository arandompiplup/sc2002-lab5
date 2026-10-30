package restaurant;

public final class Customer {
    public enum State { WAITING, SEATED, EATING, READY_TO_PAY, PAID, LEFT }

    private final int id;
    private int satisfaction = 100;
    private State state = State.WAITING;
    private Order order;
    private Table table;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;
    private int satisfactionAtPayment = -1;

    public Customer(int id) { this.id = id; }
    public int id() { return id; }
    public int satisfaction() { return satisfaction; }
    public State state() { return state; }
    public Order order() { return order; }
    public Table table() { return table; }
    public int servedTurn() { return servedTurn; }
    public int readyToPayTurn() { return readyToPayTurn; }
    public int satisfactionAtPayment() { return satisfactionAtPayment; }
    public boolean isPaid() { return state == State.PAID; }

    void seat(Table table) { this.table = table; this.state = State.SEATED; }
    void placeOrder() {
        if (state != State.SEATED || order != null) throw new IllegalStateException("Customer cannot order now.");
        order = new Order(chooseMenuItem());
    }
    /** Reproducible preference: customers cycle Salad, Burger, Pasta by arrival number. */
    private MenuItem chooseMenuItem() {
        return MenuItem.values()[(id - 1) % MenuItem.values().length];
    }
    void serve(int turn) { order.serve(); servedTurn = turn; state = State.EATING; }
    void finishEating(int turn) { state = State.READY_TO_PAY; readyToPayTurn = turn; }
    void pay() { order.markPaid(); state = State.PAID; satisfactionAtPayment = satisfaction; table.release(this); table = null; }
    void loseSatisfaction() { satisfaction = Math.max(0, satisfaction - 8); }
    void leave() {
        if (order != null) order.cancel();
        state = State.LEFT;
        if (table != null) { table.release(this); table = null; }
    }
}
