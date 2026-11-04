package restaurant;

/** Dependency-free domain checks; run with assertions enabled (-ea). */
public final class CustomerBehaviourTest {
    public static void main(String[] args) {
        Customer[] customers = {new Customer(1), new VIPCustomer(2), new CriticCustomer(1)};
        int[] satisfaction = {92, 95, 88};
        int[] bills = {900, 1350, 1800};
        MenuItem[] dishes = {MenuItem.SALAD, MenuItem.BURGER, MenuItem.PASTA};
        for (int i = 0; i < customers.length; i++) {
            Customer customer = customers[i];
            customer.loseSatisfaction();
            assert customer.satisfaction() == satisfaction[i];
            new Table(i + 1).seat(customer);
            customer.placeOrder();
            assert customer.order().item() == dishes[i];
            assert customer.order().billCents() == bills[i];
        }
        // Critic ID 1 would choose Salad without its preference override.
        Customer critic = customers[2];
        assert customers[1].calculateBillCents(905) == 815 : "Round half a cent upward";
        Table criticTable = critic.table();
        for (int i = 0; i < 20; i++) critic.loseSatisfaction();
        assert critic.satisfaction() == 0;
        critic.leave();
        assert critic.order().status() == Order.Status.CANCELLED;
        assert critic.table() == null;
        assert criticTable.isFree();

        RestaurantGame game = new RestaurantGame();
        for (int turn = 1; turn <= 18; turn++) {
            game.startTurn(turn);
            game.finishTurn();
        }
        assert game.customers().size() == 6;
        assert game.customer(2).typeName().equals("VIP");
        assert game.customer(3).typeName().equals("Critic");
        assert game.customer(3).state() == Customer.State.LEFT;
        assert game.departures() == 2;
        assert game.revenueCents() == 0;
        assert !game.victory();
        System.out.println("PASS: customer behaviour, pricing, cancellation and arrivals.");
    }
}
