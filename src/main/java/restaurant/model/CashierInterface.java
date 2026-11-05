package restaurant.model;


import java.util.List;


public interface CashierInterface {
    Customer collectOldest(List<Customer> customers, int turn);
}
