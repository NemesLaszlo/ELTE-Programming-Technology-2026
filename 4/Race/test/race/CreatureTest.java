package race;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests of the rules shared by every creature and of the numbers of the
 * individual species.
 */
public class CreatureTest {

    private static void assertState(Creature creature, int water, int distance, boolean alive) {
        assertEquals("water", water, creature.getWater());
        assertEquals("distance", distance, creature.getDistance());
        assertEquals("alive", alive, creature.isAlive());
    }

    // --- initial state and validation ---

    @Test
    public void newCreatureIsAliveAndHasNotMoved() {
        Creature creature = new SandWalker("Vandor", 4);
        assertEquals("Vandor", creature.getName());
        assertEquals(8, creature.getMaxWater());
        assertState(creature, 4, 0, true);
    }

    @Test
    public void speciesHaveTheirOwnCapacity() {
        assertEquals(8, new SandWalker("a", 1).getMaxWater());
        assertEquals(20, new Sponge("b", 1).getMaxWater());
        assertEquals(12, new Walker("c", 1).getMaxWater());
    }

    @Test
    public void initialWaterMayEqualTheCapacity() {
        assertState(new Walker("Seta", 12), 12, 0, true);
    }

    @Test
    public void nameMustNotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Sponge(null, 5));
        assertThrows(IllegalArgumentException.class, () -> new Sponge("", 5));
        assertThrows(IllegalArgumentException.class, () -> new Sponge("  ", 5));
    }

    @Test
    public void initialWaterMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new Sponge("Csuszo", 0));
        assertThrows(IllegalArgumentException.class, () -> new Sponge("Csuszo", -3));
    }

    @Test
    public void initialWaterMustNotExceedTheCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new SandWalker("Vandor", 9));
        assertThrows(IllegalArgumentException.class, () -> new Walker("Seta", 13));
        assertThrows(IllegalArgumentException.class, () -> new Sponge("Csuszo", 21));
    }

    @Test
    public void weatherMustNotBeNull() {
        Creature creature = new SandWalker("Vandor", 4);
        assertThrows(NullPointerException.class, () -> creature.liveThrough(null));
    }

    // --- the table of the species ---

    @Test
    public void sandWalkerReactions() {
        Creature sunny = new SandWalker("a", 4);
        sunny.sunny();
        assertState(sunny, 3, 3, true);

        Creature cloudy = new SandWalker("b", 4);
        cloudy.cloudy();
        assertState(cloudy, 4, 1, true);

        Creature rainy = new SandWalker("c", 4);
        rainy.rainy();
        assertState(rainy, 7, 0, true);
    }

    @Test
    public void spongeReactions() {
        Creature sunny = new Sponge("a", 10);
        sunny.sunny();
        assertState(sunny, 6, 0, true);

        Creature cloudy = new Sponge("b", 10);
        cloudy.cloudy();
        assertState(cloudy, 11, 1, true);

        Creature rainy = new Sponge("c", 10);
        rainy.rainy();
        assertState(rainy, 16, 3, true);
    }

    @Test
    public void walkerReactions() {
        Creature sunny = new Walker("a", 7);
        sunny.sunny();
        assertState(sunny, 5, 1, true);

        Creature cloudy = new Walker("b", 7);
        cloudy.cloudy();
        assertState(cloudy, 6, 2, true);

        Creature rainy = new Walker("c", 7);
        rainy.rainy();
        assertState(rainy, 10, 1, true);
    }

    // --- shared rules ---

    @Test
    public void waterIsCappedAtTheCapacity() {
        Creature sandWalker = new SandWalker("a", 7);
        sandWalker.rainy();
        assertState(sandWalker, 8, 0, true);

        Creature sponge = new Sponge("b", 18);
        sponge.rainy();
        assertState(sponge, 20, 3, true);

        Creature walker = new Walker("c", 12);
        walker.rainy();
        assertState(walker, 12, 1, true);
    }

    @Test
    public void creatureDiesWhenWaterReachesExactlyZero() {
        Creature creature = new SandWalker("a", 1);
        creature.sunny();
        assertState(creature, 0, 0, false);
    }

    @Test
    public void waterNeverGoesBelowZero() {
        Creature creature = new Sponge("a", 2);
        creature.sunny();
        assertState(creature, 0, 0, false);
    }

    @Test
    public void creatureDoesNotMoveOnTheDayItDies() {
        Creature creature = new Walker("a", 3);
        creature.sunny();
        assertState(creature, 1, 1, true);
        creature.cloudy();
        assertState(creature, 0, 1, false);
    }

    @Test
    public void deadCreatureIgnoresFurtherDays() {
        Creature creature = new Sponge("a", 4);
        creature.sunny();
        assertFalse(creature.isAlive());

        creature.rainy();
        creature.cloudy();
        creature.liveThrough(Weather.RAINY);
        assertState(creature, 0, 0, false);
    }

    @Test
    public void liveThroughIsEquivalentToTheNamedOperations() {
        Creature named = new Walker("a", 7);
        named.sunny();
        named.cloudy();
        named.rainy();

        Creature generic = new Walker("b", 7);
        generic.liveThrough(Weather.SUNNY);
        generic.liveThrough(Weather.CLOUDY);
        generic.liveThrough(Weather.RAINY);

        assertState(generic, named.getWater(), named.getDistance(), named.isAlive());
    }

    @Test
    public void toStringContainsTheImportantData() {
        String text = new SandWalker("Vandor", 4).toString();
        assertTrue(text, text.contains("Vandor"));
        assertTrue(text, text.contains("sand walker"));
        assertTrue(text, text.contains("alive"));
    }

}
