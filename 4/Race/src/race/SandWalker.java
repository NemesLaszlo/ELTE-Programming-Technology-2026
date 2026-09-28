package race;

/**
 * The sand walker ("homokjáró"): thrives in the sun and stands still in the
 * rain.
 *
 * <table border="1">
 * <caption>Reactions of the sand walker</caption>
 * <tr><th>Weather</th><th>Water change</th><th>Distance</th></tr>
 * <tr><td>sunny</td><td>-1</td><td>3</td></tr>
 * <tr><td>cloudy</td><td>0</td><td>1</td></tr>
 * <tr><td>rainy</td><td>+3</td><td>0</td></tr>
 * </table>
 */
public class SandWalker extends Creature {

    /** The code of the species in the input file. */
    public static final String CODE = "h";

    /** The largest amount of water a sand walker can store. */
    public static final int MAX_WATER = 8;

    /**
     * Creates a sand walker.
     *
     * @param name the unique name of the creature; must not be blank
     * @param initialWater the water the creature starts with; must be between
     * 1 and {@link #MAX_WATER}
     * @throws IllegalArgumentException if any argument is out of range
     */
    public SandWalker(String name, int initialWater) {
        super(name, initialWater, MAX_WATER);
    }

    @Override
    protected int waterChange(Weather weather) {
        return switch (weather) {
            case SUNNY -> -1;
            case CLOUDY -> 0;
            case RAINY -> 3;
        };
    }

    @Override
    protected int distanceGain(Weather weather) {
        return switch (weather) {
            case SUNNY -> 3;
            case CLOUDY -> 1;
            case RAINY -> 0;
        };
    }

    @Override
    public String getSpecies() {
        return "sand walker";
    }

}
