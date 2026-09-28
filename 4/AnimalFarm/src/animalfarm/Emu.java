package animalfarm;

/**
 * Emu.
 *
 * @author pinter
 */
public class Emu extends Animal {

    /** Az emu betűjele a bemeneti fájlban. */
    public static final String KIND = "E";

    // Statikus konstans, mert a küszöb a fajtára jellemző, nem az egyes állatokra.
    private static final int MALNOURISHED_THRESHOLD = 20;

    /**
     * @param name az emu neve
     * @param weight az emu súlya kilogrammban
     */
    public Emu(String name, int weight) {
        super(name, weight, KIND);
    }

    @Override
    public int getMalnourishedThreshold() {
        return MALNOURISHED_THRESHOLD;
    }

}
