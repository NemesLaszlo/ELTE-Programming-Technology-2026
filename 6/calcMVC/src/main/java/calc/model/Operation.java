package calc.model;

/**
 * A számológép műveletei – a modell része.
 *
 * Minden művelethez tartozik egy jel, ez kerül a gombra. A nézet a gombokat
 * ebből a felsorolásból hozza létre, így új művelethez elég egy új értéket
 * felvenni ide, és kiegészíteni az apply metódust: a felületen magától
 * megjelenik az új gomb.
 *
 * @author bli
 */
public enum Operation {

    ADD("+"),
    SUBTRACT("−"),
    MULTIPLY("×"),
    DIVIDE("÷"),
    POWER("^"),
    MODULO("mod");

    private final String symbol;

    /**
     * Az enum konstruktora: minden értéknél (ADD, SUBTRACT, ...) egyszer
     * lefut, a zárójelben megadott jellel.
     */
    Operation(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Megadja a művelet jelét.
     *
     * MVC: a nézet ebből írja a gombok feliratát, a vezérlő pedig ezzel
     * írja ki a számolásokat (pl. "6 × 7 = 42").
     *
     * @return a művelet jele, pl. "+"
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Elvégzi a műveletet – ez a számológép tényleges "tudása".
     *
     * MVC: a modell (CalculatorModel.calculate) hívja; a felülethez nincs
     * köze, ezért a tesztek közvetlenül is meghívhatják.
     *
     * @param left a bal oldali operandus
     * @param right a jobb oldali operandus
     * @return a művelet eredménye
     * @throws ArithmeticException nullával osztásnál, vagy ha az eredmény nem
     * véges szám
     */
    public double apply(double left, double right) {
        // double-nél a nullával osztás nem dob kivételt, hanem végtelen vagy
        // NaN lesz az eredmény, ezért ezt magunk ellenőrizzük.
        if ((this == DIVIDE || this == MODULO) && right == 0) {
            throw new ArithmeticException("Division by zero");
        }
        // switch kifejezés: minden ág egy értéket ad vissza, és a fordító
        // szól, ha egy műveletet kifelejtettünk.
        double result = switch (this) {
            case ADD -> left + right;
            case SUBTRACT -> left - right;
            case MULTIPLY -> left * right;
            case DIVIDE -> left / right;
            case POWER -> Math.pow(left, right);
            case MODULO -> left % right;
        };
        // pl. (-8) ^ 0.5 eredménye NaN, egy túl nagy hatványé végtelen
        if (!Double.isFinite(result)) {
            throw new ArithmeticException("The result is not a finite number");
        }
        return result;
    }

}
