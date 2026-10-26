"""Shared helper: read the line coverage from a JaCoCo XML report."""

import xml.etree.ElementTree as ET
from pathlib import Path


def line_coverage(report: Path) -> float:
    """Read the project-level LINE counter from a JaCoCo XML report."""
    root = ET.parse(report).getroot()
    # JaCoCo stores several counters in the XML report. We only use LINE.
    counter = next(
        item for item in root.findall("counter") if item.attrib["type"] == "LINE"
    )
    covered = int(counter.attrib["covered"])
    missed = int(counter.attrib["missed"])
    total = covered + missed
    if total == 0:
        raise RuntimeError(f"{report} contains no executable lines")
    return covered / total * 100
