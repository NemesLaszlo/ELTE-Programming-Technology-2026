package calc.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

/**
 * A műveletek tesztjei.
 */
public class OperationTest {

    /** Lebegőpontos számoknál ekkora eltérést még egyezésnek tekintünk. */
    private static final double DELTA = 1e-9;

    @Test
    public void basicOperations() {
        assertEquals(5, Operation.ADD.apply(2, 3), DELTA);
        assertEquals(-1, Operation.SUBTRACT.apply(2, 3), DELTA);
        assertEquals(42, Operation.MULTIPLY.apply(6, 7), DELTA);
        assertEquals(3.5, Operation.DIVIDE.apply(7, 2), DELTA);
        assertEquals(1024, Operation.POWER.apply(2, 10), DELTA);
        assertEquals(1, Operation.MODULO.apply(7, 3), DELTA);
    }

    @Test
    public void powerWithNegativeAndFractionalExponents() {
        assertEquals(0.125, Operation.POWER.apply(2, -3), DELTA);
        assertEquals(3, Operation.POWER.apply(9, 0.5), DELTA);
    }

    @Test
    public void divisionByZeroIsAnError() {
        ArithmeticException ex = assertThrows(ArithmeticException.class, () -> Operation.DIVIDE.apply(1, 0));
        assertEquals("Division by zero", ex.getMessage());
        assertThrows(ArithmeticException.class, () -> Operation.MODULO.apply(1, 0));
    }

    @Test
    public void resultMustBeAFiniteNumber() {
        // negatív szám törtkitevős hatványa: NaN
        assertThrows(ArithmeticException.class, () -> Operation.POWER.apply(-8, 0.5));
        // túl nagy szám: végtelen
        assertThrows(ArithmeticException.class, () -> Operation.POWER.apply(10, 1000));
    }

}
