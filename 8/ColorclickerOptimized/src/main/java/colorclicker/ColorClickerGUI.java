package colorclicker;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Az ablak: a menü, és benne a tábla felülete (BoardGUI).
 *
 * Az ablak felépítése (komponensfa):
 * <pre>
 * JFrame "Color clicker"
 * ├── JMenuBar: Game menü (New ▸ 5x5, 10x10, 15x15, 20x20; Exit)
 * └── content pane (BorderLayout)
 *     ├── CENTER: boardGUI.getBoardPanel() – a gombrács (GridLayout)
 *     └── SOUTH:  boardGUI.getTimeLabel()  – az eltelt idő
 * </pre>
 *
 * Új játéknál a régi BoardGUI óráját leállítjuk, a komponenseit kivesszük az
 * ablakból, és egy új BoardGUI-t teszünk a helyükre. Az ablak mérete a
 * pack() hívással igazodik az új táblához.
 *
 * @author pinter
 */
public class ColorClickerGUI {

    private static final int INITIAL_BOARD_SIZE = 10;
    private static final int[] BOARD_SIZES = {5, 10, 15, 20};

    private final JFrame frame;
    private BoardGUI boardGUI;

    /**
     * Felépíti és megjeleníti az ablakot.
     */
    public ColorClickerGUI() {
        frame = new JFrame("Color clicker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(createMenuBar());

        boardGUI = new BoardGUI(INITIAL_BOARD_SIZE);
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

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));

        JMenu gameMenu = new JMenu("Game");
        gameMenu.add(newMenu);
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
        boardGUI.stop();
        frame.getContentPane().remove(boardGUI.getBoardPanel());
        frame.getContentPane().remove(boardGUI.getTimeLabel());

        boardGUI = new BoardGUI(boardSize);
        frame.getContentPane().add(boardGUI.getBoardPanel(), BorderLayout.CENTER);
        frame.getContentPane().add(boardGUI.getTimeLabel(), BorderLayout.SOUTH);
        // Az ablak mérete igazodik az új táblához.
        frame.pack();
    }

}
