package calc.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * A modell tesztjei: előzmények, memória és a változások jelzése. A modell
 * nem használ Swinget, ezért felület nélkül, közvetlenül tesztelhető.
 */
public class CalculatorModelTest {

    private CalculatorModel model;
    /** Hányszor jelzett változást a modell. */
    private int changes;

    @Before
    public void setUp() {
        model = new CalculatorModel();
        changes = 0;
        model.addChangeListener(() -> changes++);
    }

    @Test
    public void calculationReturnsTheResult() {
        Calculation calculation = model.calculate(6, Operation.MULTIPLY, 7);
        assertEquals(42, calculation.result(), 0);
    }

    @Test
    public void historyStartsEmpty() {
        assertTrue(model.getHistory().isEmpty());
    }

    @Test
    public void newestCalculationIsFirstInHistory() {
        model.calculate(1, Operation.ADD, 1);
        model.calculate(2, Operation.ADD, 2);
        List<Calculation> history = model.getHistory();
        assertEquals(2, history.size());
        // A record equals metódusa mezőnként hasonlít össze.
        assertEquals(new Calculation(2, Operation.ADD, 2, 4), history.get(0));
        assertEquals(new Calculation(1, Operation.ADD, 1, 2), history.get(1));
    }

    @Test
    public void historyIsLimited() {
        for (int i = 0; i < CalculatorModel.HISTORY_LIMIT + 5; i++) {
            model.calculate(i, Operation.ADD, 0);
        }
        List<Calculation> history = model.getHistory();
        assertEquals(CalculatorModel.HISTORY_LIMIT, history.size());
        // A legrégebbiek (0..4) kikerültek, a legrégebbi megmaradt az 5.
        assertEquals(5, history.get(history.size() - 1).left(), 0);
    }

    @Test
    public void failedCalculationIsNotStoredAndNotSignalled() {
        assertThrows(ArithmeticException.class, () -> model.calculate(1, Operation.DIVIDE, 0));
        assertTrue(model.getHistory().isEmpty());
        assertEquals(0, changes);
    }

    @Test
    public void historyCannotBeModifiedFromOutside() {
        model.calculate(1, Operation.ADD, 1);
        assertThrows(UnsupportedOperationException.class, () -> model.getHistory().clear());
        assertEquals(1, model.getHistory().size());
    }

    @Test
    public void clearHistory() {
        model.calculate(1, Operation.ADD, 1);
        model.clearHistory();
        assertTrue(model.getHistory().isEmpty());
    }

    @Test
    public void memoryAddSubtractAndClear() {
        assertFalse(model.hasMemory());
        model.addToMemory(10);
        model.addToMemory(2.5);
        model.subtractFromMemory(0.5);
        assertEquals(12, model.getMemory(), 0);
        assertTrue(model.hasMemory());

        model.clearMemory();
        assertEquals(0, model.getMemory(), 0);
        assertFalse(model.hasMemory());
    }

    @Test
    public void everyChangeIsSignalled() {
        model.calculate(1, Operation.ADD, 1);
        model.addToMemory(1);
        model.subtractFromMemory(1);
        model.clearMemory();
        model.clearHistory();
        assertEquals(5, changes);
    }

}
