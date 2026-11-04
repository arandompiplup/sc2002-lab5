# Restaurant Rush Stage 2 - UML sequence diagram

This diagram extends the Stage 1 sequence directly. The scenario follows Critic customer #3, whose overridden preference always chooses Pasta. The customer arrives on turn 7, is served on turn 10, and pays on turn 12. A final victory check happens only after all 18 turns.

```mermaid
sequenceDiagram
    actor Manager
    participant CLI as RestaurantRush (CLI)
    participant Game as RestaurantGame
    participant Queue as Waiting queue
    participant Waiter
    participant Table
    participant Customer as customer3: CriticCustomer
    participant Chef
    participant Order
    participant Cashier

    Note over CLI,Game: Turn 7 - arrival and seating
    CLI->>Game: startTurn(7)
    Game->>Game: createCustomer(3)
    Game->>Customer: new CriticCustomer(3), satisfaction=100
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
    Customer->>Customer: waitingLoss() returns 12 (override)
    Note right of Customer: Satisfaction 88

    Note over CLI,Game: Turn 8 - order and first preparation unit
    CLI->>Game: startTurn(8)
    CLI->>Manager: show current state and choices
    Manager->>CLI: Waiter takes customer #3's order
    CLI->>Game: order(customer3)
    Game->>Waiter: takeOrder(customer3)
    Waiter->>Customer: placeOrder()
    Customer->>Customer: chooseMenuItem() returns PASTA (Critic override)
    Customer->>Customer: calculateBillCents(1800) returns 1800 (inherited)
    Customer->>Order: create Order(PASTA, 1800)
    Note right of Order: PREPARING, 0/2 units, $18.00
    CLI->>Manager: Chef task choice
    Manager->>CLI: Prepare customer #3's order
    CLI->>Game: prepare(customer3)
    Game->>Chef: prepare(customer3)
    Chef->>Order: prepareOneUnit()
    Note right of Order: PREPARING, 1/2 units
    CLI->>Game: finishTurn()
    Game->>Customer: loseSatisfaction() [SEATED]
    Customer->>Customer: waitingLoss() returns 12 (override)
    Note right of Customer: Satisfaction 76

    Note over CLI,Game: Turn 9 - Pasta becomes READY
    CLI->>Game: startTurn(9)
    CLI->>Manager: show current state and choices
    Manager->>CLI: Waiter waits, Chef prepares customer #3's order
    CLI->>Game: prepare(customer3)
    Game->>Chef: prepare(customer3)
    Chef->>Order: prepareOneUnit()
    Order->>Order: unitsPrepared reaches 2
    Order->>Order: status = READY
    Note right of Order: Cannot be served during this turn because Waiter acted first
    CLI->>Game: finishTurn()
    Game->>Customer: loseSatisfaction() [SEATED]
    Customer->>Customer: waitingLoss() returns 12 (override)
    Note right of Customer: Satisfaction 64

    Note over CLI,Game: Turn 10 - serve READY order
    CLI->>Game: startTurn(10)
    CLI->>Manager: show order status READY
    Note over Manager,Customer: Serve now records satisfaction 64, delaying one turn lowers it to 52
    Manager->>CLI: Waiter serves customer #3
    CLI->>Game: serve(customer3)
    Game->>Waiter: serve(customer3, turn=10)
    Waiter->>Customer: serve(10)
    Customer->>Order: serve()
    Order->>Order: status = SERVED
    Note right of Customer: state=EATING, satisfaction stops falling
    CLI->>Game: finishTurn()
    Note over Game,Customer: Served this turn, so eating does not finish yet

    Note over CLI,Game: Turn 11 - one full turn of eating completes
    CLI->>Game: startTurn(11)
    CLI->>Game: finishTurn()
    Game->>Customer: finishEating(11)
    Customer->>Customer: state = READY_TO_PAY, record readyToPayTurn=11
    Game->>Cashier: collectOldest(customers, turn=11)
    Cashier-->>Game: no eligible payer (readyToPayTurn is not earlier than turn)

    Note over CLI,Game: Turn 12 - automatic payment
    CLI->>Game: startTurn(12)
    CLI->>Game: finishTurn()
    Game->>Cashier: collectOldest(customers, turn=12)
    Cashier->>Customer: select oldest READY_TO_PAY customer
    Cashier-->>Game: customer #3
    Game->>Order: billCents()
    Order-->>Game: 1800
    Game->>Game: revenueCents += 1800
    Game->>Customer: pay()
    Customer->>Order: markPaid()
    Order->>Order: status = PAID
    Customer->>Table: release(customer #3)
    Table->>Table: occupant = null
    Note right of Customer: PAID, satisfactionAtPayment=64, table reference cleared

    Note over CLI,Game: Turns 13-18 continue with the same six-step turn sequence
    loop For each remaining turn through turn 18
        CLI->>Game: startTurn(turn)
        Manager->>CLI: make eligible Waiter and Chef choices
        CLI->>Game: finishTurn()
    end
    CLI->>Game: victory()
    Game->>Game: count PAID customers, calculate paid satisfaction average
    alt At least 4 paid AND revenue >= $45.00 AND average >= 60
        Game-->>CLI: true - VICTORY
    else Any target is missed
        Game-->>CLI: false - DEFEAT
    end
    CLI-->>Manager: show final result, paid count, revenue, average, and 18 turns used
```

## Stage 2 changes from the Stage 1 sequence

- The same arrival-to-payment Pasta scenario and participants are retained. Customer #3 is now a Critic, created by the fixed arrival setup.
- The Critic chooses Pasta polymorphically and loses 12 satisfaction per unserved turn: 88, 76, then 64. All service and payment turns remain unchanged.
- Serving on turn 10 rather than turn 11 changes this customer's recorded satisfaction from 52 to 64. With the other victory-plan actions unchanged, delaying service alone would change the final average from 78.5 to 75.5; both still win. This illustrates an outcome change without claiming a different ending.
- The bill is locked when ordering and remains full price for the Critic. VIP #2 in the surrounding victory run pays $13.50 for Burger through its billing override.
- Following the complete Stage 2 victory plan yields four paid customers, $49.50 revenue, average satisfaction 78.5, and 18 turns. 
