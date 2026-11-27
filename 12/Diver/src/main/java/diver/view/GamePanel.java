package diver.view;

import diver.model.Direction;
import diver.model.Game;
import diver.model.Position;
import diver.model.Shark;
import diver.model.Tile;
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
 * A pálya rajzolása és a billentyűzet: a NÉZET egyik fele.
 *
 * A paintComponent a modell (Game) állapotát rajzolja ki elemi grafikával,
 * mezőnként CELL × CELL képpontban: szikla, víz, gyöngy, a cápák (az
 * irányukba mutató háromszög) és a búvár. A nézetben nincs állapot: minden
 * a modellből jön; a repaint-et a Timer kéri minden ütemben (DiverGUI).
 *
 * A billentyűzet KEY BINDING (InputMap + ActionMap), WHEN_IN_FOCUSED_WINDOW
 * módban: akkor is működik, ha a fókusz az ablakon belül máshol van. A
 * búvár mezőnként lép, ezért elég a lenyomás; a lenyomva tartást az
 * operációs rendszer billentyűismétlése adja. A WASD az előírás, a nyilak
 * ráadás.
 */
@SuppressWarnings("serial") // a JPanel Serializable, de a panelt nem mentjük fájlba
public final class GamePanel extends JPanel {

    /** Egy mező mérete képpontban. */
    public static final int CELL = 36;

    private static final Color WATER = new Color(0x1E, 0x6F, 0xA8);
    private static final Color WATER_DARK = new Color(0x18, 0x5E, 0x90);
    private static final Color ROCK = new Color(0x4A, 0x40, 0x3A);
    private static final Color ROCK_LIGHT = new Color(0x6B, 0x5E, 0x55);
    private static final Color PEARL = new Color(0xF5, 0xF0, 0xE6);
    private static final Color PEARL_SHINE = Color.WHITE;
    private static final Color SHARK = new Color(0x9A, 0xA5, 0xAD);
    private static final Color SHARK_DARK = new Color(0x5C, 0x66, 0x6E);
    private static final Color VISION = new Color(0xFF, 0x40, 0x40, 70);
    private static final Color DIVER = new Color(0xFF, 0xB7, 0x00);
    private static final Color DIVER_DARK = new Color(0xB3, 0x7E, 0x00);
    private static final Color START = new Color(0xFF, 0xFF, 0xFF, 60);

    /** Billentyű -> irány. */
    private static final Map<String, Direction> KEYS = Map.of(
            "W", Direction.UP, "UP", Direction.UP,
            "S", Direction.DOWN, "DOWN", Direction.DOWN,
            "A", Direction.LEFT, "LEFT", Direction.LEFT,
            "D", Direction.RIGHT, "RIGHT", Direction.RIGHT);

    /** A modell. Új játéknál cserélődik (setGame). */
    private Game game;

    public GamePanel(Game game) {
        this.game = game;
        updatePreferredSize();
        bindKeys();
    }

    /**
     * Új játék: ettől kezdve ezt a modellt rajzolja és irányítja.
     */
    public void setGame(Game game) {
        this.game = game;
        updatePreferredSize();
        repaint();
    }

    /**
     * A panel mérete az aktuális pályához igazodik. Pályaváltás után a
     * DiverGUI hívja, majd pack()-et.
     */
    public void updatePreferredSize() {
        setPreferredSize(new Dimension(game.getCols() * CELL, game.getRows() * CELL));
    }

