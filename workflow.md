# `workflow.md` — AI Assistant Instructions for Restaurant Rush (SC2002)

## 1. Project Overview & Core Constraints

* **Project:** SC2002 Restaurant Rush (Turn-based Java command-line restaurant management game).
* **Core Rules:**
* Exactly **18 turns** maximum per game.
* **2 tables** maximum.
* **Staff limits:** 1 Waiter (1 task/turn), 1 Chef (1 prep unit/turn), 1 Cashier (1 payment/turn).
* **Money:** Store all monetary values as integer cents to avoid floating-point errors; format to 2 decimal places in the UI.
* **UI Separation:** Keep game rules and domain logic strictly out of the Command Line Interface (CLI). The UI only collects player input and displays the current state.

Before suggesting any changes or writing code, follow these safety constraints:
- No destructive changes: Do not rewrite whole sections of working code unless explicitly told to rewrite them.
- Preserve dependencies: Do not add new external packages or libraries without asking me first.
- Incremental updates: Provide code snippets or line-by-line diffs rather than generating entire 500-line files.
- Explain logic: For complex algorithms, explain your logic in 1–2 sentences before providing the code."


---

## 2. Object-Oriented Design Principles

When generating or refactoring code, the AI must follow these architectural rules:

* **Encapsulation:** Keep fields private (`private`, `protected`) and expose behavior through well-defined public methods. Avoid public mutable state across domain objects.
* **Polymorphism:** Customer types (`Regular`, `VIP`, `Critic`) must override behavior (e.g., satisfaction loss rates, discounts, order preferences) dynamically through polymorphism rather than using type-checking conditional chains (`instanceof` or switch statements) in the turn controller.
* **Composition & Separation of Responsibilities:**
* `Restaurant` or `GameController`: Manages the overall turn loop and state progression.
* `Staff` (`Waiter`, `Chef`, `Cashier`): Encapsulates staff-specific task limitations per turn.
* `Customer`: Manages satisfaction, waiting state, and order association.
* `Order`: Tracks menu items, preparation units (`READY`, `SERVED`, `PAID`).
* `Table`: Tracks occupancy status.



---

## 3. Turn Execution Sequence (Must be strictly followed)

Every turn must execute these 6 steps sequentially:

1. **Arrivals:** Scheduled customers join the waiting queue with satisfaction `100`.
2. **Manager Choices:**
* Waiter: Choose at most one task (Seat a queued customer, take an order, or serve a READY order).
* Chef: Choose at most one order to advance by 1 preparation unit (can work on an order taken this turn).


3. **Kitchen & Serving:** Waiter acts before Chef. Completed orders become `READY`. Dishes finished this turn can only be served next turn. Serving changes status to `SERVED`.
4. **Eating & Payment:** Served customers finish eating after 1 full turn and become `READY_TO_PAY`. Cashier automatically handles 1 `READY_TO_PAY` customer (oldest first). Payment updates revenue, marks order `PAID`, and frees the table.
5. **Waiting & Abandonment:** Waiting or seated customers who are not served lose satisfaction (Regular: 8 points, minimum 0). If satisfaction hits 0, they leave without paying (cancel order, free table). Served/eating customers lose nothing.
6. **Result Check:** Runs all 18 turns with no early ending. Evaluates Victory/Defeat criteria at the end of turn 18.

---

## 4. Stage-Specific Implementation Guidelines

* **Stage 1 (`stage-1` tag):**
* Focus on Regular customers and the three menu items (Salad: $9.00 / 1 unit, Burger: $15.00 / 1 unit, Pasta: $18.00 / 2 units).
* Implement full turn sequence, satisfaction tracking, abandonment, and Victory/Defeat logic.


* **Stage 2 (`stage-2` tag):**
* Introduce `VIPCustomer` (5 satisfaction loss/turn, 10% bill discount) and `CriticCustomer` (12 satisfaction loss/turn, full price, custom order/service reaction).
* Dispatch behavior via polymorphism.


* **Stage 3 (`stage-3` tag):**
* **Host:** Seats at most one queued customer per turn independently of the Waiter.
* **ComboMeal:** Combines two menu items (prep units = sum, price = 90% of combined price rounded to nearest cent).
* **Happy Hour:** Switchable by manager once per game, lasts for the current turn and the next. Applies 20% off orders taken while active; the price is locked when the order is taken and existing orders do not change. Recovers 15 satisfaction for waiting/seated customers at turn start (maximum 100). VIP discount applies first, then Happy Hour.



---

## 5. Input Handling & Error Management

* **Robustness:** Invalid manager inputs must explain why they failed and prompt for a new choice **without consuming the staff task**.
* **No Network Required:** The application must run entirely offline.