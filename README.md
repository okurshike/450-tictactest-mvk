# TicTacToe – Modul 450 / 324

Java-TicTacToe mit automatisierten Tests, Coverage-Messung und CI/CD über GitHub Actions.

- Testkonzept: [docs/Testkonzept.pdf](docs/Testkonzept.pdf)
- Coverage-Verlauf und JaCoCo-Report: https://okurshike.github.io/450-tictactest-mvk/
- Dev-Container-Image: `ghcr.io/okurshike/450-tictactest-mvk`

## Eingesetzte Technologien

| Technologie | Zweck |
| --- | --- |
| Java 21, Gradle | Build; `./gradlew build` kompiliert und testet |
| JUnit 5, AssertJ | Unit Tests (inkl. parametrisierte Tests) und lesbare Assertions |
| JUnit Pioneer | `@StdIo`: Konsolen-Eingabe vorgeben und Ausgabe prüfen (HumanPlayer, ganzes Spiel über `main`) |
| jqwik | Zusätzliches Testframework: Property-based Testing mit zufällig erzeugten Spielständen |
| JaCoCo | Misst Line und Branch Coverage; `./gradlew build` scheitert unter 90 % Branch Coverage |
| PIT (PITest) | Mutation Testing: prüft, ob die Tests Fehler im Code wirklich finden |
| GitHub Actions | CI, Coverage-Gate, Coverage-Verlauf, Release |
| GitHub Pages, Chart.js | Veröffentlicht Coverage-Verlauf und JaCoCo-Report |
| Dev Container, GHCR | Einheitliche Entwicklungsumgebung als versioniertes Docker-Image |
| OpenTofu, AWS EC2 | Infrastructure as Code: Server für das spätere Online-Deployment |

## Automatisierung der Tests (Ergänzung zum Testkonzept)

Die Tests aus dem Testkonzept laufen automatisch in diesen Workflows (`.github/workflows/`):

| Workflow | Auslöser | Was passiert |
| --- | --- | --- |
| `ci.yml` | Push auf jeden Branch, PR auf `main` | Läuft **im Dev-Container-Image**: `./gradlew build pitest` (Tests, JaCoCo, 90-%-Branch-Gate, PIT). Reports als Artifacts |
| `coverage-gate.yml` | Pull Request auf `main` | Vergleicht die Line Coverage von `main` und dem PR. Tiefer = FAIL |
| `coverage-history.yml` | Push auf `main` | Hängt die Coverage an die CSV im Branch `coverage-data` an und veröffentlicht die Seite |
| `docker-publish.yml` | Tag `v*.*.*` | Release (siehe [RELEASE.md](RELEASE.md)): prüfen, testen, JAR, Image auf GHCR, GitHub Release, Bump-PR |

Erfolgskriterium: Alle Tests grün, Branch Coverage ≥ 90 % **und** die Coverage eines PR ist nicht tiefer als die von `main`.
Branch-Schutz für `main`: siehe [RELEASE.md](RELEASE.md#branch-schutz-für-main).
Weil `main` geschützt ist, speichert der Coverage-Bot seine CSV im Branch `coverage-data`.

### Testarten und Begriffe

| Begriff | Bedeutung hier | Wo |
| --- | --- | --- |
| Line Coverage | Anteil der Codezeilen, die beim Testen mindestens einmal ausgeführt werden | JaCoCo-Report |
| Branch Coverage | Anteil der Verzweigungen (`if`, `&&`, `\|\|`, `?:`), bei denen **beide** Richtungen getestet sind. Strenger als Line Coverage, weil eine Zeile mit `a && b` ausgeführt sein kann, ohne dass `b` je falsch war | Gate in `build.gradle`, aktuell 100 % |
| Mutation Testing | PIT baut kleine Fehler (Mutanten) in den Code ein, z. B. `<` zu `<=` oder `==` zu `!=`. Ein guter Test schlägt dann fehl (Mutant „killed“). Überlebt ein Mutant, fehlt ein Test | `./gradlew pitest`, aktuell 72 von 74 Mutanten getötet (97 %) |
| Property-based Testing | Statt fester Beispiele erzeugt jqwik viele zufällige Spielstände und prüft eine Regel, die immer gelten muss, z. B. „der Spieler wählt immer ein freies Feld“ | `PlayerPropertyTest` |
| Perfect Player | Minimax: probiert jeden Zug und rechnet das Spiel bis zum Ende durch. Der Test prüft **alle** möglichen Spielverläufe und zeigt, dass er nie verliert | `PerfectPlayer`, `PerfectPlayerTest` |

Die zwei überlebenden Mutanten ändern kein prüfbares Verhalten: `>` zu `>=` im `PerfectPlayer` wählt bei
gleich guten Zügen nur einen anderen, ebenso guten Zug. Die entfernte Ausgabe des Bretts vor einem ungültigen Zug
ändert nichts am Spielablauf, sie ist nur Diagnose.

## Infrastruktur (OpenTofu / AWS)

`infra/main.tf` beschreibt eine EC2-Instanz (`t2.micro`), die per SSH erreichbar ist.

**Rolle von Provider und Ressource:**

- Der **Provider** (`aws`) ist das Plugin, das OpenTofu mit der AWS-API verbindet. Er legt fest, welche Plattform und welche Region verwendet wird, und führt die Änderungen dort aus.
- Eine **Ressource** ist ein Objekt, das OpenTofu anlegt und verwaltet. Hier sind es `aws_key_pair` (öffentlicher SSH-Schlüssel), `aws_security_group` (Firewall, Port 22) und `aws_instance` (der Server).
- **Variablen** machen Region, Instanztyp, Schlüsselpfad und SSH-Bereich konfigurierbar. **Outputs** geben nach dem Start die öffentliche IP, den DNS-Namen und den SSH-Befehl aus.

**Ablauf** (PowerShell, AWS-Zugangsdaten müssen konfiguriert sein):

```
ssh-keygen -t ed25519 -f "$HOME\.ssh\tictactest"
cd infra
tofu init
$ip = (Invoke-RestMethod https://checkip.amazonaws.com).Trim()
tofu apply -var "ssh_cidr=$ip/32"
ssh -i "$HOME\.ssh\tictactest" ec2-user@<public_ip>
tofu destroy -var "ssh_cidr=$ip/32"
```

Der private Schlüssel liegt nur in `~/.ssh` und nie im Repository. Auch `*.tfstate` und `*.tfvars` sind über `.gitignore` ausgeschlossen, weil sie sensible Werte enthalten können.

**Vorbereitung für das Deployment als Online-Container:** Der Server ist im Repository versioniert und lässt sich jederzeit gleich neu aufbauen. Für das Deployment kommen später dazu:

1. Ein Image der Anwendung auf GHCR, analog zum Dev-Container-Image in `docker-publish.yml`.
2. Docker auf der Instanz, zum Beispiel über `user_data` in `aws_instance`.
3. Eine weitere `ingress`-Regel in der Security Group für den Port der Anwendung.
4. Ein Deployment-Schritt, der sich per SSH verbindet und den Container startet.

## Release- und Versionierungskonzept

Semantic Versioning, Release per Tag `vX.Y.Z`, Bump-PR für das Dev-Container-Image und Branch-Schutz:
siehe [RELEASE.md](RELEASE.md). Änderungen pro Version: [CHANGELOG.md](CHANGELOG.md).

**Warum feste Versionen statt `latest`?** Jede Person und die CI bekommen genau dasselbe Image, und man kann
jederzeit auf eine ältere Version zurück.
