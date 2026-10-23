package colorclicker;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * Új játéknál a régi BoardGUI-t egyszerűen eldobjuk, és egy újat teszünk
 * az ablakba. (A régi órája közben tovább jár; ezt az optimalizált
 * változat állítja le.)
 *
 * @author pinter
 */
public class ColorClickerGUI {

    private JFrame frame;
    private BoardGUI boardGUI;

    private final int INITIAL_BOARD_SIZE = 10;

    public ColorClickerGUI() {
        frame = new JFrame("Color clicker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        boardGUI = new BoardGUI(INITIAL_BOARD_SIZE);
        frame.getContentPane().add(boardGUI.getBoardPanel(), BorderLayout.CENTER);
        frame.getContentPane().add(boardGUI.getTimeLabel(), BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        JMenu gameMenu = new JMenu("Game");
        menuBar.add(gameMenu);
        JMenu newMenu = new JMenu("New");
        gameMenu.add(newMenu);
        int[] boardSizes = new int[]{5, 10, 15, 20};
        for (int boardSize : boardSizes) {
            JMenuItem sizeMenuItem = new JMenuItem(boardSize + "x" + boardSize);
            newMenu.add(sizeMenuItem);
            // Névtelen osztály: a boardSize ciklusváltozót "befagyasztja".
            sizeMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.getContentPane().remove(boardGUI.getBoardPanel());
                    frame.getContentPane().remove(boardGUI.getTimeLabel());
                    boardGUI = new BoardGUI(boardSize);
                    frame.getContentPane().add(boardGUI.getBoardPanel(),
                            BorderLayout.CENTER);
                    frame.getContentPane().add(boardGUI.getTimeLabel(), BorderLayout.SOUTH);
                    // Az ablak mérete igazodik az új táblához.
                    frame.pack();
                }
            });
        }
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        gameMenu.add(exitMenuItem);
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
