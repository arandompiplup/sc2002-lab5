package restaurant;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import restaurant.game.CustomerView;
import restaurant.game.RestaurantGame;
import restaurant.game.RestaurantGameInterface;
import restaurant.game.TableView;
import restaurant.game.TurnPhase;
import restaurant.game.WaiterTask;

/**
 * Boundary: collects manager decisions and formats read-only views.
 * SRP: no satisfaction, eligibility, payment or victory rules are defined here.
 * DIP: only main() assembles the concrete game; the console depends on a contract.
 * REBUILD: start with the play loop, then displays, menus, and input recovery.
 */
public final class RestaurantRush {
    private final RestaurantGameInterface game;
    private final Scanner input;
    private final PrintStream output;

    public RestaurantRush(RestaurantGameInterface game, Scanner input, PrintStream output) {
        this.game = Objects.requireNonNull(game);
        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
    }

    public static void main(String[] args) {
        // Composition root: concrete implementations are selected in one place.
        RestaurantRush console = new RestaurantRush(new RestaurantGame(), new Scanner(System.in), System.out);
        try {
            console.play();
        } catch (NoSuchElementException ex) {
            System.out.println("\nInput ended. Game stopped before completion.");
        }
    }

    public void play() {
        output.println("\nRESTAURANT RUSH  /  Stage 1");
        while (game.phase() != TurnPhase.FINISHED) {
            game.startTurn();
            showState();
            waiterChoice();
            chefChoice();
            game.finishTurn();
            output.printf("%n  End of turn %d%n  Served: %d  |  Unhappy departures: %d%n  Revenue: $%.2f  |  Avg paid satisfaction: %s%n  Turns used: %d%n",
                    game.turn(), game.servedCount(), game.departures(), game.revenueCents() / 100.0,
                    averageText(), game.turn());
        }
        output.printf("%n============================================================%n  FINAL RESULT: %s%n============================================================%n  Paid customers        %d%n  Revenue               $%.2f%n  Avg paid satisfaction %s%n  Turns used            %d%n%n",
                game.victory() ? "VICTORY" : "DEFEAT", game.paidCount(), game.revenueCents() / 100.0,
                averageText(), game.turn());
    }

    private void showState() {
        output.printf("%n============================================================%n  TURN %02d / %d%n============================================================%n  Revenue               $%.2f%n  Avg paid satisfaction %s%n",
                game.turn(), game.maxTurns(), game.revenueCents() / 100.0, averageText());
        output.println("\n  WAITING QUEUE");
        List<CustomerView> queue = game.waiting();
        if (queue.isEmpty()) output.println("  None");
        for (int i = 0; i < queue.size(); i++) {
            CustomerView customer = queue.get(i);
            output.printf("  %d. Customer #%-2d  |  Satisfaction: %d%n", i + 1, customer.id(), customer.satisfaction());
        }
        output.println("\n  TABLES");
        for (TableView table : game.tables()) {
            CustomerView customer = table.occupant();
            if (table.isFree()) output.printf("  Table %d   Free%n", table.number());
            else output.printf("  Table %d   Customer #%d  |  Satisfaction: %d%n            %s  |  %s%n",
                    table.number(), customer.id(), customer.satisfaction(), customer.state(), customerStatus(customer));
        }
        output.println("\n  STAFF\n  Waiter    1 task available\n  Chef      1 prep unit available\n  Cashier   1 automatic payment per turn");
    }

    private String customerStatus(CustomerView customer) {
        if (!customer.hasOrder()) return "no order";
        return customer.dishName() + " - " + customer.orderStatus() + " ("
                + customer.unitsPrepared() + "/" + customer.preparationUnits() + " units)";
    }

