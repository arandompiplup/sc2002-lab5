# Restaurant Rush

A Java command-line restaurant management game for the SC2002 group assignment. The current implementation is Stage 1, rewritten as a commented learning example using basic OO and Chapter 9 design principles.

Start with:

- [Stage 1 PDF report](output/pdf/Restaurant_Rush_Stage1_Report.pdf) - design discussion, verified evidence, UML and customer-3 justification.
- [Stage 1 build/run instructions, rules and reproducible runs](stage_1.md)
- [Chapter 9 design explanations and step-by-step rebuild exercises](docs/chapter9-rebuild-guide.md)
- [Proposed Stage 1-3 class diagrams](diagrams/classes/README.md)
- [Implemented Stage 1 sequence diagrams](diagrams/sequence/README.md)
- [Original assignment brief](SC2002_Restaurant_Rush_Group_Assignment.docx)
- [AI workflow requirements](workflow.md)

The code uses private state, guarded domain operations, customer/staff inheritance, composition, immutable display snapshots and two purposeful interfaces. Comments explain the decisions and their limits rather than claiming that every class must use every SOLID principle.

Java 17+ and Python 3 are sufficient to run all verification:

```sh
python3 tests/stage1_smoke.py
```

The diagrams now use PlantUML, with SVG exports and editable `.puml` sources. Start at the [diagram index](diagrams/README.md) for the proposed three-stage classes and implemented Stage 1 service scenario. The index maps the notation to Chapters 6-9 and explains local regeneration.

Stage 2 (VIP and Critic) and Stage 3 (Host, ComboMeal, Happy Hour) are not implemented. The Stage 1 report is included above; commits, Git tags and a video are not created by generating it. The final assignment requires those stage milestones and submission evidence; see the original brief. Understand and adapt the learning implementation, and record GAI assistance using the required course declarations.
