package restaurant;

public interface OrderInterface {
    MenuItem item();
    int unitsPrepared();
    Order.Status status();
    int billCents();
    void prepareOneUnit();
    void serve();
    void markPaid();
    void cancel();
}
