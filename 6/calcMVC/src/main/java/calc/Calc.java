package calc;

import calc.controller.CalculatorController;
import calc.model.CalculatorModel;
import calc.view.CalculatorView;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * A program belépési pontja: létrehozza és összekapcsolja a modellt, a
 * nézetet és a vezérlőt.
 *
 * @author bli
 */
public class Calc {

    /**
     * Elindítja a programot: létrehozza a három MVC-részt, összeköti őket,
     * és megjeleníti az ablakot.
     *
     * Felület: itt jelenik meg a számológép ablaka. A sorrend számít: a
     * modell és a nézet független egymástól, ezért bármilyen sorrendben
     * létrejöhetnek; a vezérlőnek viszont mindkettő kell, ezért ő jön
     * utoljára, és csak a bekötés után jelenítjük meg az ablakot.
     *
     * @param args a parancssori argumentumok (nem használjuk)
     */
    public static void main(String[] args) {
        // A felületet az eseménykezelő szálon (EDT) építjük fel.
        SwingUtilities.invokeLater(() -> {
            useSystemLookAndFeel();
            // M: a számolás szabályai és az adatok – nem tud a felületről
            CalculatorModel model = new CalculatorModel();
            // V: az ablak és a komponensei – nem tud a modellről
            CalculatorView view = new CalculatorView();
            // C: összeköti a kettőt (a referenciát nem kell eltárolnunk, a
            // nézet eseménykezelői és a modell feliratkozója hivatkoznak rá)
            new CalculatorController(model, view);
            // Csak akkor jelenítjük meg az ablakot, amikor már minden
            // eseménykezelő a helyén van.
            view.show();
        });
    }

    /**
     * Az operációs rendszer saját kinézetét (look and feel) használjuk, így
     * a program Windowson Windows-os, macOS-en macOS-es lesz. Ha ez nem
     * sikerül, marad a Swing alapértelmezett (Metal) kinézete – ezért nem
     * hiba, ha nem sikerül, csak figyelmeztetünk.
     *
     * Felület: a gombok, mezők, menük a rendszer stílusában jelennek meg.
     * Még a nézet létrehozása előtt kell hívni, különben a már elkészült
     * komponensek a régi kinézettel maradnak.
     */
    private static void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            System.err.println("Could not set the system look and feel: " + ex.getMessage());
        }
    }

}
