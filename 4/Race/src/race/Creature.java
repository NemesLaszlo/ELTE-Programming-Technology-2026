package race;

import java.util.Objects;

/**
 * Common base class of the creatures taking part in the race.
 *
 * <p>A creature has a unique name, an amount of stored water, a
 * species-specific water capacity, a travelled distance and a flag telling
 * whether it is still alive. The class is abstract: only the concrete species
 * ({@link SandWalker}, {@link Sponge}, {@link Walker}) can be instantiated.</p>
 *
 * <p>How a day passes is the same for every species and is implemented once in
 * {@link #liveThrough(Weather)}; the species only supply the numbers through
 * {@link #waterChange(Weather)} and {@link #distanceGain(Weather)}.</p>
 */
public abstract class Creature {

    private final String name;
    private final int maxWater;
    private int water;
    private int distance;
    private boolean alive;

    /**
     * Creates a living creature that has not travelled yet.
     *
     * @param name the unique name of the creature; must not be blank
     * @param initialWater the water the creature starts with; must be between
     * 1 and {@code maxWater}
     * @param maxWater the largest amount of water the species can store; must
     * be positive
     * @throws IllegalArgumentException if any argument is out of range
     */
    protected Creature(String name, int initialWater, int maxWater) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The name of a creature must not be blank");
        }
        if (maxWater <= 0) {
            throw new IllegalArgumentException("The water capacity must be positive: " + maxWater);
        }
        if (initialWater <= 0) {
            throw new IllegalArgumentException(
                    "The initial water of " + name + " must be positive: " + initialWater);
        }
        if (initialWater > maxWater) {
            throw new IllegalArgumentException("The initial water of " + name + " ("
                    + initialWater + ") exceeds the capacity of its species (" + maxWater + ")");
        }
        this.name = name;
        this.maxWater = maxWater;
        this.water = initialWater;
        this.distance = 0;
        this.alive = true;
    }

    /**
     * Returns how the stored water changes on a day with the given weather.
     *
     * @param weather the weather of the day; never {@code null}
     * @return the change of the water; negative when the creature loses water
     */
    protected abstract int waterChange(Weather weather);

    /**
     * Returns how far the creature travels on a day with the given weather.
     *
     * @param weather the weather of the day; never {@code null}
     * @return the distance gained; never negative
     */
    protected abstract int distanceGain(Weather weather);

    /**
     * Returns the name of the species, for display purposes.
     *
     * @return the species name
     */
    public abstract String getSpecies();

    /**
     * Makes the creature live through one day of the race.
     *
     * <p>The water is changed first and is capped at the capacity of the
     * species. If the water runs out, the creature dies and does not move
     * that day. A dead creature ignores every further day.</p>
     *
     * @param weather the weather of the day
     * @throws NullPointerException if {@code weather} is {@code null}
     */
    public final void liveThrough(Weather weather) {
        Objects.requireNonNull(weather, "weather");
        if (!alive) {
            return;
        }
        water = Math.min(maxWater, water + waterChange(weather));
        if (water <= 0) {
            water = 0;
            alive = false;
            return;
        }
        distance += distanceGain(weather);
    }

    /**
     * Makes the creature live through a sunny day.
     *
     * @see #liveThrough(Weather)
     */
    public final void sunny() {
        liveThrough(Weather.SUNNY);
    }

    /**
     * Makes the creature live through a cloudy day.
     *
     * @see #liveThrough(Weather)
     */
    public final void cloudy() {
        liveThrough(Weather.CLOUDY);
    }

    /**
     * Makes the creature live through a rainy day.
     *
     * @see #liveThrough(Weather)
     */
    public final void rainy() {
        liveThrough(Weather.RAINY);
    }

    /**
     * Returns the unique name of the creature.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the amount of water the creature currently stores.
     *
     * @return the stored water; 0 if the creature is dead
     */
    public int getWater() {
        return water;
    }

    /**
     * Returns the largest amount of water the creature can store.
     *
     * @return the water capacity of the species
     */
    public int getMaxWater() {
        return maxWater;
    }

    /**
     * Returns the distance the creature has travelled so far.
     *
     * @return the travelled distance
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Tells whether the creature is still alive.
     *
     * @return {@code true} if the creature is alive
     */
    public boolean isAlive() {
        return alive;
    }

    @Override
    public String toString() {
        return name + " (" + getSpecies() + "): distance " + distance
                + ", water " + water + "/" + maxWater + ", " + (alive ? "alive" : "dead");
    }

}
