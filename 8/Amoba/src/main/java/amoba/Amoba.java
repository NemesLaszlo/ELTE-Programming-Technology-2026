package amoba;

import javax.swing.SwingUtilities;

/**
 * A program belépési pontja: létrehozza a modellt és a nézetet.
 */
public class Amoba {

    /**
     * @param args a parancssori argumentumok (nem használjuk)
     */
    public static void main(String[] args) {
        // A felületet az eseménykezelő szálon (EDT) építjük fel. A modell
        // (Game) itt jön létre, és a nézet (AmobaGUI) megkapja: a nézet nem
        // maga készíti a modellt, így a tesztek és egy másik felület is
        // ugyanazt a Game osztályt használhatják.
        SwingUtilities.invokeLater(() -> new AmobaGUI(new Game(BoardSize.SMALL)));
    }

}
