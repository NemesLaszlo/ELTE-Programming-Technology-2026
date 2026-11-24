package colorclicker;

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
 * A JDBC-s tároló tesztjei egy ideiglenes, üres SQLite-fájlon (ld. a 10.
 * heti JdbcHighScoreRepositoryTest). Itt a különbség: a legjobb a
 * LEGKISEBB idő, és a méret szerint szűrünk.
 */
public class JdbcHighScoreRepositoryTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 20, 10, 15, 30);

    private Path file;
    private JdbcHighScoreRepository repository;

    @Before
    public void createEmptyDatabase() throws IOException {
        file = Files.createTempFile("colorclicker-test", ".db");
        repository = new JdbcHighScoreRepository("jdbc:sqlite:" + file, null, null);
    }

    @After
    public void deleteDatabase() throws IOException {
        Files.deleteIfExists(file);
    }

    @Test
    public void emptyDatabaseGivesEmptyList() throws StorageException {
        assertTrue(repository.findBest(10, 10).isEmpty());
    }

    @Test
    public void savedResultIsReadBack() throws StorageException {
        repository.save(new HighScore("Anna", 10, 12_345, T0));

        List<HighScore> best = repository.findBest(10, 10);
        assertEquals(1, best.size());
        assertEquals(new HighScore("Anna", 10, 12_345, T0), best.get(0));
    }

    @Test
    public void bestTimeIsTheSmallest() throws StorageException {
        repository.save(new HighScore("Bence", 5, 3000, T0));
        repository.save(new HighScore("Csilla", 5, 1000, T0));
        repository.save(new HighScore("David", 5, 2000, T0));

        List<HighScore> best = repository.findBest(5, 10);

        assertEquals("Csilla", best.get(0).name());
        assertEquals("David", best.get(1).name());
        assertEquals("Bence", best.get(2).name());
    }

    @Test
    public void equalTimesAreOrderedByDateAscending() throws StorageException {
        repository.save(new HighScore("Later", 5, 1000, T0.plusHours(1)));
        repository.save(new HighScore("Earlier", 5, 1000, T0));

        assertEquals("Earlier", repository.findBest(5, 10).get(0).name());
    }

    @Test
    public void onlyTheRequestedSizeIsListed() throws StorageException {
        repository.save(new HighScore("Emma", 5, 1000, T0));
        repository.save(new HighScore("Ferenc", 10, 5000, T0));
        repository.save(new HighScore("Greta", 20, 9000, T0));

        List<HighScore> best = repository.findBest(10, 10);

        assertEquals(1, best.size());
        assertEquals("Ferenc", best.get(0).name());
    }

    @Test
    public void limitCutsTheList() throws StorageException {
        for (int i = 1; i <= 12; i++) {
            repository.save(new HighScore("Player " + i, 5, i * 1000, T0));
        }

        List<HighScore> best = repository.findBest(5, 10);

        assertEquals(10, best.size());
        assertEquals(1000, best.get(0).millis());
        assertEquals(10_000, best.get(9).millis());
    }

    @Test
    public void nameWithSqlInItIsStoredAsPlainText() throws StorageException {
        String evil = "x' OR '1'='1";

        repository.save(new HighScore(evil, 5, 1000, T0));

        assertEquals(evil, repository.findBest(5, 1).get(0).name());
    }

    @Test
    public void deleteAllEmptiesTheTable() throws StorageException {
        repository.save(new HighScore("Anna", 5, 1000, T0));

        repository.deleteAll();

        assertTrue(repository.findBest(5, 10).isEmpty());
    }

    @Test(expected = StorageException.class)
    public void unreachableDatabaseGivesStorageException() throws StorageException {
        new JdbcHighScoreRepository("jdbc:mysql://localhost:1/nincs?connectTimeout=500", "x", "x").findBest(5, 1);
    }

    @Test
    public void timeIsFormattedAsMinutesSecondsMillis() {
        assertEquals("0:00.000", HighScoresDialog.formatTime(0));
        assertEquals("0:01.234", HighScoresDialog.formatTime(1234));
        assertEquals("1:02.345", HighScoresDialog.formatTime(62_345));
    }

}
