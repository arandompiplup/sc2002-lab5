# Restaurant Rush Stage 1 - UML sequence diagram

The scenario follows customer #3, who deterministically chooses Pasta. The customer arrives on turn 7, is served on turn 10, and pays on turn 12. A final victory check happens only after all 18 turns.

```mermaid
sequenceDiagram
    actor Manager
    participant CLI as RestaurantRush (CLI)
    participant Game as RestaurantGame
    participant Queue as Waiting queue
    participant Waiter
    participant Table
    participant Customer
    participant Chef
    participant Order
    participant Cashier

    Note over CLI,Game: Turn 7 - arrival and seating
    CLI->>Game: startTurn(7)
    Game->>Customer: new Customer(3), satisfaction=100
    Game->>Queue: add customer #3
    CLI->>Manager: show turn, queue, tables, satisfaction, status, staff availability
    Manager->>CLI: choose Waiter: Seat queue position 1 at free table 1
    CLI->>Game: seat(customer3, table1)
    Game->>Waiter: seat(customer3, table1)
    Waiter->>Table: seat(customer3)
    Table->>Customer: seat(table1), state=SEATED
    Game->>Queue: remove customer #3
    CLI->>Manager: Chef task choice
    Manager->>CLI: Wait
    CLI->>Game: finishTurn()
    Game->>Customer: loseSatisfaction() [SEATED]
    Note right of Customer: Satisfaction 92

    Note over CLI,Game: Turn 8 - order and first preparation unit
    CLI->>Game: startTurn(8)
    CLI->>Manager: show current state and choices
    Manager->>CLI: Waiter takes customer #3's order
    CLI->>Game: order(customer3, PASTA)
    Game->>Waiter: takeOrder(customer3, PASTA)
    Waiter->>Customer: placeOrder(PASTA)
    Customer->>Order: create Order(PASTA)
    Note right of Order: PREPARING, 0/2 units, $18.00
    CLI->>Manager: Chef task choice
    Manager->>CLI: Prepare customer #3's order
    CLI->>Game: prepare(customer3)
    Game->>Chef: prepare(customer3)
    Chef->>Order: prepareOneUnit()
    Note right of Order: PREPARING, 1/2 units
    CLI->>Game: finishTurn()
    Game->>Customer: loseSatisfaction() [SEATED]
    Note right of Customer: Satisfaction 84

    Note over CLI,Game: Turn 9 - Pasta becomes READY
    CLI->>Game: startTurn(9)
    CLI->>Manager: show current state and choices
    Manager->>CLI: Waiter waits; Chef prepares customer #3's order
    CLI->>Game: prepare(customer3)
    Game->>Chef: prepare(customer3)
    Chef->>Order: prepareOneUnit()
    Order->>Order: unitsPrepared reaches 2
    Order->>Order: status = READY
    Note right of Order: Cannot be served during this turn because Waiter acted first
    CLI->>Game: finishTurn()
    Game->>Customer: loseSatisfaction() [SEATED]
    Note right of Customer: Satisfaction 76

    Note over CLI,Game: Turn 10 - serve READY order
    CLI->>Game: startTurn(10)
    CLI->>Manager: show order status READY
    Manager->>CLI: Waiter serves customer #3
    CLI->>Game: serve(customer3)
    Game->>Waiter: serve(customer3, turn=10)
    Waiter->>Customer: serve(10)
    Customer->>Order: serve()
    Order->>Order: status = SERVED
    Note right of Customer: state=EATING; satisfaction stops falling
    CLI->>Game: finishTurn()
    Note over Game,Customer: Served this turn, so eating does not finish yet

    Note over CLI,Game: Turn 11 - one full turn of eating completes
    CLI->>Game: startTurn(11)
    CLI->>Game: finishTurn()
    Game->>Customer: finishEating(11)
    Customer->>Customer: state = READY_TO_PAY; record readyToPayTurn=11
    Game->>Cashier: collectOldest(customers, turn=11)
    Cashier-->>Game: no eligible payer (readyToPayTurn is not earlier than turn)

    Note over CLI,Game: Turn 12 - automatic payment
    CLI->>Game: startTurn(12)
    CLI->>Game: finishTurn()
    Game->>Cashier: collectOldest(customers, turn=12)
    Cashier->>Customer: select oldest READY_TO_PAY customer
    Cashier-->>Game: customer #3
    Game->>Customer: pay()
    Customer->>Order: markPaid()
    Order->>Order: status = PAID
    Customer->>Table: release(customer #3)
    Table->>Table: occupant = null
    Game->>Game: revenueCents += 1800

    Note over CLI,Game: Turns 13-18 continue with the same six-step turn sequence
    loop For each remaining turn through turn 18
        CLI->>Game: startTurn(turn)
        Manager->>CLI: make eligible Waiter and Chef choices
        CLI->>Game: finishTurn()
    end
    CLI->>Game: victory()
    Game->>Game: count PAID customers; calculate paid satisfaction average
    alt At least 4 paid AND revenue >= $45.00 AND average >= 60
        Game-->>CLI: true - VICTORY
    else Any target is missed
        Game-->>CLI: false - DEFEAT
    end
    CLI-->>Manager: show final result, paid count, revenue, average, and 18 turns used
```
