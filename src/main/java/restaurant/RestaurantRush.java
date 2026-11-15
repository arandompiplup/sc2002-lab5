package restaurant;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.ToIntFunction;
import restaurant.game.CustomerView;
import restaurant.game.RestaurantGame;
import restaurant.game.RestaurantGameInterface;
import restaurant.game.TableView;
import restaurant.game.TurnPhase;
import restaurant.game.WaiterTask;

public final class RestaurantRush {
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[1;36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String MAGENTA = "\u001B[1;35m";

    private final boolean coloursEnabled = System.getenv("NO_COLOR") == null
            && !"dumb".equals(System.getenv("TERM"));
    private final RestaurantGameInterface game;
    private final Scanner input;
    private final PrintStream output;

    public RestaurantRush(RestaurantGameInterface game, Scanner input, PrintStream output) {
        this.game = Objects.requireNonNull(game, "Invalid 'game' input.");
        this.input = Objects.requireNonNull(input, "Input is required.");
        this.output = Objects.requireNonNull(output, "Output is required.");
    }

    public static void main(String[] args) {
        RestaurantGameInterface game = new RestaurantGame();
        new RestaurantRush(game, new Scanner(System.in), System.out).play();
    }

    public void play() {
        output.println();
        output.println(colour(MAGENTA, "+==================================+"));
        output.println(colour(MAGENTA, "|         RESTAURANT RUSH          |"));
        output.println(colour(MAGENTA, "+==================================+"));
        output.println();
        try {
            for (int turn = 1; turn <= RestaurantGame.MAX_TURNS; turn++) {
                game.startTurn(happyHourChoice(turn));
                showTurnState();
                hostChoice();
                waiterChoice();
                chefChoice();
                game.finishTurn();
                showTurnResult();
            }
            showFinalResult();
        } catch (NoSuchElementException e) {
            output.println(colour(YELLOW,
                    "Input ended before the game finished. The game stopped early."));
        }
    }

    private boolean happyHourChoice(int turn) {
        if (!game.canActivateHappyHour()) {
            return false;
        }

        while (true) {
            int choice = readInt("Activate Happy Hour on turn " + turn + "? 1 Yes, 0 Skip: ");
            if (choice == 1) {
                return true;
            }
            if (choice == 0) {
                return false;
            }
            output.println(colour(YELLOW, "Enter 0 or 1."));
        }
    }

    private void showTurnState() {
        output.printf("%nTurn %d/%d | Revenue %s | Average paid satisfaction %s%n",
                game.turn(),
                RestaurantGame.MAX_TURNS,
                money(game.revenueCents()),
                averageText());

        int happyHourTurns = game.happyHourTurnsRemaining();
        if (happyHourTurns > 0) {
            output.printf(
                    colour(MAGENTA, "Happy Hour: %d turn%s remaining (including this turn).%n"),
                    happyHourTurns, happyHourTurns == 1 ? "" : "s");
        }

        List<CustomerView> queue = game.waiting();
        output.println(colour(CYAN, "Waiting queue:"));
        if (queue.isEmpty()) {
            output.println("  (empty)");
        } else {
            for (CustomerView c : queue) {
                output.println("  " + describe(c));
            }
        }

        output.println(colour(CYAN, "Tables:"));
        for (TableView table : game.tables()) {
            if (table.isFree()) {
                output.printf(colour(GREEN, "  Table %d: free%n"), table.getNumber());
            } else {
                output.printf("  Table %d: %s%n", table.getNumber(), describe(table.getOccupant()));
            }
        }

        output.println(colour(CYAN, "Customers:"));
        if (game.customers().isEmpty()) {
            output.println("  (none)");
        } else {
            game.customers().forEach(customer -> output.println("  " + describe(customer)));
        }

        output.println("Each turn:");
        output.println("Host can seat one customer.");
        output.println("Waiter can seat, take an order, or serve one customer.");
        output.println("Chef can advance one order by one preparation unit.");
        output.println("Cashier automatically collects up to one payment.");
    }

    private String describe(CustomerView customer) {
        StringBuilder sb = new StringBuilder(String.format(Locale.ROOT,
                "%-8s #%-2d : %-12s | satisfaction %3d",
                customer.getTypeName(),
                customer.getId(),
                customer.getState(),
                customer.getSatisfaction()));

        if (customer.hasOrder()) {
            sb.append(" | ").append(customer.getDishName())
                    .append(" - ").append(customer.getOrderStatus())
                    .append(" (").append(customer.getUnitsPrepared()).append('/')
                    .append(customer.getPreparationUnits()).append(" units, ")
                    .append(money(customer.getBillCents())).append(')');
        } else {
            sb.append(" | no order");
        }
        return colour(customer.getSatisfaction() < 40 ? RED
                : customer.getSatisfaction() < 70 ? YELLOW : GREEN, sb.toString());
    }

    private String customerLabel(CustomerView customer) {
        return customer.getTypeName() + " #" + customer.getId();
    }

    private void hostChoice() {
        List<CustomerView> eligible = game.eligibleFor(WaiterTask.SEAT);
        if (eligible.isEmpty()) {
            output.println(colour(YELLOW, "Host: No customers to seat. Automatically waiting."));
            game.waitHost();
            return;
        }

        while (game.phase() == TurnPhase.HOST) {
            output.println(colour(CYAN, "\nHost: 1 Seat, 0 Wait"));
            int choice = readInt("Host choice: ");
            if (choice == 0) {
                game.waitHost();
                return;
            }
            if (choice != 1) {
                output.println(colour(YELLOW, "Enter 0 or 1. The Host task is still available."));
                continue;
            }
            try {
                seatCustomer();
            } catch (IllegalArgumentException | IllegalStateException e) {
                output.println(
                        colour(YELLOW, "That action could not be completed: " + e.getMessage()));
                output.println(colour(YELLOW, "Choose again; the Host task has not been used."));
            }
        }
    }

    private void waiterChoice() {
        if (game.eligibleFor(WaiterTask.SEAT).isEmpty()
                && game.eligibleFor(WaiterTask.ORDER).isEmpty()
                && game.eligibleFor(WaiterTask.SERVE).isEmpty()) {
            output.println(colour(YELLOW,
                    "Waiter: No customers to seat, take orders from, or serve. Automatically waiting."));
            game.waitWaiter();
            return;
        }

        while (game.phase() == TurnPhase.WAITER) {
            output.println(
                    colour(CYAN, "\nWaiter: 1 Seat, 2 Take an order, 3 Serve READY, 0 Wait"));
            showEligibleCustomers("Customers waiting to be seated:", WaiterTask.SEAT);
            showEligibleCustomers("Customers waiting to order:", WaiterTask.ORDER);
            showEligibleCustomers("Dishes ready to be served:", WaiterTask.SERVE);

            int choice = readInt("Waiter choice: ");
            if (choice == 0) {
                game.waitWaiter();
                return;
            }

            try {
                switch (choice) {
                    case 1 -> seatCustomer();
                    case 2 -> takeOrder();
                    case 3 -> serveCustomer();
                    default ->
                        output.println(colour(YELLOW,
                                "Enter 0, 1, 2, or 3. The Waiter task is still available."));
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                output.println(
                        colour(YELLOW, "That action could not be completed: " + e.getMessage()));
                output.println(colour(YELLOW, "Choose again; the Waiter task has not been used."));
            }
        }
    }

    private void showEligibleCustomers(String heading, WaiterTask task) {
        List<CustomerView> eligible = game.eligibleFor(task);
        output.println(colour(CYAN, heading));
        if (eligible.isEmpty()) {
            output.println("  (none)");
        } else {
            eligible.forEach(customer -> output.println("  " + describe(customer)));
        }
    }

    private void seatCustomer() {
        List<CustomerView> eligible = game.eligibleFor(WaiterTask.SEAT);
        if (eligible.isEmpty()) {
            output.println(colour(YELLOW, "No waiting customer can be seated right now."));
            return;
        }
        CustomerView customer = chooseCustomer("Customer ID to seat: ", eligible);

        List<TableView> freeTables = game.tables().stream().filter(TableView::isFree).toList();
        output.println("Free tables: " + freeTables.stream().map(TableView::getNumber).toList());

        int tableNumber = readInt("Table number: ");
        if (freeTables.stream().noneMatch(table -> table.getNumber() == tableNumber)) {
            output.println(colour(YELLOW, "That table is not free. Choose a table from the list."));
            return;
        }

        game.seat(customer.getId(), tableNumber);
        output.printf(colour(GREEN, "Seated %s at table %d.%n"), customerLabel(customer),
                tableNumber);
    }

    private void takeOrder() {
        List<CustomerView> eligible = game.eligibleFor(WaiterTask.ORDER);
        if (eligible.isEmpty()) {
            output.println(colour(YELLOW, "No seated customer is ready to order."));
            return;
        }
        CustomerView customer = chooseCustomer("Customer ID to take an order for: ", eligible);

        game.order(customer.getId());
        CustomerView updated = findCustomer(customer.getId());
        output.printf(colour(GREEN, "%s chose %s (%s).%n"),
                customerLabel(updated),
                updated.getDishName(),
                money(updated.getBillCents()));
    }

    private void serveCustomer() {
        List<CustomerView> eligible = game.eligibleFor(WaiterTask.SERVE);
        if (eligible.isEmpty()) {
            output.println(colour(YELLOW, "No order is ready to serve."));
            return;
        }
        CustomerView customer = chooseCustomer("Customer ID to serve: ", eligible);

        game.serve(customer.getId());
        output.printf(colour(GREEN, "Served %s.%n"), customerLabel(customer));
    }

    private void chefChoice() {
        List<CustomerView> eligible = game.eligibleForPreparation();
        if (eligible.isEmpty()) {
            output.println(colour(YELLOW, "Chef: No orders to prepare. Automatically waiting."));
            game.waitChef();
            return;
        }

        while (game.phase() == TurnPhase.CHEF) {
            output.printf(colour(CYAN, "%nChef: 1 Prepare an order (%d eligible), 0 Wait%n"),
                    eligible.size());

            int choice = readInt("Chef choice: ");
            if (choice == 0) {
                game.waitChef();
                return;
            }
            if (choice != 1) {
                output.println(colour(YELLOW, "Enter 0 or 1. The Chef unit is still available."));
                continue;
            }
            CustomerView customer =
                    chooseCustomer("Customer ID whose order to prepare: ", eligible);
            try {
                game.prepare(customer.getId());
                CustomerView updated = findCustomer(customer.getId());
                output.printf(colour(GREEN, "Prepared %s for %s (%d/%d units).%n"),
                        updated.getDishName(),
                        customerLabel(updated),
                        updated.getUnitsPrepared(),
                        updated.getPreparationUnits());
            } catch (IllegalArgumentException | IllegalStateException e) {
                output.println(
                        colour(YELLOW, "That action could not be completed: " + e.getMessage()));
                output.println(colour(YELLOW, "Choose again; the Chef unit has not been used."));
            }
        }
    }

    private CustomerView chooseCustomer(String prompt, List<CustomerView> eligible) {
        output.println(colour(CYAN, "Eligible customers:"));
        eligible.forEach(customer -> output.println("  " + describe(customer)));
        return chooseById(prompt, eligible, CustomerView::getId);
    }

    private <T> T chooseById(String prompt, List<T> choices, ToIntFunction<T> idOf) {
        while (true) {
            int id = readInt(prompt);
            for (T candidate : choices) {
                if (idOf.applyAsInt(candidate) == id) {
                    return candidate;
                }
            }
            output.println(colour(YELLOW,
                    "That ID is not eligible for this action. Choose one from the list."));
        }
    }

    private CustomerView findCustomer(int id) {
        return game.customers().stream().filter(customer -> customer.getId() == id).findFirst()
                .orElseThrow(
                        () -> new IllegalStateException("Customer disappeared from the game."));
    }

    private int readInt(String prompt) {
        while (true) {
            output.print(colour(CYAN, prompt));
            output.flush();
            if (!input.hasNextLine()) {
                throw new NoSuchElementException("No more console input.");
            }
            String line = input.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                output.println(colour(YELLOW, "Enter a whole number."));
            }
        }
    }

    private void showTurnResult() {
        output.printf(
                "End of turn %d: served %d, unhappy departures %d, revenue %s, average paid satisfaction %s, turns used %d.%n",
                game.turn(),
                game.servedCount(),
                game.departures(),
                money(game.revenueCents()),
                averageText(),
                game.turn());
    }

    private void showFinalResult() {
        output.printf("%nFinal result: %s%n",
                game.victory() ? colour(GREEN, "VICTORY") : colour(RED, "DEFEAT"));
        output.printf(
                "Paid customers: %d | Revenue: %s | Average paid satisfaction: %s | Turns used: %d%n",
                game.paidCount(),
                money(game.revenueCents()),
                averageText(),
                game.turn());
    }

    private String averageText() {
        if (game.paidCount() == 0) {
            return "N/A";
        }
        return String.format(Locale.ROOT, "%.1f", game.averagePaidSatisfaction());
    }

    private String colour(String code, String text) {
        return coloursEnabled ? code + text + RESET : text;
    }

    private String money(int cents) {
        return String.format(Locale.ROOT, "$%d.%02d", cents / 100, cents % 100);
    }
}
