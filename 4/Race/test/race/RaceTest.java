package race;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Tests of running a race and of selecting the winners.
 */
public class RaceTest {

    private static List<Weather> days(String codes) {
        List<Weather> days = new ArrayList<>();
        for (char code : codes.toCharArray()) {
            days.add(Weather.fromCode(code));
        }
        return days;
    }

    private static List<String> names(List<Creature> creatures) {
        List<String> names = new ArrayList<>();
        for (Creature creature : creatures) {
            names.add(creature.getName());
        }
        return names;
    }

    private static Race finishedRace(List<Creature> creatures, String dayCodes) {
        Race race = new Race(creatures, days(dayCodes));
        race.run();
        return race;
    }

    @Test
    public void sampleOfTheAssignmentEndsInAFourWayTie() {
        Race race = finishedRace(List.of(
                new SandWalker("Vandor", 4),
                new Walker("Seta", 7),
                new Sponge("Csuszo", 12),
                new Sponge("Siklo", 10)), "nffeeennf");

        for (Creature creature : race.getCreatures()) {
            assertTrue(creature.getName(), creature.isAlive());
            assertEquals(creature.getName(), 12, creature.getDistance());
        }
        assertEquals(List.of("Vandor", "Seta", "Csuszo", "Siklo"), names(race.getWinners()));
    }

    @Test
    public void singleWinnerIsTheOneWithTheLargestDistance() {
        Race race = finishedRace(List.of(
                new SandWalker("Vandor", 4),
                new Walker("Seta", 7),
                new Sponge("Csuszo", 12)), "nffeennnn");

        List<Creature> winners = race.getWinners();
        assertEquals(List.of("Vandor"), names(winners));
        assertEquals(17, winners.get(0).getDistance());
    }

    @Test
    public void thereIsNoWinnerIfNobodySurvives() {
        Race race = finishedRace(List.of(new SandWalker("Vandor", 4)), "nnnnnnnnn");

        assertFalse(race.getCreatures().get(0).isAlive());
        assertTrue(race.getWinners().isEmpty());
    }

    @Test
    public void deadCreatureCannotWinEvenWithTheLargestDistance() {
        // The sand walker runs 9 in three sunny days, then dies on the fourth.
        Race race = finishedRace(List.of(
                new SandWalker("Fast", 4),
                new Walker("Slow", 12)), "nnnn");

        Creature fast = race.getCreatures().get(0);
        assertFalse(fast.isAlive());
        assertEquals(9, fast.getDistance());

        List<Creature> winners = race.getWinners();
        assertEquals(List.of("Slow"), names(winners));
        assertEquals(4, winners.get(0).getDistance());
    }

    @Test
    public void livingCreatureWinsEvenWithZeroDistance() {
        Race race = finishedRace(List.of(new SandWalker("Vandor", 4)), "ee");

        List<Creature> winners = race.getWinners();
        assertEquals(List.of("Vandor"), names(winners));
        assertEquals(0, winners.get(0).getDistance());
    }

    @Test
    public void raceWithoutDaysLeavesTheCreaturesUntouched() {
        Race race = finishedRace(List.of(new Sponge("Csuszo", 12)), "");

        Creature creature = race.getCreatures().get(0);
        assertEquals(12, creature.getWater());
        assertEquals(0, creature.getDistance());
        assertTrue(race.isFinished());
    }

    @Test
    public void raceWithoutCreaturesHasNoWinner() {
        Race race = finishedRace(List.of(), "nfe");
        assertTrue(race.getWinners().isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void winnersCannotBeAskedBeforeTheRace() {
        new Race(List.of(new Sponge("Csuszo", 12)), days("n")).getWinners();
    }

    @Test(expected = IllegalStateException.class)
    public void raceCannotBeRunTwice() {
        Race race = finishedRace(List.of(new Sponge("Csuszo", 12)), "n");
        race.run();
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateNamesAreRejected() {
        new Race(List.of(new Sponge("Same", 12), new Walker("Same", 7)), days("n"));
    }

    @Test(expected = NullPointerException.class)
    public void nullCreatureListIsRejected() {
        new Race(null, days("n"));
    }

    @Test(expected = NullPointerException.class)
    public void nullDayListIsRejected() {
        new Race(List.of(), null);
    }

    @Test(expected = NullPointerException.class)
    public void nullCreatureIsRejected() {
        List<Creature> creatures = new ArrayList<>();
        creatures.add(null);
        new Race(creatures, days("n"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void creatureListCannotBeModified() {
        Race race = new Race(List.of(), days("n"));
        race.getCreatures().add(new Sponge("Csuszo", 12));
    }

    @Test
    public void laterChangesOfTheSourceListsDoNotAffectTheRace() {
        List<Creature> creatures = new ArrayList<>();
        creatures.add(new Sponge("Csuszo", 12));
        List<Weather> days = days("n");
        Race race = new Race(creatures, days);

        creatures.clear();
        days.clear();

        assertEquals(1, race.getCreatures().size());
        assertEquals(1, race.getDays().size());
    }

}
