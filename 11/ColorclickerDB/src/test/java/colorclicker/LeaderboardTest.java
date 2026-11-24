package colorclicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;

/**
 * A backend (Leaderboard) tesztjei adatbázis nélkül: a tároló egy
 * memóriabeli lista (ld. a 10. heti LeaderboardTest).
 */
public class LeaderboardTest {

    private static class InMemoryRepository implements HighScoreRepository {

        private final List<HighScore> scores = new ArrayList<>();

        @Override
        public void save(HighScore highScore) {
            scores.add(highScore);
        }

        @Override
        public List<HighScore> findBest(int size, int limit) {
            return scores.stream()
                    .filter(s -> s.size() == size)
                    .sorted(Comparator.comparingLong(HighScore::millis)
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
    public void saveBuildsTheModelWithTheTrimmedName() throws StorageException {
        LocalDateTime before = LocalDateTime.now().withNano(0);

        leaderboard.save("  Anna  ", 10, 12_345);

        assertEquals(1, repository.scores.size());
        HighScore saved = repository.scores.get(0);
        assertEquals("Anna", saved.name());
        assertEquals(10, saved.size());
        assertEquals(12_345, saved.millis());
        assertFalse(saved.createdAt().isBefore(before));
    }

    @Test
    public void bestGivesDtosWithRanksFastestFirstForTheSize() throws StorageException {
        leaderboard.save("Bence", 5, 3000);
        leaderboard.save("Csilla", 5, 1000);
        leaderboard.save("David", 10, 500);

        List<HighScoreDto> best = leaderboard.best(5, 10);

        assertEquals(2, best.size());
        assertEquals(1, best.get(0).rank());
        assertEquals("Csilla", best.get(0).name());
        assertEquals(1000, best.get(0).millis());
        assertEquals(2, best.get(1).rank());
        assertEquals("Bence", best.get(1).name());
    }

    @Test
    public void toDtoCopiesTheModelAndAddsTheRank() {
        LocalDateTime t = LocalDateTime.of(2026, 11, 20, 10, 15, 30);

        assertEquals(new HighScoreDto(3, "Emma", 5, 1234, t), Leaderboard.toDto(3, new HighScore("Emma", 5, 1234, t)));
    }

    @Test
    public void emptyLeaderboardGivesEmptyList() throws StorageException {
        assertTrue(leaderboard.best(5, 10).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyNameIsRejected() throws StorageException {
        leaderboard.save(" ", 5, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSizeIsRejected() throws StorageException {
        leaderboard.save("Hanna", 0, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeTimeIsRejected() throws StorageException {
        leaderboard.save("Hanna", 5, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void limitMustBePositive() throws StorageException {
        leaderboard.best(5, 0);
    }

    @Test
    public void isValidNameChecksBlankAndLength() {
        assertTrue(Leaderboard.isValidName("Anna"));
        assertFalse(Leaderboard.isValidName(null));
        assertFalse(Leaderboard.isValidName("  "));
        assertFalse(Leaderboard.isValidName("x".repeat(Leaderboard.MAX_NAME_LENGTH + 1)));
    }

}
