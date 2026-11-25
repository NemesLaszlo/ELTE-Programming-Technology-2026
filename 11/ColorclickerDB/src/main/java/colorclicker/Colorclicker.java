package colorclicker;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
        useCrossPlatformLookAndFeel();
        // A felületet az eseménykezelő szálon (EDT) építjük fel.
        Leaderboard leaderboard = new Leaderboard(new JdbcHighScoreRepository());
        SwingUtilities.invokeLater(() -> new ColorClickerGUI(leaderboard));
    }

    /**
     * Beállítja a Swing saját, minden rendszeren egyforma kinézetét (Metal).
     *
     * Windowson és Linuxon ez az alapértelmezés, macOS-en viszont a
     * rendszer kinézete (Aqua) az, és az a gombokat maga rajzolja ki: a
     * setBackground színét nem használja, a tábla fehér maradna. Az első
     * komponens létrehozása ELŐTT kell hívni.
     */
    private static void useCrossPlatformLookAndFeel() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ReflectiveOperationException
                | UnsupportedLookAndFeelException e) {
            // A Metal a JDK része, ez nem fordulhat elő; ha mégis, marad a
            // rendszer kinézete, a játék attól még játszható.
            System.err.println("A kinézet beállítása nem sikerült: " + e);
        }
    }

}
