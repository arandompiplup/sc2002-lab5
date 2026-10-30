package restaurant;

import java.util.*;

/** Command-line boundary for the Stage 1 game. */
public final class RestaurantRush {
    private final Scanner input = new Scanner(System.in);
    private final RestaurantGame game = new RestaurantGame();

    public static void main(String[] args) { new RestaurantRush().play(); }

    private void play() {
        System.out.println("Restaurant Rush - Stage 1");
        for (int turn = 1; turn <= RestaurantGame.MAX_TURNS; turn++) {
            game.startTurn(turn);
            showState();
            waiterChoice();
            chefChoice();
            game.finishTurn();
            System.out.printf("End of turn %d: served %d, departures %d, revenue $%.2f, average paid satisfaction %s%n%n",
                    turn, game.servedCount(), game.departures(), game.revenueCents() / 100.0, averageText());
        }
        System.out.printf("Final result: %s - %d paid, revenue $%.2f, average paid satisfaction %s, 18 turns used.%n",
                game.victory() ? "VICTORY" : "DEFEAT", paidCount(), game.revenueCents() / 100.0, averageText());
    }

    private void showState() {
        System.out.printf("\nTurn %d/18 | Revenue $%.2f | Avg paid satisfaction %s%n", game.turn(), game.revenueCents()/100.0, averageText());
        System.out.print("Waiting: ");
        if (game.waiting().isEmpty()) System.out.print("none");
        for (Customer c : game.waiting()) System.out.printf("#%d(sat %d) ", c.id(), c.satisfaction());
        System.out.println();
        for (Table t : game.tables()) {
            Customer c = t.occupant();
            if (c == null) System.out.printf("Table %d: free%n", t.number());
            else System.out.printf("Table %d: #%d sat %d, %s%n", t.number(), c.id(), c.satisfaction(), customerStatus(c));
        }
        System.out.println("Waiter: " + (game.waiter().available() ? "1 task available" : "used") + " | Chef: " + (game.chef().available() ? "1 prep unit available" : "used"));
    }
    private String customerStatus(Customer c) {
        if (c.order() == null) return "no order";
        return c.order().item().displayName() + " - " + c.order().status() + " (" + c.order().unitsPrepared() + "/" + c.order().item().preparationUnits() + " units)";
    }
    private void waiterChoice() {
        while (game.waiter().available()) {
            System.out.println("Waiter: 1 Seat, 2 Take order, 3 Serve READY, 0 Wait");
            int action = askInt("Choice: ");
            if (action == 0) return;
            if (action == 1) {
                if (game.waiting().isEmpty()) { System.out.println("No customers are waiting."); continue; }
                System.out.println("Waiting customer number: ");
                Customer c = game.waitingCustomer(askInt("Queue position: "));
                if (c == null) { System.out.println("Invalid queue position."); continue; }
                List<Table> free = game.tables().stream().filter(Table::isFree).toList();
                if (free.isEmpty()) { System.out.println("No table is free."); continue; }
                int n = askInt("Free table number " + free.stream().map(t -> Integer.toString(t.number())).toList() + ": ");
                Table table = game.table(n);
                if (table == null || !table.isFree()) { System.out.println("That table is unavailable."); continue; }
                game.seat(c, table); System.out.printf("Seated customer #%d at table %d.%n", c.id(), n); return;
            }
            if (action == 2) {
                Customer c = chooseCustomer("Customer ID to order for: ", x -> x.state() == Customer.State.SEATED && x.order() == null);
                if (c == null) continue;
                game.order(c); System.out.printf("Customer #%d chose %s.%n", c.id(), c.order().item().displayName()); return;
            }
            if (action == 3) {
                Customer c = chooseCustomer("Customer ID to serve: ", x -> x.order() != null && x.order().status() == Order.Status.READY);
                if (c == null) continue;
                game.serve(c); System.out.printf("Served customer #%d.%n", c.id()); return;
            }
            System.out.println("Enter 0, 1, 2, or 3.");
        }
    }
    private void chefChoice() {
        while (game.chef().available()) {
            System.out.println("Chef: 1 Prepare an order, 0 Wait");
            int action = askInt("Choice: ");
            if (action == 0) return;
            if (action != 1) { System.out.println("Enter 0 or 1."); continue; }
            Customer c = chooseCustomer("Customer ID whose order to prepare: ", x -> x.order() != null && x.order().status() == Order.Status.PREPARING);
            if (c == null) continue;
            game.prepare(c);
            System.out.printf("Chef advanced customer #%d's %s (%d/%d).%n", c.id(), c.order().item().displayName(), c.order().unitsPrepared(), c.order().item().preparationUnits());
        }
    }
    private Customer chooseCustomer(String prompt, java.util.function.Predicate<Customer> valid) {
        if (game.customers().stream().noneMatch(valid)) {
            System.out.println("No customers are eligible for this task. Choose another task or Wait.");
            return null;
        }
        System.out.println("Enter 0 to return to the task menu.");
        while (true) {
            int id = askInt(prompt);
            if (id == 0) return null;
            Customer c = game.customer(id);
            if (c == null) { System.out.println("No customer with that ID."); continue; }
            if (!valid.test(c)) { System.out.println("That customer is not eligible for this task."); continue; }
            return c;
        }
    }
    private int askInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            try { return Integer.parseInt(line); }
            catch (NumberFormatException ex) { System.out.println("Enter a whole number."); }
        }
    }
    private long paidCount() { return game.customers().stream().filter(Customer::isPaid).count(); }
    private String averageText() { double avg = game.averagePaidSatisfaction(); return avg < 0 ? "N/A" : String.format(Locale.ROOT, "%.1f", avg); }
}
