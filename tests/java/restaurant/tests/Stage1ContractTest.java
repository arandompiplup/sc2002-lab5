package restaurant.tests;

import java.util.List;
import restaurant.game.CustomerView;
import restaurant.game.RestaurantGame;
import restaurant.game.RestaurantGameInterface;
import restaurant.game.TableView;
import restaurant.game.TurnPhase;
import restaurant.game.WaiterTask;
import restaurant.model.Cashier;
import restaurant.model.Chef;
import restaurant.model.Customer;
import restaurant.model.MenuItem;
import restaurant.model.MenuItemInterface;
import restaurant.model.Order;
import restaurant.model.RegularCustomer;
import restaurant.model.Staff;
import restaurant.model.Table;
import restaurant.model.Waiter;

/** Behavioural contracts: assertions use observable outcomes, not implementation fields. */
public final class Stage1ContractTest {
    private static int checks;

    public static void main(String[] args) {
        turnProtocolAndSnapshots();
        invalidActionsPreserveState();
        customerLifecycle();
        abandonment();
        cashierPriorityAndLimit();
        staffSubstitutionContract();
        menuAbstractionContract();
        completeGameBoundary();
        lastArrivalCannotPay();
        System.out.println("PASS: " + checks + " Stage 1 domain/controller contract checks.");
    }

    private static void turnProtocolAndSnapshots() {
        RestaurantGameInterface game = new RestaurantGame();
        rejects(game::finishTurn);
        rejects(game::victory);
        game.startTurn();
        check(game.turn() == 1 && game.waiting().size() == 1, "Scheduled arrival");
        CustomerView oldCustomer = game.waiting().get(0);
        TableView oldTable = game.tables().get(0);
        rejects(game::startTurn); // Cannot reset staff by restarting the same turn.
        rejects(() -> game.prepare(1)); // Chef cannot go before Waiter.
        rejects(() -> game.waiting().clear());
        game.seat(1, 1);
        rejects(() -> game.order(1)); // Cannot do two Waiter actions.
        game.waitChef();
        game.finishTurn();
        check(oldCustomer.state() == Customer.State.WAITING && oldCustomer.satisfaction() == 100,
                "Snapshots retain old copied values");
        check(oldTable.isFree(), "Nested table snapshot is not live");
        game.startTurn();
        check(game.eligibleFor(WaiterTask.ORDER).size() == 1, "Controller owns eligibility");
        game.order(1);
        game.prepare(1);
        rejects(() -> game.serve(1)); // Chef completion does not reopen Waiter phase.
        game.finishTurn();
        check(game.revenueCents() == 0, "READY order is unpaid");
        game.startTurn();
        game.serve(1);
        game.waitChef();
        game.finishTurn();
        check(view(game, 1).state() == Customer.State.EATING, "Service turn is not a full eating turn");
        idleTurn(game);
        check(view(game, 1).state() == Customer.State.READY_TO_PAY, "Full eating turn completes");
        check(game.revenueCents() == 0, "Cannot pay on the turn readiness begins");
        idleTurn(game);
        check(game.paidCount() == 1 && game.revenueCents() == 900, "Payment on following turn");
        check(game.tables().get(0).isFree(), "Payment frees table");
        check(game.averagePaidSatisfaction() == 84, "Paid satisfaction is recorded");
    }

    private static void invalidActionsPreserveState() {
        RestaurantGameInterface game = new RestaurantGame();
        game.startTurn();
        rejects(() -> game.order(1));
        rejects(() -> game.seat(999, 1));
        rejects(() -> game.seat(1, 999));
        check(game.phase() == TurnPhase.WAITER && game.waiting().size() == 1,
                "Rejected controller actions preserve phase/queue");
        game.seat(1, 1); // A valid retry remains possible.
        rejects(() -> game.prepare(1));
        check(game.phase() == TurnPhase.CHEF, "Rejected preparation preserves Chef choice");
        game.waitChef();
        game.finishTurn();
        idleTurn(game);
        idleTurn(game);
        game.startTurn(); // Customer 2 arrives; occupied table 1 must be rejected.
        rejects(() -> game.seat(2, 1));
        check(game.waiting().size() == 1 && game.tables().get(0).occupant().id() == 1,
                "Failed seating changes neither association");
        game.seat(2, 2);

        Waiter waiter = new Waiter();
        Customer waiting = new RegularCustomer(1);
        rejects(() -> waiter.takeOrder(waiting));
        check(waiter.available(), "Rejected waiter task does not consume allowance");
        waiter.seat(waiting, new Table(1));
        check(!waiter.available(), "Successful task consumes allowance");
        Chef chef = new Chef();
        rejects(() -> chef.prepare(waiting, 1));
        check(chef.available(), "Rejected chef task does not consume allowance");
    }

    private static void customerLifecycle() {
        Customer customer = new RegularCustomer(3); // Pasta, two preparation units.
        Table table = new Table(1);
        rejects(() -> customer.pay(1));
        rejects(() -> customer.receiveFood(1));
        table.seat(customer);
        customer.placeOrder();
        customer.prepareFood(2);
        check(customer.order().status() == Order.Status.PREPARING, "Pasta needs two units");
        customer.prepareFood(3);
        rejects(() -> customer.receiveFood(3));
        check(customer.order().status() == Order.Status.READY, "Rejected same-turn serve preserves READY");
        customer.receiveFood(4);
        customer.waitAtTurnEnd();
        check(customer.satisfaction() == 100, "Eating customers lose no satisfaction");
        customer.finishEating(4);
        check(customer.state() == Customer.State.EATING, "No eating completion on service turn");
        customer.finishEating(5);
        rejects(() -> customer.pay(5));
        check(customer.pay(6) == 1800 && table.isFree(), "Pasta lifecycle and bill");
        rejects(() -> customer.pay(7));
        rejects(() -> customer.order().cancel());
    }

