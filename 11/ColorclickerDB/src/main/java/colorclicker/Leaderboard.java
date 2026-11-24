package colorclicker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A ranglista BACKENDJE: ezt hívja a nézet (ColorClickerGUI), és csak ezt.
 * A Leaderboard tudja a ranglista szabályait (milyen név és eredmény
 * fogadható el, mi a helyezés), a tárolást a HighScoreRepository-ra bízza,
 * és a nézetnek DTO-kat ad.
 *
 * Itt van a határ a modell és a DTO között: befelé (a tároló felé) a
 * modell megy (save -> new HighScore), kifelé (a nézet felé) a DTO
 * (best -> toDto, a helyezéssel). A nézet így sem az adatbázisról, sem a
 * modellről nem tud.
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
     * Elment egy eredményt a mostani időponttal.
     *
     * @param name a játékos neve; szóközök nélkül 1..MAX_NAME_LENGTH karakter
     * @param size a tábla mérete
     * @param millis az idő ezredmásodpercben
     * @throws IllegalArgumentException ha a név érvénytelen, vagy a méret /
     * idő nem pozitív
     * @throws StorageException ha a tárolás nem sikerült
     */
    public void save(String name, int size, long millis) throws StorageException {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Invalid name: '" + name + "'");
        }
        if (size <= 0 || millis < 0) {
            throw new IllegalArgumentException("Invalid result: " + size + ", " + millis);
        }
        repository.save(new HighScore(name.strip(), size, millis, LocalDateTime.now()));
    }

    /**
     * A legjobb idők egy táblaméretre a nézetnek: DTO-k, helyezéssel, a
     * legjobbal az elején.
     *
     * @param size a tábla mérete
     * @param limit legfeljebb ennyi eredmény; pozitív
     * @return a lista; üres, ha erre a méretre még nincs eredmény
     * @throws StorageException ha az olvasás nem sikerült
     */
    public List<HighScoreDto> best(int size, int limit) throws StorageException {
        if (limit <= 0) {
            throw new IllegalArgumentException("The limit must be positive: " + limit);
        }
        List<HighScore> best = repository.findBest(size, limit);
        List<HighScoreDto> result = new ArrayList<>();
        for (int i = 0; i < best.size(); i++) {
            result.add(toDto(i + 1, best.get(i)));
        }
        return result;
    }

    /**
     * Modell -> DTO: ami a megjelenítéshez kell, a helyezéssel.
     */
    static HighScoreDto toDto(int rank, HighScore highScore) {
        return new HighScoreDto(rank, highScore.name(), highScore.size(), highScore.millis(), highScore.createdAt());
    }

    /**
     * Elfogadható-e a név: szóközök nélkül 1..MAX_NAME_LENGTH karakter.
     * (Nem az SQL injection elleni védelem: az a tárolóban a
     * PreparedStatement.)
     */
    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.strip().length() <= MAX_NAME_LENGTH;
    }

}
