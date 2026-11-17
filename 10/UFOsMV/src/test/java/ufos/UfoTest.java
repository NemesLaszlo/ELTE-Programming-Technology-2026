package ufos;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Egy UFO mozgásának tesztjei. Nincs bennük Swing: a "túloldalon jön
 * vissza" szabályt ablak nélkül ellenőrizzük. A UFOs (kiinduló) projektben
 * ez nem lenne megírható, mert ott az UFO java.awt.Color-t tárol és maga
 * rajzolja ki magát.
 */
public class UfoTest {

    private static final double EPSILON = 1e-9;

    @Test
    public void moveAddsTheVelocity() {
        Ufo ufo = new Ufo(10, 20, 1.5, -0.5, 0xFF0000);

        ufo.move(400, 300);

        assertEquals(11.5, ufo.getX(), EPSILON);
        assertEquals(19.5, ufo.getY(), EPSILON);
        assertEquals(0xFF0000, ufo.getRgb());
    }

    @Test
    public void leavingOnTheRightComesBackOnTheLeft() {
        Ufo ufo = new Ufo(399.5, 100, 1, 0, 0);

        ufo.move(400, 300);

        assertEquals(0, ufo.getX(), EPSILON);
        assertEquals(100, ufo.getY(), EPSILON);
    }

    @Test
    public void leavingOnTheLeftComesBackOnTheRight() {
        Ufo ufo = new Ufo(0.5, 100, -1, 0, 0);

        ufo.move(400, 300);

        assertEquals(400, ufo.getX(), EPSILON);
    }

    @Test
    public void leavingAtTheBottomComesBackAtTheTop() {
        Ufo ufo = new Ufo(100, 299.5, 0, 1, 0);

        ufo.move(400, 300);

        assertEquals(0, ufo.getY(), EPSILON);
    }

    @Test
    public void leavingAtTheTopComesBackAtTheBottom() {
        Ufo ufo = new Ufo(100, 0.5, 0, -1, 0);

        ufo.move(400, 300);

        assertEquals(300, ufo.getY(), EPSILON);
    }

    @Test
    public void moveToSetsThePositionButKeepsTheVelocity() {
        Ufo ufo = new Ufo(10, 20, 1, 2, 0);

        ufo.moveTo(50, 60);

        assertEquals(50, ufo.getX(), EPSILON);
        assertEquals(60, ufo.getY(), EPSILON);
        assertEquals(1, ufo.getVelX(), EPSILON);
        assertEquals(2, ufo.getVelY(), EPSILON);
    }

}
