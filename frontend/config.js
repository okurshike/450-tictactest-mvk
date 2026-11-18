// The only file that changes per environment (local, server). Loaded before app.js.
window.TICTACTEST_CONFIG = {
  // ttyd web console that runs "java -jar tictactoe-<version>.jar"
  consoleUrl: "http://localhost:7681",
  // returns the backend version as plain text; another origin, so the server must send a CORS header
  versionUrl: "http://localhost:7681/version",
};
