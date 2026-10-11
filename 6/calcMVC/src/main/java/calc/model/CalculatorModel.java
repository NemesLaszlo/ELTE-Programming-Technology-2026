package calc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A számológép MODELLJE (az MVC "M" betűje): elvégzi a számolásokat, és
 * tárolja az előzményeket és a memóriát.
 *
 * A modell semmit nem tud a felületről, nem használ Swinget, és semmit nem
 * rajzol ki. Ha megváltozik az állapota, értesíti a feliratkozókat (observer
 * minta), és azok döntik el, mit kezdenek ezzel – a vezérlő pl. frissíti a
 * nézetet. Így a modell önmagában, felület nélkül is tesztelhető.
 *
 * A metódusait a vezérlő hívja (egy gomb vagy menüpont megnyomásakor), a
 * tesztekben pedig közvetlenül a tesztek.
 *
 * @author bli
 */
public class CalculatorModel {

    /** Legfeljebb ennyi számolást őrzünk meg az előzményekben. */
    public static final int HISTORY_LIMIT = 50;

    /** Az elvégzett számolások, a legújabb elöl. */
    private final List<Calculation> history = new ArrayList<>();
    /** A feliratkozók: minden változás után lefuttatjuk őket. */
    private final List<Runnable> listeners = new ArrayList<>();
    /** A memória (MC, MR, M+, M−) tartalma. */
    private double memory = 0;

    /**
     * Feliratkozás a modell változásaira: a listener minden változás (új
     * számolás, az előzmények törlése, a memória módosítása) után lefut.
     *
     * Ez az observer (megfigyelő) minta: aki kíváncsi a változásokra,
     * feliratkozik, a modell pedig minden változás után szól neki. A
     * listener egy Runnable, azaz egy darab kód, amit a modell most csak
     * eltárol, és majd a fireChanged futtat le.
     *
     * A vezérlő így iratkozik fel: model.addChangeListener(this::render).
     * Például ha a felhasználó megnyomja az M+ gombot:
     *   1. a vezérlő meghívja a modell addToMemory(5) metódusát,
     *   2. a modell eltárolja az 5-öt, és meghívja a fireChanged()-et,
     *   3. a fireChanged lefuttatja a feliratkozót: a vezérlő render()-jét,
     *   4. a render kiírja a nézetbe: "M = 5".
     *
     * A modell csak annyit mond: "megváltoztam" – nem tudja, ki iratkozott
     * fel, ezért nem függ a vezérlőtől és a nézettől. Ugyanez a minta, mint
     * a Swing button.addActionListener(...) hívása.
     *
     * @param listener a változáskor futtatandó kód
     */
    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Elvégez egy számolást, és felveszi az előzmények elejére. Sikertelen
     * számolás nem kerül be az előzmények közé.
     *
     * MVC: a vezérlő hívja, ha a felhasználó megnyom egy műveleti gombot.
     * Sikeres számolás után jelez a feliratkozóknak, így a felületen az
     * előzmények listája egy új sorral bővül.
     *
     * @param left a bal oldali operandus
     * @param operation a művelet
     * @param right a jobb oldali operandus
     * @return az elvégzett számolás az eredménnyel
     * @throws ArithmeticException ha a művelet nem végezhető el
     */
    public Calculation calculate(double left, Operation operation, double right) {
        double result = operation.apply(left, right);
        Calculation calculation = new Calculation(left, operation, right, result);
        history.add(0, calculation);
        // Ha megtelt, a legrégebbi (a lista végén lévő) számolás kikerül.
        if (history.size() > HISTORY_LIMIT) {
            history.remove(history.size() - 1);
        }
        fireChanged();
        return calculation;
    }

    /**
     * Lekérdezi az előzményeket.
     *
     * MVC: a vezérlő hívja, amikor kirajzolja az előzmények listáját, és
     * amikor a felhasználó visszatölt egy korábbi számolást.
     *
     * @return az előzmények, a legújabb elöl. Másolatot adunk vissza, amit
     * nem lehet módosítani: a modell állapota kívülről csak a metódusain
     * keresztül változhat.
     */
    public List<Calculation> getHistory() {
        return List.copyOf(history);
    }

    /**
     * Törli az előzményeket.
     *
     * MVC: az Edit → Clear history menüpont hívja (a vezérlő közvetlenül ezt
     * a metódust köti be). A jelzés nyomán a felületen kiürül a lista.
     */
    public void clearHistory() {
        history.clear();
        fireChanged();
    }

    /**
     * Lekérdezi a memória tartalmát.
     *
     * MVC: a vezérlő hívja az MR gombnál és a memória kiírásakor.
     *
     * @return a memóriában tárolt érték (kezdetben 0)
     */
    public double getMemory() {
        return memory;
    }

    /**
     * Megadja, van-e valami a memóriában.
     *
     * MVC: a vezérlő ez alapján dönti el, megjelenjen-e a felületen az
     * "M = ..." felirat.
     *
     * @return igaz, ha a memóriában nem 0 van
     */
    public boolean hasMemory() {
        return memory != 0;
    }

    /**
     * Hozzáadja az értéket a memóriához.
     *
     * MVC: a vezérlő hívja, ha a felhasználó megnyomja az M+ gombot. A
     * jelzés nyomán a felületen frissül az "M = ..." felirat.
     *
     * @param value a hozzáadandó érték
     */
    public void addToMemory(double value) {
        memory += value;
        fireChanged();
    }

    /**
     * Kivonja az értéket a memóriából.
     *
     * MVC: a vezérlő hívja az M− gombnál. A jelzés nyomán frissül az
     * "M = ..." felirat.
     *
     * @param value a kivonandó érték
     */
    public void subtractFromMemory(double value) {
        memory -= value;
        fireChanged();
    }

    /**
     * Kinullázza a memóriát.
     *
     * MVC: az MC gomb hívja (a vezérlő közvetlenül ezt a metódust köti be).
     * A jelzés nyomán eltűnik az "M = ..." felirat.
     */
    public void clearMemory() {
        memory = 0;
        fireChanged();
    }

    /**
     * Értesíti a feliratkozókat, hogy a modell megváltozott.
     *
     * MVC: ez a modell → nézet irány kezdete. A modell csak annyit mond,
     * hogy "változtam"; azt, hogy ebből mi lesz a felületen, a vezérlő
     * render() metódusa dönti el.
     */
    private void fireChanged() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

}
