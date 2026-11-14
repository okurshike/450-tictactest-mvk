package ch.bbw.m450.tictactoe.players;

import static org.assertj.core.api.Assertions.assertThat;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

// Pioneer @StdIo ersetzt System.in durch die angegebenen Zeilen und fängt System.out ab
class HumanPlayerTest {

    @Test
    @StdIo({"4", "7"})
    void play_readsOneMovePerLine_andAsksForEachMove(StdOut out) {
        var player = new HumanPlayer();
        var board = new Stone[TicTacToeMain.BOARD_SIZE];

        assertThat(player.play(board, Stone.CROSS)).isEqualTo(4);
        assertThat(player.play(board, Stone.CIRCLE)).isEqualTo(7);

        assertThat(out.capturedString())
                .contains("next CROSS? (0-8)")
                .contains("next CIRCLE? (0-8)");
    }
}
