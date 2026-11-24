package dodger.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dodger.model.FallingObject.Kind;
import java.util.Random;
import org.junit.Test;

/**
 * A modell tesztjei. Nincs bennük Swing: a játék szabályait ablak és
 * billentyűzet nélkül ellenőrizzük.
 *
 * Két eszköz teszi a játékot kiszámíthatóvá:
 * - a Game rögzített magú Random-ot kap (new Random(42));
 * - ahol a véletlen zavarna, a csomagszintű spawn metódussal konkrét
 *   tárgyat teszünk a hajó fölé, és egy olyan Random-ot adunk, ami soha
 *   nem hoz létre új tárgyat (NoSpawn).
 */
public class GameTest {

    private static final int SEED = 42;

    /** Olyan "véletlen", amivel a Game soha nem hoz létre új tárgyat. */
    @SuppressWarnings("serial")
    private static class NoSpawn extends Random {
        @Override
        public double nextDouble() {
            return 1.0; // soha nem kisebb a spawn-valószínűségnél
        }
    }

    private Game quietGame() {
        return new Game(new NoSpawn());
    }

    /** Egy tárgy közvetlenül a hajó fölé, ami a következő ütemben eléri. */
    private FallingObject above(Game game, Kind kind, int speed) {
        Ship ship = game.getShip();
        int size = FallingObject.sizeOf(kind);
        return new FallingObject(kind, ship.getX(), ship.getY() - size, speed);
    }

    // ----- Az új játék -----

    @Test
    public void newGameStartsAtTheBottomCenterWithFullLives() {
        Game game = new Game(new Random(SEED));

        assertEquals(Game.LIVES, game.getLives());
        assertEquals(0, game.getScore());
        assertEquals(0, game.getElapsedMillis());
        assertFalse(game.isOver());
        assertTrue(game.getObjects().isEmpty());
        Ship ship = game.getShip();
        assertEquals((Game.WIDTH - Ship.SIZE) / 2, ship.getX());
        assertTrue(ship.getBottom() <= Game.HEIGHT);
    }

    // ----- A hajó mozgása -----

    @Test
    public void shipMovesWhileTheDirectionIsPressed() {
        Game game = quietGame();
        int startX = game.getShip().getX();

        game.press(Direction.LEFT);
        game.tick();
        game.tick();

        assertEquals(startX - 2 * Ship.SPEED, game.getShip().getX());

        game.release(Direction.LEFT);
        game.tick();

        assertEquals(startX - 2 * Ship.SPEED, game.getShip().getX());
    }

    @Test
    public void shipMovesDiagonallyWithTwoDirections() {
        Game game = quietGame();
        int startX = game.getShip().getX();
        int startY = game.getShip().getY();

        game.press(Direction.UP);
        game.press(Direction.RIGHT);
        game.tick();

        assertEquals(startX + Ship.SPEED, game.getShip().getX());
        assertEquals(startY - Ship.SPEED, game.getShip().getY());
    }

    @Test
    public void shipStopsAtTheEdges() {
        Game game = quietGame();
        game.press(Direction.LEFT);
        game.press(Direction.DOWN);

        for (int i = 0; i < 1000; i++) {
            game.tick();
        }

        assertEquals(0, game.getShip().getX());
        assertEquals(Game.HEIGHT - Ship.SIZE, game.getShip().getY());

        game.release(Direction.LEFT);
        game.release(Direction.DOWN);
        game.press(Direction.RIGHT);
        game.press(Direction.UP);
        for (int i = 0; i < 1000; i++) {
            game.tick();
        }

        assertEquals(Game.WIDTH - Ship.SIZE, game.getShip().getX());
        assertEquals(0, game.getShip().getY());
    }

    // ----- A hulló tárgyak -----

    @Test
    public void objectsFallByTheirSpeed() {
        Game game = quietGame();
        FallingObject asteroid = new FallingObject(Kind.ASTEROID, 10, 0, 3);
        game.spawn(asteroid);

        game.tick();

        assertEquals(3, asteroid.getY());
        assertEquals(10, asteroid.getX());
    }

