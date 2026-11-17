package ufos;

import javax.swing.SwingUtilities;

/**
 * A program belépési pontja. KIINDULÓ változat.
 *
 * @author bli
 */
public class UFOs {

    /**
     * @param args a parancssori argumentumok (nem használjuk)
     */
    public static void main(String[] args) {
        // A felületet az eseménykezelő szálon (EDT) építjük fel.
        SwingUtilities.invokeLater(() -> new UFOGUI(10));
    }

}
