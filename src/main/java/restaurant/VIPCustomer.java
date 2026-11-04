package restaurant;

/** A patient customer with a ten percent discount, rounded to the nearest cent. */
public final class VIPCustomer extends Customer {
    public VIPCustomer(int id) { super(id); }

    @Override public String typeName() { return "VIP"; }
    @Override protected int waitingLoss() { return 5; }
    @Override protected int calculateBillCents(int baseCents) {
        return (int) ((baseCents * 90L + 50) / 100);
    }
}
