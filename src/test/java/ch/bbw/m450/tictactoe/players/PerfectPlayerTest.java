package ch.bbw.m450.tictactoe.players;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.junit.jupiter.api.Test;

class PerfectPlayerTest {

    private final PerfectPlayer perfect = new PerfectPlayer();

    @Test
    void play_takesWinningField() {
        // X X _      X gewinnt sofort mit Feld 2
        // O O _
        // _ _ _
        var board = board("XX_OO____");

        assertThat(perfect.play(board, Stone.CROSS)).isEqualTo(2);
    }

    @Test
    void play_blocksOpponentWin() {
        // O O _      X muss Feld 2 blockieren, sonst gewinnt O
        // X _ _
        // _ _ X
        var board = board("OO_X____X");

        assertThat(perfect.play(board, Stone.CROSS)).isEqualTo(2);
    }

    @Test
    void play_doesNotChangeTheBoard() {
        var board = board("X___O____");
        var copy = board.clone();

        perfect.play(board, Stone.CROSS);

        assertThat(board).containsExactly(copy);
    }

    @Test
    void play_throwsOnFullBoard() {
        assertThatThrownBy(() -> perfect.play(board("XOXXOOOXX"), Stone.CROSS))
                .isInstanceOf(IllegalStateException.class);
    }

    // Kern des Perfect Players: er verliert nie, egal welche Züge der Gegner macht.
    // Geprüft wird jeder mögliche Spielverlauf, einmal als X und einmal als O.
    @Test
    void neverLoses_againstAnyOpponent_asCross() {
        assertNeverLoses(new Stone[TicTacToeMain.BOARD_SIZE], Stone.CROSS, Stone.CROSS);
    }

    @Test
    void neverLoses_againstAnyOpponent_asCircle() {
        assertNeverLoses(new Stone[TicTacToeMain.BOARD_SIZE], Stone.CROSS, Stone.CIRCLE);
    }

    @Test
    void winsAgainstGreedy() {
        assertThat(TicTacToeMain.play(perfect, new GreedyPlayer())).isEqualTo(Stone.CROSS);
        assertThat(TicTacToeMain.play(new GreedyPlayer(), perfect)).isEqualTo(Stone.CIRCLE);
    }

    private void assertNeverLoses(Stone[] board, Stone toMove, Stone perfectColor) {
        if (TicTacToeMain.isWin(board, perfectColor) || isFull(board)) {
            return;
        }
        if (toMove == perfectColor) {
            board[perfect.play(board, toMove)] = toMove;
            assertNeverLoses(board, toMove.opponent(), perfectColor);
            return;
        }
        for (var i = 0; i < TicTacToeMain.BOARD_SIZE; i++) {
            if (board[i] == null) {
                var next = board.clone();
                next[i] = toMove;
                assertThat(TicTacToeMain.isWin(next, toMove)).as("opponent wins on %s", java.util.Arrays.toString(next)).isFalse();
                assertNeverLoses(next, toMove.opponent(), perfectColor);
            }
        }
    }

    private static boolean isFull(Stone[] board) {
        for (var stone : board) {
            if (stone == null) {
                return false;
            }
        }
        return true;
    }

    /** "X", "O" or "_" for each of the 9 fields, row by row. */
    static Stone[] board(String fields) {
        var board = new Stone[TicTacToeMain.BOARD_SIZE];
        for (var i = 0; i < board.length; i++) {
            board[i] = switch (fields.charAt(i)) {
                case 'X' -> Stone.CROSS;
                case 'O' -> Stone.CIRCLE;
                default -> null;
            };
        }
        return board;
    }
}
