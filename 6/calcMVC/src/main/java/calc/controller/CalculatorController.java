package calc.controller;

import calc.model.Calculation;
import calc.model.CalculatorModel;
import calc.model.Operation;
import calc.view.CalculatorView;
import calc.view.CalculatorView.Operand;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * A számológép VEZÉRLŐJE (az MVC "C" betűje): összeköti a nézetet és a
 * modellt.
 *
 * Két irányban dolgozik:
 * <ul>
 * <li>felhasználói esemény → modell: beolvassa a nézetből a szövegeket,
 * számmá alakítja őket, meghívja a modellt, és kezeli a hibákat;</li>
 * <li>modell → nézet: feliratkozik a modell változásaira, és újrarajzolja az
 * előzményeket és a memóriát.</li>
 * </ul>
 *
 * A vezérlő nem számol: a számolás szabályai a modellben vannak, a vezérlő
 * csak közvetít. A szöveg és a szám közötti átalakítás (beolvasás, formázás)
 * viszont itt van: a modell számokkal dolgozik, a nézet szövegekkel, a kettő
 * közötti fordítás a vezérlő dolga.
 *
 * A metódusok nagy része eseménykezelő: a konstruktor köti be őket a nézet
 * egy-egy gombjához, menüpontjához. A táblázat a README-ben is megvan
 * ("Esemény – eseménykezelő párosítások").
 *
 * @author bli
 */
public class CalculatorController {

    /** A kiírt számok pontossága (értékes jegyek száma). */
    private static final MathContext DISPLAY_PRECISION = new MathContext(12);

    private final CalculatorModel model;
    private final CalculatorView view;

    /** Az Enter ezt a műveletet ismétli meg. */
    private Operation lastOperation = Operation.ADD;

    /**
     * A kijelzett eredmény. Double (és nem double) típusú, mert így a null
     * jelentheti azt, hogy nincs eredmény (pl. törlés vagy hiba után).
     */
    private Double currentResult;

    /**
     * Összeköti a nézetet és a modellt.
     *
     * Felület: nem hoz létre semmit (azt a nézet teszi), hanem "életet lehel"
     * a felületbe: minden gombhoz, menüponthoz hozzárendeli, melyik
     * metódus fusson a megnyomásakor. A végén kirajzolja a modell kezdő
     * állapotát (üres előzmények, üres memória), és kiír egy üdvözlő
     * üzenetet az állapotsorba.
     *
     * MVC: a Calc hozza létre, a modell és a nézet létrehozása után.
     *
     * @param model a modell
     * @param view a nézet
     */
    public CalculatorController(CalculatorModel model, CalculatorView view) {
        this.model = model;
        this.view = view;

        // Felhasználói esemény -> a vezérlő vagy a modell egy metódusa.
        //
        // A "this::calculate" metódusreferencia: nem hívja meg a metódust,
        // hanem átadja a nézetnek, hogy majd a gomb megnyomásakor hívja meg.
        // Ugyanaz, mint az "operation -> calculate(operation)" lambda.
        view.onOperation(this::calculate);
        // Lambda: ha a hívandó kódnak más paraméter kell, vagy több utasítás.
        view.onEnterInOperand(() -> calculate(lastOperation));
        view.onCopyResult(this::copyResult);
        view.onUseResult(this::useResult);
        view.onSwap(this::swapOperands);
        view.onClear(this::clear);
        view.onHistorySelected(this::loadFromHistory);
        view.onMemoryRecall(this::recallMemory);
        view.onMemoryAdd(this::addResultToMemory);
        view.onMemorySubtract(this::subtractResultFromMemory);
        // Nem csak a saját (this) metódusunk adható át, hanem bármelyik
        // objektumé: itt egyenesen a modell, illetve a nézet metódusa fut le.
        view.onClearHistory(model::clearHistory);
        view.onMemoryClear(model::clearMemory);
        view.onExit(view::close);

        // A modell változása -> a nézet frissítése.
        model.addChangeListener(this::render);
        render();
        view.showStatus("Enter two numbers and choose an operation.");
    }

    // ==================================================================
    // Eseménykezelők: a nézet hívja őket, ha a felhasználó csinál valamit.
    // ==================================================================

