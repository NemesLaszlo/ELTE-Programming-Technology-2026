package race;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * End-to-end tests of the command line program.
 */
public class MainTest {

    private static final String SINGLE_WINNER = """
            3
            Vandor h 4
            Seta l 7
            Csuszo s 12
            nffeennnn
            """;

    private static final String FOUR_WAY_TIE = """
            4
            Vandor h 4
            Seta l 7
            Csuszo s 12
            Siklo s 10
            nffeeennf
            """;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private String createFile(String content) throws IOException {
        return Files.writeString(folder.newFile().toPath(), content).toString();
    }

    /**
     * Runs the program with the given arguments.
     *
     * @param typedInput what the user types on the standard input
     */
    private int run(String typedInput, String... args) {
        return Main.run(args,
                new ByteArrayInputStream(typedInput.getBytes(Charset.defaultCharset())),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String errors() {
        return err.toString(StandardCharsets.UTF_8);
    }

    @Test
    public void winnerIsPrintedForAFileGivenAsArgument() throws Exception {
        assertEquals(Main.EXIT_OK, run("", createFile(SINGLE_WINNER)));
        assertTrue(output(), output().contains("Winner: Vandor (distance: 17)"));
        assertEquals("", errors());
    }

    @Test
    public void fileNameIsAskedWhenThereIsNoArgument() throws Exception {
        String typed = createFile(SINGLE_WINNER) + System.lineSeparator();

        assertEquals(Main.EXIT_OK, run(typed));
        assertTrue(output(), output().contains("Enter the name of the input file"));
        assertTrue(output(), output().contains("Winner: Vandor"));
    }

    @Test
    public void tieIsReported() throws Exception {
        assertEquals(Main.EXIT_OK, run("", createFile(FOUR_WAY_TIE)));
        assertTrue(output(), output().contains(
                "Tie! Winners: Vandor, Seta, Csuszo, Siklo (distance: 12)"));
    }

    @Test
    public void missingWinnerIsReported() throws Exception {
        assertEquals(Main.EXIT_OK, run("", createFile("1\nVandor h 4\nnnnnnnnnn\n")));
        assertTrue(output(), output().contains("There is no winner"));
    }

    @Test
    public void missingFileIsAnError() {
        String missing = new File(folder.getRoot(), "missing.txt").getPath();

        assertEquals(Main.EXIT_ERROR, run("", missing));
        assertTrue(errors(), errors().contains("the file does not exist"));
        assertEquals("", output());
    }

    @Test
    public void invalidContentIsAnError() throws Exception {
        assertEquals(Main.EXIT_ERROR, run("", createFile("1\nVandor x 4\nnfe\n")));
        assertTrue(errors(), errors().contains("invalid input"));
        assertTrue(errors(), errors().contains("Line 2"));
    }

    @Test
    public void directoryInsteadOfFileIsAnError() {
        assertEquals(Main.EXIT_ERROR, run("", folder.getRoot().getPath()));
        assertTrue(errors(), errors().startsWith("Error:"));
    }

    @Test
    public void emptyFileNameIsAnError() {
        assertEquals(Main.EXIT_ERROR, run(System.lineSeparator()));
        assertTrue(errors(), errors().contains("no file name"));
    }

    @Test
    public void closedInputIsAnError() {
        assertEquals(Main.EXIT_ERROR, run(""));
        assertTrue(errors(), errors().contains("no file name"));
    }

    @Test
    public void tooManyArgumentsAreAnError() {
        assertEquals(Main.EXIT_ERROR, run("", "a.txt", "b.txt"));
        assertTrue(errors(), errors().contains("Usage"));
    }

}
