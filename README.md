# Restaurant Rush

A Java command-line restaurant management game for the SC2002 group assignment. The current implementation is Stage 1, rewritten as a commented learning example using basic OO and Chapter 9 design principles.

Start with:

- [Stage 1 build/run instructions, rules and reproducible runs](stage_1.md)
- [Chapter 9 design explanations and step-by-step rebuild exercises](docs/chapter9-rebuild-guide.md)
- [Stage 1 UML class diagram](diagrams/stage1-class-diagram.md)
- [Stage 1 UML sequence diagram](diagrams/stage1-sequence-diagram.md)
- [Original assignment brief](SC2002_Restaurant_Rush_Group_Assignment.docx)
- [AI workflow requirements](workflow.md)

The code uses private state, guarded domain operations, customer/staff inheritance, composition, immutable display snapshots and two purposeful interfaces. Comments explain the decisions and their limits rather than claiming that every class must use every SOLID principle.

Java 17+ and Python 3 are sufficient to run all verification:

```sh
python3 tests/stage1_smoke.py
```

GitHub renders the Mermaid diagrams directly. In VS Code, use a Mermaid-capable Markdown preview. SVG exports of the current diagrams are available: [class diagram](diagrams/stage1-class-diagram.svg) and [sequence diagram](diagrams/stage1-sequence-diagram.svg). The Markdown diagrams remain the editable source; regenerate the SVGs after changing them.

Stage 2 (VIP and Critic) and Stage 3 (Host, ComboMeal, Happy Hour) are not implemented. This rewrite does not create commits, Git tags, a report or a video. The final assignment requires those stage milestones and submission evidence; see the original brief. Understand and adapt the learning implementation, and record GAI assistance using the required course declarations.
