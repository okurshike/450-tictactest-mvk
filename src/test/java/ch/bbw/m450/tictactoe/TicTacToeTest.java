package ch.bbw.m450.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import org.junit.jupiter.api.Test;

public class TicTacToeTest {
	@Test
	void dummy() {
		assertThat(1 + 1).isEqualTo(2);
	}

    @Test
	void isWin_detectsRowWin() {
		var board = new Stone[]{
            null, Stone.CIRCLE, null,
			Stone.CROSS, Stone.CROSS, Stone.CROSS,
			Stone.CIRCLE, null, null
		};

		assertThat(TicTacToeMain.isWin(board, Stone.CROSS)).isTrue();
	}

    @Test
	void isWin_detectsDiagonalWin() {
		var board = new Stone[]{
				Stone.CROSS, null, Stone.CIRCLE,
				null, Stone.CROSS, null,
				Stone.CIRCLE, null, Stone.CROSS
		};

		assertThat(TicTacToeMain.isWin(board, Stone.CROSS)).isTrue();
	}

    @Test
    void play_rejectsUsingSamePlayerForCrossAndCircle() {
        var samePlayer = new GreedyPlayer();

        assertThatThrownBy(() -> TicTacToeMain.play(samePlayer, samePlayer))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isWin_returnsFalse_whenBoardHasNoWinner() {
        Stone[] board = {
            Stone.CROSS, Stone.CIRCLE, Stone.CROSS,
            Stone.CROSS, Stone.CIRCLE, Stone.CIRCLE,
            Stone.CIRCLE, Stone.CROSS, Stone.CROSS
        };

        boolean crossWon = TicTacToeMain.isWin(board, Stone.CROSS);
        boolean circleWon = TicTacToeMain.isWin(board, Stone.CIRCLE);

        assertThat(crossWon).isFalse();
        assertThat(circleWon).isFalse();
    }

    @Test
    void play_twoGreedyPlayers_resultsInCrossWinning() {
        var crossPlayer = new GreedyPlayer();
        var circlePlayer = new GreedyPlayer();

        var result = TicTacToeMain.play(crossPlayer, circlePlayer);

        assertThat(result).isEqualTo(Stone.CROSS);
    }
}
