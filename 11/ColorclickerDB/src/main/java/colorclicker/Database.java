package colorclicker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Az adatbázis-kapcsolat adatai egy helyen. Csak itt kell átírni valamit,
 * ha SQLite helyett a Dockerben futó MySQL-t használjuk: a program többi
 * része (JdbcHighScoreRepository, Leaderboard, a nézet) mindkettővel ugyanaz. Ugyanaz az osztály,
 * mint a 10. heti HighScores és a Dodger projektben.
 */
public final class Database {

    // ----- SQLite (alapértelmezett) -----
    // Az adatbázis egyetlen fájl a projekt mappájában (ahonnan a programot
    // indítjuk). Nem kell hozzá szerver, felhasználó, jelszó. Ha a fájl nem
    // létezik, a driver létrehozza.
    public static final String URL = "jdbc:sqlite:colorclicker.db";
    public static final String USER = null;
    public static final String PASSWORD = null;

    // ----- MySQL (Docker) -----
    // Ha fut a progtech-mysql konténer (ld. compose.yaml és a README), a fenti
    // három sort kommentezzük ki, ezeket pedig vegyük elő. A felhasználó, a
    // jelszó és az adatbázis neve a konténer indításakor megadott
    // MYSQL_USER / MYSQL_PASSWORD / MYSQL_DATABASE értéke: parancssorból a
    // "docker inspect progtech-mysql", a Docker Desktopban a konténer
    // Inspect fülén (Environment) olvasható ki; a port a "docker ps"
    // PORTS oszlopában (3306->3306).
    // public static final String URL = "jdbc:mysql://localhost:3306/progtech";
    // public static final String USER = "progtech";
    // public static final String PASSWORD = "progtech";

    private Database() {
    }

    /**
     * Új kapcsolat az adatbázishoz. A hívó felelőssége lezárni: mindig
     * try-with-resources-ben használjuk (ld. JdbcHighScoreRepository).
     *
     * @return a megnyitott kapcsolat
     * @throws SQLException ha nem sikerül kapcsolódni (nem fut a szerver,
     * rossz jelszó, nincs ilyen adatbázis, ...)
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
