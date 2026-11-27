package diver;

import diver.db.JdbcHighScoreRepository;
import diver.model.Leaderboard;
import diver.view.DiverGUI;
import javax.swing.SwingUtilities;

/**
 * A program belépési pontja. Itt kötjük össze a rétegeket: a nézet a
 * backendet (Leaderboard) kapja, a backend a JDBC-s tárolót. A nézet így
 * nem tud a tárolás módjáról; ha más tárolót akarunk, csak ez a sor változik.
 */
public class Diver {

    /**
     * @param args a parancssori argumentumok (nem használjuk)
     */
    public static void main(String[] args) {
        // A felületet az eseménykezelő szálon (EDT) építjük fel.
        Leaderboard leaderboard = new Leaderboard(new JdbcHighScoreRepository());
        SwingUtilities.invokeLater(() -> new DiverGUI(leaderboard));
    }

}
