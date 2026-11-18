const { consoleUrl, versionUrl } = window.TICTACTEST_CONFIG;

document.getElementById("console").src = consoleUrl;

// textContent, not innerHTML: the fetched text is shown as text and can never inject HTML
async function showVersion(elementId, url) {
  const element = document.getElementById(elementId);
  try {
    const response = await fetch(url, { cache: "no-store" });
    if (!response.ok) {
      throw new Error("HTTP " + response.status);
    }
    element.textContent = (await response.text()).trim();
  } catch (error) {
    // e.g. CORS blocked, server down or file missing; the browser console shows the exact reason
    element.textContent = "unbekannt";
    console.warn("Version von " + url + " nicht lesbar:", error);
  }
}

showVersion("backend-version", versionUrl);
// version.txt is written by the release workflow, so it is missing in a local checkout
showVersion("frontend-version", "version.txt");
