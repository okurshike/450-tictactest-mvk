# Changelog

Alle relevanten Änderungen pro Version. Format: [Keep a Changelog](https://keepachangelog.com/de/1.1.0/),
Versionen nach [Semantic Versioning](https://semver.org/lang/de/).

## [Unreleased]

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
