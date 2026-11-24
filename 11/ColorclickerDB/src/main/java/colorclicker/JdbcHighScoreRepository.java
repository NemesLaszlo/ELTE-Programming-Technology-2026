package colorclicker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * A ranglista tárolója adatbázisban, JDBC-vel (ld. a 10. heti HighScores
 * projektet). Ugyanaz a felépítés, csak a tábla más: itt az eredmény az
 * idő, és a méret szerint szűrünk.
 *
 * - Ez az egyetlen osztály, amelyik SQL-t tartalmaz; a SQLException-t
 *   StorageException-ként adja tovább.
 * - CODE-FIRST: a táblát a program hozza létre, ha még nincs.
 * - Minden művelet saját kapcsolatot nyit és zár (try-with-resources).
 * - Minden SQL PreparedStatement: a felhasználótól jövő név ? helyére kerül.
 */
public class JdbcHighScoreRepository implements HighScoreRepository {

    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS results (
                name       VARCHAR(50) NOT NULL,
                size       INTEGER     NOT NULL,
                millis     BIGINT      NOT NULL,
                created_at TIMESTAMP   NOT NULL
            )""";
    private static final String INSERT
            = "INSERT INTO results (name, size, millis, created_at) VALUES (?, ?, ?, ?)";
    // A legjobb idő a legkisebb: ASC. Azonos időnél a korábbi előrébb.
    private static final String SELECT_BEST = """
            SELECT name, size, millis, created_at
            FROM results
            WHERE size = ?
            ORDER BY millis ASC, created_at ASC
            LIMIT ?""";
    private static final String DELETE_ALL = "DELETE FROM results";

    private final String url;
    private final String user;
    private final String password;
    private boolean tableChecked;

    /**
     * A Database.java-ban beállított adatbázist használja.
     */
    public JdbcHighScoreRepository() {
        this(Database.URL, Database.USER, Database.PASSWORD);
    }

    /**
     * Tetszőleges adatbázist használ (a teszteknek: ideiglenes SQLite-fájl).
     */
    public JdbcHighScoreRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public void save(HighScore highScore) throws StorageException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {
            statement.setString(1, highScore.name());
            statement.setInt(2, highScore.size());
            statement.setLong(3, highScore.millis());
            statement.setTimestamp(4, Timestamp.valueOf(highScore.createdAt()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Could not save the score: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HighScore> findBest(int size, int limit) throws StorageException {
        List<HighScore> result = new ArrayList<>();
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(SELECT_BEST)) {
            statement.setInt(1, size);
            statement.setInt(2, limit);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new HighScore(
                            rows.getString("name"),
                            rows.getInt("size"),
                            rows.getLong("millis"),
                            rows.getTimestamp("created_at").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new StorageException("Could not read the leaderboard: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void deleteAll() throws StorageException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(DELETE_ALL);
        } catch (SQLException e) {
            throw new StorageException("Could not clear the leaderboard: " + e.getMessage(), e);
        }
    }

    private Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(url, user, password);
        if (!tableChecked) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(CREATE_TABLE);
                tableChecked = true;
            } catch (SQLException e) {
                connection.close();
                throw e;
            }
        }
        return connection;
    }

}
