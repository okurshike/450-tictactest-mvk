# Release-Prozess

Wie aus einem Stand von `main` eine veröffentlichte Version wird.

```text
Feature-Branch → Pull Request auf main (CI + Coverage Gate grün) → Merge
  → Version in build.gradle + Abschnitt in CHANGELOG.md (ebenfalls per PR)
  → Tag vX.Y.Z auf main pushen
  → docker-publish.yml: Bedingungen prüfen → Tests + Branch-Gate → JAR → Smoke-Test
                        → Dev-Container-Image auf GHCR → GitHub Release
  → Bump-PR: Dev Container und CI auf das neue Image umstellen → nach grüner CI mergen
```

## Versionierung

[Semantic Versioning](https://semver.org/lang/de/) `MAJOR.MINOR.PATCH`, Tag mit `v` davor (`v1.1.0`).

| Stelle | Wann | Beispiel |
| --- | --- | --- |
| MAJOR | inkompatible Änderung | neue Java-Version im Dev Container, geändertes `TicTacToePlayer`-Interface |
| MINOR | neue Funktion, Bestehendes läuft weiter | neuer Gegner (`PerfectPlayer`) |
| PATCH | nur Fehlerbehebung | `HumanPlayer` verliert keine Eingaben mehr |

Die Version steht an **einer** Stelle: `version` in `build.gradle`. Der Workflow bricht ab, wenn der Tag nicht
`v` + diese Version ist. So passen Tag, JAR-Name (`tictactoe-X.Y.Z.jar`) und Image-Tag immer zusammen.

## Release erstellen

1. Auf einem Branch `version` in `build.gradle` erhöhen und die Einträge aus `## [Unreleased]` in
   `CHANGELOG.md` unter `## [X.Y.Z] - JJJJ-MM-TT` verschieben. PR auf `main`, nach grüner CI mergen.
2. Tag auf `main` setzen:
   ```
   git switch main
   git pull
   git tag -a v1.1.0 -m "Release v1.1.0"
   git push origin v1.1.0
   ```
3. Unter **Actions → Release** den Workflow beobachten. Danach liegt unter **Releases** das JAR,
   unter **Packages** das Image `ghcr.io/okurshike/450-tictactest-mvk:v1.1.0`.
4. Der Workflow öffnet den PR `chore(devcontainer): use image v1.1.0`. CI läuft darin schon **im neuen
   Image**. Ist sie grün, mergen. Erst ab diesem Merge nutzen Dev Container und CI die neue Version.

## Was der Workflow prüft (`docker-publish.yml`)

| Prüfung | Bei Fehler |
| --- | --- |
| Tag = `v` + `version` aus `build.gradle` | Abbruch, nichts veröffentlicht |
| Getaggter Commit liegt auf `main` | Abbruch |
| `CHANGELOG.md` hat einen Abschnitt für die Version | Abbruch |
| `./gradlew build` im Dev-Container-Image: Unit- und E2E-Tests grün, Branch Coverage ≥ 90 % | Abbruch |
| Smoke-Test: das JAR spielt ein ganzes Spiel bis „winner is: CROSS“ | Abbruch |
| Image startet (`java -version`) | kein Release, kein Bump-PR |

Schlägt etwas fehl, gibt es keinen Release. Den Fehler per PR beheben, dann den Tag löschen und neu setzen
(`git push origin :refs/tags/v1.1.0`). Ein Tag mit **veröffentlichtem** Release wird nie verschoben, dann gibt es
eine neue PATCH-Version.

War der Fehler nicht im Code (z. B. Ausfall von GitHub), den Workflow neu starten: **Actions → Release → Run
workflow**, unter *Use workflow from* den **Tag** wählen. Auf einem Branch gestartet, tut der Workflow nichts.

Der Release-Text enthält die Commit-ID, den CHANGELOG-Abschnitt und den Startbefehl. Angehängt sind das JAR und
`tictactoe-X.Y.Z.jar.sha256`.

## Veröffentlichten Release testen

Erst wenn das veröffentlichte JAR selbst läuft, ist der Release erfolgreich:

```bash
mkdir release-test && cd release-test
curl -LO https://github.com/okurshike/450-tictactest-mvk/releases/download/v1.1.0/tictactoe-1.1.0.jar
curl -LO https://github.com/okurshike/450-tictactest-mvk/releases/download/v1.1.0/tictactoe-1.1.0.jar.sha256
sha256sum -c tictactoe-1.1.0.jar.sha256      # muss "OK" ausgeben
java -jar tictactoe-1.1.0.jar                # Spiel muss starten und bis zum Ende spielbar sein
```

Unter Windows: `Get-FileHash tictactoe-1.1.0.jar` und den Hash mit dem Inhalt der `.sha256`-Datei vergleichen.

## Dev-Container-Image und Bump-PR

- Die Image-Version steht nur in `.devcontainer/devcontainer.json`. `ci.yml` liest sie dort aus
  (Job `image`) und startet den Build in diesem Container. Lokale Umgebung und CI sind dadurch gleich.
- Ein PR, der `.devcontainer/` ändert, baut das Image schon im PR und prüft, dass Java startet
  (`devcontainer-check.yml`). Veröffentlicht wird es dort nicht, damit es in der Registry keine ungeprüften Images gibt.
- Neue Images entstehen nur über einen Release-Tag, nie mit `latest`. Ein altes Image bleibt verfügbar,
  ein Rollback ist ein PR, der die Version in `devcontainer.json` zurücksetzt.
- **Einmalig einrichten:** Ein Fine-grained Personal Access Token (nur dieses Repo, Rechte *Contents* und
  *Pull requests*: Read and write) als Repository-Secret `BUMP_PR_TOKEN` speichern
  (Settings → Secrets and variables → Actions). Ein PR mit dem normalen `GITHUB_TOKEN` würde keine CI starten
  und liesse sich wegen des Branch-Schutzes nicht mergen.

## Branch-Schutz für `main`

Settings → Rules → Rulesets → **New branch ruleset**, Ziel `main` (oder Settings → Branches → Add rule):

- **Require a pull request before merging**: kein direkter Push auf `main`
- **Require status checks to pass**: `build` (CI) und `compare` (Coverage Gate)
- **Block force pushes** und **Restrict deletions**

Damit gelangen Änderungen, auch der Bump-PR, nur mit grüner CI auf `main`. Ein Release kann deshalb nur aus
getestetem Code entstehen.
