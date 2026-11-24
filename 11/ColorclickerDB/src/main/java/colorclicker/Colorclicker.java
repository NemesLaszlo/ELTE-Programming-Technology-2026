package colorclicker;

import javax.swing.SwingUtilities;

/**
 * A program belépési pontja. Itt kötjük össze a rétegeket: a nézet a
 * backendet (Leaderboard) kapja, a backend a JDBC-s tárolót.
 *
 * @author pinter
 */
public class Colorclicker {

    /**
     * @param args a parancssori argumentumok (nem használjuk)
     */
    public static void main(String[] args) {
        // A felületet az eseménykezelő szálon (EDT) építjük fel.
        Leaderboard leaderboard = new Leaderboard(new JdbcHighScoreRepository());
        SwingUtilities.invokeLater(() -> new ColorClickerGUI(leaderboard));
    }

}
