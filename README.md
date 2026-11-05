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

## Compile the skeleton

From the repository root with a JDK installed:

```sh
mkdir -p out
javac -d out $(find src/main/java -name '*.java')
```

Compiling does not make the game playable. Implement and verify Stage 1 first, then add Stage 2 and Stage 3 behavior. Each stage must be a separately runnable Git commit with its required tag; the current skeleton is not a completed Stage 1.
