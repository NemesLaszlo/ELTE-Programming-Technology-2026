package dodger.model;

/**
 * A tárolás hibája: az adatbázis nem érhető el, nem sikerült írni vagy
 * olvasni. A tároló (JdbcHighScoreRepository) ebbe csomagolja a
 * SQLException-t, így a backend és a felület nem függ a java.sql csomagtól:
 * nekik mindegy, hogy a hiba egy SQL-szerveré vagy egy fájlé volt.
 *
 * Ellenőrzött kivétel: a hívónak döntenie kell, mit tesz vele (a konzolos
 * program kiírja, a játék felugró üzenetet mutat, és megy tovább).
 */
public class StorageException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param message mi nem sikerült, emberi nyelven
     * @param cause az eredeti kivétel (pl. SQLException)
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

}
