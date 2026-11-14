package ch.bbw.m450.tictactoe.players;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

// jqwik (Property-based Testing): statt einzelner Beispiele erzeugt jqwik zufällige Spielstände
// und prüft, dass die Eigenschaft für alle gilt. Bei einem Fehler zeigt es das kleinste Gegenbeispiel.
class PlayerPropertyTest {

    @Property
    void perfectPlayer_alwaysChoosesFreeField(@ForAll("unfinishedBoards") Stone[] board) {
        var move = new PerfectPlayer().play(board, nextColor(board));

        assertThat(move).isBetween(0, TicTacToeMain.BOARD_SIZE - 1);
        assertThat(board[move]).isNull();
    }

    @Property
    void greedyPlayer_alwaysChoosesFirstFreeField(@ForAll("unfinishedBoards") Stone[] board) {
        var move = new GreedyPlayer().play(board, nextColor(board));

        assertThat(board[move]).isNull();
        for (var i = 0; i < move; i++) {
            assertThat(board[i]).isNotNull();
        }
    }

    /** Boards reached by random legal moves, stopped before the game is over. */
    @Provide
    Arbitrary<Stone[]> unfinishedBoards() {
        return Arbitraries.integers().between(0, TicTacToeMain.BOARD_SIZE - 1)
                .list().uniqueElements().ofMaxSize(TicTacToeMain.BOARD_SIZE - 1)
                .map(PlayerPropertyTest::playUntilWin);
    }

    private static Stone[] playUntilWin(List<Integer> moves) {
        var board = new Stone[TicTacToeMain.BOARD_SIZE];
        var color = Stone.CROSS;
        for (var move : moves) {
            board[move] = color;
            if (TicTacToeMain.isWin(board, color)) {
                board[move] = null; // keep the board unfinished
                break;
            }
            color = color.opponent();
        }
        return board;
    }

    private static Stone nextColor(Stone[] board) {
        var stones = 0;
        for (var stone : board) {
            if (stone != null) {
                stones++;
            }
        }
        return stones % 2 == 0 ? Stone.CROSS : Stone.CIRCLE;
    }
}
