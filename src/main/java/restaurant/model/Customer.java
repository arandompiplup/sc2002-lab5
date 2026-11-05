package restaurant.model;

public abstract class Customer {

    public enum State { WAITING, SEATED, EATING, READY_TO_PAY, PAID, LEFT }
    private int id;
    private int satisfaction = 100;
    private State state = State.WAITING;
    private Table table;
    private Order order;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;
    private int satisfactionAtPayment = -1;

    protected Customer(int id) { throw new UnsupportedOperationException("Not implemented"); }

    public boolean canOrder() { throw new UnsupportedOperationException("Not implemented"); }

    public boolean canBeServed(int turn) { throw new UnsupportedOperationException("Not implemented"); }

    public boolean canPay(int turn) { throw new UnsupportedOperationException("Not implemented"); }

    public void placeOrder() { throw new UnsupportedOperationException("Not implemented"); }

    public void receiveFood(int turn) { throw new UnsupportedOperationException("Not implemented"); }

    public void finishEating(int turn) { throw new UnsupportedOperationException("Not implemented"); }

    public int pay(int turn) { throw new UnsupportedOperationException("Not implemented"); }

    public void waitAtTurnEnd() { throw new UnsupportedOperationException("Not implemented"); }

    protected abstract int satisfactionLoss();

    protected abstract MenuItemInterface chooseMenuItem();
}
