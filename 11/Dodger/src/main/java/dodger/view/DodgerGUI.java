package dodger.view;

import dodger.model.Game;
import dodger.model.HighScoreDto;
import dodger.model.Leaderboard;
import dodger.model.StorageException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * Az ablak: a NÉZET másik fele. A menü, az állapotsor (pont, élet, idő), a
 * játéktér (GamePanel), és a Timer, ami a játékot hajtja.
 *
 * A játék menete:
 * 1. a Timer TICK_MS-enként meghívja a modell tick-jét, újrarajzolja a
 *    játékteret és frissíti az állapotsort;
 * 2. ha a modell szerint vége, a Timer megáll, egy felugró ablak bekéri a
 *    nevet, az eredmény az adatbázisba kerül (Leaderboard), megjelenik a
 *    ranglista, és automatikusan új játék indul;
 * 3. a Game ▸ High scores menü bármikor megmutatja a ranglistát (közben a
 *    játék áll), a Game ▸ New game új játékot kezd.
 *
 * A nézet csak a BACKENDET hívja: a játékhoz a Game-et, a ranglistához a
 * Leaderboardot, és DTO-kat kap. Az adatbázist a backend fedi el: a nézet
 * nem lát SQL-t, kapcsolatot, SQLException-t. Ha az adatbázis nem érhető el
 * (pl. nem fut a MySQL-konténer), a Leaderboard StorageException-t dob, a
 * nézet üzenetet mutat, és a játék attól még játszható.
 */
public class DodgerGUI {

    /** Ennyi eredmény látszik a ranglistán. */
    private static final int TOP = 10;

    private final JFrame frame;
    private final GamePanel gamePanel;
    private final JLabel scoreLabel = new JLabel();
    private final JLabel livesLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final Timer timer;
    /** A ranglista backendje. */
    private final Leaderboard leaderboard;
    /** A modell. */
    private Game game;

    /**
     * @param leaderboard a ranglista backendje (a main köti össze a tárolóval)
     */
    public DodgerGUI(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
        game = new Game();

        frame = new JFrame("Dodger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(createMenuBar());

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 4));
        statusBar.add(scoreLabel);
        statusBar.add(livesLabel);
        statusBar.add(timeLabel);
        frame.add(statusBar, BorderLayout.NORTH);

        gamePanel = new GamePanel(game);
        frame.add(gamePanel, BorderLayout.CENTER);

        timer = new Timer(Game.TICK_MS, e -> tick());

        refreshStatus();
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        timer.start();
    }

    private JMenuBar createMenuBar() {
        // A menü gyorsbillentyűje: Windowson és Linuxon Ctrl, macOS-en Cmd.
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic(KeyEvent.VK_G);

        JMenuItem newGame = new JMenuItem("New game");
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
        newGame.addActionListener(e -> newGame());
        gameMenu.add(newGame);

        JMenuItem highScoresItem = new JMenuItem("High scores");
        highScoresItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, shortcut));
        highScoresItem.addActionListener(e -> showHighScores());
        gameMenu.add(highScoresItem);

        gameMenu.addSeparator();
        JMenuItem exit = new JMenuItem("Exit");
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcut));
        exit.addActionListener(e -> System.exit(0));
        gameMenu.add(exit);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(gameMenu);
        return menuBar;
    }

    /**
     * Egy ütem: a modell lép, a nézet frissül; a játék végén mentés és új
     * játék.
     */
    private void tick() {
        game.tick();
        gamePanel.repaint();
        refreshStatus();
        if (game.isOver()) {
            timer.stop();
            gameOver();
        }
    }

    private void refreshStatus() {
        scoreLabel.setText("Score: " + game.getScore());
        livesLabel.setText("Lives: " + game.getLives());
        long seconds = game.getElapsedMillis() / 1000;
        timeLabel.setText(String.format("Time: %d:%02d", seconds / 60, seconds % 60));
    }

    /**
     * A játék vége: név bekérése, mentés, ranglista, új játék.
     */
    private void gameOver() {
        String name = askName();
        if (name != null) {
            try {
                leaderboard.save(name, game.getScore());
            } catch (StorageException e) {
                showDatabaseError(e);
            }
        }
        showHighScores();
        newGame();
    }

    /**
     * Addig kérdez, amíg nem üres a név, vagy a játékos a Cancel-t választja.
     *
     * @return a név, vagy null, ha a játékos nem akarja menteni
     */
    private String askName() {
        String message = "Game over! Your score: " + game.getScore()
                + " in " + timeLabel.getText().substring("Time: ".length()) + ".\nYour name:";
        while (true) {
            String name = JOptionPane.showInputDialog(frame, message, "Game over", JOptionPane.PLAIN_MESSAGE);
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
     * A ranglista: a játék áll, amíg az ablak nyitva van.
     */
    private void showHighScores() {
        boolean wasRunning = timer.isRunning();
        timer.stop();
        try {
            List<HighScoreDto> top = leaderboard.top(TOP);
            new HighScoresDialog(frame, top).setVisible(true);
        } catch (StorageException e) {
            showDatabaseError(e);
        }
        if (wasRunning) {
            timer.start();
        }
    }

    private void showDatabaseError(StorageException e) {
        JOptionPane.showMessageDialog(frame,
                "The database is not available:\n" + e.getMessage()
                + "\n\nIs the MySQL container running? (See Database.java and the README.)",
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    private void newGame() {
        game = new Game();
        gamePanel.setGame(game);
        refreshStatus();
        timer.restart();
    }

}