    /**
     * Elvégez egy számolást a két mező tartalmával.
     *
     * Lépései: kiolvassa a két mezőt a nézetből, számmá alakítja őket,
     * a modellel elvégezteti a számolást, és kiírja az eredményt.
     *
     * Felület: siker esetén a Result mezőben megjelenik az eredmény, az
     * állapotsorban a teljes számolás (pl. "6 × 7 = 42"), az előzmények
     * listája pedig a modell értesítése nyomán (render) bővül egy sorral.
     * Hiba esetén a Result mező kiürül, és az állapotsorban piros
     * hibaüzenet jelenik meg; ha a hibát egy mező okozta, az piros hátteret
     * kap.
     *
     * MVC: eseménykezelő – egy műveleti gomb (+ − × ÷ ^ mod), vagy Enter
     * az A / B mezőben.
     *
     * @param operation a választott művelet
     */
    private void calculate(Operation operation) {
        lastOperation = operation;
        double left;
        double right;
        try {
            left = parse(view.getOperandText(Operand.A));
        } catch (NumberFormatException ex) {
            showInvalidOperand(Operand.A);
            return;
        }
        try {
            right = parse(view.getOperandText(Operand.B));
        } catch (NumberFormatException ex) {
            showInvalidOperand(Operand.B);
            return;
        }
        try {
            // A számolást a modell végzi, nem a vezérlő.
            Calculation calculation = model.calculate(left, operation, right);
            showResult(calculation.result());
            view.showStatus(describe(calculation));
        } catch (ArithmeticException ex) {
            // pl. nullával osztás: a modell dobja, a vezérlő jelzi a felületen
            showResult(null);
            view.showError(null, ex.getMessage());
        }
    }

    /**
     * A vágólapra másolja az eredményt.
     *
     * Felület: az állapotsorban megjelenik, mi került a vágólapra; ha nincs
     * eredmény, hibaüzenet.
     *
     * MVC: eseménykezelő – Edit → Copy result (Ctrl+Shift+C). A modellt nem
     * érinti.
     */
    private void copyResult() {
        if (currentResult == null) {
            view.showError(null, "There is no result to copy");
        } else if (view.copyToClipboard(format(currentResult))) {
            view.showStatus("Copied to clipboard: " + format(currentResult));
        } else {
            view.showError(null, "The clipboard is not available, please try again");
        }
    }

    /**
     * Az eredményt átírja az A mezőbe, így láncolhatók a műveletek
     * (pl. 2 + 3 = 5, majd 5 × 4 = 20).
     *
     * Felület: az A mezőbe az eredmény kerül, a B és a Result mező kiürül,
     * a kurzor a B mezőbe ugrik.
     *
     * MVC: eseménykezelő – Edit → Use result as A (Ctrl+R). A modellt nem
     * érinti.
     */
    private void useResult() {
        if (currentResult == null) {
            view.showError(null, "There is no result to use");
            return;
        }
        view.setOperandText(Operand.A, format(currentResult));
        view.setOperandText(Operand.B, "");
        showResult(null);
        view.showStatus("The result was moved to A.");
        view.focusOperand(Operand.B);
    }

    /**
     * Felcseréli a két operandust.
     *
     * Felület: az A és a B mező tartalma helyet cserél.
     *
     * MVC: eseménykezelő – Edit → Swap A and B (Ctrl+Shift+S). A modellt nem
     * érinti.
     */
    private void swapOperands() {
        String a = view.getOperandText(Operand.A);
        view.setOperandText(Operand.A, view.getOperandText(Operand.B));
        view.setOperandText(Operand.B, a);
        view.showStatus("A and B were swapped.");
    }

    /**
     * Kiüríti a mezőket.
     *
     * Felület: az A, a B és a Result mező üres lesz, a kurzor az A mezőbe
     * ugrik. Az előzmények és a memória megmaradnak.
     *
     * MVC: eseménykezelő – Edit → Clear (Ctrl+L). A modellt nem érinti.
     */
    private void clear() {
        view.setOperandText(Operand.A, "");
        view.setOperandText(Operand.B, "");
        showResult(null);
        view.showStatus("Cleared.");
        view.focusOperand(Operand.A);
    }

    /**
     * Visszatölt egy korábbi számolást a mezőkbe.
     *
     * Felület: az A, a B és a Result mezőbe a kiválasztott számolás adatai
     * kerülnek; az Enter ezután ennek a számolásnak a műveletét ismétli.
     *
     * MVC: eseménykezelő – dupla kattintás vagy Enter az előzmények egy
     * során. A számolás adatait a modelltől kérdezi le (getHistory).
     *
     * @param index a számolás helye az előzmények között
     */
    private void loadFromHistory(int index) {
        Calculation calculation = model.getHistory().get(index);
        view.setOperandText(Operand.A, format(calculation.left()));
        view.setOperandText(Operand.B, format(calculation.right()));
        showResult(calculation.result());
        lastOperation = calculation.operation();
        view.showStatus("Loaded: " + describe(calculation));
    }

    /**
     * Beírja a memória tartalmát abba az operandusmezőbe, amelyik utoljára
     * fókuszban volt.
     *
     * Felület: az A vagy a B mezőbe a memória értéke kerül, és a kurzor oda
     * ugrik.
     *
     * MVC: eseménykezelő – MR gomb. A memória értékét a modelltől kérdezi le.
     */
    private void recallMemory() {
        Operand target = view.getActiveOperand();
        view.setOperandText(target, format(model.getMemory()));
        view.showStatus("Memory recalled into " + target + ".");
        view.focusOperand(target);
    }

