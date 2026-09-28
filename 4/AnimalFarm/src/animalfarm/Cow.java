package animalfarm;

/**
 * Tehén.
 *
 * @author pinter
 */
public class Cow extends Animal {

    /** A tehén betűjele a bemeneti fájlban. */
    public static final String KIND = "T";

    // Statikus konstans, mert a küszöb a fajtára jellemző, nem az egyes állatokra.
    private static final int MALNOURISHED_THRESHOLD = 100;

    /**
     * @param name a tehén neve
     * @param weight a tehén súlya kilogrammban
     */
    public Cow(String name, int weight) {
        super(name, weight, KIND);
    }

    @Override
    public int getMalnourishedThreshold() {
        return MALNOURISHED_THRESHOLD;
    }

}
