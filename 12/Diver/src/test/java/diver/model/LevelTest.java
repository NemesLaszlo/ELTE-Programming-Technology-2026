package diver.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import org.junit.Test;

/**
 * A pálya beolvasásának és ellenőrzésének tesztjei, és a 10 beépített
 * pálya ellenőrzése: mind betölthető, és mindegyik végigjátszható.
 */
public class LevelTest {

    private static final List<String> SMALL = List.of(
            "#####",
            "#D.P#",
            "#.#.#",
            "#H.V#",
            "#####");

    @Test
    public void parseReadsTilesStartAndSharks() {
        Level level = Level.parse(SMALL);

        assertEquals(5, level.getRows());
        assertEquals(5, level.getCols());
        assertEquals(new Position(1, 1), level.getStart());
        assertEquals(Tile.ROCK, level.getTile(new Position(0, 0)));
        assertEquals(Tile.WATER, level.getTile(new Position(1, 2)));
        assertEquals(Tile.PEARL, level.getTile(new Position(1, 3)));
        assertEquals(Tile.ROCK, level.getTile(new Position(2, 2)));
        assertEquals(1, level.getPearlCount());

        // A D, H, V alatt víz van.
        assertEquals(Tile.WATER, level.getTile(new Position(1, 1)));
        assertEquals(Tile.WATER, level.getTile(new Position(3, 1)));
        assertEquals(Tile.WATER, level.getTile(new Position(3, 3)));

        assertEquals(2, level.getSharks().size());
        assertEquals(new Position(3, 1), level.getSharks().get(0).getPosition());
        assertEquals(Direction.RIGHT, level.getSharks().get(0).getDirection());
        assertEquals(new Position(3, 3), level.getSharks().get(1).getPosition());
        assertEquals(Direction.DOWN, level.getSharks().get(1).getDirection());
    }

    @Test
    public void copiesAreIndependentOfTheLevel() {
        Level level = Level.parse(SMALL);

        Tile[][] tiles = level.copyTiles();
        tiles[1][3] = Tile.WATER;
        List<Shark> sharks = level.newSharks();

        assertEquals(Tile.PEARL, level.getTile(new Position(1, 3)));
        assertNotSame(level.getSharks().get(0), sharks.get(0));
    }

    @Test
    public void raggedRowsAreRejected() {
        assertRejected(List.of("####", "#D.P#", "####"), "Line 2");
    }

    @Test
    public void unknownCharacterIsRejected() {
        assertRejected(List.of("#####", "#D.X#", "#####"), "unknown symbol 'X'");
    }

    @Test
    public void missingStartIsRejected() {
        assertRejected(List.of("#####", "#..P#", "#####"), "No start");
    }

    @Test
    public void twoStartsAreRejected() {
        assertRejected(List.of("#####", "#DDP#", "#####"), "more than one start");
    }

    @Test
    public void missingPearlIsRejected() {
        assertRejected(List.of("#####", "#D..#", "#####"), "No pearl");
    }

    @Test
    public void emptyLevelIsRejected() {
        assertRejected(List.of(), "Empty");
    }

    private static void assertRejected(List<String> lines, String messagePart) {
        try {
            Level.parse(lines);
            fail("accepted an invalid level");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(messagePart));
        }
    }

    @Test
    public void reachablePearlsMakeTheLevelPlayable() {
        assertTrue(Level.parse(SMALL).isPlayable());
    }

    @Test
    public void pearlBehindRocksMakesTheLevelUnplayable() {
        Level level = Level.parse(List.of(
                "######",
                "#D.#P#",
                "#..#.#",
                "######"));

        assertFalse(level.isPlayable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadRejectsUnknownNumber() {
        Level.load(Level.LEVEL_COUNT + 1);
    }

    @Test
    public void allBuiltInLevelsLoadAndArePlayable() {
        List<Level> levels = Level.loadAll();

        assertEquals(Level.LEVEL_COUNT, levels.size());
        for (int i = 0; i < levels.size(); i++) {
            Level level = levels.get(i);
            assertTrue("level " + (i + 1) + ": has pearls", level.getPearlCount() > 0);
            assertTrue("level " + (i + 1) + ": has sharks", !level.getSharks().isEmpty());
            assertTrue("level " + (i + 1) + ": is playable", level.isPlayable());
            // A kezdőhelyen induláskor nem lát cápa.
            for (Shark shark : level.getSharks()) {
                assertFalse("level " + (i + 1) + ": a shark sees the start",
                        shark.sees(level.getStart(), level));
            }
        }
    }

}
