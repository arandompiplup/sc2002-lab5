# Restaurant Rush - Stage 1

This version implements only the required regular-customer game. Read [the Chapter 9 rebuild guide](docs/chapter9-rebuild-guide.md) alongside the source: it explains the responsibilities, contracts, trade-offs and order in which to recreate the program.

## Build and run

Java 17 or newer; no libraries, build system or network access required. From the repository root:

```sh
javac -Xlint:all -d out src/main/java/restaurant/*.java src/main/java/restaurant/game/*.java src/main/java/restaurant/model/*.java
java -cp out restaurant.RestaurantRush
```

Run all domain/controller contracts and CLI regression scenarios with Python 3 and Java 17+:

```sh
python3 tests/stage1_smoke.py
```

The test runner compiles into a temporary directory, so it never depends on existing `out/` classes.

## Required behaviour

- All 18 turns run. Arrivals occur at the start of turns 1, 4, 7, 10, 13 and 16.
- Two tables; one Waiter task, one Chef preparation unit and one automatic Cashier payment per turn.
- Salad: 900 cents / 1 unit; Burger: 1500 cents / 1 unit; Pasta: 1800 cents / 2 units. Displays use two decimal places.
- Customer IDs choose Salad, Burger, Pasta cyclically. Customers choose when ordering; the manager never chooses food.
- The controller enforces Waiter before Chef, including when called without the CLI. A dish completed this turn cannot be served this turn.
- Serving on turn T leads to READY_TO_PAY at the end of T+1 and payment at T+2 at the earliest. Tables remain occupied until payment or departure.
- Cashier priority is the earliest READY_TO_PAY turn, breaking ties by arrival/customer ID. This documents our interpretation of "oldest first".
- Waiting/seated customers lose 8 satisfaction per turn, clamped at zero. At zero they leave, cancel unfinished orders and release tables. Eating and ready-to-pay customers lose nothing.
- Invalid actions explain the issue and permit a retry without consuming a task. Entering 0 at a selector returns to its task menu. EOF stops with an incomplete-game message, without announcing a final result.
- Revenue includes only collected payments. Average satisfaction uses values recorded at payment; no payments displays N/A.
- After turn 18, Victory requires at least 4 paid customers, 4500 cents revenue and average paid satisfaction at least 60. No earlier victory check ends the game.
- Every turn-end display includes served count, departures, revenue, average and turns used.

## Reproducible victory

Queue position and customer ID are different prompts. The seat entries below identify the customer; choose that customer's current queue position.

| Turn | Waiter | Chef |
| --- | --- | --- |
| 1 | Seat #1 at table 1 | Wait |
| 2 | Order #1 | Prepare #1 |
| 3 | Serve #1 | Wait |
| 4 | Seat #2 at table 2 | Wait |
| 5 | Order #2 | Prepare #2 |
| 6 | Serve #2 | Wait |
| 7 | Seat #3 at table 1 | Wait |
| 8 | Order #3 | Prepare #3 |
| 9 | Wait | Prepare #3 |
| 10 | Serve #3 | Wait |
| 11 | Seat #4 at table 2 | Wait |
| 12 | Order #4 | Prepare #4 |
| 13 | Serve #4 | Wait |
| 14-18 | Wait | Wait |

Expected: 4 paid customers, $51.00 revenue, average satisfaction 80.0, Victory after turn 18.

## Reproducible defeat

Choose Wait for both staff on every turn (36 zero inputs). Expected: 0 paid, $0.00, N/A satisfaction, 2 unhappy departures and Defeat after turn 18.

## Design evidence

- [Class diagram](diagrams/stage1-class-diagram.md)
- [Sequence diagram](diagrams/stage1-sequence-diagram.md)
- [Chapter 9 explanation and reconstruction exercises](docs/chapter9-rebuild-guide.md)

These are Stage 1 diagrams, not the required final Stage 3 submission diagrams. No stage tags or commits are created automatically by this rewrite; preserve reviewed, runnable milestones in your group's repository.
