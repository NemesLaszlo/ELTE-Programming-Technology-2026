package colorclicker;

/**
 * A tárolás hibája: az adatbázis nem érhető el, nem sikerült írni vagy
 * olvasni. A tároló (JdbcHighScoreRepository) ebbe csomagolja a
 * SQLException-t, így a backend és a nézet nem függ a java.sql csomagtól.
 * Ellenőrzött kivétel: a nézet üzenetet mutat, és a játék megy tovább.
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
