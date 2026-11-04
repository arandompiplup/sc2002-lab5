package restaurant;

/** An impatient customer who always chooses the two-unit Pasta dish at full price. */
public final class CriticCustomer extends Customer {
    public CriticCustomer(int id) { super(id); }

    @Override public String typeName() { return "Critic"; }
    @Override protected int waitingLoss() { return 12; }
    @Override protected MenuItem chooseMenuItem() { return MenuItem.PASTA; }
}
