package calc;

import java.util.function.DoubleBinaryOperator;

/**
 * A számológép műveletei.
 *
 * Minden művelethez tartozik egy jel (ez kerül a gombra) és egy függvény, ami
 * a két operandusból kiszámolja az eredményt. A számolás így nem a felületen
 * történik: a GUI csak beolvassa az operandusokat, meghívja a műveletet és
 * kiírja az eredményt. Új művelethez elég egy új sort felvenni ide, a gombja
 * magától megjelenik a felületen.
 *
 * @author bli
 */
public enum Operation {

    ADD("+", (a, b) -> a + b),
    SUBTRACT("-", (a, b) -> a - b),
    MULTIPLY("*", (a, b) -> a * b),
    DIVIDE("/", (a, b) -> {
        // Lebegőpontos számoknál a nullával osztás nem dob kivételt, hanem
        // végtelent vagy NaN-t ad, ezért ezt nekünk kell kiszűrnünk.
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }),
    POWER("^", Math::pow);

    private final String symbol;
    private final DoubleBinaryOperator function;

    Operation(String symbol, DoubleBinaryOperator function) {
        this.symbol = symbol;
        this.function = function;
    }

    /**
     * @return a művelet jele, ez a felirat kerül a gombra
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Elvégzi a műveletet.
     *
     * @param a az első operandus
     * @param b a második operandus
     * @return a művelet eredménye
     * @throws ArithmeticException ha az eredmény nem véges valós szám, pl.
     * nullával osztásnál, negatív szám törtkitevős hatványánál vagy
     * túlcsordulásnál
     */
    public double apply(double a, double b) {
        double result = function.applyAsDouble(a, b);
        if (!Double.isFinite(result)) {
            throw new ArithmeticException("The result is not a finite number");
        }
        return result;
    }

}
