package animalfarm;

/**
 * Ló.
 *
 * @author pinter
 */
public class Horse extends Animal {

    /** A ló betűjele a bemeneti fájlban. */
    public static final String KIND = "L";

    // Statikus konstans, mert a küszöb a fajtára jellemző, nem az egyes állatokra.
    private static final int MALNOURISHED_THRESHOLD = 60;

    /**
     * @param name a ló neve
     * @param weight a ló súlya kilogrammban
     */
    public Horse(String name, int weight) {
        super(name, weight, KIND);
    }

    @Override
    public int getMalnourishedThreshold() {
        return MALNOURISHED_THRESHOLD;
    }

}
