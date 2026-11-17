package ufos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

/**
 * A modell tesztjei. A terek rögzített magú Random-ot kapnak
 * (new Random(42)), így a véletlen helyek, sebességek és színek minden
 * futtatáskor ugyanazok.
 */
public class SpaceTest {

    private static final int SEED = 42;

    private Space space(int ufoCount) {
        return new Space(400, 300, ufoCount, new Random(SEED));
    }

    // ----- Az új tér -----

    @Test
    public void newSpaceHasTheRequestedUfos() {
        Space space = space(10);

        assertEquals(10, space.getUfoCount());
        assertEquals(10, space.getUfos().size());
        assertEquals(400, space.getWidth());
        assertEquals(300, space.getHeight());
        assertEquals(0xFFFFFF, space.getBackgroundRgb());
    }

    @Test
    public void newUfosAreInsideTheSpaceWithSlowSpeed() {
        Space space = space(100);

        for (Ufo ufo : space.getUfos()) {
            assertTrue(ufo.getX() >= 0 && ufo.getX() < 400);
            assertTrue(ufo.getY() >= 0 && ufo.getY() < 300);
            assertTrue(Math.abs(ufo.getVelX()) <= Space.MAX_SPEED);
            assertTrue(Math.abs(ufo.getVelY()) <= Space.MAX_SPEED);
            assertTrue(ufo.getRgb() >= 0 && ufo.getRgb() <= 0xFFFFFF);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeUfoCountIsRejected() {
        space(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sizeMustBePositive() {
        new Space(0, 300, 1, new Random(SEED));
    }

    @Test
    public void sameSeedGivesTheSameSpace() {
        Space a = space(5);
        Space b = space(5);

        for (int i = 0; i < 5; i++) {
            assertEquals(a.getUfos().get(i).getX(), b.getUfos().get(i).getX(), 0);
            assertEquals(a.getUfos().get(i).getRgb(), b.getUfos().get(i).getRgb());
        }
    }

    // ----- Hozzáadás, elvétel -----

    @Test
    public void addUfosAppendsToTheEnd() {
        Space space = space(2);
        Ufo first = space.getUfos().get(0);

        space.addUfos(3);

        assertEquals(5, space.getUfoCount());
        assertEquals(first, space.getUfos().get(0));
    }

    @Test
    public void removeUfosTakesTheOldestOnes() {
        Space space = space(3);
        Ufo last = space.getUfos().get(2);

        assertEquals(2, space.removeUfos(2));

        assertEquals(1, space.getUfoCount());
        assertEquals(last, space.getUfos().get(0));
    }

    @Test
    public void removingMoreThanExistsEmptiesTheSpace() {
        Space space = space(3);

        assertEquals(3, space.removeUfos(1000));

        assertEquals(0, space.getUfoCount());
        assertEquals(0, space.removeUfos(1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void theUfoListCannotBeModifiedFromOutside() {
        space(1).getUfos().clear();
    }

    // ----- Lépés -----

    @Test
    public void stepMovesEveryUfoByItsVelocity() {
        Space space = space(10);
        List<double[]> before = new ArrayList<>();
        for (Ufo ufo : space.getUfos()) {
            before.add(new double[]{ufo.getX(), ufo.getY(), ufo.getVelX(), ufo.getVelY()});
        }

        space.step();

        for (int i = 0; i < 10; i++) {
            Ufo ufo = space.getUfos().get(i);
            double[] b = before.get(i);
            double expectedX = b[0] + b[2];
            double expectedY = b[1] + b[3];
            // Ha átfordult a tér szélén, a túloldalon van; különben pontosan
            // a sebességgel ment tovább.
            if (expectedX >= 0 && expectedX <= 400) {
                assertEquals(expectedX, ufo.getX(), 1e-9);
            }
            if (expectedY >= 0 && expectedY <= 300) {
                assertEquals(expectedY, ufo.getY(), 1e-9);
            }
        }
    }

    @Test
    public void ufosStayInsideTheSpaceAfterManySteps() {
        Space space = space(20);

        for (int i = 0; i < 10_000; i++) {
            space.step();
        }

        for (Ufo ufo : space.getUfos()) {
            assertTrue(ufo.getX() >= 0 && ufo.getX() <= 400);
            assertTrue(ufo.getY() >= 0 && ufo.getY() <= 300);
        }
    }

    @Test
    public void ufosOccasionallyChangeColor() {
        Space space = space(20);
        List<Integer> before = new ArrayList<>();
        for (Ufo ufo : space.getUfos()) {
            before.add(ufo.getRgb());
        }

        // 20 UFO × 1000 lépés × 0.003 ≈ 60 színváltás várható.
        for (int i = 0; i < 1000; i++) {
            space.step();
        }

        int changed = 0;
        for (int i = 0; i < 20; i++) {
            if (space.getUfos().get(i).getRgb() != before.get(i)) {
                changed++;
            }
        }
        assertTrue("at least one UFO should change its color", changed > 0);
    }

    // ----- Egér, háttér, méret -----

    @Test
    public void moveAllToPutsEveryUfoToThePoint() {
        Space space = space(5);

        space.moveAllTo(12.5, 34);

        for (Ufo ufo : space.getUfos()) {
            assertEquals(12.5, ufo.getX(), 0);
            assertEquals(34, ufo.getY(), 0);
        }
    }

    @Test
    public void newBackgroundChangesTheColor() {
        Space space = space(0);
        int before = space.getBackgroundRgb();

        space.newBackground();

        assertNotEquals(before, space.getBackgroundRgb());
    }

    @Test
    public void resizeChangesTheWrapAroundBorder() {
        Space space = space(0);
        space.addUfos(1);
        space.moveAllTo(395, 100);

        space.resize(200, 200);
        space.step();

        // A 395 már a 200 széles téren kívül van: átfordul a bal szélre.
        assertEquals(0, space.getUfos().get(0).getX(), 0);
    }

}