    private void bindKeys() {
        for (Map.Entry<String, Direction> entry : KEYS.entrySet()) {
            String key = entry.getKey();
            Direction direction = entry.getValue();
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed " + key), "move-" + key);
            getActionMap().put("move-" + key, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // A modell dönt: ha szikla van ott, nem lép. A következő
                    // ütemben a Timer úgyis újrarajzol, de így azonnal látszik.
                    game.move(direction);
                    repaint();
                }
            });
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int row = 0; row < game.getRows(); row++) {
            for (int col = 0; col < game.getCols(); col++) {
                drawTile(g2, new Position(row, col));
            }
        }
        for (Shark shark : game.getSharks()) {
            drawVision(g2, shark);
        }
        for (Shark shark : game.getSharks()) {
            drawShark(g2, shark);
        }
        drawDiver(g2, game.getDiver());
    }

    private void drawTile(Graphics2D g2, Position position) {
        int x = position.col() * CELL;
        int y = position.row() * CELL;
        Tile tile = game.getTile(position);
        if (tile == Tile.ROCK) {
            g2.setColor(ROCK);
            g2.fillRect(x, y, CELL, CELL);
            g2.setColor(ROCK_LIGHT);
            g2.fillRect(x + 3, y + 3, CELL - 6, CELL - 6);
        } else {
            // Sakktáblaszerű víz, hogy látszódjanak a mezők.
            g2.setColor((position.row() + position.col()) % 2 == 0 ? WATER : WATER_DARK);
            g2.fillRect(x, y, CELL, CELL);
            if (tile == Tile.PEARL) {
                int d = CELL / 2;
                g2.setColor(PEARL);
                g2.fillOval(x + (CELL - d) / 2, y + (CELL - d) / 2, d, d);
                g2.setColor(PEARL_SHINE);
                g2.fillOval(x + CELL / 2 - 3, y + CELL / 2 - 6, 5, 5);
            }
        }
    }

    /**
     * A cápa látótere: halvány piros a mezőkön, amiket lát. Így a játékos
     * látja, hova nem szabad lépni. (Ugyanazt a szabályt rajzolja, amit a
     * modell Shark.sees-e számol: a cápa előtti VISION mező, sziklán át nem.)
     */
    private void drawVision(Graphics2D g2, Shark shark) {
        Position ahead = shark.getPosition();
        for (int distance = 1; distance <= Shark.VISION; distance++) {
            ahead = ahead.step(shark.getDirection());
            if (ahead.row() < 0 || ahead.row() >= game.getRows()
                    || ahead.col() < 0 || ahead.col() >= game.getCols()
                    || !game.getTile(ahead).isPassable()) {
                return;
            }
            g2.setColor(VISION);
            g2.fillRect(ahead.col() * CELL, ahead.row() * CELL, CELL, CELL);
        }
    }

    /**
     * A cápa: az irányába mutató háromszög, hátul egy kis uszony.
     */
    private void drawShark(Graphics2D g2, Shark shark) {
        int x = shark.getPosition().col() * CELL;
        int y = shark.getPosition().row() * CELL;
        int cx = x + CELL / 2;
        int cy = y + CELL / 2;
        int r = CELL / 2 - 4;
        Polygon body = new Polygon();
        switch (shark.getDirection()) {
            case RIGHT -> {
                body.addPoint(cx + r, cy);
                body.addPoint(cx - r, cy - r / 2);
                body.addPoint(cx - r, cy + r / 2);
            }
            case LEFT -> {
                body.addPoint(cx - r, cy);
                body.addPoint(cx + r, cy - r / 2);
                body.addPoint(cx + r, cy + r / 2);
            }
            case DOWN -> {
                body.addPoint(cx, cy + r);
                body.addPoint(cx - r / 2, cy - r);
                body.addPoint(cx + r / 2, cy - r);
            }
            case UP -> {
                body.addPoint(cx, cy - r);
                body.addPoint(cx - r / 2, cy + r);
                body.addPoint(cx + r / 2, cy + r);
            }
        }
        g2.setColor(SHARK);
        g2.fillPolygon(body);
        g2.setColor(SHARK_DARK);
        g2.drawPolygon(body);
    }

    /**
     * A búvár: sárga kör, sötét szegéllyel; a kezdőhely halvány keret.
     */
    private void drawDiver(Graphics2D g2, Position diver) {
        int x = diver.col() * CELL;
        int y = diver.row() * CELL;
        int d = CELL - 10;
        g2.setColor(DIVER);
        g2.fillOval(x + 5, y + 5, d, d);
        g2.setColor(DIVER_DARK);
        g2.drawOval(x + 5, y + 5, d, d);
        // A búvármaszk: két kis "szem".
        g2.fillOval(x + CELL / 2 - 7, y + CELL / 2 - 4, 5, 5);
        g2.fillOval(x + CELL / 2 + 2, y + CELL / 2 - 4, 5, 5);
        g2.setColor(START);
    }

}
