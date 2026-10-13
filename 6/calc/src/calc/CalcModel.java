package calc;

/**
 * A számológép MODELLJE (az MV "M" betűje): elvégzi a számolást, és tárolja
 * az utolsó eredményt.
 *
 * A modell semmit nem tud a felületről, nem használ Swinget. A GUI
 * eseménykezelői hívják a metódusait, majd a GUI a modell állapota alapján
 * frissíti magát (ld. CalcGUI.refresh). A számolás szabályai az Operation
 * enumban vannak, a modell ezeket használja.
 *
 * @author bli
 */
public class CalcModel {

    /**
     * Az utolsó sikeres számolás eredménye. Double (és nem double) típusú,
     * mert így a null jelentheti azt, hogy nincs eredmény (pl. törlés vagy
     * hiba után).
     */
    private Double result;

    /**
     * Elvégez egy számolást, és eltárolja az eredményt. Sikertelen számolás
     * után nincs eredmény: ne maradjon meg egy korábbi, már nem érvényes
     * érték.
     *
     * @param a az első operandus
     * @param operation a művelet
     * @param b a második operandus
     * @throws ArithmeticException ha a művelet nem végezhető el, pl. nullával
     * osztásnál
     */
    public void calculate(double a, Operation operation, double b) {
        result = null;
        result = operation.apply(a, b);
    }

    /**
     * @return igaz, ha van eredmény
     */
    public boolean hasResult() {
        return result != null;
    }

    /**
     * @return az utolsó eredmény
     * @throws IllegalStateException ha nincs eredmény
     */
    public double getResult() {
        if (result == null) {
            throw new IllegalStateException("There is no result");
        }
        return result;
    }

    /**
     * Törli az eredményt.
     */
    public void clearResult() {
        result = null;
    }

}
