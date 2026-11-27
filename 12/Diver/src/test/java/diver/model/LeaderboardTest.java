package diver.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;

/**
 * A backend (Leaderboard) tesztjei ADATBÁZIS NÉLKÜL: a tároló egy
 * memóriabeli lista. Ez a HighScoreRepository interfész haszna: a
 * Leaderboard szabályait (név-ellenőrzés, a mentés időpontja, a modell ->
 * DTO átalakítás, a helyezés, a lista hossza) gyorsan, fájl és szerver
 * nélkül ellenőrizzük; az SQL-t a JdbcHighScoreRepositoryTest teszteli
 * külön.
 */
public class LeaderboardTest {

    /** Tároló egy listában: a JDBC-s tároló helyett, a teszthez. */
    private static class InMemoryRepository implements HighScoreRepository {

        private final List<HighScore> scores = new ArrayList<>();

        @Override
        public void save(HighScore highScore) {
            scores.add(highScore);
        }

        @Override
        public List<HighScore> findTop(int limit) {
            return scores.stream()
                    .sorted(Comparator.comparingInt(HighScore::score).reversed()
                            .thenComparing(HighScore::createdAt))
                    .limit(limit)
                    .toList();
        }

        @Override
        public void deleteAll() {
            scores.clear();
        }
    }

    private final InMemoryRepository repository = new InMemoryRepository();
    private final Leaderboard leaderboard = new Leaderboard(repository);

    @Test
    public void saveBuildsTheModelWithTheTrimmedNameAndTheCurrentTime() throws StorageException {
        LocalDateTime before = LocalDateTime.now().withNano(0);

        leaderboard.save("  Anna  ", 42);

        assertEquals(1, repository.scores.size());
        HighScore saved = repository.scores.get(0);
        assertEquals("Anna", saved.name());
        assertEquals(42, saved.score());
        assertFalse(saved.createdAt().isBefore(before));
    }

    @Test
    public void topGivesDtosWithRanksBestFirst() throws StorageException {
        leaderboard.save("Bence", 10);
        leaderboard.save("Csilla", 30);
        leaderboard.save("David", 20);

        List<HighScoreDto> top = leaderboard.top(2);

        assertEquals(2, top.size());
        assertEquals(1, top.get(0).rank());
        assertEquals("Csilla", top.get(0).name());
        assertEquals(30, top.get(0).score());
        assertEquals(2, top.get(1).rank());
        assertEquals("David", top.get(1).name());
    }

    @Test
    public void toDtoCopiesTheModelAndAddsTheRank() {
        LocalDateTime t = LocalDateTime.of(2026, 11, 20, 10, 15, 30);

        HighScoreDto dto = Leaderboard.toDto(3, new HighScore("Emma", 7, t));

        assertEquals(new HighScoreDto(3, "Emma", 7, t), dto);
    }

    @Test
    public void emptyLeaderboardGivesEmptyList() throws StorageException {
        assertTrue(leaderboard.top(10).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyNameIsRejected() throws StorageException {
        leaderboard.save("   ", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullNameIsRejected() throws StorageException {
        leaderboard.save(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooLongNameIsRejected() throws StorageException {
        leaderboard.save("x".repeat(Leaderboard.MAX_NAME_LENGTH + 1), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void limitMustBePositive() throws StorageException {
        leaderboard.top(0);
    }

    @Test
    public void clearEmptiesTheLeaderboard() throws StorageException {
        leaderboard.save("Anna", 1);
        leaderboard.save("Bence", 2);

        leaderboard.clear();

        assertTrue(leaderboard.top(10).isEmpty());
    }

    @Test
    public void rejectedNamesAreNotStored() {
        try {
            leaderboard.save("", 1);
        } catch (IllegalArgumentException | StorageException e) {
            // várt
        }

        assertTrue(repository.scores.isEmpty());
    }

    @Test
    public void isValidNameChecksBlankAndLength() {
        assertTrue(Leaderboard.isValidName("Anna"));
        assertTrue(Leaderboard.isValidName(" Anna "));
        assertTrue(Leaderboard.isValidName("x".repeat(Leaderboard.MAX_NAME_LENGTH)));
        assertFalse(Leaderboard.isValidName(null));
        assertFalse(Leaderboard.isValidName(""));
        assertFalse(Leaderboard.isValidName("   "));
        assertFalse(Leaderboard.isValidName("x".repeat(Leaderboard.MAX_NAME_LENGTH + 1)));
    }

}
