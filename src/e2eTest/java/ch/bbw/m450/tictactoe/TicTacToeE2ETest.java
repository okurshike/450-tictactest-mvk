package ch.bbw.m450.tictactoe;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

// End-to-end: startet das gebaute JAR wie ein Benutzer ("java -jar") und spielt über die Konsole.
// Timeout: hängt ein Spiel (z. B. wartet ewig auf Eingabe), schlägt der Test fehl statt die CI zu blockieren.
@Timeout(value = 10, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TicTacToeE2ETest {

    @Test
    void humanWinsWithDiagonal() throws Exception {
        assertThat(play("4\n8\n2\n6\n")).contains("winner is: CROSS");
    }

    @Test
    void greedyWinsWithTopRow() throws Exception {
        // X: 8, 7, 3   O (Greedy): 0, 1, 2
        assertThat(play("8\n7\n3\n")).contains("winner is: CIRCLE");
    }

    @Test
    void invalidInputIsRejectedAndGameContinues() throws Exception {
        var garbage = "abc\n\n99999999999999999999\n-1\n\033[A\n";
        var output = play(garbage + "4\n8\n2\n6\n");

        assertThat(output).contains("Choose a free field").contains("winner is: CROSS").doesNotContain("Exception");
    }

    @Test
    void occupiedFieldIsRejected() throws Exception {
        // Greedy O nimmt Feld 0, danach ist 0 besetzt
        var output = play("4\n0\n8\n2\n6\n");

        assertThat(output).contains("Choose a free field").contains("winner is: CROSS");
    }

    @Test
    void endOfInputAbortsWithoutStackTrace() throws Exception {
        var output = play("4\n");

        assertThat(output).contains("game aborted").doesNotContain("Exception");
    }

    /** Runs the jar with the given console input and returns everything it printed (stdout and stderr). */
    private static String play(String input) throws Exception {
        var java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        var process = new ProcessBuilder(java, "-jar", System.getProperty("tictactoe.jar"))
                .redirectErrorStream(true)
                .start();
        try (var stdin = process.getOutputStream()) {
            stdin.write(input.getBytes(UTF_8));
        }
        var output = new String(process.getInputStream().readAllBytes(), UTF_8);
        assertThat(process.waitFor(5, TimeUnit.SECONDS)).as("game finished").isTrue();
        assertThat(process.exitValue()).as("exit code").isZero();
        return output;
    }
}
