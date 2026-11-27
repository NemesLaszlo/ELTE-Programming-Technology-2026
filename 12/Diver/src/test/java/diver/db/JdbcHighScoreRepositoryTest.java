package diver.db;

import diver.model.HighScore;
import diver.model.HighScoreRepository;
import diver.model.StorageException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A JDBC-s tároló tesztjei. Minden teszt egy ÜRES, ideiglenes
 * SQLite-fájlt kap, és a végén törli: a tesztek nem nyúlnak a "valódi"
 * diver.db-hez, és nem függnek egymástól. Ehhez kell a tároló második
 * konstruktora (URL paraméterrel).
 *
 * Az első teszt egyben a code-first ellenőrzése is: az ideiglenes fájlban
 * nincs tábla, a tároló hozza létre.
 */
public class JdbcHighScoreRepositoryTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 20, 10, 15, 30);

    private Path file;
    private JdbcHighScoreRepository repository;

    @Before
    public void createEmptyDatabase() throws IOException {
        file = Files.createTempFile("diver-test", ".db");
        repository = new JdbcHighScoreRepository("jdbc:sqlite:" + file, null, null);
    }

    @After
    public void deleteDatabase() throws IOException {
        Files.deleteIfExists(file);
    }

    @Test
    public void emptyDatabaseGivesEmptyList() throws StorageException {
        assertTrue(repository.findTop(10).isEmpty());
    }

    @Test
    public void savedScoreIsReadBack() throws StorageException {
        repository.save(new HighScore("Anna", 42, T0));

        List<HighScore> top = repository.findTop(10);
        assertEquals(1, top.size());
        assertEquals(new HighScore("Anna", 42, T0), top.get(0));
    }

    @Test
    public void topIsOrderedByScoreDescending() throws StorageException {
        repository.save(new HighScore("Bence", 10, T0));
        repository.save(new HighScore("Csilla", 30, T0.plusMinutes(1)));
        repository.save(new HighScore("David", 20, T0.plusMinutes(2)));

        List<HighScore> top = repository.findTop(10);

        assertEquals("Csilla", top.get(0).name());
        assertEquals("David", top.get(1).name());
        assertEquals("Bence", top.get(2).name());
    }

    @Test
    public void equalScoresAreOrderedByDateAscending() throws StorageException {
        repository.save(new HighScore("Later", 10, T0.plusHours(1)));
        repository.save(new HighScore("Earlier", 10, T0));

        List<HighScore> top = repository.findTop(10);

        assertEquals("Earlier", top.get(0).name());
        assertEquals("Later", top.get(1).name());
    }

    @Test
    public void limitCutsTheList() throws StorageException {
        for (int i = 1; i <= 15; i++) {
            repository.save(new HighScore("Player " + i, i, T0.plusSeconds(i)));
        }

        List<HighScore> top = repository.findTop(10);

        assertEquals(10, top.size());
        assertEquals(15, top.get(0).score());
        assertEquals(6, top.get(9).score());
    }

    @Test
    public void nameWithSqlInItIsStoredAsPlainText() throws StorageException {
        // Ha a nevet szövegösszefűzéssel tennénk az SQL-be, ez a "név" egy
        // második utasítást is futtatna. A PreparedStatement miatt csak egy
        // (fura) szöveg marad.
        String evil = "Robert'); DROP TABLE highscores; --";

        repository.save(new HighScore(evil, 5, T0));

        List<HighScore> top = repository.findTop(10);
        assertEquals(1, top.size());
        assertEquals(evil, top.get(0).name());
    }

    @Test
    public void deleteAllEmptiesTheTable() throws StorageException {
        repository.save(new HighScore("Ferenc", 1, T0));
        repository.save(new HighScore("Greta", 2, T0));

        repository.deleteAll();

        assertTrue(repository.findTop(10).isEmpty());
    }

    @Test
    public void anotherRepositoryOnTheSameFileSeesTheData() throws StorageException {
        repository.save(new HighScore("Hanna", 7, T0));

        JdbcHighScoreRepository another = new JdbcHighScoreRepository("jdbc:sqlite:" + file, null, null);

        assertEquals(1, another.findTop(10).size());
    }

    @Test(expected = StorageException.class)
    public void unreachableDatabaseGivesStorageException() throws StorageException {
        // Nincs MySQL-szerver ezen a porton: a SQLException StorageException-ként jön.
        new JdbcHighScoreRepository("jdbc:mysql://localhost:1/nincs?connectTimeout=500", "x", "x").findTop(1);
    }

}