    private void waiterChoice() {
        while (game.phase() == TurnPhase.WAITER) {
            output.println("\n  WAITER - Choose one task\n  ----------------------------------------\n  [1] Seat a customer\n  [2] Take an order\n  [3] Serve a ready order\n  [0] Wait\n");
            int action = askInt("Waiter choice");
            try {
                // UI action selection is not a customer-type dispatch chain.
                switch (action) {
                    case 0 -> game.waitWaiter();
                    case 1 -> seatChoice();
                    case 2 -> {
                        CustomerView customer = chooseCustomer("Customer ID to order for", game.eligibleFor(WaiterTask.ORDER));
                        if (customer != null) {
                            game.order(customer.id());
                            output.printf("  Customer #%d chose %s.%n", customer.id(), customerView(customer.id()).dishName());
                        }
                    }
                    case 3 -> {
                        CustomerView customer = chooseCustomer("Customer ID to serve", game.eligibleFor(WaiterTask.SERVE));
                        if (customer != null) {
                            game.serve(customer.id());
                            output.printf("  Served customer #%d.%n", customer.id());
                        }
                    }
                    default -> output.println("  Enter 0, 1, 2, or 3.");
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                // Expected action failures are reported here; model methods never print.
                output.println("  " + ex.getMessage());
            }
        }
    }

    private void seatChoice() {
        List<CustomerView> eligible = game.eligibleFor(WaiterTask.SEAT);
        if (eligible.isEmpty()) {
            output.println("  No queued customer can be seated at a free table.");
            return;
        }
        int position = askInt("Queue position (0 to return)");
        if (position == 0) return;
        if (position < 1 || position > eligible.size()) {
            output.println("  Invalid queue position.");
            return;
        }
        // Formatting free tables is presentation; game.seat still enforces validity.
        List<Integer> freeNumbers = game.tables().stream().filter(TableView::isFree).map(TableView::number).toList();
        int table = askInt("Free table number " + freeNumbers + " (0 to return)");
        if (table == 0) return;
        CustomerView customer = eligible.get(position - 1);
        game.seat(customer.id(), table);
        output.printf("  Seated customer #%d at table %d.%n", customer.id(), table);
    }

    private void chefChoice() {
        while (game.phase() == TurnPhase.CHEF) {
            output.println("\n  CHEF - Choose one task\n  ----------------------------------------\n  [1] Prepare an order\n  [0] Wait\n");
            int action = askInt("Chef choice");
            if (action == 0) {
                game.waitChef();
                return;
            }
            if (action != 1) {
                output.println("  Enter 0 or 1.");
                continue;
            }
            CustomerView customer = chooseCustomer("Customer ID whose order to prepare", game.eligibleForPreparation());
            if (customer == null) continue;
            try {
                game.prepare(customer.id());
                CustomerView updated = customerView(customer.id());
                output.printf("  Chef advanced customer #%d's %s (%d/%d).%n",
                        updated.id(), updated.dishName(), updated.unitsPrepared(), updated.preparationUnits());
            } catch (IllegalArgumentException | IllegalStateException ex) {
                output.println("  " + ex.getMessage());
            }
        }
    }

    private CustomerView chooseCustomer(String prompt, List<CustomerView> eligible) {
        if (eligible.isEmpty()) {
            output.println("  No customers are eligible for this task. Choose another task or Wait.");
            return null;
        }
        output.println("\n  ELIGIBLE CUSTOMERS");
        for (CustomerView customer : eligible) {
            output.printf("  ID %-3d  Table %d  |  Satisfaction: %d%n          %s%n",
                    customer.id(), customer.tableNumber(), customer.satisfaction(), customerStatus(customer));
        }
        output.println("  Enter 0 to return to the task menu.");
        while (true) {
            int id = askInt(prompt);
            if (id == 0) return null;
            for (CustomerView customer : eligible) if (customer.id() == id) return customer;
            output.println(customerView(id) == null ? "  No customer with that ID."
                    : "  That customer is not eligible for this task.");
        }
    }

    private CustomerView customerView(int id) {
        for (CustomerView customer : game.customers()) if (customer.id() == id) return customer;
        return null;
    }

    private int askInt(String prompt) {
        while (true) {
            output.print("  " + prompt + " > ");
            String line = input.nextLine().trim();
            try { return Integer.parseInt(line); }
            catch (NumberFormatException ex) { output.println("  Enter a whole number."); }
        }
    }

    private String averageText() {
        double average = game.averagePaidSatisfaction();
        return average < 0 ? "N/A" : String.format(Locale.ROOT, "%.1f", average);
    }
}
