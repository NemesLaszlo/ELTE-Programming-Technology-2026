package arkanoid;

import java.awt.Graphics;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * Egy pálya: a téglák. Fájlból töltődik be, kezeli a labda és a téglák
 * ütközését, tudja, hogy vége-e (nincs több tégla), és kirajzolja a
 * téglákat.
 *
 * A pályafájl egy szöveges rács: minden sor egy téglasor, minden karakter
 * egy tégla helye. Egy számjegy (0-9) a tégla fajtája (a brick0X.png kép),
 * minden más karakter (szóköz) üres hely. Egy tégla 40 × 20 képpont, a
 * 800 széles játéktérbe 20 fér egymás mellé.
 *
 * A fájlokat a program a MUNKAKÖNYVTÁRHOZ képest keresi (data/levels/...):
 * ezért a projekt mappájából kell indítani (az IDE-k és a Maven így teszik).
 * A Diver mintában a pályák az src/main/resources mappában vannak, és a jar
 * belsejéből töltődnek: az bárhonnan indítva működik.
 *
 * @author bli
 */
public class Level {

    public static final int BRICK_WIDTH = 40;
    public static final int BRICK_HEIGHT = 20;

    private final List<Brick> bricks = new ArrayList<>();

    /**
     * Betölti a pályát a fájlból.
     *
     * @param levelPath a fájl útvonala
     * @throws IOException ha a fájl nem olvasható
     */
    public Level(Path levelPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(levelPath, StandardCharsets.UTF_8)) {
            int row = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                for (int col = 0; col < line.length(); col++) {
                    char blockType = line.charAt(col);
                    if (Character.isDigit(blockType)) {
                        Image image = new ImageIcon("data/images/brick0" + blockType + ".png").getImage();
                        bricks.add(new Brick(col * BRICK_WIDTH, row * BRICK_HEIGHT,
                                BRICK_WIDTH, BRICK_HEIGHT, image));
                    }
                }
                row++;
            }
        }
    }

    /**
     * Ha a labda eltalál egy téglát, a tégla eltűnik.
     *
     * @param ball a labda
     * @return igaz, ha volt ütközés (a labdának pattannia kell)
     */
    public boolean collides(Ball ball) {
        for (Brick brick : bricks) {
            if (ball.collides(brick)) {
                bricks.remove(brick);
                return true;
            }
        }
        return false;
    }

    /**
     * @return igaz, ha nincs több tégla
     */
    public boolean isOver() {
        return bricks.isEmpty();
    }

    public void draw(Graphics g) {
        for (Brick brick : bricks) {
            brick.draw(g);
        }
    }

}
