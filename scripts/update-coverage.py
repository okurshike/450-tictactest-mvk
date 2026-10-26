"""Add the current JaCoCo line coverage to the CSV used by GitHub Pages."""

import csv
import os
import sys
from datetime import datetime, timezone
from pathlib import Path

from jacoco import line_coverage

if len(sys.argv) != 2:
    raise SystemExit("Usage: update-coverage.py HISTORY_CSV")

report = Path("build/reports/jacoco/test/jacocoTestReport.xml")
history = Path(sys.argv[1])

coverage = line_coverage(report)

commit = os.environ.get("GITHUB_SHA", "local")[:7]
date = datetime.now(timezone.utc).strftime("%Y-%m-%d")

rows = []
if history.exists():
    with history.open(newline="", encoding="utf-8") as history_file:
        rows = list(csv.reader(history_file))

# GitHub lets us re-run a workflow. Replace an existing entry for the same
# commit instead of showing that commit twice in the chart.
data_rows = [row for row in rows[1:] if len(row) >= 2 and row[1] != commit]
data_rows.append([date, commit, f"{coverage:.2f}"])

with history.open("w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerow(["Datum", "Commit", "Coverage"])
    writer.writerows(data_rows)

print(f"Coverage: {coverage:.2f}%")
