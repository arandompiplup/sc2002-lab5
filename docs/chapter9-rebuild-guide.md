# Rebuilding Stage 1 with Chapter 9

This is an annotated learning implementation, not a claim that putting SOLID labels in comments proves a good design. Rebuild each small class yourselves and explain its decisions in your own words. The Java comments identify responsibilities, contracts and extension points; tests check externally visible rules.

Only Stage 1 features run: regular customers, the fixed menu, two tables and three staff members. There are no VIPs, Critics, Hosts, combos or Happy Hour implementations.

## 1. The lecture's objective

Chapter 9 pages 12-17 emphasise managing dependencies to minimise the impact of change: encapsulation, low coupling and high cohesion. Pages 30-45 discuss SRP, OCP, LSP, ISP and DIP. A working game can still have a poor design if changing one rule causes unrelated classes to change.

Read these local lecture files alongside this guide:

- [Chapter 9: Design Principles](</Users/shawn/Desktop/sc2002/lecture_materials/17S1_CECZ2002_PPT_Chapter9DesignPrinciples_V1.0(1).pdf>): pages 14-20, 24-26, 30-45.
- [Chapter 3: Inheritance](</Users/shawn/Desktop/sc2002/lecture_materials/SC2002_Chapter 3_Inheritance.pdf>): pages 24-35 and 48-50.
- [Chapter 5: Polymorphism](</Users/shawn/Desktop/sc2002/lecture_materials/17S1_CE2002_PPT_Chapter05Polymorphism_V1.0.pdf>): pages 7-9, 16 and 27-31.

The lecture PDF calls DIP "Dependency Injection Principle". Its standard name is Dependency Inversion Principle. Constructor injection is one technique used here to implement it.

## 2. Responsibilities before code (SRP and cohesion)

| Class | Responsibility | Does not own |
| --- | --- | --- |
| `RestaurantRush` | Input, menus, output formatting and input recovery | Eligibility, satisfaction, payment rules |
| `RestaurantGame` | Coordinate turns, own game objects and aggregate results | Console formatting; individual order transitions |
| `Customer` | Customer lifecycle, satisfaction and associated order/table | Global turn scheduling or revenue |
| `RegularCustomer` | Regular waiting loss and deterministic food preference | A separate copy of the lifecycle |
| `Order` | Preparation, READY/SERVED/PAID/CANCELLED transitions and bill | Table occupancy or game revenue |
| `Table` | Occupancy and seating association | Customer preferences or staff allowance |
| `Staff` | Common one-successful-task allowance | Every possible staff task |
| `Waiter` | Seat, take order, serve | Computing satisfaction |
| `Chef` | One unit of preparation | Choosing what the customer orders |
| `Cashier` | Select the oldest eligible payer and collect once | Total revenue or victory |
| `CustomerView`, `TableView` | Immutable display data copied from entities | Mutating the running game |

SRP means one cohesive reason to change, not one method per class. The controller legitimately coordinates multiple entities. It delegates individual rules rather than becoming a God class that directly edits every field.

Exercise: change the way currency is displayed. Which class changes? Then change regular waiting loss. Which class changes? The answers should be `RestaurantRush` and `RegularCustomer`, respectively.

## 3. Encapsulation: behaviour, not public setters

Fields are private. Methods describe valid operations (`placeOrder`, `receiveFood`, `pay`) rather than arbitrary assignments such as `setState(PAID)`.

- `Order.serve(turn)` requires READY from an earlier turn.
- `Customer.pay(turn)` requires READY_TO_PAY from an earlier turn.
- `Table.seat(customer)` validates both occupancy and waiting state before changing either object.
- The Waiter records a task only after its domain operation succeeds.
- The game validates the current phase before each command. An invalid call cannot restart a turn or reorder staff actions.

The controller's public API accepts IDs, not caller-supplied Customer/Table objects. It looks up the actual objects it owns, preventing an unrelated customer's object from being inserted through a command.

The CLI sees **copied snapshots**, not live entities. Returning an unmodifiable list of mutable customers would protect only the list, not the objects inside it. `CustomerView` copies primitive values, strings and immutable enums; `TableView` holds another snapshot. Old snapshots do not change as the game runs.

Model classes collaborate through public behaviour methods because the controller is in a different Java package. Their local preconditions are checked, while global sequencing belongs to the controller. Entities are not claimed to independently enforce the entire game protocol; their live references are kept behind the game boundary.

