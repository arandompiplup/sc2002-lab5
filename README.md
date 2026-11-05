# Restaurant Rush

A Java command-line restaurant game for the SC2002 group assignment. The current source is a **skeleton**, not a playable game: most methods still throw `UnsupportedOperationException`.

## Project layout

- `src/main/java/restaurant/RestaurantRush.java` — console entry point and interface.
- `src/main/java/restaurant/game/` — game controller, display views, and turn/action enums.
- `src/main/java/restaurant/model/` — customers, staff, orders, tables, and menu items.
- `tests/` — existing test scripts and any Java tests; these need to be reconciled with the skeleton as functionality is rebuilt.
- `diagrams/classes/` — proposed class diagrams for Stages 1–3.
- `diagrams/sequence/` — sequence diagrams.
- `docs/` — design notes and previous report material.
- `lab5_doc.md` and `SC2002_Restaurant_Rush_Group_Assignment.docx` — assignment brief.

The diagrams and earlier report describe designs or previous implementations; they may not match the current skeleton. Update them as the game is implemented.

## Implementation checklist

Checked items have their current Stage 1 code or declarations in place; they have not necessarily been tested as part of a playable game. Unchecked items still need implementation or integration.

### Stage 1 — Basic operation

- [x] `MenuItem` and `MenuItemInterface` (the required menu still needs to be set up)
- [x] `Order` (the chef/controller must enforce the per-turn preparation limit)
- [x] `Customer` (abstract base; integration with table and order flow remains to be verified)
- [x] `RegularCustomer`
- [x] `Table` and `TableInterface` integration
- [x] `Staff`, `Waiter`, `Chef`, `Cashier` integration
- [ ] `RestaurantGame` and `RestaurantGameInterface` integration
- [ ] `CustomerView` and `TableView`
- [x] `WaiterTask` and `TurnPhase` enums
- [ ] `RestaurantRush` command-line interface
- [ ] Complete playable game, including reproducible victory and defeat runs

### Stage 2 — Customer behaviour

- [ ] `VIPCustomer`
- [ ] `CriticCustomer`
- [ ] Integrate customer selection, VIP pricing and documented Critic behaviour

### Stage 3 — New role and menu policy

- [ ] Host role
- [ ] `ComboMeal`
- [ ] Happy Hour pricing and satisfaction behaviour
- [ ] Integrate and verify Stage 3 features
