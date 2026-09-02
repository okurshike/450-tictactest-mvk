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
        var board = rowWinBoard();

		assertThat(TicTacToeMain.isWin(board, Stone.CROSS)).isTrue();
	}

    @Test
    void isWin_detectsDiagonalWin() {
        var board = diagonalWinBoard();

		assertThat(TicTacToeMain.isWin(board, Stone.CROSS)).isTrue();
	}

    @Test
    void play_rejectsUsingSamePlayerForCrossAndCircle() {
        var player = greedyPlayer();

        assertThatThrownBy(() -> TicTacToeMain.play(player, player))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isWin_returnsFalse_whenBoardHasNoWinner() {
        var board = boardWithoutWinner();

        assertThat(TicTacToeMain.isWin(board, Stone.CROSS)).isFalse();
        assertThat(TicTacToeMain.isWin(board, Stone.CIRCLE)).isFalse();
    }

    @Test
    void play_twoGreedyPlayers_resultsInCrossWinning() {
        var crossPlayer = greedyPlayer();
        var circlePlayer = greedyPlayer();

        var winner = TicTacToeMain.play(crossPlayer, circlePlayer);

        assertThat(winner).isEqualTo(Stone.CROSS);
    }

    
    // Helpers

    private GreedyPlayer greedyPlayer() {
        return new GreedyPlayer();
    }


    // Fixtures

    private Stone[] rowWinBoard() {
        return new Stone[]{
                null, Stone.CIRCLE, null,
                Stone.CROSS, Stone.CROSS, Stone.CROSS,
                Stone.CIRCLE, null, null
        };
    }

    private Stone[] diagonalWinBoard() {
        return new Stone[]{
                Stone.CROSS, null, Stone.CIRCLE,
                null, Stone.CROSS, null,
                Stone.CIRCLE, null, Stone.CROSS
        };
    }

    private Stone[] boardWithoutWinner() {
        return new Stone[]{
                Stone.CROSS, Stone.CIRCLE, Stone.CROSS,
                Stone.CROSS, Stone.CIRCLE, Stone.CIRCLE,
                Stone.CIRCLE, Stone.CROSS, Stone.CROSS
        };
    }
}