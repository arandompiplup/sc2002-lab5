package restaurant.model;
import java.util.List;

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
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public State getState() {
        return state;
    }

    public Table getTable() {
        return table;
    }

    public Order getOrder() {
        return order;
    }

    public int getReadyToPayTurn() {
        if (readyToPayTurn == -1) {
            throw new IllegalStateException("Customer is not ready to pay yet.");
        }
        return readyToPayTurn;
    }

    public int getSatisfactionAtPayment() {
        return satisfactionAtPayment;
    }

    public boolean canOrder() {
        return state == State.SEATED && order == null;
    }

    public boolean canBeServed(int turn) {
        return state == State.SEATED && order != null && order.canBeServed(turn);
    }

    public boolean canPay(int turn) {
        return state == State.READY_TO_PAY && readyToPayTurn < turn;
    }

    void seatAtTable(Table table) {
        if (state != State.WAITING)
            throw new IllegalStateException("Customer cannot be seated now.");
        
        if (table == null)
            throw new IllegalArgumentException("Table cannot be null.");
        
        if (!table.isFree())
            throw new IllegalStateException("Table is not free.");

        this.table = table;
        state = State.SEATED;
    }

    public void placeOrder(MenuItemInterface item, int billCents) {
        if (!canOrder()) {
            throw new IllegalStateException("Customer cannot order now.");
        }
        if (item == null) {
            throw new IllegalArgumentException("Menu item cannot be null.");
        }
        if (billCents < 0) {
            throw new IllegalArgumentException("Bill cannot be negative.");
        }

        order = new Order(item, billCents);
    }

    public void receiveFood(int turn) {
        if (!canBeServed(turn))
            throw new IllegalStateException("Customer cannot be served now.");

        order.serve(turn);
        servedTurn = turn;
        state = State.EATING;
    }

    public void finishEating(int turn) {
        if (state != State.EATING || servedTurn >= turn)
            throw new IllegalStateException("Customer cannot finish eating now.");

        readyToPayTurn = turn;
        state = State.READY_TO_PAY;
    }

    public int pay(int turn) {
        if (!canPay(turn))
            throw new IllegalStateException("Customer cannot pay now.");

        satisfactionAtPayment = satisfaction;
        order.markPaid();
        table.release(this);
        table = null;
        state = State.PAID;

        return order.getBillCents();
    }

    public void waitAtTurnEnd() {
        if (state != State.WAITING && state != State.SEATED) return;

        satisfaction = Math.max(0, satisfaction - satisfactionLoss());
        if (satisfaction > 0) return;

        if (order != null) order.cancel();

        if (table != null) {
            table.release(this);
            table = null;
        }

        state = State.LEFT;
    }

    public final MenuItemInterface selectItem(List<MenuItemInterface> menu) {
        if (!canOrder()) {
            throw new IllegalStateException("Customer cannot order now.");
        }
        if (menu == null || menu.isEmpty()) {
            throw new IllegalArgumentException("Menu cannot be empty.");
        }
        return chooseMenuItem(menu);
    }

    public int billAfterDiscount(int baseCents) {
        if (baseCents < 0) {
            throw new IllegalArgumentException("Bill cannot be negative.");
        }
        return baseCents;
    }

    protected abstract int satisfactionLoss();

    protected abstract MenuItemInterface chooseMenuItem(List<MenuItemInterface> menu);
}
