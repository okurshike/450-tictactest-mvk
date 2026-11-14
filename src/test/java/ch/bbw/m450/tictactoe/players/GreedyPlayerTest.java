package ch.bbw.m450.tictactoe.players;

import static ch.bbw.m450.tictactoe.players.PerfectPlayerTest.board;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import org.junit.jupiter.api.Test;

class GreedyPlayerTest {

    @Test
    void play_choosesFirstFreeField() {
        assertThat(new GreedyPlayer().play(board("XO_X_____"), Stone.CIRCLE)).isEqualTo(2);
    }

    @Test
    void play_throwsOnFullBoard() {
        assertThatThrownBy(() -> new GreedyPlayer().play(board("XOXXOOOXX"), Stone.CROSS))
                .isInstanceOf(IllegalStateException.class);
    }
}
