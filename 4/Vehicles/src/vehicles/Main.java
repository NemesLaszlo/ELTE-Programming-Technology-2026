package vehicles;

import java.io.IOException;

/**
 * A program belépési pontja.
 *
 * @author bli
 */
public class Main {

    /** Ezt a fájlt olvassuk be, ha a felhasználó nem ad meg mást. */
    private static final String DEFAULT_FILE = "data.txt";

    /** A program kilépési kódja hiba esetén. */
    private static final int EXIT_ERROR = 1;

    /**
     * @param args a parancssori argumentumok; az első (nem kötelező) a
     * beolvasandó fájl neve
     */
    public static void main(String[] args) {
        String filename = args.length > 0 ? args[0] : DEFAULT_FILE;

        Database database = new Database();
        try {
            database.read(filename);
            database.report();
        } catch (IOException ex) {
            // Ide jutunk akkor is, ha a fájl nem létezik.
            System.err.println("Could not read the file: " + filename);
            System.exit(EXIT_ERROR);
        } catch (InvalidInputException ex) {
            System.err.println("Invalid input! " + ex.getMessage());
            System.exit(EXIT_ERROR);
        }
    }

}
