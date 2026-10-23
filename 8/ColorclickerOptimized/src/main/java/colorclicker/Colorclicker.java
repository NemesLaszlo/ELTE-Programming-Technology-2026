package colorclicker;

import javax.swing.SwingUtilities;

/**
 * A program belépési pontja.
 *
 * @author pinter
 */
public class Colorclicker {

    /**
     * @param args a parancssori argumentumok (nem használjuk)
     */
    public static void main(String[] args) {
        // A felületet az eseménykezelő szálon (EDT) építjük fel.
        SwingUtilities.invokeLater(() -> new ColorClickerGUI());
    }

}
