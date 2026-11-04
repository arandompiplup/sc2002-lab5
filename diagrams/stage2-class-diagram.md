# Restaurant Rush Stage 2 - UML class diagram

This diagram extends the Stage 1 diagram with the implemented customer changes described in `stage_2_plan.md`. Visibility markers use UML notation: `+` public, `-` private, `#` protected.

```mermaid
classDiagram
    class RestaurantRush {
        -Scanner input
        -RestaurantGame game
        +main(String[] args)$ void
        -play() void
        -showState() void
        -waiterChoice() void
        -chefChoice() void
        -chooseCustomer(String, Predicate) Customer
        -askInt(String) int
    }

    class RestaurantGame {
        +MAX_TURNS int$
        -List~Table~ tables
        -List~Customer~ customers
        -Deque~Customer~ waiting
        -Waiter waiter
        -Chef chef
        -Cashier cashier
        -int revenueCents
        -int servedCount
        -int departures
        -int turn
        -createCustomer(int) Customer
        +startTurn(int) void
        +finishTurn() void
        +seat(Customer, Table) void
        +order(Customer) void
        +serve(Customer) void
        +prepare(Customer) void
        +victory() boolean
        +averagePaidSatisfaction() double
        +waiting() List~Customer~
        +customers() List~Customer~
        +tables() List~Table~
    }

    class Customer {
        -int id
        -int satisfaction
        -State state
        -Order order
        -Table table
        -int servedTurn
        -int readyToPayTurn
        -int satisfactionAtPayment
        ~seat(Table) void
        ~placeOrder() void
        #chooseMenuItem() MenuItem
        #waitingLoss() int
        #calculateBillCents(int) int
        +typeName() String
        ~serve(int) void
        ~finishEating(int) void
        ~pay() void
        ~loseSatisfaction() void
        ~leave() void
    }

    class VIPCustomer {
        +VIPCustomer(int)
        +typeName() String
        #waitingLoss() int
        #calculateBillCents(int) int
    }

    class CriticCustomer {
        +CriticCustomer(int)
        +typeName() String
        #waitingLoss() int
        #chooseMenuItem() MenuItem
    }

    class Order {
        -MenuItem item
        -int billCents
        +Order(MenuItem)
        +Order(MenuItem, int billCents)
        -int unitsPrepared
        -Status status
        +prepareOneUnit() void
        +serve() void
        +markPaid() void
        +cancel() void
        +billCents() int
    }

    class Table {
        -int number
        -Customer occupant
        +isFree() boolean
        ~seat(Customer) void
        ~release(Customer) void
    }

    class Waiter {
        -boolean used
        +beginTurn() void
        +available() boolean
        +seat(Customer, Table) void
        +takeOrder(Customer) void
        +serve(Customer, int) void
    }

    class Chef {
        -boolean used
        +beginTurn() void
        +available() boolean
        +prepare(Customer) void
    }

    class Cashier {
        +collectOldest(List~Customer~, int) Customer
    }

    class MenuItem {
        <<enumeration>>
        SALAD
        BURGER
        PASTA
        -String displayName
        -int priceCents
        -int preparationUnits
    }

    class CustomerState {
        <<enumeration>>
        WAITING
        SEATED
        EATING
        READY_TO_PAY
        PAID
        LEFT
    }

    class OrderStatus {
        <<enumeration>>
        PREPARING
        READY
        SERVED
        PAID
        CANCELLED
    }

    Customer <|-- VIPCustomer
    Customer <|-- CriticCustomer
    RestaurantGame ..> VIPCustomer : creates arrival 2
    RestaurantGame ..> CriticCustomer : creates arrival 3

    RestaurantRush ..> RestaurantGame : presents state and forwards choices
    RestaurantRush ..> MenuItem : displays ordered dish
    RestaurantGame *-- "2" Table : owns
    RestaurantGame *-- "0..*" Customer : tracks
    RestaurantGame *-- Waiter : owns
    RestaurantGame *-- Chef : owns
    RestaurantGame *-- Cashier : owns
    RestaurantGame o-- "0..*" Customer : waiting queue subset
    Customer *-- "0..1" Order : placed order
    Customer --> "0..1" Table : seated at
    Table --> "0..1" Customer : occupant
    Order --> "1" MenuItem : contains
    Customer --> CustomerState : state
    Order --> OrderStatus : status
    Waiter ..> Customer : seats, takes order, serves
    Waiter ..> Table : seats customer
    Chef ..> Customer : advances order preparation
    Cashier ..> Customer : selects payer
    Cashier ..> Order : paid order updated through customer
```

## Design notes

- **Encapsulation:** domain state is private; game operations update it through methods.
- **Composition:** `RestaurantGame` owns its staff and tables; each `Customer` owns at most one `Order`.
- **Responsibility split:** `RestaurantRush` handles console input/output, `RestaurantGame` advances the rules, staff classes enforce work limits, and `Customer`, `Order`, and `Table` own their state transitions.
- **Polymorphism:** `Customer` retains Regular defaults (loss 8, full price, ID-based dish cycle). `VIPCustomer` overrides loss to 5 and billing to 90% of full price. `CriticCustomer` overrides loss to 12 and always chooses Pasta. Shared lifecycle methods call these overrides through `Customer` references.
- **Encapsulation:** `Order` stores a private final bill fixed at ordering. `Customer.placeOrder()` selects the item, calls `calculateBillCents()`, and creates the order; the cashier path uses the stored amount.
- **Stage 1 continuity:** the existing classes, associations, staff responsibilities, and turn flow are preserved. Only arrival registration, customer behaviour, order billing, and CLI labels change. The queue relationship is a subset of tracked customers, not separate ownership. `CustomerState` and `OrderStatus` label the nested enums `Customer.State` and `Order.Status`.

