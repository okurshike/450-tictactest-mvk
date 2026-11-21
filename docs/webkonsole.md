# Webkonsole

TicTacToe ist ein Konsolenprogramm. Damit man es im Browser spielen kann, ohne den Code zu ändern, läuft das
JAR hinter **ttyd**. Eine statische Webseite (`frontend/`) zeigt die Regeln, bettet die Konsole ein und nennt
die Versionen von Backend und Frontend.

```text
Browser ── http ──> Frontend (index.html, style.css, app.js, config.js, version.txt)
   │
   └── iframe ──> ttyd :7681 ── WebSocket ──> pro Verbindung ein Prozess: java -jar tictactoe-X.Y.Z.jar
```

## Tool und Gründe

**ttyd** stellt ein beliebiges Konsolenprogramm über HTTP und WebSocket im Browser dar (Terminal mit xterm.js).

| Grund | Erklärung |
| --- | --- |
| Keine Änderung am Spiel | Das Spiel liest weiter von `System.in` und schreibt auf `System.out`. Es ist dasselbe JAR wie im Release, und alle Tests gelten weiter. |
| Klein und einfach | Ein einzelnes Programm, im Dev Container mit `apk add ttyd` installiert. Keine Laufzeit wie Node.js nötig. |
| Ein Prozess pro Verbindung | Jeder Browser-Tab bekommt ein eigenes, unabhängiges Spiel. |
| Fester Befehl | ttyd startet nur den Befehl, der beim Start angegeben ist (siehe Sicherheit). |

Alternativen wären eine eigene Web-Oberfläche (Spiel müsste umgeschrieben werden), GoTTY (wird nicht mehr
gepflegt) oder WeTTY (braucht Node.js und ist für SSH-Logins gedacht).

## Lokal starten

Im Dev Container, mit den Dateien aus einem Release (JAR und `tictactest-frontend-X.Y.Z.zip`):

```sh
apk add ttyd python3

# Terminal 1: Backend (Webkonsole)
ttyd --writable -p 7681 java -jar tictactoe-1.3.0.jar

# Terminal 2: Frontend
mkdir frontend && cd frontend
unzip ../tictactest-frontend-1.3.0.zip
python3 -m http.server 8000
```

Dann <http://localhost:8000> öffnen. Die Konsole allein läuft auch direkt auf <http://localhost:7681>.
`--writable` ist nötig, sonst kann man im Browser nichts eintippen.

## Konfiguration

Alles, was je nach Umgebung anders ist, steht in `frontend/config.js`:

| Wert | Bedeutung | Lokal |
| --- | --- | --- |
| `consoleUrl` | Adresse der ttyd-Konsole für das `<iframe>` | `http://localhost:7681` |
| `versionUrl` | Adresse, die die Backend-Version als Text liefert | `http://localhost:7681/version` |

Für den Server wird nur diese Datei angepasst. `index.html`, `app.js` und `style.css` bleiben gleich.

## Versionsabfrage

| Version | Quelle | Wer schreibt sie |
| --- | --- | --- |
| Frontend | `version.txt` neben `index.html` (gleicher Origin) | Release-Workflow (`docker-publish.yml`), bevor er das ZIP packt |
| Backend | `GET versionUrl` (anderer Origin) | später nginx auf dem Server |

`app.js` lädt beide mit `fetch` und zeigt sie unten auf der Seite. Klappt das nicht, steht dort „unbekannt“, und
der Grund erscheint in der Browser-Konsole. Die Texte werden mit `textContent` eingesetzt, nicht mit
`innerHTML`, damit eine manipulierte Antwort kein HTML oder JavaScript einschleusen kann.

### Lokaler Test mit zwei Servern

Frontend auf `localhost:8000`, ttyd auf `localhost:7681`. Ein anderer Port ist ein anderer **Origin**.
Ergebnis in der Browser-Konsole (Firefox):

```text
Version von version.txt nicht lesbar: Error: HTTP 404
Cross-Origin Request Blocked: The Same Origin Policy disallows reading the remote resource at
http://localhost:7681/version. (Reason: CORS header 'Access-Control-Allow-Origin' missing). Status code: 404.
Version von http://localhost:7681/version nicht lesbar: TypeError: NetworkError when attempting to fetch resource.
```

- `version.txt` fehlt lokal, weil sie erst im Release entsteht. Aus dem Release-ZIP ist sie vorhanden.
- Die Backend-Version wird **blockiert**: Der Browser verlangt vom anderen Origin den Header
  `Access-Control-Allow-Origin`, und ttyd sendet ihn nicht. Zusätzlich kennt ttyd keinen Pfad `/version` (404).
- Lösung auf dem Server: nginx liefert `/version` aus und setzt den Header, nur für die Adresse des Frontends
  (nicht `*`). Das gehört zur Infrastruktur, nicht in den Release.