    /**
     * Hozzáadja a kijelzett eredményt a memóriához.
     *
     * Felület: az állapotsor jobb oldalán frissül az "M = ..." felirat – de
     * nem itt, hanem a modell értesítése nyomán, a render() metódusban.
     *
     * MVC: eseménykezelő – M+ gomb. A memória a modellben van, ezért a
     * modell addToMemory metódusát hívja.
     */
    private void addResultToMemory() {
        if (currentResult == null) {
            view.showError(null, "There is no result to store in memory");
            return;
        }
        model.addToMemory(currentResult);
        view.showStatus("Added to memory.");
    }

    /**
     * Kivonja a kijelzett eredményt a memóriából.
     *
     * Felület: frissül az "M = ..." felirat (a render() metódusban).
     *
     * MVC: eseménykezelő – M− gomb; a modell subtractFromMemory metódusát
     * hívja.
     */
    private void subtractResultFromMemory() {
        if (currentResult == null) {
            view.showError(null, "There is no result to store in memory");
            return;
        }
        model.subtractFromMemory(currentResult);
        view.showStatus("Subtracted from memory.");
    }

    // ==================================================================
    // A modell -> nézet irány: a modell változásakor fut.
    // ==================================================================

    /**
     * A nézet modellből származó részeit rajzolja újra: az előzményeket és a
     * memóriát.
     *
     * Felület: a History lista a modell előzményeit mutatja (a legújabb
     * felül), az állapotsor jobb oldala a memóriát.
     *
     * MVC: a modell változásjelzésére fut (model.addChangeListener), tehát
     * minden számolás, előzménytörlés és memóriaművelet után – akárhonnan
     * is jött a változás. Ezért nem kell az egyes eseménykezelőkben külön
     * frissíteni a listát.
     */
    private void render() {
        List<String> lines = new ArrayList<>();
        for (Calculation calculation : model.getHistory()) {
            lines.add(describe(calculation));
        }
        view.showHistory(lines);
        view.showMemory(model.hasMemory() ? format(model.getMemory()) : null);
    }

    // ==================================================================
    // Segédmetódusok
    // ==================================================================

    /**
     * Hibás operandust jelez.
     *
     * Felület: a Result mező kiürül, a hibás mező piros hátteret kap, az
     * állapotsorban megjelenik a hibaüzenet.
     *
     * MVC: vezérlő, a calculate hívja, ha egy mező nem szám.
     */
    private void showInvalidOperand(Operand operand) {
        showResult(null);
        view.showError(operand, "Operand " + operand + " is not a valid number");
    }

    /**
     * Kiírja az eredményt, és megjegyzi (a másoláshoz, az M+-hoz stb.);
     * null esetén törli.
     *
     * Felület: megváltozik a Result mező tartalma.
     */
    private void showResult(Double result) {
        currentResult = result;
        view.showResult(result == null ? "" : format(result));
    }

    /**
     * Számmá alakítja a beírt szöveget. A tizedesvesszőt is elfogadjuk, mert
     * magyar billentyűzeten jellemzően vesszőt írunk.
     *
     * MVC: a nézet szövegét alakítja a modell számává (nézet → modell
     * irányú fordítás).
     *
     * @throws NumberFormatException ha a szöveg nem szám (az üres szöveg sem)
     */
    private static double parse(String text) {
        double value = Double.parseDouble(text.trim().replace(',', '.'));
        // A parseDouble a "NaN" és az "Infinity" szöveget is elfogadja.
        if (!Double.isFinite(value)) {
            throw new NumberFormatException("Not a finite number: " + text);
        }
        return value;
    }

    /**
     * Szöveggé alakítja a számot, 12 értékes jegyre kerekítve.
     *
     * A double kettes számrendszerben tárol, ezért pl. a 0.1 nem ábrázolható
     * pontosan, és 0.1 + 0.2 eredménye 0.30000000000000004. A kerekítés miatt
     * ez a kijelzőn 0.3 lesz. Az egész számok ".0" nélkül jelennek meg, a
     * nagyon nagyok normálalakban (pl. 1E+20).
     *
     * MVC: a modell számát alakítja a nézet szövegévé (modell → nézet
     * irányú fordítás).
     */
    private static String format(double value) {
        BigDecimal rounded = BigDecimal.valueOf(value).round(DISPLAY_PRECISION).stripTrailingZeros();
        return Math.abs(value) < 1e15 ? rounded.toPlainString() : rounded.toString();
    }

    /**
     * Egy számolás olvasható alakja; így jelenik meg az előzmények listájában
     * és az állapotsorban.
     *
     * @return pl. "6 × 7 = 42"
     */
    private static String describe(Calculation calculation) {
        return format(calculation.left()) + " " + calculation.operation().getSymbol() + " "
                + format(calculation.right()) + " = " + format(calculation.result());
    }

}
