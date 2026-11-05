# UML diagrams

## Class diagrams - agreed three-stage design

| Stage | SVG | PlantUML source |
| --- | --- | --- |
| 1 - basic service | [View](classes/stage1.svg) | [Edit](classes/stage1.puml) |
| 2 - VIP and Critic | [View](classes/stage2.svg) | [Edit](classes/stage2.puml) |
| 3 - Host, Seating, combos and Happy Hour | [View](classes/stage3.svg) | [Edit](classes/stage3.puml) |

These are **proposed designs**, including the agreed constructor-based MenuItem. They do not assert that Stage 2/3 or the menu refactor have been implemented. [Design notes and assumptions](classes/README.md).

## Sequence diagrams - implemented Stage 1

| Interaction | SVG | PlantUML source |
| --- | --- | --- |
| Scenario map and result | [View](sequence/overview.svg) | [Edit](sequence/overview.puml) |
| Arrival and order | [View](sequence/01-order.svg) | [Edit](sequence/01-order.puml) |
| Preparation and service | [View](sequence/02-service.svg) | [Edit](sequence/02-service.puml) |
| Eating and payment | [View](sequence/03-payment.svg) | [Edit](sequence/03-payment.puml) |

These retain the current code's method signatures and support the existing Stage 1 report. They are not yet sequence diagrams for the proposed three-stage implementation. [Scenario explanation](sequence/README.md).

## Folder layout

```text
diagrams/
  README.md
  render_plantuml.py
  classes/              Stage 1, 2, 3 class sources and SVGs
  sequence/             Stage 1 scenario sources and SVGs
  shared/               Shared style and class definitions
```

The superseded standalone class exports have been removed. Their PlantUML source snapshot is retained with the report at `docs/report/diagram-sources/`; the completed PDF is unchanged.

## Regenerate

From the repository root, with Java 17+, Python 3 and an official PlantUML JAR:

```sh
python3 diagrams/render_plantuml.py --jar /path/to/plantuml.jar
```

This regenerates all seven current exports beside their sources. To render only one group:

```sh
python3 diagrams/render_plantuml.py --jar /path/to/plantuml.jar --source-dir diagrams/classes
```

All rendering runs locally. Class layouts use Smetana, so Graphviz is unnecessary. The validated PlantUML version is 1.2026.9.

## Course notation

Chapter 6 pp. 18-25, 30, 40 and 42-45 guide member notation, visibility, multiplicities, ownership, inheritance, realisation and responsibility stereotypes. Chapter 7 pp. 5-8, 10-13 and 20-23 guide lifelines, activations, creation, replies and guarded/ref fragments. Chapters 8-9 guide responsibility and dependency decisions. Detailed assumptions are beside each diagram group.
