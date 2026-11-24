package colorclicker;

import java.util.List;

/**
 * A ranglista tárolója: amit a backend (Leaderboard) a tárolástól elvár.
 * Interfész: a programban a JDBC-s változat (JdbcHighScoreRepository), a
 * tesztekben egy memóriabeli lista valósítja meg. A modellt (HighScore)
 * kapja és adja; a nézetnek szánt DTO a Leaderboardban készül.
 */
public interface HighScoreRepository {

    /**
     * Eltárol egy eredményt.
     *
     * @param highScore az eredmény
     * @throws StorageException ha a tárolás nem sikerült
     */
    void save(HighScore highScore) throws StorageException;

    /**
     * A legjobb idők egy táblaméretre: a legkisebb idő elöl, azonos időnél
     * a korábbi előrébb.
     *
     * @param size a tábla mérete
     * @param limit legfeljebb ennyi eredmény
     * @return a lista; üres, ha erre a méretre még nincs eredmény
     * @throws StorageException ha az olvasás nem sikerült
     */
    List<HighScore> findBest(int size, int limit) throws StorageException;

    /**
     * Minden eredményt töröl.
     *
     * @throws StorageException ha a törlés nem sikerült
     */
    void deleteAll() throws StorageException;

}
