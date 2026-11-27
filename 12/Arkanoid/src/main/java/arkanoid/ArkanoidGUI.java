package arkanoid;

import javax.swing.JFrame;

/**
 * Az ablak: egyetlen komponens van benne, a GameEngine (a játéktér).
 *
 * @author bli
 */
public class ArkanoidGUI {

    private final JFrame frame;
    private final GameEngine gameArea;

    public ArkanoidGUI() {
        frame = new JFrame("Arkanoid");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gameArea = new GameEngine();
        frame.getContentPane().add(gameArea);

        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
