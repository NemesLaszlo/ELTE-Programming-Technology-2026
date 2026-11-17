package highscores;

import java.util.List;

/**
 * A ranglista tárolója: amit a backend (Leaderboard) a tárolástól elvár.
 * Interfész, mert a backendnek mindegy, HOGYAN tárolódnak az eredmények:
 * a program SQLite-ban vagy MySQL-ben (JdbcHighScoreRepository), a tesztek
 * egy memóriabeli listában (LeaderboardTest.InMemoryRepository).
 *
 * A metódusok a MODELLT (HighScore) kapják és adják; SQL-t, kapcsolatot,
 * táblát a hívó nem lát. A felületnek szánt DTO-t nem itt, hanem a
 * Leaderboardban készítjük.
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
     * A legjobb eredmények: pontszám szerint csökkenő sorrendben, azonos
     * pontszámnál a korábbi előrébb.
     *
     * @param limit legfeljebb ennyi eredmény
     * @return a lista, a legjobb eredménnyel az elején; üres, ha még nincs eredmény
     * @throws StorageException ha az olvasás nem sikerült
     */
    List<HighScore> findTop(int limit) throws StorageException;

    /**
     * Minden eredményt töröl.
     *
     * @throws StorageException ha a törlés nem sikerült
     */
    void deleteAll() throws StorageException;

}
