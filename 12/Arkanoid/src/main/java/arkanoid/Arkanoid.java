package arkanoid;

import javax.swing.SwingUtilities;

/**
 * A program belépési pontja.
 *
 * @author bli
 */
public class Arkanoid {

    /**
     * @param args a parancssori argumentumok (nem használjuk)
     */
    public static void main(String[] args) {
        // A felületet az eseménykezelő szálon (EDT) építjük fel.
        SwingUtilities.invokeLater(() -> new ArkanoidGUI());
    }

}
