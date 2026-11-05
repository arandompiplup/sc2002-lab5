# Stage 1 class diagram

[Open SVG export](stage1-class-diagram.svg)

The source packages are `restaurant` (CLI), `restaurant.game` (control and views) and `restaurant.model` (entities and staff). Key operations are shown; simple accessors are omitted for readability. `+` public, `-` private, `#` protected, `~` package access, `*` abstract operation, `$` static member.

```mermaid
classDiagram
    class RestaurantRush {
        <<boundary>>
        -RestaurantGameInterface game
        -Scanner input
        -PrintStream output
        +RestaurantRush(RestaurantGameInterface, Scanner, PrintStream)
        +main(String[] args)$ void
        +play() void
        -waiterChoice() void
        -chefChoice() void
        -showState() void
        -askInt(String) int
    }
    class RestaurantGameInterface {
        <<interface>>
        +phase() TurnPhase
        +startTurn() void
        +seat(int customerId, int tableNumber) void
        +order(int customerId) void
        +serve(int customerId) void
        +waitWaiter() void
        +prepare(int customerId) void
        +waitChef() void
        +finishTurn() void
        +eligibleFor(WaiterTask) List~CustomerView~
        +eligibleForPreparation() List~CustomerView~
        +customers() List~CustomerView~
        +waiting() List~CustomerView~
        +tables() List~TableView~
        +paidCount() int
        +averagePaidSatisfaction() double
        +victory() boolean
    }
    class RestaurantGame {
        <<control>>
        +MAX_TURNS int$
        -List~Customer~ customers
        -List~Customer~ waiting
        -List~Table~ tables
        -Waiter waiter
        -Chef chef
        -Cashier cashier
        -TurnPhase phase
        -int turn
        -int revenueCents
        -int servedCount
        -int departures
        +startTurn() void
        +finishTurn() void
        -requirePhase(TurnPhase) void
        -findCustomer(int) Customer
        -findTable(int) Table
        -viewsOf(List~Customer~) List~CustomerView~
    }
    class Customer {
        <<abstract entity>>
        -int id
        -int satisfaction
        -State state
        -Table table
        -Order order
        -int servedTurn
        -int readyToPayTurn
        -int satisfactionAtPayment
        #Customer(int id)
        +canBeSeated() boolean
        +canOrder() boolean
        +canPrepare() boolean
        +canBeServed(int turn) boolean
        +canPay(int turn) boolean
        ~seatAt(Table) void
        +placeOrder() void
        +prepareFood(int turn) void
        +receiveFood(int turn) void
        +finishEating(int turn) void
        +pay(int turn) int
        +waitAtTurnEnd() void
        -leave() void
        #satisfactionLoss()* int
        #chooseMenuItem()* MenuItemInterface
    }
    class RegularCustomer {
        +RegularCustomer(int id)
        #satisfactionLoss() int
        #chooseMenuItem() MenuItemInterface
    }
    class Table {
        <<entity>>
        -int number
        -Customer occupant
        +Table(int number)
        +isFree() boolean
        +seat(Customer) void
        ~release(Customer) void
    }
    class Order {
        <<entity>>
        -MenuItemInterface item
        -int billCents
        -int unitsPrepared
        -int readyTurn
        -Status status
        +Order(MenuItemInterface)
        +canBeServed(int turn) boolean
        +prepareOneUnit(int turn) void
        +serve(int turn) void
        +markPaid() void
        +cancel() void
        +billCents() int
    }
    class MenuItemInterface {
        <<interface>>
        +displayName() String
        +priceCents() int
        +preparationUnits() int
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
    class Staff {
        <<abstract>>
        -boolean used
        +beginTurn() void
        +available() boolean
        #requireAvailable() void
        #recordSuccessfulTask() void
    }
    class Waiter {
        +seat(Customer, Table) void
        +takeOrder(Customer) void
        +serve(Customer, int turn) void
    }
    class Chef {
        +prepare(Customer, int turn) void
    }
    class Cashier {
        +collectPayment(List~Customer~, int turn) int
    }
    class CustomerView {
        <<immutable snapshot>>
        -int id
        -int satisfaction
        -State state
        -int tableNumber
        -String dishName
        -Status orderStatus
        -int unitsPrepared
        -int preparationUnits
        ~CustomerView(Customer)
        +hasOrder() boolean
    }
    class TableView {
        <<immutable snapshot>>
        -int number
        -CustomerView occupant
        ~TableView(Table)
        +isFree() boolean
    }
    class TurnPhase {
        <<enumeration>>
        BETWEEN_TURNS
        WAITER
        CHEF
        END_OF_TURN
        FINISHED
    }
    class WaiterTask {
        <<enumeration>>
        SEAT
        ORDER
        SERVE
    }
    class CustomerState {
        <<enumeration Customer.State>>
        WAITING
        SEATED
        EATING
        READY_TO_PAY
        PAID
        LEFT
    }
    class OrderStatus {
        <<enumeration Order.Status>>
        PREPARING
        READY
        SERVED
        PAID
        CANCELLED
    }

    RestaurantRush --> "1" RestaurantGameInterface : injected dependency
    RestaurantRush ..> RestaurantGame : main creates
    RestaurantGameInterface <|.. RestaurantGame
    RestaurantGame "1" *-- "2" Table : owns
    RestaurantGame "1" *-- "0..6" Customer : owns
    RestaurantGame "1" *-- "1" Waiter : owns
    RestaurantGame "1" *-- "1" Chef : owns
    RestaurantGame "1" *-- "1" Cashier : owns
    RestaurantGame --> TurnPhase
    RestaurantGameInterface ..> WaiterTask
    RestaurantGameInterface ..> CustomerView
    RestaurantGameInterface ..> TableView
    Customer <|-- RegularCustomer
    Customer "1" *-- "0..1" Order : creates and owns
    Customer "0..1" -- "0..1" Table : temporary seating association
    Customer --> CustomerState
    Order --> OrderStatus
    Order --> "1" MenuItemInterface : uses immutable item
    MenuItemInterface <|.. MenuItem
    RegularCustomer ..> MenuItem : deterministic preference
    Staff <|-- Waiter
    Staff <|-- Chef
    Staff <|-- Cashier
    Waiter ..> Customer
    Waiter ..> Table
    Chef ..> Customer
    Cashier ..> Customer
    RestaurantGame ..> CustomerView : copies state
    RestaurantGame ..> TableView : copies state
    CustomerView ..> Customer : constructor copies values
    TableView ..> Table : constructor copies values
    TableView "1" *-- "0..1" CustomerView : copied occupant
    note for RestaurantGame "waiting is an ordered subset of owned customers, not a second ownership relationship"
    note for Customer "Final lifecycle methods guard transitions; protected hooks provide polymorphic variation"
    note for Staff "Shared allowance only; each role defines its own tasks"
    note for CustomerView "No live Customer, Table, Order or Staff references reach the CLI"
```

The interface-based CLI and item dependencies are meaningful DIP boundaries. Internal concrete staff ownership is intentional. The abstract customer hierarchy is an OCP extension point; Stage 1 contains only RegularCustomer and makes no claim to implement later customer features. See the rebuild guide for contract details and trade-offs.
