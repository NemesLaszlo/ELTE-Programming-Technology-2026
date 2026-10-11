package calc;

import javax.swing.SwingUtilities;

/**
 * A program belépési pontja.
 *
 * @author bli
 */
public class Calc {

    /** A szövegmezők szélessége oszlopokban (nagyjából karakterszám). */
    private static final int FIELD_WIDTH = 20;

    /**
     * @param args a parancssori argumentumok (nem használjuk)
     */
    public static void main(String[] args) {
        // A Swing nem szálbiztos: a felületet az eseménykezelő szálon (Event
        // Dispatch Thread, EDT) kell felépíteni. Az invokeLater a lambdát
        // ezen a szálon futtatja le, amint sorra kerül.
        SwingUtilities.invokeLater(() -> new CalcGUI(FIELD_WIDTH));
    }

}
