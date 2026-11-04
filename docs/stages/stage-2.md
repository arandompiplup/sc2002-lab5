# Restaurant Rush Stage 2

Stage 2 adds VIP and Critic customers using the shared Customer lifecycle. Regular customers lose 8 satisfaction per unserved turn and pay full price. VIPs lose 5 and receive a 10% discount, rounded to the nearest cent. Critics lose 12 and always choose Pasta at full price. Prices are locked when ordering; all other service rules and victory targets remain unchanged.

The six arrivals are Regular #1 at turn 1, VIP #2 at turn 4, Critic #3 at turn 7, and Regular #4–#6 at turns 10, 13, and 16. The CLI displays customer types and locked bills.

## Run and verify

Requires Java 17 or newer, without external libraries or network access:

```powershell
javac -Xlint:all -d out src/main/java/restaurant/*.java
java -cp out restaurant.RestaurantRush
```

Run the domain checks and CLI regressions:

```powershell
javac -Xlint:all -d out src/main/java/restaurant/*.java src/test/java/restaurant/CustomerBehaviourTest.java
java -ea -cp out restaurant.CustomerBehaviourTest
python tests/stage2_smoke.py
```

These checks have not been executed in the current environment because Java and its compiler are unavailable.

## Reproducible runs

Use the Waiter and Chef actions from the [Stage 1 victory notes](stage-1.md). The same actions now produce an expected four paid customers, $49.50 revenue, and average satisfaction 78.5: #1 pays on turn 5 at satisfaction 84, VIP #2 on turn 8 at 90, Critic #3 on turn 12 at 64, and #4 on turn 15 at 76. Continue through turn 18 for Victory.

For Defeat, wait with both staff throughout all 18 turns. Expected: no payments, $0.00 revenue, average N/A, and two unhappy departures.

## Design

- [Class diagram](../diagrams/stage-2/class-diagram.md)
- [Sequence diagram](../diagrams/stage-2/sequence-diagram.md)

Customer overrides dispatch patience, pricing, and preferences. Arrival setup selects types only; the controller has no type-based behaviour logic. The existing staff and turn algorithm are retained.
