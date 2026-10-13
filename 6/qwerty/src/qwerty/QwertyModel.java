package qwerty;

/**
 * A billentyűzet MODELLJE (az MV "M" betűje): a beírt szöveg és a Caps
 * állapota.
 *
 * A modell semmit nem tud a felületről, nem használ Swinget. A GUI
 * eseménykezelői hívják a metódusait, majd a GUI a modell állapota alapján
 * frissíti magát (ld. QwertyGUI.refresh).
 *
 * @author pinter
 */
public class QwertyModel {

    /** A beírt szöveg. A StringBuilder hatékonyan bővíthető és rövidíthető. */
    private final StringBuilder text = new StringBuilder();
    /** Igaz, ha nagybetűket írunk. */
    private boolean capsLock = true;

    /**
     * Beír egy betűt a szöveg végére, a Caps szerinti alakban.
     *
     * @param letter a betű
     */
    public void type(String letter) {
        text.append(applyCase(letter));
    }

    /**
     * Törli az utolsó karaktert, ha van.
     */
    public void backspace() {
        if (text.length() > 0) {
            text.deleteCharAt(text.length() - 1);
        }
    }

    /**
     * Törli a teljes szöveget.
     */
    public void clear() {
        text.setLength(0);
    }

    /**
     * Átkapcsolja a Caps-et: kisbetűkről nagybetűkre, vagy fordítva.
     */
    public void toggleCapsLock() {
        capsLock = !capsLock;
    }

    /**
     * @return igaz, ha nagybetűket írunk
     */
    public boolean isCapsLock() {
        return capsLock;
    }

    /**
     * A Caps állapotának megfelelően kis- vagy nagybetűssé alakítja a
     * szöveget. A GUI is ezt használja a betűgombok feliratához.
     *
     * @param letter a betű (vagy szöveg)
     * @return a Caps szerinti alak
     */
    public String applyCase(String letter) {
        return capsLock ? letter.toUpperCase() : letter.toLowerCase();
    }

    /**
     * @return a beírt szöveg
     */
    public String getText() {
        return text.toString();
    }

    /**
     * @return a beírt karakterek száma
     */
    public int getLength() {
        return text.length();
    }

}
