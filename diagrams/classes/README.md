# Proposed three-stage UML design

This set represents the design agreed in the discussion, including constructor-based menu items. It is a proposal, not a claim that these stages have been implemented. The existing Stage 1 PDF report is unchanged; the source snapshot for its diagrams is in `docs/report/diagram-sources/` at the repository root.

| Stage | SVG | Editable entry point |
| --- | --- | --- |
| 1 - regular customer and basic service | [View diagram](stage1.svg) | [PlantUML](stage1.puml) |
| 2 - VIP and Critic customer extensions | [View diagram](stage2.svg) | [PlantUML](stage2.puml) |
| 3 - Host, Seating, ComboMeal, Happy Hour | [View diagram](stage3.svg) | [PlantUML](stage3.puml) |

`../shared/class-model.puml` defines shared classes and relationships using stage conditions. Each entry file selects its stage. `../shared/style.puml` contains the formatting. Green identifies additions in the displayed stage; yellow identifies changes from Stage 2 to Stage 3. Unchanged VIP/Critic subclasses are not highlighted again in Stage 3.

## Stage 1 assumptions

- `MenuItem` is an immutable ordinary class, created with a name, integer-cent price and preparation workload. This differs from the menu enum in the current Java implementation.
- RestaurantGame holds an ordered `List<MenuItemInterface>` with three items: Salad (900 cents, 1 unit), Burger (1500, 1), Pasta (1800, 2).
- Game-to-menu and Order-to-item are plain navigable associations. Many orders can reference the same immutable item object. Each Order owns its separate progress, status and locked bill.
- The game composes its staff, customers and two tables. A Customer composes zero or one Order. The game does not separately compose Order.
- The waiting list is an ordered subset of customers. Customer/Table is a temporary association with zero or one at both ends.
- The Customer parent supplies the lifecycle and full-price default `billAfterDiscount()`. RegularCustomer supplies an 8-point loss and ID-based cyclic menu choice. The pricing hook is not a playable VIP feature in Stage 1.

## Stage 2 additions

VIPCustomer and CriticCustomer inherit from Customer. They retain the shared lifecycle and override the narrow variation points. VIP loses 5 points and overrides `billAfterDiscount()` for 10% off. Critic loses 12 and inherits full price.

The proposed Critic preference is the item with greatest preparation workload, breaking ties by the ordered menu. This is a proposed documented behaviour, not a course-mandated choice. At least one VIP and one Critic replace regular arrivals; there are still six total arrivals.

## Stage 3 additions

### Host and Seating

Host extends Staff, with its own allowance; Waiter keeps its own allowance and seating capability. Both implement `Seating.seat(customer, table)`. Game owns a Host and can delegate through `seatUsing(role: Seating, ...)`; its dashed dependency on Seating represents a method-parameter dependency, not a stored extra staff member.

After the start-of-turn decisions and satisfaction recovery, the controller accepts an optional Host seating action (or `waitHost()`), then the normal Waiter choice, then the Chef choice. Successful work by one role never consumes the other's task. These are proposed protocol changes; the current TurnPhase enum would need updating when implemented.

### ComboMeal and catalogue

ComboMeal implements MenuItemInterface. Its `first` and `second` fields reference existing immutable single dishes. They are associations, not composition diamonds: neither combo owns an exclusive copy of those dish definitions.

- Preparation units are the sum of the two components.
- Price is 90% of their combined price, rounded to the nearest cent.
- The proposed catalogue contains three base dishes and three distinct unordered combos: Salad+Burger, Salad+Pasta, Burger+Pasta. Thus Game's menu multiplicity is six in the Stage 3 proposal.
- Reverse-order duplicates and repeated-dish pairs are excluded in this proposal. The assignment does not prescribe this particular catalogue; change the documented policy and multiplicity together if the group chooses differently.
- Regular/VIP customers can cycle through the full ordered catalogue by ID. The Critic's greatest-workload preference also works with combos. Customers still select their own food.

### Happy Hour remains coordinated by RestaurantGame

The proposed `startTurn()` opens a turn-start decision phase. `activateHappyHour()` may succeed only in that phase, once per game. `beginService()` closes that decision phase, applies active-turn recovery once and opens the staff choices. This prevents the recovery from being applied before activation or repeated through invalid choices.

RestaurantGame tracks `happyHourUsed` and `happyHourStartTurn`. It is active on the activation turn and the next. The UI should query/display the remaining duration; scalar display queries are omitted from these principal-operation diagrams.

`recoverWaitingCustomers()` asks eligible waiting/seated customers to recover 15 via their own guarded `recoverSatisfaction()` method, capped at 100. Paid, eating and departed customers are not affected.

The intended ordering collaboration is:

1. Controller validates the action and asks `customer.selectItem(menu)`; that method dispatches the customer's preference hook.
2. Controller asks `customer.billAfterDiscount(item.priceCents())`, using VIP polymorphism rather than type checks.
3. Controller applies Happy Hour's 20% reduction if active, rounding the charge to cents.
4. Waiter takes the order using the selected item and final charge; Customer creates `Order(item, billCents)`.
5. Order retains that final amount. Cashier later collects it without recalculating discounts. Previously placed orders do not change.

The manager never selects the dish. Passing the customer's selected item between collaborators does not transfer that decision to the player.

## Notation and scope

The notation follows Chapter 6: `name: Type`, method signatures, visibility markers, italic abstract names/operations, hollow generalisation triangles, dashed interface realisation, ownership diamonds and multiplicities. Key operations are shown, not every accessor or eligibility helper. Reference fields are represented by association ends.

The report retains the original supporting-type diagram for the implemented view objects and enums; it is not a specification of the proposed Stage 3 phase protocol. These are class diagrams, not replacements for the full sequence scenario. Review/update both diagrams and code together as implementation proceeds.

## Regenerate

From the repository root, with Java 17+, Python 3 and an official PlantUML JAR:

```sh
python3 diagrams/render_plantuml.py --jar /path/to/plantuml.jar --source-dir diagrams/classes
```

The renderer uses the built-in Smetana layout engine and does not need Graphviz or an online rendering server.
