package ch.bbw.m450.tictactoe.players;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import ch.bbw.m450.tictactoe.TicTacToeMain;
import ch.bbw.m450.tictactoe.TicTacToePlayer;

/**
 * Simple human-player taking input from stdin.
 */
public class HumanPlayer implements TicTacToePlayer {

	// one Scanner per player: a new Scanner per move would lose input that the previous one already buffered
	private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

	@Override
	public int play(Stone[] board, Stone colorToPlay) {
		System.out.println(TicTacToeMain.toString(board) + "where to put the next " + colorToPlay + "? (0-8): ");
		while (true) {
			// a single digit 0-8 only: rejects text, empty lines, huge numbers and escape codes without parsing them
			var line = scanner.nextLine().trim(); // NoSuchElementException at end of input, handled in main
			if (line.matches("[0-8]") && board[Integer.parseInt(line)] == null) {
				return Integer.parseInt(line);
			}
			System.out.println("Choose a free field from 0 to 8.");
		}
	}
}
