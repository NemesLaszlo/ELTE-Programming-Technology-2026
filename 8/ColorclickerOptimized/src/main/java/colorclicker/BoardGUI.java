package colorclicker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A tábla felülete (az MV "V" betűje: a nézet). OPTIMALIZÁLT változat.
 *
 * A nézet felépíti a gombrácsot és az órát, az eseménykezelője meghívja a
 * modellt (Board.click), majd a modelltől visszakapott lista alapján CSAK A
 * VÁLTOZOTT gombokat frissíti. A nézetben nincs állapot és nincs szabály:
 * nem tudja, hány mező színeződik ki, és milyen színnel, ezt mind a modell
 * dönti el.
 *
 * Mi történik egy kattintásra? (click)
 * 1. ha a mezőre nem lehet kattintani (már színes), nem történik semmi;
 * 2. a modell kiszínezi a mezőt és a véletlen mezőket, és visszaadja a
 *    helyüket (5 mező);
 * 3. a nézet ezt az 5 gombot frissíti: a 20 × 20-as táblán 5-öt a 400
 *    helyett;
 * 4. frissül az óra, és ha a modell szerint vége a játéknak, az óra megáll
 *    és üzenet jelenik meg.
 *
 * Hasonlítsuk össze a Colorclicker projekt BoardGUI osztályával: ott a
 * szabályok az eseménykezelőben vannak, és a refresh() minden gombot
 * újrarajzol.
 *
 * @author pinter
 */
public class BoardGUI {

    /** Egy gomb mérete képpontban. */
    private static final Dimension BUTTON_SIZE = new Dimension(80, 40);
    /** Az óra ennyi ezredmásodpercenként frissül. */
    private static final int TIMER_DELAY = 10;

    /** A modell: a tábla, a kattintások, az idő és a szabályok. */
    private final Board board;
    /** A gombok; buttons[sor][oszlop] a modell (sor, oszlop) mezője. */
    private final JButton[][] buttons;
    private final JPanel boardPanel;
    private final JLabel timeLabel;
    /**
     * Az óra. A Timer a Swing része, az eseménykezelője az EDT-n fut, ezért
     * nyúlhat a komponensekhez. A nézeté, mert a megjelenítés frissítése a
     * dolga; az időt a modell méri.
     */
    private final Timer timer;

    /**
     * Felépíti a gombrácsot és az órát, és elindítja a játékot.
     *
     * @param boardSize a tábla mérete
     */
    public BoardGUI(int boardSize) {
        board = new Board(boardSize);
        boardPanel = new JPanel(new GridLayout(boardSize, boardSize));
        buttons = new JButton[boardSize][boardSize];
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                JButton button = new JButton();
                button.setPreferredSize(BUTTON_SIZE);
                // Minden gomb saját eseménykezelőt kap, ami tudja a gomb
                // helyét (ld. ButtonListener).
                button.addActionListener(new ButtonListener(row, col));
                buttons[row][col] = button;
                boardPanel.add(button);
            }
        }

        timeLabel = new JLabel(" ");
        timeLabel.setHorizontalAlignment(JLabel.RIGHT);
        timer = new Timer(TIMER_DELAY, e -> refreshTime());

        refreshAll();
        timer.start();
    }

    /**
     * Kattintás egy mezőre: a modell módosítása, majd a változott gombok
     * frissítése.
     */
    private void click(int row, int col) {
        // A már színes mezőre kattintás hatástalan. Ezt a modell dönti el,
        // a nézet csak megkérdezi.
        if (!board.canClick(row, col)) {
            return;
        }
        List<Position> changed = board.click(row, col);
        // Az optimalizálás: csak a változott gombokat frissítjük.
        for (Position position : changed) {
            refreshField(position);
        }
        refreshTime();
        if (board.isOver()) {
            timer.stop();
            JOptionPane.showMessageDialog(boardPanel,
                    "You have won in " + board.getElapsedMillis() + " ms.", "Congrats!",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Egy gomb beállítása a modell megfelelő mezője alapján. Ez az egyetlen
     * hely, ahol a nézet egy mező állapotát olvassa.
     */
    private void refreshField(Position position) {
        Field field = board.get(position);
        JButton button = buttons[position.row()][position.col()];
        if (field.isEmpty()) {
            button.setBackground(null);
            button.setText("");
        } else {
            // A modell egész számként tárolja a színt, a Color itt készül.
            button.setBackground(new Color(field.getRgb()));
            button.setText(String.valueOf(field.getNumber()));
        }
    }

    private void refreshTime() {
        timeLabel.setText(board.getElapsedMillis() + " ms");
    }

    /**
     * A teljes felület frissítése a modellből. Csak induláskor kell: utána
     * minden kattintás pontosan a változott gombokat frissíti.
     */
    private void refreshAll() {
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                refreshField(new Position(row, col));
            }
        }
        refreshTime();
    }

    /**
     * Megállítja az órát. Akkor kell hívni, ha a táblát eldobjuk (új játék),
     * különben a Timer tovább járna, és a régi tábla a memóriában maradna.
     */
    public void stop() {
        timer.stop();
    }

    public JPanel getBoardPanel() {
        return boardPanel;
    }

    public JLabel getTimeLabel() {
        return timeLabel;
    }

    /**
     * Egy gomb eseménykezelője. Névvel rendelkező belső osztály, mert
     * paramétere van (a gomb helye), és minden gomb kap egyet. Csak
     * továbbít: a szabályok a modellben vannak.
     */
    class ButtonListener implements ActionListener {

        private final int row;
        private final int col;

        public ButtonListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            click(row, col);
        }

    }

}
