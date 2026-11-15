# Restaurant Rush

GitHub repository URL — <https://github.com/arandompiplup/sc2002-lab5>

## Stage versions

- [Stage 1 (`stage-1`)](https://github.com/arandompiplup/sc2002-lab5/tree/stage-1)
- [Stage 2 (`stage-2`)](https://github.com/arandompiplup/sc2002-lab5/tree/stage-2)
- [Stage 3 (`stage-3`)](https://github.com/arandompiplup/sc2002-lab5/tree/stage-3)

## Stage transitions

Class counts include top-level Java classes, including abstract classes. Interfaces and enums are listed separately. Formatting changes count as modifications.

| Transition | Classes added | Existing classes modified | Existing classes unchanged |
| --- | ---: | ---: | ---: |
| Stage 1 → Stage 2 | 2 | 6 | 7 |
| Stage 2 → Stage 3 | 2 | 4 | 11 |

### Stage 1 → Stage 2

Stage 2 adds VIP and Critic customers. The game creates them on scheduled turns and uses the shared customer and ordering flow to apply their different behaviour. [Compare Stage 1 and Stage 2 on GitHub](https://github.com/arandompiplup/sc2002-lab5/compare/6b20c9c...c8cfaba)

| Change | Files | Reason |
| --- | --- | --- |
| Added | `diagrams/classes/stage2.svg` | Show the new customer types in the Stage 2 class diagram. |
| Added | `VIPCustomer.java`, `CriticCustomer.java` | Define VIP billing and satisfaction rules and the Critic's preference for the dish with the longest preparation time. |
| Modified | `RestaurantGame.java` | Schedule VIP #2 on turn 4 and Critic #4 on turn 10. |
| Modified | `Customer.java`, `RegularCustomer.java` | Define the customer type-name method and the regular customer's display name. |
| Modified | `CustomerView.java`, `RestaurantRush.java` | Carry the customer type name to the CLI and display it. |

No files were deleted. The top-level interfaces and enums stayed the same.

### Stage 2 → Stage 3

Stage 3 adds a Host phase, combo meals, and Happy Hour. The Host can seat a customer before the Waiter's task, and both roles use the same seating interface. [Compare Stage 2 and the Stage 3 on GitHub](https://github.com/arandompiplup/sc2002-lab5/compare/c8cfaba...d066c94)

| Change | Files | Reason |
| --- | --- | --- |
| Added | `diagrams/classes/stage3.svg` | Show the Stage 3 classes and relationships. |
| Added | `01-order.svg`, `02-service.svg`, `03-payment.svg`, `overview.svg`, `stage3.svg` under `diagrams/sequence/` | Document game interactions through ordering, service, and payment. |
| Added | `Host.java`, `Seating.java` | Add the Host role and define the seating operation shared with the Waiter. |
| Added | `ComboMeal.java` | Implement `MenuItemInterface` for paired dishes with a combined price and preparation time. |
| Modified | `RestaurantGame.java` | Select the seating role by phase, build the combo menu, and apply Happy Hour timing and billing rules. |
| Modified | `RestaurantGameInterface.java`, `TurnPhase.java` | Expose Happy Hour and Host operations and add the `HOST` phase. |
| Modified | `RestaurantRush.java` | Add CLI prompts for Happy Hour and Host decisions and display the new phase. |
| Modified | `Customer.java` | Restore satisfaction for eligible customers during Happy Hour. |
| Modified | `Waiter.java` | Implement the shared `Seating` interface. |

No files were deleted.

## Demo video

Watch the [recorded group demo](./demo-vid.mp4).

| Segment | Timestamp | What it shows |
| --- | --- | --- |
| 1 | 0:00 | Launching the game from the submitted project (presented by Ryan) |
| 2 | 0:30 | Manager choices with limited staff (presented by Oswald) |
| 3 | 1:45 | A dish going from READY to SERVED, followed by payment (presented by Kon Chern) |
| 4 | 2:50 | VIP behaviour (presented by Kon Chern) |
| 5 | 3:51 | Stage 3 Happy Hour feature (presented by Kon Chern) |
| 6 | 5:25 | Reproducible victory and defeat runs (presented by Kon Chern) |
| 7 | 6:18 | Stage tag comparisons and diagrams (presented by Shawn & Ryan) |
