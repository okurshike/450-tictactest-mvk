package ch.bbw.m450.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import ch.bbw.m450.tictactoe.players.PerfectPlayer;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import java.util.stream.Stream;

public class TicTacToeTest {

    @ParameterizedTest
    // test daten aus methode "winningboards" holen
    @MethodSource("winningBoards")
    void isWin_returnsTrueForWinningBoards(Stone[] board, Stone player) {
        assertThat(TicTacToeMain.isWin(board, player)).isTrue();
    }

    // das gleiche player wird für cross und circle angegeben
    @Test
    void play_rejectsUsingSamePlayerForCrossAndCircle() {
        var player = greedyPlayer();

        assertThatThrownBy(() -> TicTacToeMain.play(player, player))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    // test daten aus methode "nonwinningboards" holen
    @MethodSource("nonWinningBoards")
    void isWin_returnsFalseForNonWinningBoards(Stone[] board, Stone player) {
        assertThat(TicTacToeMain.isWin(board, player)).isFalse();
    }

    @Test
    // 2 greedyplayer spielen(X0-O1-X2-O3-X4-O5-X6...)
    void play_twoGreedyPlayers_resultsInCrossWinning() {
        var crossPlayer = greedyPlayer();
        var circlePlayer = greedyPlayer();

        var winner = TicTacToeMain.play(crossPlayer, circlePlayer);

        assertThat(winner).isEqualTo(Stone.CROSS);
    }

    // jede der 8 Linien Stein für Stein aufbauen: erst der dritte Stein gewinnt (deckt alle && / || Zweige ab)
    @ParameterizedTest
    @MethodSource("lines")
    void isWin_onlyCompleteLineWins(int a, int b, int c) {
        var board = new Stone[TicTacToeMain.BOARD_SIZE];
        board[a] = Stone.CROSS;
        assertThat(TicTacToeMain.isWin(board, Stone.CROSS)).isFalse();
        board[b] = Stone.CROSS;
        assertThat(TicTacToeMain.isWin(board, Stone.CROSS)).isFalse();
        board[c] = Stone.CROSS;
        assertThat(TicTacToeMain.isWin(board, Stone.CROSS)).isTrue();
        assertThat(TicTacToeMain.isWin(board, Stone.CIRCLE)).isFalse();
    }

    // ungültige Züge: ausserhalb des Feldes (zu klein / zu gross) und besetztes Feld
    @ParameterizedTest
    @ValueSource(ints = {-1, 9})
    void play_rejectsMoveOutsideBoard(int position) {
        assertThatThrownBy(() -> TicTacToeMain.play((b, c) -> position, greedyPlayer()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void play_rejectsMoveOnOccupiedField() {
        assertThatThrownBy(() -> TicTacToeMain.play(greedyPlayer(), (b, c) -> 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("0");
    }

    // zwei perfekte Spieler: niemand kann gewinnen
    @Test
    @StdIo
    void play_twoPerfectPlayers_isDraw(StdOut out) {
        assertThat(TicTacToeMain.play(new PerfectPlayer(), new PerfectPlayer())).isNull();
        assertThat(out.capturedString()).contains("it's a draw!");
    }

    @Test
    void toString_showsStonesAndFreeFieldNumbers() {
        var board = new Stone[TicTacToeMain.BOARD_SIZE];
        board[0] = Stone.CROSS;
        board[1] = Stone.CIRCLE;

        var text = TicTacToeMain.toString(board);

        assertThat(text).contains("X", "O", "2", "8").doesNotContain("0  ");
        assertThat(text.indexOf("X")).as("X auf Feld 0, O auf Feld 1").isLessThan(text.indexOf("O"));
    }

    // Pioneer: ganzes Spiel über die Konsole (Mensch X gegen Greedy O), X gewinnt mit 2-4-6
    @Test
    @StdIo({"4", "8", "2", "6"})
    void main_playsCompleteGameFromConsole(StdOut out) {
        TicTacToeMain.main(new String[0]);

        assertThat(out.capturedString()).contains("winner is: CROSS");
    }

    // Eingabe endet mitten im Spiel (Ctrl-D): sauber abbrechen statt Stacktrace
    @Test
    @StdIo({"4"})
    void main_abortsCleanlyWhenInputEnds(StdOut out) {
        TicTacToeMain.main(new String[0]);

        assertThat(out.capturedString()).contains("game aborted");
    }

    // Helpers
    private GreedyPlayer greedyPlayer() {
        return new GreedyPlayer();
    }

    // Fixtures
    private static Stream<Arguments> lines() {
        return Stream.of(
                Arguments.of(0, 1, 2), Arguments.of(3, 4, 5), Arguments.of(6, 7, 8), // Zeilen
                Arguments.of(0, 3, 6), Arguments.of(1, 4, 7), Arguments.of(2, 5, 8), // Spalten
                Arguments.of(0, 4, 8), Arguments.of(2, 4, 6)                         // Diagonalen
        );
    }

    private static Stream<Object[]> winningBoards() {
        return Stream.of(
                new Object[] {
                        new Stone[] {
                                Stone.CROSS, Stone.CROSS, Stone.CROSS,
                                null, Stone.CIRCLE, null,
                                Stone.CIRCLE, null, null
                        },
                        Stone.CROSS
                },

                new Object[] {
                        new Stone[] {
                                Stone.CROSS, null, Stone.CIRCLE,
                                null, Stone.CROSS, null,
                                Stone.CIRCLE, null, Stone.CROSS
                        },
                        Stone.CROSS
                },

                new Object[] {
                        new Stone[] {
                                Stone.CIRCLE, Stone.CROSS, null,
                                Stone.CIRCLE, null, Stone.CROSS,
                                Stone.CIRCLE, null, null
                        },
                        Stone.CIRCLE
                }
        );
    };

    private static Stream<Object[]> nonWinningBoards() {
        return Stream.of(
            new Object[] {
                new Stone[] {
                    Stone.CROSS, Stone.CIRCLE, Stone.CROSS,
                    Stone.CROSS, Stone.CIRCLE, Stone.CIRCLE,
                    Stone.CIRCLE, Stone.CROSS, Stone.CROSS
                },
                Stone.CROSS
            },
            new Object[] {
                new Stone[] {
                    Stone.CROSS, Stone.CIRCLE, Stone.CROSS,
                Stone.CROSS, Stone.CIRCLE, Stone.CIRCLE,
                Stone.CIRCLE, Stone.CROSS, Stone.CROSS
                },
                Stone.CIRCLE
            }
        );
    };
}