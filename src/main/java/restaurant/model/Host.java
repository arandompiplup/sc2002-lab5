package restaurant.model;

public final class Host extends Staff implements Seating {
    @Override
    public void seat(Customer customer, Table table) {
        requireAvailable();
        table.seat(customer);
        recordSuccessfulTask();
    }
}
