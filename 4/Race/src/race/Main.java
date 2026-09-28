package race;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Command line entry point of the race simulation.
 *
 * <p>Usage: {@code java race.Main [input file]}. When no file is given as an
 * argument, the program asks for its name on the standard input.</p>
 */
public final class Main {

    /** Exit code of a successful run. */
    static final int EXIT_OK = 0;
    /** Exit code used when the input could not be read or was invalid. */
    static final int EXIT_ERROR = 1;

    private Main() {
        // Not meant to be instantiated.
    }

    /**
     * Starts the program and terminates the JVM with the resulting exit code.
     *
     * @param args the command line arguments; the optional first one is the
     * name of the input file
     */
    public static void main(String[] args) {
        System.exit(run(args, System.in, System.out, System.err));
    }

    /**
     * Runs the program on the given streams. Separated from
     * {@link #main(String[])} so that it can be tested without terminating
     * the JVM.
     *
     * @param args the command line arguments
     * @param in the stream the file name is asked from, if it is not in
     * {@code args}
     * @param out the stream of the results
     * @param err the stream of the error messages
     * @return {@link #EXIT_OK} on success, {@link #EXIT_ERROR} otherwise
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        if (args.length > 1) {
            err.println("Usage: java race.Main [input file]");
            return EXIT_ERROR;
        }
        String fileName = "";
        try {
            fileName = args.length == 1 ? args[0] : askFileName(in, out);
            if (fileName.isBlank()) {
                return fail(err, "no file name was given.");
            }
            Race race = RaceFileReader.read(Path.of(fileName.strip()));
            race.run();
            printResult(race, out);
            return EXIT_OK;
        } catch (InvalidPathException ex) {
            return fail(err, "not a valid file name: " + fileName);
        } catch (NoSuchFileException ex) {
            return fail(err, "the file does not exist: " + fileName);
        } catch (InvalidInputException ex) {
            return fail(err, "invalid input in " + fileName + ": " + ex.getMessage());
        } catch (IOException ex) {
            return fail(err, "could not read the input: " + ex.getMessage());
        }
    }

    private static int fail(PrintStream err, String message) {
        err.println("Error: " + message);
        return EXIT_ERROR;
    }

    /**
     * Asks the user for the name of the input file.
     *
     * @return the typed name; empty if the input has ended
     */
    private static String askFileName(InputStream in, PrintStream out) throws IOException {
        out.print("Enter the name of the input file: ");
        out.flush();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, Charset.defaultCharset()));
        String answer = reader.readLine();
        return answer == null ? "" : answer;
    }

    private static void printResult(Race race, PrintStream out) {
        out.println("The race lasted " + race.getDays().size() + " day(s).");
        out.println();
        out.println("Final standings:");
        for (Creature creature : race.getCreatures()) {
            out.println("  " + creature);
        }
        out.println();
        out.println(describeWinners(race.getWinners()));
    }

    private static String describeWinners(List<Creature> winners) {
        if (winners.isEmpty()) {
            return "There is no winner: no creature survived the race.";
        }
        List<String> names = new ArrayList<>();
        for (Creature winner : winners) {
            names.add(winner.getName());
        }
        // Every winner travelled the same distance, so any of them can be asked.
        String distance = " (distance: " + winners.get(0).getDistance() + ")";
        String title = winners.size() == 1 ? "Winner: " : "Tie! Winners: ";
        return title + String.join(", ", names) + distance;
    }

}