    @Test
    public void objectsLeavingTheFieldDisappear() {
        Game game = quietGame();
        game.spawn(new FallingObject(Kind.STAR, 10, Game.HEIGHT - 1, 1));

        game.tick();

        assertTrue(game.getObjects().isEmpty());
    }

    @Test
    public void randomGameSpawnsObjectsAtTheTopInsideTheField() {
        Game game = new Game(new Random(SEED));

        for (int i = 0; i < 500; i++) {
            game.tick();
        }

        assertFalse(game.getObjects().isEmpty());
        for (FallingObject object : game.getObjects()) {
            assertTrue(object.getX() >= 0 && object.getRight() <= Game.WIDTH);
            assertTrue(object.getSpeed() >= Game.MIN_SPEED && object.getSpeed() <= Game.MAX_SPEED);
        }
    }

    @Test
    public void sameSeedGivesTheSameGame() {
        Game a = new Game(new Random(SEED));
        Game b = new Game(new Random(SEED));

        for (int i = 0; i < 200; i++) {
            a.tick();
            b.tick();
        }

        assertEquals(a.getObjects().size(), b.getObjects().size());
        for (int i = 0; i < a.getObjects().size(); i++) {
            assertEquals(a.getObjects().get(i).getX(), b.getObjects().get(i).getX());
            assertEquals(a.getObjects().get(i).getKind(), b.getObjects().get(i).getKind());
        }
    }

    // ----- Ütközések -----

    @Test
    public void catchingAStarScoresAPoint() {
        Game game = quietGame();
        game.spawn(above(game, Kind.STAR, 1));

        game.tick();

        assertEquals(1, game.getScore());
        assertEquals(Game.LIVES, game.getLives());
        assertTrue("the caught star disappears", game.getObjects().isEmpty());
    }

    @Test
    public void hittingAnAsteroidCostsALife() {
        Game game = quietGame();
        game.spawn(above(game, Kind.ASTEROID, 1));

        game.tick();

        assertEquals(Game.LIVES - 1, game.getLives());
        assertEquals(0, game.getScore());
        assertTrue("the hit asteroid disappears", game.getObjects().isEmpty());
        assertFalse(game.isOver());
    }

    @Test
    public void objectsNextToTheShipDoNotCollide() {
        Game game = quietGame();
        Ship ship = game.getShip();
        // Pontosan a hajó jobb széle mellett, azonos magasságban: nem ér össze.
        game.spawn(new FallingObject(Kind.ASTEROID, ship.getRight(), ship.getY(), 0));

        game.tick();

        assertEquals(Game.LIVES, game.getLives());
        assertEquals(1, game.getObjects().size());
    }

    // ----- A játék vége -----

    @Test
    public void losingAllLivesEndsTheGame() {
        Game game = quietGame();

        for (int i = 0; i < Game.LIVES; i++) {
            game.spawn(above(game, Kind.ASTEROID, 1));
            game.tick();
        }

        assertEquals(0, game.getLives());
        assertTrue(game.isOver());
    }

    @Test
    public void nothingHappensAfterTheGameIsOver() {
        Game game = quietGame();
        for (int i = 0; i < Game.LIVES; i++) {
            game.spawn(above(game, Kind.ASTEROID, 1));
            game.tick();
        }
        long elapsed = game.getElapsedMillis();
        int x = game.getShip().getX();
        game.spawn(above(game, Kind.STAR, 1));
        game.press(Direction.LEFT);

        game.tick();

        assertEquals(elapsed, game.getElapsedMillis());
        assertEquals(x, game.getShip().getX());
        assertEquals(0, game.getScore());
        assertEquals(1, game.getObjects().size());
    }

    // ----- Az idő -----

    @Test
    public void elapsedTimeIsCountedInTicks() {
        Game game = quietGame();

        for (int i = 0; i < 50; i++) {
            game.tick();
        }

        assertEquals(50L * Game.TICK_MS, game.getElapsedMillis());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void theObjectListCannotBeModifiedFromOutside() {
        quietGame().getObjects().add(new FallingObject(Kind.STAR, 0, 0, 1));
    }

}
