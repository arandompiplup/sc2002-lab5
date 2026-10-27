# Restaurant Rush Stage 1 - UML class diagram

This diagram reflects the current Stage 1 Java source. Visibility markers use UML notation: `+` public, `-` private, `#` protected.

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
        -customerChoice(Customer) MenuItem
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
        +startTurn(int) void
        +finishTurn() void
        +seat(Customer, Table) void
        +order(Customer, MenuItem) void
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
        ~placeOrder(MenuItem) void
        ~serve(int) void
        ~finishEating(int) void
        ~pay() void
        ~loseSatisfaction() void
        ~leave() void
    }

    class Order {
        -MenuItem item
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
        +takeOrder(Customer, MenuItem) void
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

    RestaurantRush ..> RestaurantGame : presents state and forwards choices
    RestaurantRush ..> MenuItem : deterministic customer choice
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
- Stage 1 has only regular customers, so the diagram contains no customer subclasses or polymorphic customer behavior.