Server-Logs dazu: `http.server` meldet 200 für `/`, `app.js`, `style.css`, `config.js` und 404 für
`version.txt`. ttyd meldet pro Tab `WS /ws - 127.0.0.1, clients: 1`.

## Verhalten (getestet)

| Fall | Beobachtung |
| --- | --- |
| Spiel im Browser | Läuft wie in der Konsole, Eingabe der Feldnummer + Enter. |
| Spielende | Der Java-Prozess beendet sich. ttyd bietet „Reconnect“ an, das startet ein **neues** Spiel. |
| Ungültige Eingabe (`abc`, leer, `99`, besetztes Feld) | Das Spiel fragt erneut („Choose a free field from 0 to 8.“) statt abzustürzen. |
| Zwei Tabs | Zwei Verbindungen = **zwei JVMs**, zwei unabhängige Spiele. Neu laden = neue Verbindung = neues Spiel. |

## Speicher: wie viele Spiele passen in 1 GB?

Jeder Tab ist eine eigene JVM. Gemessen mit zwei wartenden Spielen (`tictactoe-1.2.0.jar`, Temurin 21).
Gemessen wurde auf dem Entwicklungsrechner (Windows, 30 GB RAM), nicht im Container. Unter Linux mit
`ps -o pid,rss,args` gemessen, sind ähnliche Werte zu erwarten.

| Start | belegt (Working Set ≈ RSS) | reserviert (committed) |
| --- | --- | --- |
| `java -jar …` (Standard) | 42,6 MB | 592 MB |
| `java -Xmx32m -XX:+UseSerialGC -jar …` | 41,7 MB | 63 MB |

**Warum der Unterschied wichtig ist:** Ohne Vorgabe wählt die JVM ihren Heap nach der Grösse des Rechners
(Start 1/64, Maximum 1/4 des RAM). Auf einem 1-GB-Server darf **jede** JVM also bis zu 256 MB Heap nehmen.
Belegt ist am Anfang wenig, aber der Speicher kann wachsen, bis der Server ausgeht.

**Rechnung für 1 GB:** Etwa 250 MB bleiben für Betriebssystem, nginx und ttyd, also rund 750 MB für Spiele.

- im Ruhezustand: 750 MB / ~42 MB ≈ **17 Spiele**
- sicher mit Obergrenze (`-Xmx32m`, ~63 MB reserviert): 750 MB / ~63 MB ≈ **11 Spiele**
- ohne Obergrenze im schlechtesten Fall (256 MB Heap): nur **2–3 Spiele**

Empfehlung für den Server: Heap begrenzen und die Zahl der Verbindungen bei ttyd deckeln:

```sh
ttyd --writable -i 127.0.0.1 -p 7681 --max-clients 10 java -Xmx32m -XX:+UseSerialGC -jar tictactoe-X.Y.Z.jar
```

## Sicherheit

- **Der Befehl ist immer das JAR, nie eine Shell.** ttyd gibt jedem, der die URL kennt, genau das Programm,
  das beim Start angegeben ist. Mit `ttyd bash` könnte jeder Besucher Befehle auf dem Server ausführen
  (Dateien lesen, Schlüssel stehlen, den Server übernehmen). Mit `java -jar …` kann man nur TicTacToe spielen.
- **ttyd nur auf localhost:** Ohne Option lauscht ttyd auf allen Netzwerk-Interfaces
  (`lws_socket_bind: source ads 0.0.0.0` im Log). Auf dem Server deshalb `-i 127.0.0.1`. Erreichbar ist es nur
  über nginx, das HTTPS, den CORS-Header und Limits übernimmt.
- **Ungültige Eingaben** führen nicht zum Absturz (siehe oben, abgesichert durch Unit- und E2E-Tests).
- **Ressourcen begrenzen:** `--max-clients` und `-Xmx` verhindern, dass viele Tabs den Server lahmlegen.
- **CORS gezielt:** `Access-Control-Allow-Origin` nur für die Adresse des Frontends, nicht `*`.
- Das Frontend setzt fremden Text nur mit `textContent` ein (kein `innerHTML`).

## Was gehört in den Release, was in die Infrastruktur?

| Release (`docker-publish.yml`, Assets auf GitHub) | Infrastruktur (`infra/`, `user_data`) |
| --- | --- |
| `tictactoe-X.Y.Z.jar` + `.sha256` | ttyd installieren, systemd-Unit, die ttyd mit dem JAR startet |
| `tictactest-frontend-X.Y.Z.zip` (inkl. `version.txt`) | nginx-Konfiguration: HTTPS, Weiterleitung an ttyd, `/version`, CORS |

Der Release beschreibt **was** ausgeliefert wird und ist auf jedem Server gleich. systemd und nginx beschreiben
**wie ein bestimmter Server** es betreibt: Pfade, Domain, Ports, Limits. Sie ändern sich mit dem Server, nicht
mit der Version des Spiels, und gehören deshalb in `user_data` der EC2-Instanz.
