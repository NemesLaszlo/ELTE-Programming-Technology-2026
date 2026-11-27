package diver.db;

import diver.model.HighScore;
import diver.model.HighScoreRepository;
import diver.model.StorageException;
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
 * A ranglista tárolója adatbázisban, JDBC-vel. Ez az EGYETLEN osztály,
 * amelyik SQL-t tartalmaz és az adatbázissal beszél. Kifelé a
 * HighScoreRepository interfészt mutatja: a modellt (HighScore) kapja és
 * adja, a hibát StorageException-ként jelzi.
 *
 * CODE-FIRST: a táblát a program hozza létre, ha még nincs
 * (CREATE TABLE IF NOT EXISTS). Nem kell előre semmit sem csinálni az
 * adatbázisban: egy üres SQLite-fájl vagy egy frissen indított
 * MySQL-konténer is azonnal jó. A tábla szerkezete a kódban van, a kóddal
 * együtt verziózva.
 *
 * Minden művelet SAJÁT kapcsolatot nyit és zár (try-with-resources). Egy
 * asztali programnak ez bőven elég, és nincs "beragadt" kapcsolat, amit
 * elfelejtünk lezárni, vagy amit a szerver időközben eldobott.
 *
 * Minden SQL-utasítás PreparedStatement, a felhasználótól jövő értékek
 * (név, pontszám) ? helyére kerülnek. Ez véd az SQL injection ellen: a név
 * akkor is csak egy szöveg marad, ha aposztróf vagy SQL-kulcsszó van benne.
 */
public class JdbcHighScoreRepository implements HighScoreRepository {

    // Ugyanez az SQL SQLite-ban és MySQL-ben is működik. (Ha azonosító oszlop
    // is kellene, az a két adatbázisban másképp írandó, ld. a README-t.)
    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS highscores (
                name       VARCHAR(50) NOT NULL,
                score      INTEGER     NOT NULL,
                created_at TIMESTAMP   NOT NULL
            )""";
    private static final String INSERT
            = "INSERT INTO highscores (name, score, created_at) VALUES (?, ?, ?)";
    private static final String SELECT_TOP = """
            SELECT name, score, created_at
            FROM highscores
            ORDER BY score DESC, created_at ASC
            LIMIT ?""";
    private static final String DELETE_ALL = "DELETE FROM highscores";

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
     * Tetszőleges adatbázist használ. A tesztek egy ideiglenes SQLite-fájlt
     * adnak meg, hogy ne a "valódi" ranglistába írjanak.
     *
     * @param url a JDBC URL
     * @param user a felhasználó (SQLite-nál null)
     * @param password a jelszó (SQLite-nál null)
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
            statement.setInt(2, highScore.score());
            statement.setTimestamp(3, Timestamp.valueOf(highScore.createdAt()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Could not save the score: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HighScore> findTop(int limit) throws StorageException {
        List<HighScore> result = new ArrayList<>();
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(SELECT_TOP)) {
            statement.setInt(1, limit);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new HighScore(
                            rows.getString("name"),
                            rows.getInt("score"),
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

    /**
     * Új kapcsolat, és az első alkalommal a tábla létrehozása, ha még nincs.
     */
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
