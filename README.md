# Restaurant Rush

Restaurant Rush is a Java command-line restaurant management game for the SC2002 group assignment. The manager allocates limited Waiter and Chef actions while customers move through the restaurant's service flow.

## Project documentation

- [Stage 1 guide](stage_1.md) - implementation scope, build/run instructions, rules, and reproducible victory and defeat runs.
- [Stage 1 UML class diagram](diagrams/stage1-class-diagram.md)
- [Stage 1 UML sequence diagram](diagrams/stage1-sequence-diagram.md)
- [Stage 2 UML class diagram](diagrams/stage2-class-diagram.md)
- [Stage 2 UML sequence diagram](diagrams/stage2-sequence-diagram.md)
- [Assignment brief](lab5_doc.md)
- [AI workflow requirements](workflow.md)

## Current stage

The current source includes Stage 2 VIP and Critic customers. See the [Stage 2 guide](stage_2.md) for rules, build instructions, and reproducible runs. Stage 3 (Host, ComboMeal, and Happy Hour) has not been implemented yet.

## Preview the Mermaid diagrams

In VS Code, install **Mermaid Preview** by **Mermaid OSS**:

1. Open Extensions with `Ctrl+Shift+X`.
2. Search for **Mermaid Preview** by **Mermaid OSS** and install it.
3. Open either diagram Markdown file and run **Markdown: Open Preview** from the Command Palette (`Ctrl+Shift+P`).

GitHub also renders Mermaid diagrams in Markdown previews.

## Assignment submission

The assignment submission includes source code, a README, a PDF report, a recorded video, and signed declarations. The brief sets the group submission deadline as November 15, 2026 at 11:59 pm. See [the assignment brief](lab5_doc.md) for the full requirements.

## Stage 1 to Stage 2 changes

New files: VIPCustomer.java and CriticCustomer.java supply customer overrides; tests/CustomerBehaviourTest.java and tests/stage2_smoke.py check domain behaviour and full runs; stage_2.md documents the stage.

Changed files: Customer.java exposes behaviour hooks with Regular defaults; Order.java stores the locked bill; RestaurantGame.java registers mixed arrivals; RestaurantRush.java displays types and bills. The turn algorithm and staff classes are unchanged. Stage tags and a commit comparison link remain to be added when the runnable versions are committed.
