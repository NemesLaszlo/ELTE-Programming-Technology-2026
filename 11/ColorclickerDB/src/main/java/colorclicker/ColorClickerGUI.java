package colorclicker;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Az ablak: a menü, és benne a tábla felülete (BoardGUI). Ez a 8. heti
 * ColorclickerOptimized ablaka, kiegészítve a RANGLISTÁVAL:
 * - a játék végén (gameOver) bekéri a nevet, elmenti az időt az
 *   adatbázisba (Leaderboard), megmutatja a legjobb időket, és új játékot
 *   indít ugyanakkora táblán;
 * - a Game ▸ High scores menü bármikor megmutatja az aktuális méret
 *   legjobb időit.
 *
 * Az ablak felépítése (komponensfa):
 * <pre>
 * JFrame "Color clicker"
 * ├── JMenuBar: Game menü (New ▸ 5x5, 10x10, 15x15, 20x20; High scores; Exit)
 * └── content pane (BorderLayout)
 *     ├── CENTER: boardGUI.getBoardPanel() – a gombrács (GridLayout)
 *     └── SOUTH:  boardGUI.getTimeLabel()  – az eltelt idő
 * </pre>
 *
 * Új játéknál a régi BoardGUI óráját leállítjuk, a komponenseit kivesszük az
 * ablakból, és egy új BoardGUI-t teszünk a helyükre. Az ablak mérete a
 * pack() hívással igazodik az új táblához.
 *
 * A nézet csak a backendet hívja: a játékhoz a Board-ot (a BoardGUI-n át),
 * a ranglistához a Leaderboardot, és DTO-kon át beszél vele. Az adatbázist a backend
 * fedi el: a nézet nem lát SQL-t, kapcsolatot, SQLException-t. Ha az
 * adatbázis nem érhető el (pl. nem fut a MySQL-konténer), a Leaderboard
 * StorageException-t dob, a nézet üzenetet mutat, és a játék megy tovább.
 *
 * @author pinter
 */
public class ColorClickerGUI {

    private static final int INITIAL_BOARD_SIZE = 10;
    private static final int[] BOARD_SIZES = {5, 10, 15, 20};
    /** Ennyi eredmény látszik a ranglistán. */
    private static final int TOP = 10;

    private final JFrame frame;
    /** A ranglista backendje. */
    private final Leaderboard leaderboard;
    private BoardGUI boardGUI;
    /** Az aktuális tábla mérete: ehhez tartozik a ranglista. */
    private int boardSize = INITIAL_BOARD_SIZE;

    /**
     * Felépíti és megjeleníti az ablakot.
     *
     * @param leaderboard a ranglista backendje (a main köti össze a tárolóval)
     */
    public ColorClickerGUI(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
        frame = new JFrame("Color clicker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(createMenuBar());

        boardGUI = new BoardGUI(boardSize, this::gameOver);
        frame.getContentPane().add(boardGUI.getBoardPanel(), BorderLayout.CENTER);
        frame.getContentPane().add(boardGUI.getTimeLabel(), BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenu newMenu = new JMenu("New");
        for (int boardSize : BOARD_SIZES) {
            JMenuItem sizeMenuItem = new JMenuItem(boardSize + "x" + boardSize);
            // A lambda a boardSize ciklusváltozót "befagyasztja" (a for-each
            // változója effectively final).
            sizeMenuItem.addActionListener(e -> newGame(boardSize));
            newMenu.add(sizeMenuItem);
        }

        JMenuItem highScoresMenuItem = new JMenuItem("High scores");
        // Windowson és Linuxon Ctrl+H, macOS-en Cmd+H.
        highScoresMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        highScoresMenuItem.addActionListener(e -> showHighScores());

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));

        JMenu gameMenu = new JMenu("Game");
        gameMenu.add(newMenu);
        gameMenu.add(highScoresMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(exitMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(gameMenu);
        return menuBar;
    }

    /**
     * Új játék a megadott méretű táblával: a régi tábla helyére új kerül.
     */
    private void newGame(int boardSize) {
        this.boardSize = boardSize;
        boardGUI.stop();
        frame.getContentPane().remove(boardGUI.getBoardPanel());
        frame.getContentPane().remove(boardGUI.getTimeLabel());

        boardGUI = new BoardGUI(boardSize, this::gameOver);
        frame.getContentPane().add(boardGUI.getBoardPanel(), BorderLayout.CENTER);
        frame.getContentPane().add(boardGUI.getTimeLabel(), BorderLayout.SOUTH);
        // Az ablak mérete igazodik az új táblához.
        frame.pack();
    }

    /**
     * A játék vége (a BoardGUI hívja): név bekérése, mentés, ranglista, új
     * játék ugyanakkora táblán.
     */
    private void gameOver() {
        long millis = boardGUI.getBoard().getElapsedMillis();
        String name = askName(millis);
        if (name != null) {
            try {
                leaderboard.save(name, boardSize, millis);
            } catch (StorageException e) {
                showDatabaseError(e);
            }
        }
        showHighScores();
        newGame(boardSize);
    }

    /**
     * Addig kérdez, amíg nem üres a név, vagy a játékos a Cancel-t választja.
     *
     * @return a név, vagy null, ha a játékos nem akarja menteni
     */
    private String askName(long millis) {
        String message = "You have won in " + HighScoresDialog.formatTime(millis) + ".\nYour name:";
        while (true) {
            String name = JOptionPane.showInputDialog(frame, message, "Congrats!", JOptionPane.PLAIN_MESSAGE);
            if (name == null) {
                return null;
            }
            if (Leaderboard.isValidName(name)) {
                return name.strip();
            }
            message = "Please enter a name (1-" + Leaderboard.MAX_NAME_LENGTH + " characters):";
        }
    }

    /**
     * Az aktuális méret legjobb idői. A játék közben is megnézhető; az óra
     * ilyenkor tovább jár, ez a játékos kockázata.
     */
    private void showHighScores() {
        try {
            List<HighScoreDto> best = leaderboard.best(boardSize, TOP);
            new HighScoresDialog(frame, boardSize, best).setVisible(true);
        } catch (StorageException e) {
            showDatabaseError(e);
        }
    }

    private void showDatabaseError(StorageException e) {
        JOptionPane.showMessageDialog(frame,
                "The database is not available:\n" + e.getMessage()
                + "\n\nIs the MySQL container running? (See Database.java and the README.)",
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

}
