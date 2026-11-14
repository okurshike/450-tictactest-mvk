package ch.bbw.m450.tictactoe.players;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;

/**
 * Player that never loses: tries every move and looks ahead until the game ends (minimax).
 */
public class PerfectPlayer implements TicTacToePlayer {

	@Override
	public int play(Stone[] board, Stone colorToPlay) {
		var bestMove = -1;
		var bestScore = Integer.MIN_VALUE;
		for (var i = 0; i < TicTacToeMain.BOARD_SIZE; i++) {
			if (board[i] == null) {
				board[i] = colorToPlay;
				var score = -score(board, colorToPlay.opponent());
				board[i] = null; // undo, the caller's board stays unchanged
				if (score > bestScore) {
					bestScore = score;
					bestMove = i;
				}
			}
		}
		if (bestMove < 0) {
			throw new IllegalStateException("cannot play at all");
		}
		return bestMove;
	}

	/**
	 * @return the outcome for {@code toMove} if both sides play perfectly: 1 win, 0 draw, -1 loss
	 */
	private static int score(Stone[] board, Stone toMove) {
		if (TicTacToeMain.isWin(board, toMove.opponent())) {
			return -1;
		}
		var best = Integer.MIN_VALUE;
		for (var i = 0; i < TicTacToeMain.BOARD_SIZE; i++) {
			if (board[i] == null) {
				board[i] = toMove;
				best = Math.max(best, -score(board, toMove.opponent()));
				board[i] = null;
			}
		}
		return best == Integer.MIN_VALUE ? 0 : best; // no free field left: draw
	}
}
