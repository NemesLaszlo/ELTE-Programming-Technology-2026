package colorclicker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A tábla felülete – KIINDULÓ, nem optimalizált változat.
 *
 * Ez az osztály egyszerre
 * - felépíti a gombrácsot és az időkijelzőt,
 * - tárolja a játék állapotát (hányadik kattintásnál járunk, mely mezők
 *   jöhetnek még sorra, mikor indult a játék),
 * - és tartalmazza a szabályokat: egy kattintásra a mező és még 4 véletlen
 *   üres mező kapja ugyanazt a véletlen színt és a kattintás sorszámát.
 *
 * Mi történik egy kattintásra? (ButtonListener.actionPerformed)
 * 1. ha a mező már színes, nem történik semmi;
 * 2. különben a mező és 4 további véletlen üres mező színt és sorszámot kap
 *    (ez a modell módosítása);
 * 3. a refresh() MINDEN gomb hátterét és feliratát újra beállítja: a
 *    20 × 20-as táblán 400 gombot, pedig csak 5 változott;
 * 4. a refresh() végül megnézi, vége van-e a játéknak, és ha igen, megállítja
 *    az órát és üzenetet mutat.
 *
 * A 3. lépés a hét feladata: az optimalizált változatban csak azokat a
 * gombokat frissítjük, amelyek állapota változott. Ehhez tudnunk kell, melyek
 * ezek, és ezt a modellnek kell megmondania (ld. ColorclickerOptimized,
 * a Board.click visszatérési értéke).
 *
 * @author pinter
 */
public class BoardGUI {

    private JButton[][] buttons;
    private Board board;
    private JPanel boardPanel;
    private JLabel timeLabel;
    /**
     * Az összes mező helye, induláskor összekeverve. Kattintáskor a lista
     * végéről vesszük le a következő színezendő mezőket, így minden mező
     * legfeljebb egyszer kerül sorra.
     */
    private ArrayList<Point> points;

    private Random random = new Random();
    /** Hányadik kattintásnál járunk; ez kerül a mezőkre feliratként. */
    private int clickNum = 0;
    private long startTime;
    private Timer timer;

    /** Egy kattintásra ennyi további mező színeződik ki. */
    private final int NUM_COLORED_FIELDS = 4;

    public BoardGUI(int boardSize) {
        board = new Board(boardSize);
        boardPanel = new JPanel();
        points = new ArrayList<>();
        boardPanel.setLayout(new GridLayout(board.getBoardSize(), board.getBoardSize()));
        buttons = new JButton[board.getBoardSize()][board.getBoardSize()];
        for (int i = 0; i < board.getBoardSize(); ++i) {
            for (int j = 0; j < board.getBoardSize(); ++j) {
                JButton button = new JButton();
                // Minden gomb saját eseménykezelőt kap, ami tudja a gomb
                // helyét (ld. ButtonListener).
                button.addActionListener(new ButtonListener(i, j));
                button.setPreferredSize(new Dimension(80, 40));
                buttons[i][j] = button;
                boardPanel.add(button);
                points.add(new Point(i, j));
            }
        }
        Collections.shuffle(points);

        timeLabel = new JLabel(" ");
        timeLabel.setHorizontalAlignment(JLabel.RIGHT);
        // Az óra: 10 ms-onként kiírja az eltelt időt. A Timer a Swing része,
        // az eseménykezelője az EDT-n fut, ezért nyúlhat a komponensekhez.
        timer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLabel.setText(elapsedTime() + " ms");
            }
        });
        startTime = System.currentTimeMillis();
        timer.start();
    }

    public long elapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * A TELJES tábla újrarajzolása: minden gomb hátterét és feliratát újra
     * beállítjuk, azokét is, amelyek nem változtak. A setBackground és a
     * setText mindegyike újrarajzolást kér a gombra, ezért egy 20 × 20-as
     * táblán kattintásonként 800 ilyen hívás történik 10 helyett.
     */
    public void refresh() {
        for (int i = 0; i < board.getBoardSize(); ++i) {
            for (int j = 0; j < board.getBoardSize(); ++j) {
                Field field = board.get(i, j);
                JButton button = buttons[i][j];
                button.setBackground(field.getColor());
                if (field.getColor() != null) {
                    button.setText(String.valueOf(field.getNumber()));
                } else {
                }
            }
        }
        if (board.isOver()) {
            timer.stop();
            JOptionPane.showMessageDialog(boardPanel, "You have won in " + elapsedTime() + " ms.", "Congrats!",
                    JOptionPane.PLAIN_MESSAGE);

        }
    }

    public JPanel getBoardPanel() {
        return boardPanel;
    }

    /**
     * Egy gomb eseménykezelője. Itt vannak a játék szabályai: ez dönti el,
     * mi színeződik ki és milyen színnel.
     */
    class ButtonListener implements ActionListener {

        private int x, y;

        public ButtonListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Már színes mezőre kattintva nem történik semmi.
            if (board.get(x, y).getColor() == null) {
                Color color = new Color(random.nextInt(256),
                        random.nextInt(256), random.nextInt(256));
                board.get(x, y).setColor(color);
                board.get(x, y).setNumber(++clickNum);
                // Még NUM_COLORED_FIELDS üres mező ugyanezt a színt és
                // sorszámot kapja. A már színes mezőket átugorjuk, de azok
                // is kikerülnek a listából.
                // (Ha a mezők száma nem osztható 5-tel, a lista előbb
                // kiürülhet, mint ahogy 4 üres mezőt találnánk; a menüben
                // választható méreteknél ez nem fordul elő.)
                for (int i = 0; i < NUM_COLORED_FIELDS;) {
                    Point point = points.remove(points.size() - 1);
                    if (board.get(point).getColor() == null) {
                        board.get(point).setColor(color);
                        board.get(point).setNumber(clickNum);
                        i++;
                    }
                }
                refresh();
            }
        }
    }

    public JLabel getTimeLabel() {
        return timeLabel;
    }

}
