# Restaurant Rush - Stage 1

Stage 1 is the basic-operation version of the game: regular customers, three menu items, two tables, and a Waiter, Chef, and Cashier. This guide documents the current implementation and how to run it independently.

## Run

Requires Java 17 or newer. From the repository root:

```powershell
javac -d out src/main/java/restaurant/*.java
java -cp out restaurant.RestaurantRush
```

The game uses no network access or external dependencies.

Run the CLI regression checks with Java 17+ and Python 3:

```sh
python3 tests/stage1_smoke.py
```

## Stage 1 rules implemented

- The game runs all 18 turns. Arrivals occur at the start of turns 1, 4, 7, 10, 13, and 16.
- There are two tables, one Waiter task per turn, one Chef preparation unit per turn, and one automatic Cashier payment per turn.
- Menu: Salad ($9.00, 1 preparation unit), Burger ($15.00, 1 unit), and Pasta ($18.00, 2 units). Money is stored as integer cents.
- Customers deterministically choose their dish when the manager takes their order: customer IDs cycle Salad, Burger, Pasta. The choice is made inside `Customer`, not by the CLI. The manager chooses which customer to seat, order for, serve, and prepare.
- Invalid actions explain the issue and allow another choice without consuming the staff task. Tasks with no eligible customers return to the task menu; enter 0 at a customer-ID prompt to return to that menu.
- The Waiter acts before the Chef. The Chef can work on an order taken this turn, but an order that becomes READY cannot be served until a later turn.
- Customers eat for one full turn after service, become READY_TO_PAY, and can pay on the following turn. The table is freed on payment.
- Waiting and seated customers lose 8 satisfaction at turn end. At zero they leave without paying; unfinished orders are cancelled.
- Victory requires at least 4 paid customers, $45.00 revenue, and an average paid-customer satisfaction of at least 60. Otherwise, the result is Defeat.

## Reproducible Stage 1 run notes

Dish choices are deterministic by customer ID: #1 Salad, #2 Burger, #3 Pasta, then repeat.

- **Victory:** Waiter task by turn: 1 seat #1; 2 order #1; 3 serve #1; 4 seat #2; 5 order #2; 6 serve #2; 7 seat #3; 8 order #3; 9 wait; 10 serve #3; 11 seat #4; 12 order #4; 13 serve #4. Use the Chef on turns 2 (#1), 5 (#2), 8 and 9 (#3), and 12 (#4); wait on other turns. Let the Cashier act automatically. Customers #1-#4 pay by turn 15 for $51.00 total, with average satisfaction 80.
- **Defeat:** choose Wait for both staff on every turn. No customer pays, so the final result is Defeat.

These are input plans for capturing test notes or a demonstration. The game always processes all 18 turns.

## Stage 1 diagrams

- [Class diagram](../diagrams/stage-1/class-diagram.md)
- [Sequence diagram](../diagrams/stage-1/sequence-diagram.md)

See the repository [README](../../README.md) for Mermaid preview instructions and general project information.
