"""Add the current JaCoCo line coverage to the CSV used by GitHub Pages."""

import csv
import os
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path

report = Path("build/reports/jacoco/test/jacocoTestReport.xml")
history = Path("coverage-page/coverage-history.csv")

# JaCoCo stores several counters in the XML report. The graph uses line
# coverage, so we only read the counter whose type is LINE.
root = ET.parse(report).getroot()

counter = next(
    c for c in root.findall("counter")
    if c.attrib["type"] == "LINE"
)

covered = int(counter.attrib["covered"])
missed = int(counter.attrib["missed"])

total = covered + missed
if total == 0:
    raise RuntimeError("JaCoCo report contains no executable lines")

coverage = covered / total * 100

commit = os.environ.get("GITHUB_SHA", "local")[:7]
date = datetime.now(timezone.utc).strftime("%Y-%m-%d")

# The file may already exist but still be empty, as it is in a fresh clone.
# In that case it does not contain a useful header yet.
history.parent.mkdir(parents=True, exist_ok=True)
has_content = history.exists() and history.stat().st_size > 0

rows = []
if has_content:
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
