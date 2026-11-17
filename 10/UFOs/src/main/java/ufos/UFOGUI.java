package ufos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Az ablak: a rajzterület, alatta a gombsor, és a két Timer, ami az
 * animációt hajtja. KIINDULÓ változat.
 *
 * Az animáció elve: egy javax.swing.Timer 30 ezredmásodpercenként
 * (kb. 33 képkocka/mp) lépteti az UFO-kat, majd repaint()-et kér. A Timer
 * eseménykezelője az EDT-n fut, ezért nyúlhat a komponensekhez.
 *
 * Mi a gond ezzel a változattal?
 * - Az UFO-k listája, a hozzáadás/elvétel szabálya (UFOAdder), a léptetés
 *   és a színváltás szabálya (a colorTimer-ben: minden 10 ms-ban 1/1000
 *   eséllyel) mind a felületben van; teszt nem írható rájuk.
 * - A gombok eseménykezelői és a Timer ugyanazt a listát módosítják, a
 *   DrawArea pedig ugyanazt rajzolja: három osztály osztozik egy
 *   ArrayList-en, és egyik sem "gazdája".
 *
 * @author bli
 */
public class UFOGUI {

    private final JFrame frame;
    private final DrawArea drawArea;
    private final ArrayList<UFO> ufos;
    private final Timer timer;
    private final Timer colorTimer;

    public UFOGUI(int numUFOs) {
        frame = new JFrame("UFOs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ufos = new ArrayList<>();
        for (int i = 0; i < numUFOs; i++) {
            ufos.add(new UFO());
        }
        drawArea = new DrawArea(ufos);
        drawArea.setPreferredSize(new Dimension(400, 400));
        frame.getContentPane().add(BorderLayout.CENTER, drawArea);

        JPanel buttonPanel = new JPanel();
        JButton backgroundButton = new JButton("Change background");
        backgroundButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                drawArea.updateBackground();
            }
        });
        buttonPanel.add(backgroundButton);
        for (int amount : new int[]{-1000, -100, -10, -1, 1, 10, 100, 1000}) {
            JButton button = new JButton(String.valueOf(amount));
            button.addActionListener(new UFOAdder(amount));
            buttonPanel.add(button);
        }
        frame.getContentPane().add(BorderLayout.SOUTH, buttonPanel);

        // Az animáció: léptetés, majd újrarajzolás kérése.
        timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Dimension d = drawArea.getSize();
                for (UFO ufo : ufos) {
                    ufo.move(d.width, d.height);
                }
                frame.repaint();
            }
        });
        // A színváltás: 10 ms-onként minden UFO 1/1000 eséllyel új színt kap.
        colorTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (UFO ufo : ufos) {
                    if (Math.random() < 1e-3) {
                        ufo.updateColor();
                    }
                }
            }
        });
        colorTimer.start();
        timer.start();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * A számgombok eseménykezelője: pozitív szám esetén ennyi UFO-t ad hozzá,
     * negatív esetén ennyit vesz el (ha van).
     */
    class UFOAdder implements ActionListener {

        private final int amount;

        public UFOAdder(int amount) {
            this.amount = amount;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (amount > 0) {
                for (int i = 0; i < amount; i++) {
                    ufos.add(new UFO());
                }
            } else {
                for (int i = 0; i < -amount; i++) {
                    if (!ufos.isEmpty()) {
                        ufos.remove(0);
                    }
                }
            }
        }
    }

}
