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
| JaCoCo | Misst die Line Coverage, erzeugt XML- und HTML-Report |
| GitHub Actions | CI, Coverage-Gate, Coverage-Verlauf, Release |
| GitHub Pages, Chart.js | Veröffentlicht Coverage-Verlauf und JaCoCo-Report |
| Dev Container, GHCR | Einheitliche Entwicklungsumgebung als versioniertes Docker-Image |
| OpenTofu, AWS EC2 | Infrastructure as Code: Server für das spätere Online-Deployment |

## Automatisierung der Tests (Ergänzung zum Testkonzept)

Die Tests aus dem Testkonzept laufen automatisch in diesen Workflows (`.github/workflows/`):

| Workflow | Auslöser | Was passiert |
| --- | --- | --- |
| `ci.yml` | Push auf jeden Branch | `./gradlew build` (inkl. Tests und JaCoCo), Report als Artifact |
| `coverage-gate.yml` | Pull Request auf `main` | Vergleicht die Line Coverage von `main` und dem PR. Tiefer = FAIL |
| `coverage-history.yml` | Push auf `main` | Hängt die Coverage an die CSV im Branch `coverage-data` an und veröffentlicht die Seite |
| `docker-publish.yml` | Tag `v*.*.*` | Release: Dev-Container-Image auf GHCR und GitHub Release |

Erfolgskriterium: Alle Tests grün **und** die Coverage eines PR ist nicht tiefer als die von `main`.

### Branch Protection für `main`

`main` ist geschützt (Settings → Branches → Branch protection rule für `main`):

- Require a pull request before merging (kein direkter Push auf `main`)
- Require status checks to pass: `build` (CI) und `compare` (Coverage Gate)

So kommt nur Code auf `main`, der getestet ist und die Coverage nicht senkt.
Darum speichert der Coverage-Bot seine CSV im Branch `coverage-data` und nicht auf `main`.

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

**Versionierung:** [Semantic Versioning](https://semver.org) `vMAJOR.MINOR.PATCH`

| Teil | Erhöhen bei | Beispiel |
| --- | --- | --- |
| MAJOR | inkompatibler Änderung (z. B. andere Java-Version im Dev Container) | `v2.0.0` |
| MINOR | neuer Funktion, rückwärtskompatibel | `v1.1.0` |
| PATCH | Bugfix | `v1.0.1` |

**Ablauf eines Releases:**

1. Änderung auf einem Feature-Branch, Pull Request auf `main`. CI und Coverage Gate müssen grün sein.
2. Nach dem Merge auf `main` einen Tag setzen und pushen:
   ```
   git checkout main
   git pull
   git tag v1.0.1
   git push origin v1.0.1
   ```
3. Der Tag startet `docker-publish.yml`:
   - baut das Dev-Container-Image und veröffentlicht es als `ghcr.io/okurshike/450-tictactest-mvk:v1.0.1`
   - erstellt ein GitHub Release mit automatisch erzeugten Release Notes
4. Soll der Dev Container die neue Version verwenden, wird `image` in `.devcontainer/devcontainer.json` per Pull Request angepasst.

**Warum feste Versionen statt `latest`?** Jede Person bekommt genau dasselbe Image, und man kann jederzeit auf eine ältere Version zurück.
