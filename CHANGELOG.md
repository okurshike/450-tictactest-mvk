# Changelog

Alle relevanten Änderungen pro Version. Format: [Keep a Changelog](https://keepachangelog.com/de/1.1.0/),
Versionen nach [Semantic Versioning](https://semver.org/lang/de/).

## [Unreleased]

## [1.6.0] - 2026-11-24

### Changed
- Backend: Begrüssung beim Spielstart (Nachweis: Nur-Backend-Release).

## [1.5.0] - 2026-11-24

### Changed
- Frontend: grüner Hintergrund statt blau (Nachweis: Nur-Frontend-Release).

## [1.4.0] - 2026-11-23

### Changed
- Frontend: blauer Hintergrund (hell und dunkel), damit Release B sichtbar ist.

## [1.3.0] - 2026-11-18

### Added
- Web-Frontend (`frontend/`): Regeln, Konsole im Browser (ttyd im `<iframe>`), Backend- und Frontend-Version.
- Release enthält `tictactest-frontend-<version>.zip` mit `version.txt`.
- Dokumentation der Webkonsole (`docs/webkonsole.md`): Start, Konfiguration, Versionsabfrage, Speicherbedarf, Sicherheit.

## [1.2.0] - 2026-11-17

### Added
- End-to-End-Tests gegen das gebaute JAR (`./gradlew e2eTest`).
- Release enthält eine SHA-256-Prüfsumme und die Commit-ID und lässt sich manuell neu starten.
- Coverage Gate kommentiert sein Ergebnis im Pull Request.
- PRs, die den Dev Container ändern, bauen und prüfen das Image.

### Changed
- Coverage Gate, Coverage-Verlauf und Release laufen ebenfalls im Dev-Container-Image statt mit `setup-java`.
- Coverage Gate und Coverage-Verlauf messen Branch Coverage statt Line Coverage, wie das Build-Gate.
- Abhängigkeiten aktualisiert: JUnit 6.1.3, jqwik 1.10.2, PIT 1.30.1, AssertJ 3.27.7, JaCoCo 0.8.16.
- Lock-Datei für den OpenTofu-Provider (`infra/.terraform.lock.hcl`) ist versioniert.

### Fixed
- Ungültige Eingabe (Text, leere Zeile, zu grosse Zahl, besetztes Feld) beendet das Spiel nicht mehr, es wird neu gefragt.
- Ende der Eingabe (Ctrl-D) bricht das Spiel mit einer Meldung ab statt mit einem Stacktrace.

## [1.1.0] - 2026-11-14

### Added
- `PerfectPlayer`: Computer-Gegner, der nie verliert (Minimax).
- Tests mit JUnit Pioneer (Konsolen-Ein-/Ausgabe) und jqwik (Property-based Testing).
- Mutation Testing mit PIT und 90-%-Branch-Coverage-Gate im Build.
- Release prüft Version, Changelog und Tests und spielt mit dem JAR ein ganzes Spiel.
- Release öffnet automatisch einen Pull Request, der den Dev Container auf das neue Image umstellt.

### Changed
- CI läuft im Dev-Container-Image statt mit `setup-java`.
- JAR heisst jetzt `tictactoe-<version>.jar` und ist mit `java -jar` startbar.

### Fixed
- `HumanPlayer` verlor Eingaben, weil pro Zug ein neuer `Scanner` erstellt wurde.

## [1.0.0] - 2026-10-26

### Added
- Erste Version: TicTacToe auf der Konsole, Unit Tests, JaCoCo, Coverage Gate, Coverage-Verlauf, Dev-Container-Image.
