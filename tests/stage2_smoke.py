"""Run Stage 2 CLI regressions with Java 17+ and Python 3, without dependencies."""

from pathlib import Path
import subprocess
import tempfile


ROOT = Path(__file__).resolve().parents[1]


def run_game(classes, choices, expected):
    result = subprocess.run(
        ["java", "-cp", classes, "restaurant.RestaurantRush"],
        input="\n".join(map(str, choices)) + "\n",
        capture_output=True,
        text=True,
        timeout=10,
        check=True,
    )
    assert result.stdout.count(" / 18") == 18, result.stdout
    for text in expected:
        assert text in result.stdout, result.stdout


def main():
    import shutil
    if not shutil.which('javac') or not shutil.which('java'):
        raise SystemExit('Java 17+ is required: add java and javac to PATH.')
    with tempfile.TemporaryDirectory(prefix="restaurant-stage2-") as classes:
        subprocess.run(
            ["javac", "-Xlint:all", "-d", classes,
             *map(str, sorted((ROOT / "src/main/java/restaurant").glob("*.java"))), str(ROOT / "tests/CustomerBehaviourTest.java")],
            check=True,
        )
        subprocess.run(['java', '-ea', '-cp', classes, 'restaurant.CustomerBehaviourTest'], check=True, timeout=10)
        waiter = {
            1: [1, 1, 1], 2: [2, 1], 3: [3, 1], 4: [1, 1, 2],
            5: [2, 2], 6: [3, 2], 7: [1, 1, 1], 8: [2, 3],
            10: [3, 3], 11: [1, 1, 2], 12: [2, 4], 13: [3, 4],
        }
        chef = {2: [1, 1], 5: [1, 2], 8: [1, 3], 9: [1, 3], 12: [1, 4]}
        victory = "FINAL RESULT: VICTORY"

        def plan():
            return [choice for turn in range(1, 19)
                    for choice in waiter.get(turn, [0]) + chef.get(turn, [0])]

        run_game(classes, plan(), [victory, "Paid customers        4", "Revenue               $49.50",
                                    "Avg paid satisfaction 78.5", "Customer #3 chose Pasta.", "VIP bill: $13.50", "Critic bill: $18.00", "(VIP)", "(Critic)"])
        run_game(classes, [0] * 36, [
            "FINAL RESULT: DEFEAT", "Paid customers        0",
            "Revenue               $0.00", "Avg paid satisfaction N/A",
            "End of turn 18\n  Served: 0  |  Unhappy departures: 2",
        ])

        # Try unavailable Order/Serve/Prepare tasks, then continue normally.
        # Cancel both customer selectors after an invalid ID and retain each task.
        waiter[1] = ["bad", 99, 2, 3, 1, 1, 1]
        chef[1] = [1, 0]
        waiter[2] = [2, 999, 0, 2, 1]
        chef[2] = [1, 999, 0, 1, 1]
        run_game(classes, plan(), [
            victory,
            "No customers are eligible for this task.",
            "Enter 0 to return to the task menu.",
            "No customer with that ID.",
            "Enter a whole number.",
        ])
    print("PASS: Stage 2 victory, defeat, unavailable tasks, cancellation, and invalid input.")


if __name__ == "__main__":
    main()
