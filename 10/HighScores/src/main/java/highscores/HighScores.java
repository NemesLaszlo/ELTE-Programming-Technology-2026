package highscores;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Adatbázis-bevezető: egy konzolos ranglista. Menüből lehet új eredményt
 * felvenni (név és pontszám), kiírni a 10 legjobbat, és törölni a
 * ranglistát. Az eredmények az adatbázisban maradnak a futtatások között
 * (SQLite: a highscores.db fájl a projekt mappájában).
 *
 * Ez a program a "felület": csak a backendet (Leaderboard) hívja, és
 * DTO-kat kap tőle (HighScoreDto); a modellt (HighScore) nem is látja.
 * Nincs benne SQL, kapcsolat, SQLException: azt mind a
 * JdbcHighScoreRepository rejti el. A main az a hely, ahol a rétegeket
 * összekötjük: new Leaderboard(new JdbcHighScoreRepository()).
 *
 * A bemenetet a NetBeans / IntelliJ Output ablakában vagy a terminálban
 * lehet begépelni (Scanner a System.in-en).
 */
public class HighScores {

    private static final int TOP = 10;
    private static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Leaderboard leaderboard;
    private final Scanner in;

    HighScores(Leaderboard leaderboard, Scanner in) {
        this.leaderboard = leaderboard;
        this.in = in;
    }

    /**
     * @param args a parancssori argumentumok (nem használjuk)
     */
    public static void main(String[] args) {
        System.out.println("Leaderboard - database: " + Database.URL);
        // A rétegek összekötése: a backend a JDBC-s tárolót kapja.
        Leaderboard leaderboard = new Leaderboard(new JdbcHighScoreRepository());
        try (Scanner in = new Scanner(System.in)) {
            new HighScores(leaderboard, in).run();
        }
    }

    /**
     * A menü: addig kérdez, amíg a felhasználó ki nem lép (vagy el nem fogy
     * a bemenet).
     */
    void run() {
        while (true) {
            System.out.println();
            System.out.println("1 - Add a new score");
            System.out.println("2 - Top " + TOP);
            System.out.println("3 - Clear the leaderboard");
            System.out.println("0 - Exit");
            System.out.print("> ");
            String choice = nextLine();
            if (choice == null || choice.strip().equals("0")) {
                System.out.println("Bye!");
                return;
            }
            switch (choice.strip()) {
                case "1" -> addScore();
                case "2" -> printTop();
                case "3" -> clear();
                default -> System.out.println("No such menu item: " + choice);
            }
        }
    }

    private void addScore() {
        System.out.print("Name: ");
        String name = nextLine();
        if (name == null) {
            return;
        }
        if (!Leaderboard.isValidName(name)) {
            System.out.println("The name must be 1-" + Leaderboard.MAX_NAME_LENGTH + " characters.");
            return;
        }
        System.out.print("Score: ");
        String scoreText = nextLine();
        if (scoreText == null) {
            return;
        }
        int score;
        try {
            score = Integer.parseInt(scoreText.strip());
        } catch (NumberFormatException e) {
            System.out.println("The score must be a whole number: " + scoreText);
            return;
        }
        try {
            leaderboard.save(name, score);
            System.out.println("Saved: " + name.strip() + ", " + score + " points.");
        } catch (StorageException e) {
            printError(e);
        }
    }

    private void printTop() {
        try {
            List<HighScoreDto> top = leaderboard.top(TOP);
            if (top.isEmpty()) {
                System.out.println("No scores yet.");
                return;
            }
            System.out.println("Top " + TOP + ":");
            for (HighScoreDto highScore : top) {
                System.out.printf("%2d. %-12s %4d   %s%n", highScore.rank(), highScore.name(),
                        highScore.score(), highScore.createdAt().format(DATE_FORMAT));
            }
        } catch (StorageException e) {
            printError(e);
        }
    }

    private void clear() {
        System.out.print("Delete all scores? (yes/no): ");
        String answer = nextLine();
        if (answer == null || !answer.strip().equalsIgnoreCase("yes")) {
            System.out.println("Nothing changed.");
            return;
        }
        try {
            leaderboard.clear();
            System.out.println("The leaderboard is empty.");
        } catch (StorageException e) {
            printError(e);
        }
    }

    /**
     * @return a következő sor, vagy null, ha elfogyott a bemenet
     */
    private String nextLine() {
        return in.hasNextLine() ? in.nextLine() : null;
    }

    private static void printError(StorageException e) {
        // A leggyakoribb okok: nem fut a MySQL-konténer, rossz
        // felhasználó/jelszó, nincs ilyen adatbázis. Az üzenet megmondja.
        System.out.println("Database error: " + e.getMessage());
    }

}
