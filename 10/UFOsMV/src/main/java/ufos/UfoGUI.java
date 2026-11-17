package ufos;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Az ablak: a NÉZET másik fele. Felépíti a rajzterületet, a gombsort és az
 * állapotsort, és egyetlen Timerrel hajtja az animációt.
 *
 * Az eseménykezelők csak továbbítanak a modellnek, majd frissítést kérnek:
 * - a Timer: space.step(), majd drawArea.repaint();
 * - a számgombok: space.addUfos / removeUfos;
 * - a "Change background" gomb: space.newBackground().
 *
 * Hasonlítsuk össze a UFOs projekt UFOGUI osztályával: ott az UFO-k
 * listája, a hozzáadás szabálya és a színváltás esélye mind itt volt, és
 * két Timer kellett hozzá.
 */
public class UfoGUI {

    /** A rajzterület kezdeti mérete képpontban. */
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    /** Ennyi ezredmásodpercenként lép az animáció (kb. 33 képkocka / mp). */
    private static final int FRAME_DELAY = 30;

    /** A modell. */
    private final Space space;
    private final JFrame frame;
    private final DrawArea drawArea;
    private final JLabel statusLabel;
    private final Timer timer;

    public UfoGUI(int ufoCount) {
        space = new Space(WIDTH, HEIGHT, ufoCount);

        frame = new JFrame("UFOs (MV)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        drawArea = new DrawArea(space);
        frame.add(drawArea, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton backgroundButton = new JButton("Change background");
        backgroundButton.addActionListener(e -> {
            space.newBackground();
            drawArea.repaint();
        });
        buttonPanel.add(backgroundButton);
        for (int amount : new int[]{-1000, -100, -10, -1, 1, 10, 100, 1000}) {
            JButton button = new JButton(String.valueOf(amount));
            // Egy lambda is elég: a gomb csak továbbít a modellnek.
            button.addActionListener(e -> changeUfoCount(amount));
            buttonPanel.add(button);
        }
        frame.add(buttonPanel, BorderLayout.SOUTH);

        statusLabel = new JLabel();
        frame.add(statusLabel, BorderLayout.NORTH);
        refreshStatus();

        // Az animáció: a modell lép, a nézet újrarajzol. A Timer eseménykezelője
        // az EDT-n fut, ezért nyúlhat a komponensekhez.
        timer = new Timer(FRAME_DELAY, e -> {
            space.step();
            drawArea.repaint();
        });
        timer.start();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void changeUfoCount(int amount) {
        if (amount > 0) {
            space.addUfos(amount);
        } else {
            space.removeUfos(-amount);
        }
        refreshStatus();
        // Nem kötelező: a Timer hamarosan úgyis újrarajzol. Így viszont a gomb
        // hatása azonnal látszik.
        drawArea.repaint();
    }

    private void refreshStatus() {
        statusLabel.setText(" UFOs: " + space.getUfoCount());
    }

}