Exercise: attempt to seat a customer at an occupied table, then choose a free table. Verify that the first attempt changes neither table and does not consume the task.

## 4. OCP: extend the variation, preserve the shared algorithm

`Customer` is an abstract parent. Its final `waitAtTurnEnd()` implements the common rule and calls an overridable `satisfactionLoss()`:

```java
satisfaction = Math.max(0, satisfaction - satisfactionLoss());
```

`RegularCustomer` supplies 8. Its overridden `chooseMenuItem()` cycles the fixed menu by customer ID. The controller uses a `Customer` reference and never branches on customer subtype.

For a later stage, add the required customer subclasses and change the arrival setup where appropriate. The satisfaction loop should remain unchanged. A small setup modification does not invalidate OCP: the aim is to keep stable behavioural algorithms independent of the variants.

`Order` accepts `MenuItemInterface`, whose focused contract provides a name, price and preparation units. Stage 1 uses the `MenuItem` enum. Another immutable implementation could be substituted without rewriting preparation logic. A test-only alternate dish verifies this; it is not a new playable feature.

Limits: this is an extension point, not a completed Stage 2/3 design. Discounts, order-time price calculation and Happy Hour policy still require deliberate later work. Storing the Stage 1 bill once is not a full discount system.

Exercise: explain which methods a later customer type would override, and which lifecycle methods it must retain.

## 5. LSP: parent contracts must remain true

LSP is about behaviour, not merely whether `extends` compiles.

For every `Customer` subtype:

- Construction starts waiting with satisfaction 100.
- `satisfactionLoss()` must return a positive value.
- `chooseMenuItem()` must return a non-null, valid immutable item.
- The same seating, ordering, eating, payment and departure rules apply.
- Invalid transitions leave the object unchanged.

Fields stay private. Final lifecycle methods prevent subclasses replacing the common protocol; protected abstract methods expose only the intended variation. A malicious or incorrect override can still violate the documented hooks' contract, so review and test each new subtype. Stage 1 has only one production customer subtype, so do not claim comprehensive proof for future types.

`Staff` is also a genuine parent: every role has the same `beginTurn()` and `available()` behaviour. Role-specific tasks stay on the appropriate subtype. The common allowance is consumed only after success. Tests exercise the parent contract across Waiter, Chef and Cashier and the task limits themselves.

Exercise: why would a subtype that requires a seated customer to order twice violate the common customer contract?

## 6. ISP: interfaces describe a client's needs

The old one-interface-per-entity approach was removed. There is no interface for Customer, Order, Table, Waiter, Chef or Cashier solely mirroring every public method.

Two meaningful interfaces remain:

- `RestaurantGameInterface`: operations and queries used by the console, without entity mutation or staff-reset access.
- `MenuItemInterface`: the small item contract used by an order.

The console currently uses both game queries and commands, so there is no demonstrated need to split that interface further. A future scoreboard that only reads results may justify a smaller read-only interface. ISP does not require creating speculative interfaces now.

`Staff` deliberately does not declare `cook`, `seat` and `collectPayment` together. That would force roles to expose irrelevant operations or throw unsupported-operation errors.

Exercise: if you introduce a display-only client, identify the smallest contract it needs before adding a new interface.

## 7. DIP: depend on the contract at a useful boundary

The CLI field and constructor use `RestaurantGameInterface`:

```java
private final RestaurantGameInterface game;

public RestaurantRush(RestaurantGameInterface game, Scanner input, PrintStream output) {
    // Assign validated constructor arguments.
}
```

Only `main()` assembles the concrete `RestaurantGame` and passes it in. This is the composition root. Tests or another launcher can supply another implementation without changing the console. Standard input/output are also supplied, making console behaviour easier to exercise.

`Order` depends on `MenuItemInterface`, not the fixed dish enum.

The controller intentionally constructs concrete staff and tables. There is only one implementation of each role, and they are genuine owned parts of the game. This is a pragmatic boundary choice, not a claim that every dependency in the project is inverted. Introduce more abstractions only when independent variation makes them useful. No DI framework is necessary.

Exercise: point to the line where the concrete game is selected, and explain why it belongs in startup code rather than inside the console field declaration.

## 8. Composition and inheritance serve different purposes

