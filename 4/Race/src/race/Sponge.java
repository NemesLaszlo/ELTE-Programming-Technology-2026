package race;

/**
 * The sponge ("szivacs"): soaks up the rain and suffers in the sun.
 *
 * <table border="1">
 * <caption>Reactions of the sponge</caption>
 * <tr><th>Weather</th><th>Water change</th><th>Distance</th></tr>
 * <tr><td>sunny</td><td>-4</td><td>0</td></tr>
 * <tr><td>cloudy</td><td>+1</td><td>1</td></tr>
 * <tr><td>rainy</td><td>+6</td><td>3</td></tr>
 * </table>
 */
public class Sponge extends Creature {

    /** The code of the species in the input file. */
    public static final String CODE = "s";

    /** The largest amount of water a sponge can store. */
    public static final int MAX_WATER = 20;

    /**
     * Creates a sponge.
     *
     * @param name the unique name of the creature; must not be blank
     * @param initialWater the water the creature starts with; must be between
     * 1 and {@link #MAX_WATER}
     * @throws IllegalArgumentException if any argument is out of range
     */
    public Sponge(String name, int initialWater) {
        super(name, initialWater, MAX_WATER);
    }

    @Override
    protected int waterChange(Weather weather) {
        return switch (weather) {
            case SUNNY -> -4;
            case CLOUDY -> 1;
            case RAINY -> 6;
        };
    }

    @Override
    protected int distanceGain(Weather weather) {
        return switch (weather) {
            case SUNNY -> 0;
            case CLOUDY -> 1;
            case RAINY -> 3;
        };
    }

    @Override
    public String getSpecies() {
        return "sponge";
    }

}
