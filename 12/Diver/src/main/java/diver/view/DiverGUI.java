package diver.view;

import diver.model.Game;
import diver.model.HighScoreDto;
import diver.model.Leaderboard;
import diver.model.StorageException;
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
 * Az ablak: a NÉZET másik fele. A menü, az állapotsor (pálya, gyöngyök,
 * életek, pont, idő), a pálya (GamePanel), és a Timer, ami a játékot
 * hajtja.
 *
 * A játék menete:
 * 1. a Timer TICK_MS-enként meghívja a modell tick-jét (a cápák minden
 *    tizedik ütemben lépnek), újrarajzolja a pályát és frissíti az
 *    állapotsort; ha közben pályát váltott a játék, az ablak mérete az új
 *    pályához igazodik (pack);
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
public class DiverGUI {

    /** Ennyi eredmény látszik a ranglistán. */
    private static final int TOP = 10;

    private final JFrame frame;
    private final GamePanel gamePanel;
    private final JLabel levelLabel = new JLabel();
    private final JLabel pearlsLabel = new JLabel();
    private final JLabel livesLabel = new JLabel();
    private final JLabel scoreLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final Timer timer;
    /** A ranglista backendje. */
    private final Leaderboard leaderboard;
    /** A modell. */
    private Game game;
    /** Az utoljára kirajzolt pálya sorszáma: pályaváltáskor pack() kell. */
    private int shownLevel;

    /**
     * @param leaderboard a ranglista backendje (a main köti össze a tárolóval)
     */
    public DiverGUI(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
        game = new Game();
        shownLevel = game.getLevelNumber();

        frame = new JFrame("Diver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(createMenuBar());

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        statusBar.add(levelLabel);
        statusBar.add(pearlsLabel);
        statusBar.add(livesLabel);
        statusBar.add(scoreLabel);
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
     * Egy ütem: a modell lép, a nézet frissül; pályaváltáskor új ablakméret;
     * a játék végén mentés és új játék.
     */
    private void tick() {
        game.tick();
        if (game.getLevelNumber() != shownLevel) {
            shownLevel = game.getLevelNumber();
            gamePanel.updatePreferredSize();
            frame.pack();
        }
        gamePanel.repaint();
        refreshStatus();
        if (game.isOver()) {
            timer.stop();
            gameOver();
        }
    }

    private void refreshStatus() {
        levelLabel.setText("Level: " + game.getLevelNumber() + "/" + game.getLevelCount());
        pearlsLabel.setText("Pearls left: " + game.getPearlsLeft());
        livesLabel.setText("Lives: " + game.getLives());
        scoreLabel.setText("Score: " + game.getScore());
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
        String message = "Game over! You collected " + game.getScore() + " pearls in "
                + timeLabel.getText().substring("Time: ".length()) + ".\nYour name:";
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
        shownLevel = game.getLevelNumber();
        gamePanel.setGame(game);
        frame.pack();
        refreshStatus();
        timer.restart();
    }

}
