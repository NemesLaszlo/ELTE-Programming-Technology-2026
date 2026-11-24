package dodger.view;

import dodger.model.Direction;
import dodger.model.FallingObject;
import dodger.model.Game;
import dodger.model.Ship;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * A játéktér rajzolása és a billentyűzet: a NÉZET egyik fele.
 *
 * A paintComponent a modell (Game) állapotát rajzolja ki elemi grafikával:
 * a hajó egy háromszög, az aszteroidák szürke körök, a csillagok sárga
 * csillagok. A nézetben nincs állapot: minden a modellből jön, a repaint
 * pedig a Timer minden ütésére megtörténik (DodgerGUI).
 *
 * A billentyűzetet KEY BINDING-gel kezeljük (InputMap + ActionMap), nem
 * KeyListener-rel: a key binding akkor is működik, ha nem a panelen van a
 * fókusz, hanem valahol az ablakban (WHEN_IN_FOCUSED_WINDOW). A lenyomás
 * (press) és a felengedés (release) külön esemény: a modell így tudja, hogy
 * a billentyűt lenyomva tartják.
 *
 * A WASD a 3. beadandó előírása; az írás kedvéért a nyilak is ugyanazt
 * csinálják.
 */
@SuppressWarnings("serial") // a JPanel Serializable, de a panelt nem mentjük fájlba
public final class GamePanel extends JPanel {

    private static final Color BACKGROUND = new Color(0x10, 0x10, 0x2A);
    private static final Color SHIP = new Color(0x4F, 0xC3, 0xF7);
    private static final Color ASTEROID = new Color(0x8D, 0x8D, 0x8D);
    private static final Color ASTEROID_EDGE = new Color(0x55, 0x55, 0x55);
    private static final Color STAR = new Color(0xFF, 0xD5, 0x4F);

    /** Billentyű -> irány. A WASD az előírás, a nyilak ráadás. */
    private static final Map<String, Direction> KEYS = Map.of(
            "W", Direction.UP, "UP", Direction.UP,
            "S", Direction.DOWN, "DOWN", Direction.DOWN,
            "A", Direction.LEFT, "LEFT", Direction.LEFT,
            "D", Direction.RIGHT, "RIGHT", Direction.RIGHT);

    /** A modell. Új játéknál cserélődik (setGame). */
    private Game game;

    public GamePanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(Game.WIDTH, Game.HEIGHT));
        setBackground(BACKGROUND);
        bindKeys();
    }

    /**
     * Új játék: ettől kezdve ezt a modellt rajzolja és irányítja.
     */
    public void setGame(Game game) {
        this.game = game;
        repaint();
    }

    /**
     * Minden billentyűhöz két akció: lenyomás és felengedés. A KeyStroke
     * szövege: "pressed W" / "released W".
     */
    private void bindKeys() {
        for (Map.Entry<String, Direction> entry : KEYS.entrySet()) {
            String key = entry.getKey();
            Direction direction = entry.getValue();
            bind("pressed " + key, "press-" + key, () -> game.press(direction));
            bind("released " + key, "release-" + key, () -> game.release(direction));
        }
    }

    private void bind(String keyStroke, String actionName, Runnable action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStroke), actionName);
        getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (FallingObject object : game.getObjects()) {
            if (object.getKind() == FallingObject.Kind.ASTEROID) {
                drawAsteroid(g2, object);
            } else {
                drawStar(g2, object);
            }
        }
        drawShip(g2, game.getShip());
    }

    /**
     * A hajó: felfelé mutató háromszög a befoglaló téglalapban.
     */
    private void drawShip(Graphics2D g2, Ship ship) {
        Polygon triangle = new Polygon();
        triangle.addPoint(ship.getX() + ship.getWidth() / 2, ship.getY());
        triangle.addPoint(ship.getRight(), ship.getBottom());
        triangle.addPoint(ship.getX(), ship.getBottom());
        g2.setColor(SHIP);
        g2.fillPolygon(triangle);
    }

    /**
     * Az aszteroida: kör, sötétebb szegéllyel.
     */
    private void drawAsteroid(Graphics2D g2, FallingObject asteroid) {
        g2.setColor(ASTEROID);
        g2.fillOval(asteroid.getX(), asteroid.getY(), asteroid.getWidth(), asteroid.getHeight());
        g2.setColor(ASTEROID_EDGE);
        g2.drawOval(asteroid.getX(), asteroid.getY(), asteroid.getWidth() - 1, asteroid.getHeight() - 1);
    }

    /**
     * A csillag: ötágú csillag a befoglaló téglalapba írva. A csúcsok a
     * körön 72 fokonként, a belső pontok köztük, kisebb sugáron.
     */
    private void drawStar(Graphics2D g2, FallingObject star) {
        double cx = star.getX() + star.getWidth() / 2.0;
        double cy = star.getY() + star.getHeight() / 2.0;
        double outer = star.getWidth() / 2.0;
        double inner = outer * 0.45;
        Polygon polygon = new Polygon();
        for (int i = 0; i < 10; i++) {
            double radius = (i % 2 == 0) ? outer : inner;
            double angle = Math.toRadians(-90 + i * 36);
            polygon.addPoint((int) Math.round(cx + radius * Math.cos(angle)),
                    (int) Math.round(cy + radius * Math.sin(angle)));
        }
        g2.setColor(STAR);
        g2.fillPolygon(polygon);
    }

}