    private static void abandonment() {
        Customer customer = new RegularCustomer(1);
        Table table = new Table(1);
        table.seat(customer);
        customer.placeOrder();
        for (int turn = 1; turn <= 12; turn++) customer.waitAtTurnEnd();
        check(customer.satisfaction() == 4, "Regular waiting loss is 8 per turn");
        customer.waitAtTurnEnd();
        check(customer.satisfaction() == 0 && customer.state() == Customer.State.LEFT, "Clamp and abandon");
        check(customer.order().status() == Order.Status.CANCELLED && table.isFree(), "Cancel and release on abandonment");
        rejects(() -> customer.pay(14));
        rejects(() -> customer.prepareFood(14));

        Customer ready = new RegularCustomer(1);
        Table readyTable = new Table(2);
        readyTable.seat(ready);
        ready.placeOrder();
        ready.prepareFood(1);
        for (int turn = 1; turn <= 13; turn++) ready.waitAtTurnEnd();
        check(ready.order().status() == Order.Status.CANCELLED && readyTable.isFree(),
                "READY but unserved orders are also cancelled on abandonment");
    }

    private static void cashierPriorityAndLimit() {
        Customer laterReady = readyCustomer(1, 5);
        Customer earlierReady = readyCustomer(2, 3);
        Cashier cashier = new Cashier();
        check(cashier.collectPayment(List.of(laterReady, earlierReady), 6) == 1500,
                "Longest payment wait wins over arrival ID");
        check(earlierReady.isPaid() && !laterReady.isPaid(), "Only selected customer pays");
        rejects(() -> cashier.collectPayment(List.of(laterReady), 6));
        cashier.beginTurn();
        check(cashier.collectPayment(List.of(laterReady), 7) == 900, "Allowance resets next turn");
        Customer second = readyCustomer(2, 3);
        Customer first = readyCustomer(1, 3);
        cashier.beginTurn();
        cashier.collectPayment(List.of(second, first), 4);
        check(first.isPaid(), "Arrival ID breaks ready-time ties");
    }

    private static Customer readyCustomer(int id, int readyTurn) {
        Customer customer = new RegularCustomer(id);
        new Table(id).seat(customer);
        customer.placeOrder();
        customer.prepareFood(readyTurn - 2);
        customer.receiveFood(readyTurn - 1);
        customer.finishEating(readyTurn);
        return customer;
    }

    private static void staffSubstitutionContract() {
        // Use the parent contract with all three concrete types (LSP).
        for (Staff staff : List.of(new Waiter(), new Chef(), new Cashier())) {
            staff.beginTurn();
            check(staff.available(), "Every Staff subtype honours beginTurn/available");
        }
    }

    private static void menuAbstractionContract() {
        // Test-only alternate implementation: proves Order uses the abstraction.
        MenuItemInterface alternate = new MenuItemInterface() {
            public String displayName() { return "Test dish"; }
            public int priceCents() { return 1234; }
            public int preparationUnits() { return 1; }
        };
        Order order = new Order(alternate);
        order.prepareOneUnit(1);
        order.serve(2);
        order.markPaid();
        check(order.billCents() == 1234, "Order accepts another valid menu implementation");
        check(new Order(MenuItem.SALAD).billCents() == 900, "Built-in item obeys same contract");
    }

    private static void completeGameBoundary() {
        RestaurantGameInterface game = new RestaurantGame();
        for (int turn = 1; turn <= 18; turn++) idleTurn(game);
        check(game.phase() == TurnPhase.FINISHED && game.turn() == 18, "Exactly 18 turns");
        check(game.customers().size() == 6, "Exactly six scheduled arrivals");
        check(game.departures() == 2 && game.revenueCents() == 0, "Unpaid customers generate no revenue");
        check(game.averagePaidSatisfaction() == -1 && !game.victory(), "No-payment result is defeat/N-A");
        rejects(game::startTurn);
        rejects(game::finishTurn);
        rejects(() -> game.seat(6, 1));
    }

    private static void idleTurn(RestaurantGameInterface game) {
        game.startTurn();
        game.waitWaiter();
        game.waitChef();
        game.finishTurn();
    }

    private static void lastArrivalCannotPay() {
        RestaurantGameInterface game = new RestaurantGame();
        for (int turn = 1; turn <= 15; turn++) idleTurn(game);
        game.startTurn();
        game.seat(6, 1);
        game.waitChef();
        game.finishTurn();
        game.startTurn();
        game.order(6); // Customer 6 chooses two-unit Pasta.
        game.prepare(6);
        game.finishTurn();
        game.startTurn();
        rejects(() -> game.serve(6));
        game.waitWaiter();
        game.prepare(6);
        game.finishTurn();
        check(view(game, 6).orderStatus() == Order.Status.READY && game.paidCount() == 0,
                "Turn-16 arrival cannot pay by turn 18 even with immediate attention");
    }

    private static CustomerView view(RestaurantGameInterface game, int id) {
        for (CustomerView customer : game.customers()) if (customer.id() == id) return customer;
        throw new AssertionError("Missing customer " + id);
    }

    private static void rejects(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException ex) {
            checks++;
            return;
        }
        throw new AssertionError("Invalid action was accepted");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