- A `RegularCustomer` **is a** Customer.
- A Waiter/Chef/Cashier **is a** Staff member with a shared task allowance.
- A game **has** its staff, tables and customers.
- A customer creates and owns an Order.
- A table and customer have a temporary association; neither owns the other's lifetime.
- Orders reference shared enum menu items. An order does not exclusively own a menu item.

Reusing a few lines is not by itself a reason for inheritance. Here the small Staff parent models a common rule; its base API has no role-specific tasks.

## 9. Turn protocol and contracts

```text
BETWEEN_TURNS -> startTurn -> WAITER
WAITER -> successful waiter task OR waitWaiter -> CHEF
CHEF -> successful prepare OR waitChef -> END_OF_TURN
END_OF_TURN -> finishTurn -> BETWEEN_TURNS (turns 1-17)
END_OF_TURN -> finishTurn -> FINISHED (turn 18)
```

`startTurn()` owns the turn number and resets allowances exactly once. The CLI cannot pass arbitrary turn numbers. A rejected command leaves the phase unchanged, so retry is possible. `victory()` is valid only after the final turn. This enforces the protocol even without a CLI.

`finishTurn()` asks customers to complete eating, delegates one payment to Cashier, applies each customer's waiting behaviour and records departures. It adds only the actual collected amount to revenue.

The cashier interprets "oldest first" as earliest READY_TO_PAY turn, with arrival ID as a tie-breaker. A customer who becomes ready during this turn is excluded until the next turn.

## 10. Rebuild in small steps

1. Read the assignment and write this responsibility table yourselves.
2. Implement `MenuItemInterface`, `MenuItem` and `Order`. Test two-unit Pasta and same-turn service rejection.
3. Implement `Table`, abstract `Customer` and `RegularCustomer`. Test seating, ordering, eating, payment and abandonment.
4. Implement `Staff`, then Waiter, Chef and Cashier. Test that invalid tasks do not consume work and that the Cashier cannot charge twice.
5. Implement `RestaurantGame` phases and controller commands. Add arrivals and results. Use `Customer` references in its collections.
6. Implement immutable views and the game interface. Verify that saved views do not change when the live game changes.
7. Write the CLI last, injecting the game interface. It asks the controller for eligibility instead of reimplementing it.
8. Recreate the UML from the code. Run the reproducible victory and defeat plans in `stage_1.md`.
9. Discuss one alternative: a strategy object for customer behaviour could replace subclasses, but the small inheritance hierarchy fits the course's introductory examples and Stage 2 requirement naturally.
10. Review and preserve the runnable Stage 1 version before proceeding. Record actual changes, understanding and GAI assistance as required by the brief; this guide is not a substitute for your group's own reflection.

## 11. What changed from the previous design

| Earlier design | Current design | Reason |
| --- | --- | --- |
| CLI defines eligibility predicates | Game queries delegate eligibility to entities | SRP; one definition of each rule |
| Caller passes arbitrary turn number | Controller advances its own phase/turn | Encapsulation |
| Live staff exposed through getters | Staff remain private to controller | Prevent allowance resets from UI |
| Live mutable entities returned | Copied immutable view objects | Protect nested state |
| Task marked used before validation | Mark used after a successful operation | Preserve failed-action contract |
| Final concrete Customer | Abstract Customer plus RegularCustomer | Focused behavioural extension |
| Concrete menu enum in Order | MenuItemInterface in Order | Useful OCP/DIP boundary |
| Interfaces mirror every entity | Two client-relevant contracts | Simpler design; ISP reasoning |
| Flat package declarations in subfolders | Actual restaurant/game/model packages | Real package visibility boundaries |
| Stale nonrecursive source discovery | Recursive test discovery and updated build command | Reproducibility |

## 12. Verification and limits

Run `python3 tests/stage1_smoke.py`. It compiles production and test sources with `javac -Xlint:all` into a fresh temporary directory and runs:

- Controller phase/order guards, rejected actions, immutable nested views and all 18 turns.
- Preparation, service timing, one full eating turn, next-turn payment and table release.
- Regular satisfaction loss, cancellation, abandonment and unpaid revenue exclusion.
- Cashier oldest-ready selection, ties and one-payment allowance.
- Staff base contracts and an alternate menu implementation.
- Full CLI victory ($51.00, four paid, average 80.0), defeat and invalid-input/cancellation scenarios.

These checks supply evidence for specific contracts; they are not a formal proof of all possible future extensions. The diagrams describe Stage 1. Final submission still needs Stage 2/3, final matching diagrams, runnable tags and the other assignment evidence.
