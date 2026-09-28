package race;

/**
 * The walker ("lépegető"): moves in every weather, fastest under clouds.
 *
 * <table border="1">
 * <caption>Reactions of the walker</caption>
 * <tr><th>Weather</th><th>Water change</th><th>Distance</th></tr>
 * <tr><td>sunny</td><td>-2</td><td>1</td></tr>
 * <tr><td>cloudy</td><td>-1</td><td>2</td></tr>
 * <tr><td>rainy</td><td>+3</td><td>1</td></tr>
 * </table>
 */
public class Walker extends Creature {

    /** The code of the species in the input file. */
    public static final String CODE = "l";

    /** The largest amount of water a walker can store. */
    public static final int MAX_WATER = 12;

    /**
     * Creates a walker.
     *
     * @param name the unique name of the creature; must not be blank
     * @param initialWater the water the creature starts with; must be between
     * 1 and {@link #MAX_WATER}
     * @throws IllegalArgumentException if any argument is out of range
     */
    public Walker(String name, int initialWater) {
        super(name, initialWater, MAX_WATER);
    }

    @Override
    protected int waterChange(Weather weather) {
        return switch (weather) {
            case SUNNY -> -2;
            case CLOUDY -> -1;
            case RAINY -> 3;
        };
    }

    @Override
    protected int distanceGain(Weather weather) {
        return switch (weather) {
            case SUNNY -> 1;
            case CLOUDY -> 2;
            case RAINY -> 1;
        };
    }

    @Override
    public String getSpecies() {
        return "walker";
    }

}
