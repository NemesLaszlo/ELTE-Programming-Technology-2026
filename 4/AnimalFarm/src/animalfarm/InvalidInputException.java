package animalfarm;

/**
 * Hibás bemeneti adatot jelző kivétel.
 *
 * Ellenőrzött (checked) kivétel, vagyis a hívónak kötelező foglalkoznia vele:
 * vagy elkapja, vagy továbbdobja. Így nem lehet "elfelejteni" a hibás fájl
 * kezelését.
 *
 * @author bli
 */
public class InvalidInputException extends Exception {

    /**
     * A fájl egészére vonatkozó hiba.
     *
     * @param message a hiba leírása
     */
    public InvalidInputException(String message) {
        super(message);
    }

    /**
     * A fájl egy adott sorához tartozó hiba. A sor száma bekerül az üzenetbe,
     * így a felhasználó tudja, hol keresse a hibát.
     *
     * @param lineNumber a hibás sor száma (1-től számozva)
     * @param message a hiba leírása
     * @param cause az eredeti kivétel, ami a hibát kiváltotta
     */
    public InvalidInputException(int lineNumber, String message, Throwable cause) {
        super("Line " + lineNumber + ": " + message, cause);
    }

}
