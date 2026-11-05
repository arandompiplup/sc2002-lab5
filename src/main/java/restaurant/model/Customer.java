package restaurant.model;

/**
 * Entity: owns one customer's lifecycle, satisfaction and order.
 * SRP: this class knows customer rules, not console input or the game loop.
 * OCP: subclasses supply preferences/loss; the shared service flow stays here.
 * REBUILD: write the state enum and constructor, then each legal transition.
 */
public abstract class Customer {
    public enum State { WAITING, SEATED, EATING, READY_TO_PAY, PAID, LEFT }

    private final int id;
    private int satisfaction = 100;
    private State state = State.WAITING;
    private Table table;
    private Order order;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;
    private int satisfactionAtPayment = -1;

    protected Customer(int id) {
        if (id < 1) throw new IllegalArgumentException("Customer ID must be positive.");
        this.id = id;
    }

    public final int id() { return id; }
    public final int satisfaction() { return satisfaction; }
    public final State state() { return state; }
    public final Table table() { return table; }
    public final Order order() { return order; }
    public final int readyToPayTurn() { return readyToPayTurn; }
    public final int satisfactionAtPayment() { return satisfactionAtPayment; }
    public final boolean isPaid() { return state == State.PAID; }
    public final boolean canBeSeated() { return state == State.WAITING; }
    public final boolean canOrder() { return state == State.SEATED && order == null; }
    public final boolean canPrepare() {
        return state == State.SEATED && order != null && order.status() == Order.Status.PREPARING;
    }
    public final boolean canBeServed(int turn) {
        return state == State.SEATED && order != null && order.canBeServed(turn);
    }
    public final boolean canPay(int turn) {
        return state == State.READY_TO_PAY && readyToPayTurn < turn;
    }

    // Package access: only the model's seating collaboration uses this method.
    final void seatAt(Table table) {
        if (!canBeSeated()) throw new IllegalStateException("Customer is not waiting.");
        this.table = table;
        state = State.SEATED;
    }

    public final void placeOrder() {
        if (!canOrder()) throw new IllegalStateException("Customer cannot order now.");
        // Composition: each order is created for, and owned by, one customer.
        order = new Order(chooseMenuItem());
    }

    public final void prepareFood(int turn) {
        if (!canPrepare()) throw new IllegalStateException("Customer has no order being prepared.");
        order.prepareOneUnit(turn);
    }

    public final void receiveFood(int turn) {
        if (!canBeServed(turn)) throw new IllegalStateException("Order must be READY from an earlier turn.");
        order.serve(turn);
        servedTurn = turn;
        state = State.EATING;
    }

    public final void finishEating(int turn) {
        if (state == State.EATING && servedTurn < turn) {
            state = State.READY_TO_PAY;
            readyToPayTurn = turn;
        }
    }

    public final int pay(int turn) {
        if (!canPay(turn)) throw new IllegalStateException("Customer cannot pay yet.");
        int bill = order.billCents();
        order.markPaid();
        table.release(this);
        table = null;
        state = State.PAID;
        satisfactionAtPayment = satisfaction;
        return bill;
    }

    public final void waitAtTurnEnd() {
        if (state != State.WAITING && state != State.SEATED) return;
        satisfaction = Math.max(0, satisfaction - satisfactionLoss());
        if (satisfaction == 0) leave();
    }

    private void leave() {
        if (order != null) order.cancel();
        if (table != null) table.release(this);
        table = null;
        state = State.LEFT;
    }

    /** LSP contract: return a positive loss; shared logic keeps satisfaction in 0..100. */
    protected abstract int satisfactionLoss();

    /** LSP contract: return a non-null valid item; the manager never chooses it. */
    protected abstract MenuItemInterface chooseMenuItem();
}
