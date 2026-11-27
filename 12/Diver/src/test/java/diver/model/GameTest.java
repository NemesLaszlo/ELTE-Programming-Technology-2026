package diver.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

/**
 * A modell tesztjei. Nincs bennük Swing és adatbázis: a játék szabályait
 * kis, kézzel megadott pályákon ellenőrizzük. A pálya szövegként van a
 * tesztben, így azonnal látszik, mit vizsgálunk.
 */
public class GameTest {

    /** Egy cápa nélküli pálya: a búvár mozgásához és a gyöngyökhöz. */
    private static final Level OPEN = Level.parse(List.of(
            "######",
            "#D.P.#",
            "#.#..#",
            "#P..P#",
            "######"));

    private Game game(String... lines) {
        return new Game(List.of(Level.parse(List.of(lines))));
    }

    /** Ennyi ütem, hogy a cápák egyet lépjenek. */
    private static void sharkStep(Game game) {
        for (int i = 0; i < Game.SHARK_PERIOD; i++) {
            game.tick();
        }
    }

    // ----- Az új játék -----

    @Test
    public void newGameStartsOnTheFirstLevelWithFullLives() {
        Game game = new Game(List.of(OPEN));

        assertEquals(1, game.getLevelNumber());
        assertEquals(1, game.getLevelCount());
        assertEquals(new Position(1, 1), game.getDiver());
        assertEquals(Game.LIVES, game.getLives());
        assertEquals(0, game.getScore());
        assertEquals(3, game.getPearlsLeft());
        assertEquals(0, game.getElapsedMillis());
        assertFalse(game.isOver());
        assertTrue(game.getSharks().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void atLeastOneLevelIsRequired() {
        new Game(List.of());
    }

    @Test
    public void builtInLevelsGameStartsOnLevelOne() {
        Game game = new Game();

        assertEquals(1, game.getLevelNumber());
        assertEquals(Level.LEVEL_COUNT, game.getLevelCount());
        assertEquals(Level.load(1).getStart(), game.getDiver());
    }

    // ----- A búvár mozgása -----

    @Test
    public void diverMovesIntoWater() {
        Game game = new Game(List.of(OPEN));

        assertTrue(game.move(Direction.RIGHT));
        assertEquals(new Position(1, 2), game.getDiver());
        assertTrue(game.move(Direction.LEFT));
        assertTrue(game.move(Direction.DOWN));
        assertTrue(game.move(Direction.DOWN));
        assertEquals(new Position(3, 1), game.getDiver());
    }

    @Test
    public void rocksAndTheEdgeBlockTheDiver() {
        Game game = new Game(List.of(OPEN));

        assertFalse(game.move(Direction.UP));       // szikla (a keret)
        assertFalse(game.move(Direction.LEFT));
        assertEquals(new Position(1, 1), game.getDiver());

        game.move(Direction.RIGHT);
        assertFalse(game.move(Direction.DOWN));     // (2, 2) szikla
        assertEquals(new Position(1, 2), game.getDiver());
    }

    @Test
    public void edgeWithoutRocksBlocksTheDiver() {
        Game game = game(
                "D.P",
                "...");

        assertFalse(game.move(Direction.UP));
        assertFalse(game.move(Direction.LEFT));
        assertEquals(new Position(0, 0), game.getDiver());
    }

    // ----- Gyöngyök -----

    @Test
    public void steppingOnAPearlCollectsIt() {
        Game game = new Game(List.of(OPEN));

        game.move(Direction.RIGHT);
        game.move(Direction.RIGHT);                 // (1, 3): gyöngy

        assertEquals(1, game.getScore());
        assertEquals(2, game.getPearlsLeft());
        assertEquals(Tile.WATER, game.getTile(new Position(1, 3)));
        // A Level maga nem változott.
        assertEquals(Tile.PEARL, OPEN.getTile(new Position(1, 3)));
    }

    @Test
    public void collectingAllPearlsStartsTheNextLevel() {
        Level second = Level.parse(List.of(
                "####",
                "#.D#",
                "#P.#",
                "####"));
        Game game = new Game(List.of(Level.parse(List.of("DP")), second));

        game.move(Direction.RIGHT);

        assertEquals(2, game.getLevelNumber());
        assertEquals(1, game.getScore());
        assertEquals(1, game.getPearlsLeft());
        assertEquals(new Position(1, 2), game.getDiver());
        assertEquals(Game.LIVES, game.getLives());
    }

    @Test
    public void afterTheLastLevelTheFirstComesAgain() {
        Game game = new Game(List.of(Level.parse(List.of("DP"))));

        game.move(Direction.RIGHT);

        assertEquals(1, game.getLevelNumber());
        assertEquals(1, game.getScore());
        assertEquals(1, game.getPearlsLeft());
        assertEquals(new Position(0, 0), game.getDiver());
        assertEquals(Tile.PEARL, game.getTile(new Position(0, 1)));
    }

    // ----- A cápák -----

    @Test
    public void sharksMoveEveryPeriod() {
        Game game = game(
                "#####",
                "#H..#",
                "#..P#",
                "##D##",
                "#####");
        Shark shark = game.getSharks().get(0);

        for (int i = 0; i < Game.SHARK_PERIOD - 1; i++) {
            game.tick();
        }
        assertEquals(new Position(1, 1), shark.getPosition());

        game.tick();
        assertEquals(new Position(1, 2), shark.getPosition());
        assertEquals(Direction.RIGHT, shark.getDirection());
    }

    @Test
    public void sharkTurnsAroundAtARock() {
        Game game = game(
                "#####",
                "#.H.#",
                "#...#",
                "#D.P#",
                "#####");
        Shark shark = game.getSharks().get(0);

        sharkStep(game);
        assertEquals(new Position(1, 3), shark.getPosition());
        sharkStep(game);                            // a szikla előtt: megfordul, nem lép
        assertEquals(new Position(1, 3), shark.getPosition());
        assertEquals(Direction.LEFT, shark.getDirection());
        sharkStep(game);
        assertEquals(new Position(1, 2), shark.getPosition());
    }

    @Test
    public void sharkTurnsAroundAtTheEdge() {
        Game game = game(
                "V..",
                "...",
                ".DP");
        Shark shark = game.getSharks().get(0);

        sharkStep(game);
        sharkStep(game);
        assertEquals(new Position(2, 0), shark.getPosition());
        sharkStep(game);                            // a széle: megfordul
        assertEquals(Direction.UP, shark.getDirection());
        assertEquals(new Position(2, 0), shark.getPosition());
    }

    @Test
    public void sharkPassesOverPearls() {
        Game game = game(
                "#####",
                "#HP.#",
                "##D##",
                "#####");

        sharkStep(game);

        assertEquals(new Position(1, 2), game.getSharks().get(0).getPosition());
        assertEquals(Tile.PEARL, game.getTile(new Position(1, 2)));
        assertEquals(1, game.getPearlsLeft());
    }

    // ----- Elkapás -----

    @Test
    public void walkingInFrontOfASharkCostsALifeAndRespawns() {
        Game game = game(
                "#####",
                "#D..#",
                "#.H.#",
                "#.P.#",
                "#####");
        // A cápa (2, 2) jobbra néz: a (2, 3) mezőt látja.
        game.move(Direction.RIGHT);
        game.move(Direction.RIGHT);                 // (1, 3): biztonságos
        assertEquals(Game.LIVES, game.getLives());

        game.move(Direction.DOWN);                  // (2, 3): a cápa előtt

        assertEquals(Game.LIVES - 1, game.getLives());
        assertEquals(new Position(1, 1), game.getDiver());
        assertFalse(game.isOver());
    }

    @Test
    public void sharkMovingNextToTheDiverCatchesIt() {
        Game game = game(
                "#####",
                "#H.D#",
                "#..P#",
                "#####");
        // A cápa (1, 1) → (1, 2): előtte a (1, 3), ahol a búvár áll.
        sharkStep(game);

        assertEquals(Game.LIVES - 1, game.getLives());
        assertEquals(new Position(1, 3), game.getDiver());    // a kezdőhely
    }

    @Test
    public void sharkDoesNotSeeThroughRocks() {
        Game game = game(
                "#####",
                "#H#D#",
                "#..P#",
                "#####");

        sharkStep(game);                            // a szikla miatt megfordul

        assertEquals(Game.LIVES, game.getLives());
    }

    @Test
    public void sharkDoesNotSeeSideways() {
        Game game = game(
                "#####",
                "#D..#",
                "#H..#",
                "#.P.#",
                "#####");
        // A cápa (2, 1) jobbra néz; a búvár (1, 1) fölötte van.
        assertEquals(Game.LIVES, game.getLives());

        game.move(Direction.RIGHT);                 // (1, 2): a cápa fölött átlósan
        assertEquals(Game.LIVES, game.getLives());
    }

    @Test
    public void losingAllLivesEndsTheGame() {
        Game game = game(
                "#####",
                "#D..#",
                "#.H.#",
                "#.P.#",
                "#####");

        for (int i = 0; i < Game.LIVES; i++) {
            game.move(Direction.RIGHT);
            game.move(Direction.RIGHT);
            game.move(Direction.DOWN);              // a cápa elé
        }

        assertEquals(0, game.getLives());
        assertTrue(game.isOver());
    }

    @Test
    public void nothingHappensAfterTheGameIsOver() {
        Game game = game(
                "#####",
                "#D..#",
                "#.H.#",
                "#.P.#",
                "#####");
        for (int i = 0; i < Game.LIVES; i++) {
            game.move(Direction.RIGHT);
            game.move(Direction.RIGHT);
            game.move(Direction.DOWN);
        }
        long elapsed = game.getElapsedMillis();
        Position sharkPosition = game.getSharks().get(0).getPosition();

        assertFalse(game.move(Direction.RIGHT));
        sharkStep(game);

        assertEquals(new Position(1, 1), game.getDiver());
        assertEquals(elapsed, game.getElapsedMillis());
        assertEquals(sharkPosition, game.getSharks().get(0).getPosition());
    }

    // ----- Az idő -----

    @Test
    public void elapsedTimeIsCountedInTicks() {
        Game game = new Game(List.of(OPEN));

        for (int i = 0; i < 40; i++) {
            game.tick();
        }

        assertEquals(40L * Game.TICK_MS, game.getElapsedMillis());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void theSharkListCannotBeModifiedFromOutside() {
        new Game(List.of(OPEN)).getSharks().add(new Shark(new Position(0, 0), Direction.UP));
    }

}
