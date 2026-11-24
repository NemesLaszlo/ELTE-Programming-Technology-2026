package dodger.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A ranglista BACKENDJE: ezt hívja a felület (a nézet), és csak ezt. A Leaderboard tudja a ranglista szabályait (milyen
 * név fogadható el, mikor születik az eredmény, mi a helyezés), a tárolást
 * a HighScoreRepository-ra bízza, és a felületnek DTO-kat ad.
 *
 * Itt van a határ a modell és a DTO között:
 * - befelé (a tároló felé) a MODELL megy: a felület egyszerű értékeket ad
 *   (név, pontszám), a backend ebből készíti a HighScore-t az időponttal;
 * - kifelé (a felület felé) a DTO: a tároló HighScore-okat ad, a backend
 *   ebből készít HighScoreDto-t, a helyezéssel kiegészítve (toDto).
 *
 * A felület így sem az adatbázisról, sem a modellről nem tud: nem lát
 * SQL-t, kapcsolatot, SQLException-t, HighScore-t. Ha egyszer a tárolás
 * módja vagy a modell változik, a felülethez nem kell nyúlni.
 */
public class Leaderboard {

    /** A név legnagyobb hossza; egyezik a tábla VARCHAR(50)-ével. */
    public static final int MAX_NAME_LENGTH = 50;

    private final HighScoreRepository repository;

    /**
     * @param repository a tároló (a programban JdbcHighScoreRepository)
     */
    public Leaderboard(HighScoreRepository repository) {
        this.repository = repository;
    }

    /**
     * Elment egy eredményt a mostani időponttal. A felület egyszerű
     * értékeket ad; a modellt (HighScore) a backend készíti belőlük.
     *
     * @param name a játékos neve; szóközök nélkül 1..MAX_NAME_LENGTH karakter
     * @param score a pontszám
     * @throws IllegalArgumentException ha a név üres vagy túl hosszú
     * @throws StorageException ha a tárolás nem sikerült
     */
    public void save(String name, int score) throws StorageException {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Invalid name: '" + name + "'");
        }
        repository.save(new HighScore(name.strip(), score, LocalDateTime.now()));
    }

    /**
     * A legjobb eredmények a felületnek: DTO-k, helyezéssel, a legjobbal az
     * elején.
     *
     * @param limit legfeljebb ennyi eredmény; pozitív
     * @return a lista; üres, ha még nincs eredmény
     * @throws StorageException ha az olvasás nem sikerült
     */
    public List<HighScoreDto> top(int limit) throws StorageException {
        if (limit <= 0) {
            throw new IllegalArgumentException("The limit must be positive: " + limit);
        }
        List<HighScore> best = repository.findTop(limit);
        List<HighScoreDto> result = new ArrayList<>();
        for (int i = 0; i < best.size(); i++) {
            result.add(toDto(i + 1, best.get(i)));
        }
        return result;
    }

    /**
     * Minden eredményt töröl.
     *
     * @throws StorageException ha a törlés nem sikerült
     */
    public void clear() throws StorageException {
        repository.deleteAll();
    }

    /**
     * Modell -> DTO: ami a megjelenítéshez kell, a helyezéssel.
     *
     * @param rank a helyezés, 1-től
     * @param highScore az eredmény
     * @return a felületnek szánt DTO
     */
    static HighScoreDto toDto(int rank, HighScore highScore) {
        return new HighScoreDto(rank, highScore.name(), highScore.score(), highScore.createdAt());
    }

    /**
     * Elfogadható-e a név: szóközök nélkül 1..MAX_NAME_LENGTH karakter. A
     * felület is ezzel ellenőriz, mielőtt menteni próbálna, hogy a
     * felhasználó értelmes üzenetet kapjon, ne kivételt.
     *
     * Ez NEM az SQL injection elleni védelem (az a tárolóban a
     * PreparedStatement), hanem közönséges bemenet-ellenőrzés: üres nevet
     * nem akarunk a ranglistán, és a VARCHAR(50)-be nem fér több.
     *
     * @param name a név
     * @return igaz, ha elfogadható
     */
    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.strip().length() <= MAX_NAME_LENGTH;
    }

}
