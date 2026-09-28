package race;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A multi-day race of creatures.
 *
 * <p>The race is won by the creature that travels the farthest while staying
 * alive. A race can be run only once, because running it changes the state of
 * the participating creatures.</p>
 */
public class Race {

    private final List<Creature> creatures;
    private final List<Weather> days;
    private boolean finished;

    /**
     * Creates a race that has not been run yet.
     *
     * @param creatures the participants, in starting order
     * @param days the weather of each day of the race, in order
     * @throws NullPointerException if an argument or one of its elements is
     * {@code null}
     * @throws IllegalArgumentException if two creatures have the same name
     */
    public Race(List<Creature> creatures, List<Weather> days) {
        Objects.requireNonNull(creatures, "creatures");
        Objects.requireNonNull(days, "days");
        // Unmodifiable copies: later changes of the caller's lists must not
        // affect the race. List.copyOf also rejects null elements.
        this.creatures = List.copyOf(creatures);
        this.days = List.copyOf(days);
        this.finished = false;
        checkUniqueNames();
    }

    private void checkUniqueNames() {
        Set<String> names = new HashSet<>();
        for (Creature creature : creatures) {
            if (!names.add(creature.getName())) {
                throw new IllegalArgumentException(
                        "Duplicate creature name: " + creature.getName());
            }
        }
    }

    /**
     * Runs the race: every creature lives through every day, in order.
     *
     * @throws IllegalStateException if the race has already been run
     */
    public void run() {
        if (finished) {
            throw new IllegalStateException("The race has already been run");
        }
        for (Weather day : days) {
            for (Creature creature : creatures) {
                creature.liveThrough(day);
            }
        }
        finished = true;
    }

    /**
     * Returns the winners of the race: the living creatures that travelled
     * the farthest.
     *
     * <p>The list has more than one element if several creatures are tied for
     * the first place, and is empty if no creature survived the race.</p>
     *
     * @return the winners in starting order; never {@code null}
     * @throws IllegalStateException if the race has not been run yet
     */
    public List<Creature> getWinners() {
        if (!finished) {
            throw new IllegalStateException("The race has not been run yet");
        }
        int bestDistance = 0;
        for (Creature creature : creatures) {
            if (creature.isAlive()) {
                bestDistance = Math.max(bestDistance, creature.getDistance());
            }
        }
        List<Creature> winners = new ArrayList<>();
        for (Creature creature : creatures) {
            if (creature.isAlive() && creature.getDistance() == bestDistance) {
                winners.add(creature);
            }
        }
        return winners;
    }

    /**
     * Returns the participants of the race.
     *
     * @return an unmodifiable list of the creatures, in starting order
     */
    public List<Creature> getCreatures() {
        return creatures;
    }

    /**
     * Returns the weather of each day of the race.
     *
     * @return an unmodifiable list of the days, in order
     */
    public List<Weather> getDays() {
        return days;
    }

    /**
     * Tells whether the race has already been run.
     *
     * @return {@code true} if {@link #run()} has completed
     */
    public boolean isFinished() {
        return finished;
    }

}
