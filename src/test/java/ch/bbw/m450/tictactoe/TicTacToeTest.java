package ch.bbw.m450.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.bbw.m450.tictactoe.TicTacToePlayer.Stone;
import ch.bbw.m450.tictactoe.players.GreedyPlayer;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class TicTacToeTest {

    @ParameterizedTest
    // test daten aus methode "winningboards" holen
    @MethodSource("winningBoards")
    void isWin_returnsTrueForWinningBoards(Stone[] board, Stone player) {
        assertThat(TicTacToeMain.isWin(board, player)).isTrue();
    }

    // ausfs gleiche Feld Cross und Circle gleichzeitig
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

    // Helpers
    private GreedyPlayer greedyPlayer() {
        return new GreedyPlayer();
    }

    // Fixtures
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