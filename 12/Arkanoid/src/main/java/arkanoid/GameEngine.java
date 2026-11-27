package arkanoid;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * A játéktér: a játék állapota, a szabályok, a billentyűzet ÉS a rajzolás
 * egy osztályban. Egy kis példához elég; a 3. beadandóhoz a Diver minta
 * szerkezete (modell külön, tesztekkel) az ajánlott.
 *
 * Amit érdemes megfigyelni:
 * - KÉPEK: a sprite-ok nem alakzatok, hanem PNG/JPG képek (ImageIcon), a
 *   data/images mappából; a drawImage rajzolja őket a sprite méretére
 *   nyújtva;
 * - BILLENTYŰZET key bindinggel: az InputMap a billentyűhöz egy nevet
 *   rendel, az ActionMap a névhez egy akciót; a WHEN_IN_FOCUSED_WINDOW miatt
 *   nem kell a panelnek fókuszban lennie;
 * - PÁLYAFÁJLOK: a Level szöveges fájlból olvassa a téglákat
 *   (data/levels/level00.txt ...); ha elfogytak a téglák, jön a következő;
 * - A JÁTÉKHUROK: a Timer minden képkockán (1000 / FPS ms) lépteti a labdát
 *   és az ütőt, kezeli az ütközéseket, majd repaint-et kér.
 *
 * Irányítás: balra / jobbra nyíl vagy A / D: az ütő elindul abba az
 * irányba; lefelé nyíl vagy S: megáll; Escape: szünet.
 *
 * @author bli
 */
@SuppressWarnings("serial") // a JPanel Serializable, de a panelt nem mentjük fájlba
public final class GameEngine extends JPanel {

    /** A játéktér mérete képpontban. */
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private static final int FPS = 240;
    private static final int PADDLE_Y = 550;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 20;
    private static final int PADDLE_MOVEMENT = 2;
    private static final int BALL_RADIUS = 20;
    /** Ennyi pályafájl van a data/levels mappában: level00.txt ... */
    private static final int LEVEL_COUNT = 3;

    private final Image background;
    private final Image paddleImage;
    private final Image ballImage;
    private final Timer newFrameTimer;

    private boolean paused;
    private int levelNum;
    private Level level;
    private Ball ball;
    private Paddle paddle;

    public GameEngine() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        background = new ImageIcon("data/images/background.jpg").getImage();
        paddleImage = new ImageIcon("data/images/paddle.png").getImage();
        ballImage = new ImageIcon("data/images/ball.png").getImage();

        bind("LEFT", "left", () -> paddle.setVelX(-PADDLE_MOVEMENT));
        bind("A", "left", () -> paddle.setVelX(-PADDLE_MOVEMENT));
        bind("RIGHT", "right", () -> paddle.setVelX(PADDLE_MOVEMENT));
        bind("D", "right", () -> paddle.setVelX(PADDLE_MOVEMENT));
        bind("DOWN", "stop", () -> paddle.setVelX(0));
        bind("S", "stop", () -> paddle.setVelX(0));
        bind("ESCAPE", "pause", () -> paused = !paused);

        restart();
        newFrameTimer = new Timer(1000 / FPS, e -> newFrame());
        newFrameTimer.start();
    }

    /**
     * Egy billentyű bekötése: lenyomásra lefut az akció.
     */
    private void bind(String key, String actionName, Runnable action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), actionName);
        getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    /**
     * Az aktuális pálya betöltése, az ütő és a labda a kezdőhelyére.
     */
    private void restart() {
        try {
            level = new Level(Path.of("data", "levels", String.format("level%02d.txt", levelNum)));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load the level file: level" + levelNum, e);
        }
        paddle = new Paddle(350, PADDLE_Y, PADDLE_WIDTH, PADDLE_HEIGHT, paddleImage);
        ball = new Ball(400, 300, BALL_RADIUS, BALL_RADIUS, ballImage);
    }

    /**
     * Egy képkocka: a játék egy lépése, majd újrarajzolás.
     */
    private void newFrame() {
        if (!paused) {
            // Vízszintes lépés; ha téglát talált, vízszintesen pattan.
            ball.moveX();
            if (level.collides(ball)) {
                ball.invertVelX();
            }
            // Függőleges lépés; ha alul kiment, elölről kezdjük.
            if (!ball.moveY()) {
                levelNum = 0;
                restart();
            }
            if (level.collides(ball)) {
                ball.invertVelY();
            }
            if (paddle.collides(ball)) {
                ball.invertVelY();
            }
            paddle.move();
        }
        if (level.isOver()) {
            levelNum = (levelNum + 1) % LEVEL_COUNT;
            restart();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, WIDTH, HEIGHT, null);
        level.draw(g);
        paddle.draw(g);
        ball.draw(g);
    }

}
