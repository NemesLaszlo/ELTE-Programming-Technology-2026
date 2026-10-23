package amoba;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * A potyogós amőba felülete (az MV "V" betűje: a nézet).
 *
 * A nézet felépíti az ablakot, a menüt, a gombrácsot és az állapotsort. Az
 * eseménykezelői csak továbbítanak a modellnek (Game), majd a modell
 * állapota alapján frissítik a felületet. A nézetben nincs állapot és nincs
 * szabály: nem tudja, hova esik a jel, ki következik, ki nyert, ezt mind a
 * modelltől kérdezi.
 *
 * Mi történik egy kattintásra? (drop)
 * 1. ha az oszlopba nem lehet lépni (tele van, vagy vége a játéknak), nem
 *    történik semmi;
 * 2. a modell leejti a jelet, és visszaadja, hova esett;
 * 3. a nézet csak ezt az egy gombot és az állapotsort frissíti (ld. a
 *    ColorclickerOptimized projektet: csak azt frissítjük, ami változott);
 * 4. ha a modell szerint vége a játéknak, üzenet mutatja az eredményt, majd
 *    automatikusan új játék kezdődik ugyanakkora táblán.
 *
 * Az ablak felépítése (komponensfa):
 * <pre>
 * JFrame "Potyogós amőba"
 * ├── JMenuBar: Játék menü (Új játék ▸ 8 × 5, 10 × 6, 12 × 7; Kilépés)
 * └── content pane (BorderLayout)
 *     ├── CENTER: boardPanel (GridLayout, sorok × oszlopok gomb)
 *     └── SOUTH:  statusLabel – a tábla mérete, és hogy ki következik
 * </pre>
 */
public class AmobaGUI {

    /** Egy mező mérete képpontban. */
    private static final Dimension BUTTON_SIZE = new Dimension(60, 60);
    private static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 24);
    private static final Color X_COLOR = new Color(200, 30, 30);
    private static final Color O_COLOR = new Color(30, 60, 200);
    private static final int GAP = 5;

    /** A modell: a tábla, a soron következő játékos, a játék vége. */
    private final Game game;

    private final JFrame frame;
    private final JPanel boardPanel;
    private final JLabel statusLabel;
    /** A gombok; buttons[sor][oszlop] a modell (sor, oszlop) mezője. */
    private JButton[][] buttons;

    /**
     * Felépíti és megjeleníti az ablakot a modell aktuális táblájával.
     *
     * @param game a modell
     */
    public AmobaGUI(Game game) {
        this.game = game;

        frame = new JFrame("Potyogós amőba");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(createMenuBar());

        boardPanel = new JPanel();
        statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));

        JPanel content = new JPanel(new BorderLayout());
        content.add(boardPanel, BorderLayout.CENTER);
        content.add(statusLabel, BorderLayout.SOUTH);
        frame.setContentPane(content);

        rebuildBoard();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenu newGameMenu = new JMenu("Új játék");
        // Minden táblamérethez egy menüpont; a felirat a BoardSize.toString.
        for (BoardSize size : BoardSize.values()) {
            JMenuItem sizeItem = new JMenuItem(size.toString());
            sizeItem.addActionListener(e -> newGame(size));
            newGameMenu.add(sizeItem);
        }

        JMenuItem exitItem = new JMenuItem("Kilépés");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> System.exit(0));

        JMenu gameMenu = new JMenu("Játék");
        gameMenu.setMnemonic(KeyEvent.VK_J);
        gameMenu.add(newGameMenu);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(gameMenu);
        return menuBar;
    }

    /**
     * Új játék a menüből választott méretű táblán. A gombrács újraépül, és az
     * ablak mérete a pack() hívással igazodik az új táblához.
     */
    private void newGame(BoardSize size) {
        game.newGame(size);
        rebuildBoard();
        frame.pack();
    }

    /**
     * Újraépíti a gombrácsot a modell táblájának mérete szerint, és
     * frissíti a teljes felületet.
     */
    private void rebuildBoard() {
        Board board = game.getBoard();
        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(board.getRows(), board.getColumns()));
        buttons = new JButton[board.getRows()][board.getColumns()];
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getColumns(); col++) {
                JButton button = new JButton();
                button.setPreferredSize(BUTTON_SIZE);
                button.setFont(BUTTON_FONT);
                // A jel az oszlopba esik, ezért az eseménykezelőnek elég az
                // oszlopot tudnia: egy oszlop minden gombja ugyanazt csinálja.
                button.addActionListener(new ColumnListener(col));
                buttons[row][col] = button;
                boardPanel.add(button);
            }
        }
        refreshAll();
        // A layout-kezelő újraszámolja a gombok helyét, majd újrarajzolunk.
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    /**
     * Kattintás egy oszlopra: a modell lép, a nézet a változott mezőt
     * frissíti, és a játék végén új játékot indít.
     */
    private void drop(int col) {
        // Tele oszlopba, vagy a játék vége után nem lehet lépni. Ezt a modell
        // dönti el, a nézet csak megkérdezi.
        if (!game.canDrop(col)) {
            return;
        }
        Position landed = game.drop(col);
        // Csak az az egy gomb változott, ahova a jel esett.
        refreshField(landed);
        refreshStatus();
        if (game.isOver()) {
            showResult();
            // A feladat szerint a játék végén automatikusan új játék kezdődik.
            game.newGame();
            refreshAll();
        }
    }

    /**
     * Az eredmény kiírása a játék végén. A modális ablak bezárásáig a tábla
     * még a befejezett játékot mutatja.
     */
    private void showResult() {
        String message = game.isDraw() ? "Döntetlen!" : game.getWinner() + " nyert!";
        JOptionPane.showMessageDialog(frame, message + "\nÚj játék kezdődik.",
                "Vége a játéknak", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Egy gomb beállítása a modell megfelelő mezője alapján. Ez az egyetlen
     * hely, ahol a nézet egy mező állapotát olvassa.
     */
    private void refreshField(Position position) {
        Player owner = game.getBoard().get(position);
        JButton button = buttons[position.row()][position.col()];
        if (owner == null) {
            button.setText("");
        } else {
            button.setText(owner.name());
            button.setForeground(owner == Player.X ? X_COLOR : O_COLOR);
        }
    }

    private void refreshStatus() {
        Board board = game.getBoard();
        String size = "Tábla: " + board.getColumns() + " × " + board.getRows() + "      ";
        if (game.isDraw()) {
            statusLabel.setText(size + "Döntetlen");
        } else if (game.isOver()) {
            statusLabel.setText(size + game.getWinner() + " nyert");
        } else {
            statusLabel.setText(size + game.getCurrentPlayer() + " következik");
        }
    }

    /**
     * A teljes felület frissítése a modellből: minden mező és az állapotsor.
     * Új játéknál kell; egy lépés után elég a változott mezőt frissíteni.
     */
    private void refreshAll() {
        Board board = game.getBoard();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getColumns(); col++) {
                refreshField(new Position(row, col));
            }
        }
        refreshStatus();
    }

    /**
     * Egy oszlop gombjainak eseménykezelője. Névvel rendelkező belső osztály,
     * mert paramétere van (az oszlop), és minden gomb kap egyet. Csak
     * továbbít: a szabályok a modellben vannak.
     */
    class ColumnListener implements ActionListener {

        private final int col;

        public ColumnListener(int col) {
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            drop(col);
        }

    }

}
