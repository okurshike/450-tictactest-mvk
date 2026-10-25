// Draws the coverage history of main above the JaCoCo report.
// The workflow copies this file and coverage-history.csv next to the
// JaCoCo index.html and inserts the <canvas id="coverageChart"> into it.

// no-store: always load the newest CSV instead of a cached one.
fetch("coverage-history.csv", { cache: "no-store" })
    .then(response => {
        if (!response.ok) {
            throw new Error("CSV not found (HTTP " + response.status + ")");
        }
        return response.text();
    })
    .then(text => {
        // Skip the header and ignore empty lines
        const rows = text.trim().split(/\r?\n/).slice(1).filter(row => row.trim() !== "");

        const labels = [];
        const coverage = [];

        rows.forEach(row => {
            const [date, commit, value] = row.split(",");
            labels.push(date + " (" + commit + ")");
            coverage.push(Number(value));
        });

        new Chart(
            document.getElementById("coverageChart"),
            {
                type: "line",
                data: {
                    labels: labels,
                    datasets: [{
                        label: "Line Coverage %",
                        data: coverage,
                        borderColor: "#2a7ae2",
                        backgroundColor: "#2a7ae2",
                        tension: 0.2
                    }]
                },
                options: {
                    scales: {
                        // Coverage is a percentage, so the axis always shows 0-100
                        y: {
                            min: 0,
                            max: 100,
                            title: { display: true, text: "Coverage (%)" }
                        },
                        x: {
                            title: { display: true, text: "Datum (Commit)" }
                        }
                    }
                }
            }
        );
    })
    .catch(error => {
        document.getElementById("coverageChart").replaceWith("Fehler beim Laden: " + error.message);
    });
