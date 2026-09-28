package race;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Reads the description of a race from text, line by line.
 *
 * <p>The expected format, with the fields of a line separated by
 * whitespace:</p>
 * <pre>
 * 4                 number of creatures (at least 1)
 * Vandor h 4        name, species code, initial water - once per creature
 * Seta l 7
 * Csuszo s 12
 * Siklo s 10
 * nffeeennf         the days of the race, one letter per day
 * </pre>
 *
 * <p>Species codes: {@code h} sand walker, {@code s} sponge, {@code l} walker.
 * Day codes: {@code n} sunny, {@code f} cloudy, {@code e} rainy. Blank lines
 * are ignored. Files are read as UTF-8.</p>
 */
public final class RaceFileReader {

    private final BufferedReader reader;
    private int lineNumber;

    private RaceFileReader(Reader source) {
        this.reader = new BufferedReader(source);
        this.lineNumber = 0;
    }

    /**
     * Reads a race from a file.
     *
     * @param file the path of the input file
     * @return the race described by the file, not run yet
     * @throws IOException if the file does not exist or cannot be read
     * @throws InvalidInputException if the content of the file is invalid
     */
    public static Race read(Path file) throws IOException, InvalidInputException {
        // No catch block: the try is here only to close the file. The exceptions
        // are declared in the signature and handled by the caller (Main).
        try (BufferedReader source = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return read(source);
        }
    }

    /**
     * Reads a race from a character stream. The stream is not closed.
     *
     * @param source the stream to read the description from
     * @return the race described by the stream, not run yet
     * @throws IOException if reading the stream fails
     * @throws InvalidInputException if the content of the stream is invalid
     */
    public static Race read(Reader source) throws IOException, InvalidInputException {
        return new RaceFileReader(source).readRace();
    }

    private Race readRace() throws IOException, InvalidInputException {
        int count = parseNextLine("the number of creatures", RaceFileReader::parseCount);

        List<Creature> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(parseNextLine("a creature", RaceFileReader::parseCreature));
        }
        List<Weather> days = parseNextLine("the days of the race", RaceFileReader::parseDays);

        String extra = nextLine();
        if (extra != null) {
            throw new InvalidInputException("Line " + lineNumber
                    + ": unexpected content after the days of the race: " + extra);
        }

        try {
            return new Race(creatures, days);
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException(ex.getMessage(), ex);
        }
    }

    /**
     * Reads the next non-blank line and keeps track of its number.
     *
     * @return the line, or {@code null} at the end of the input
     */
    private String nextLine() throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (!line.isBlank()) {
                return line;
            }
        }
        return null;
    }

    /**
     * Reads the next line and parses its whitespace-separated fields.
     *
     * <p>Every problem of the line is reported together with the number of
     * the line. This is the only place where the exceptions of the parsing
     * are translated, so the parsers themselves can stay simple.</p>
     *
     * @param expected what the line should contain, for the error message
     * @param parser converts the fields of the line
     */
    private <T> T parseNextLine(String expected, Function<Scanner, T> parser)
            throws IOException, InvalidInputException {
        String line = nextLine();
        if (line == null) {
            throw new InvalidInputException("Unexpected end of input: expected " + expected);
        }
        Scanner fields = new Scanner(line);
        try {
            T result = parser.apply(fields);
            if (fields.hasNext()) {
                throw new IllegalArgumentException("More data than expected: " + fields.next());
            }
            return result;
        } catch (InputMismatchException ex) {
            // The rejected field stays in the scanner, so it can still be read.
            throw new InvalidInputException(lineNumber,
                    "Expected a whole number, but found: " + fields.next(), ex);
        } catch (NoSuchElementException ex) {
            throw new InvalidInputException(lineNumber,
                    "Data is missing from the end of the line", ex);
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException(lineNumber, ex.getMessage(), ex);
        }
    }

    private static int parseCount(Scanner fields) {
        int count = fields.nextInt();
        if (count < 1) {
            throw new IllegalArgumentException(
                    "The number of creatures must be at least 1: " + count);
        }
        return count;
    }

    private static Creature parseCreature(Scanner fields) {
        String name = fields.next();
        String species = fields.next();
        int water = fields.nextInt();
        return switch (species) {
            case SandWalker.CODE -> new SandWalker(name, water);
            case Sponge.CODE -> new Sponge(name, water);
            case Walker.CODE -> new Walker(name, water);
            default -> throw new IllegalArgumentException("Unknown species code: " + species);
        };
    }

    private static List<Weather> parseDays(Scanner fields) {
        List<Weather> days = new ArrayList<>();
        for (char code : fields.next().toCharArray()) {
            days.add(Weather.fromCode(code));
        }
        return days;
    }

}
