"""Shared helper: read the branch coverage from a JaCoCo XML report."""

import xml.etree.ElementTree as ET
from pathlib import Path


def branch_coverage_percent(report: Path) -> float:
    """Read the project-level BRANCH counter from a JaCoCo XML report."""
    root = ET.parse(report).getroot()
    # JaCoCo stores several counters in the XML report. We use BRANCH, the same counter as the 90 % gate in build.gradle.
    counter = next(
        item for item in root.findall("counter") if item.attrib["type"] == "BRANCH"
    )
    covered = int(counter.attrib["covered"])
    missed = int(counter.attrib["missed"])
    total = covered + missed
    if total == 0:
        raise RuntimeError(f"{report} contains no branches")
    return covered / total * 100
