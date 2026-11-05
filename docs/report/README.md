# Stage 1 report sources

The delivered report is `output/pdf/Restaurant_Rush_Stage1_Report.pdf`. Its text is also exported to `docs/stage1-report.md` for review. The report describes the current Stage 1 only; page 11 records the remaining final-assignment requirements.

`build_report.py` contains the authored text, ReportLab layout and PDF assembly. It requires ReportLab, pypdf and the macOS Arial/Courier fonts used in this build. These are report-authoring tools, not dependencies of the restaurant game.

The build directory supplied to `--build-dir` must contain `evidence.json` (the structure captured in `verified-runs.json`) and one vector PDF for each historical report diagram basename: the core class diagram, supporting types, scenario map and three sequence panels. The diagram PDFs must each be one page; the script verifies that and verifies the report body has exactly 12 pages before assembly.

The six diagram PDFs were generated locally by printing the existing SVG exports through an isolated headless Chromium session. Core/sequence-detail figures use A2 portrait pages; the supporting-types and scenario-map figures use A4. The final report retains selectable vector text and an outline for all 18 pages.

Verified runs were captured on 5 November 2026 after clean compilation. The JSON includes exact CLI inputs and final outputs for the normal victory, all-Wait defeat and delayed-service experiment. It supplements the existing `tests/stage1_smoke.py` runner and is evidence of these runs, not an automatically updated test result.

The original report diagram sources are retained in `docs/report/diagram-sources/`. Render that snapshot when rebuilding this historical report; the current `diagrams/classes/` set is the later proposed design.
