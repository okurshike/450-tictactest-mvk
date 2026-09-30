"""Compare line coverage from main with the branch or pull request."""

import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def line_coverage(report: Path) -> float:
    """Read the project-level LINE counter from a JaCoCo XML report."""
    root = ET.parse(report).getroot()
    counter = next(
        item for item in root.findall("counter") if item.attrib["type"] == "LINE"
    )
    covered = int(counter.attrib["covered"])
    missed = int(counter.attrib["missed"])
    total = covered + missed
    if total == 0:
        raise RuntimeError(f"{report} contains no executable lines")
    return covered / total * 100


if len(sys.argv) != 3:
    raise SystemExit("Usage: compare-coverage.py MAIN_REPORT BRANCH_REPORT")

main_coverage = line_coverage(Path(sys.argv[1]))
branch_coverage = line_coverage(Path(sys.argv[2]))
difference = branch_coverage - main_coverage
# Equal coverage is allowed: the assignment only rejects a branch when its
# coverage is lower than main.
passed = difference >= 0
result = "PASS" if passed else "FAIL"

lines = [
    "## Test Coverage Gate",
    "",
    "| Version | Line Coverage |",
    "| --- | ---: |",
    f"| main | {main_coverage:.2f} % |",
    f"| Branch | {branch_coverage:.2f} % |",
    "",
    f"**Differenz:** {difference:+.2f} Prozentpunkte  ",
    f"**{result}** - Coverage Gate {'bestanden' if passed else 'nicht bestanden'}",
]

message = "\n".join(lines)
print(message)

# GitHub displays this file as a formatted summary on the workflow run page.
# It is not present during a normal local run, so writing it is optional.
summary = os.environ.get("GITHUB_STEP_SUMMARY")
if summary:
    with Path(summary).open("a", encoding="utf-8") as summary_file:
        summary_file.write(message + "\n")

if not passed:
    # A non-zero exit code is what turns the GitHub Actions check red.
    raise SystemExit(1)
