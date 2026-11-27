package diver.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Egy pálya KIINDULÓ állapota: a mezők rácsa, a búvár kezdőhelye és a
 * cápák kezdőhelye és iránya. A Level nem változik a játék közben (a
 * gyöngyök felvételét a Game a saját másolatán könyveli), ezért ugyanaz a
 * Level akárhányszor újrakezdhető.
 *
 * A pálya egy szöveges rács, soronként egy sor, karakterenként egy mező:
 * <pre>
 *   #   szikla
 *   .   víz
 *   P   gyöngy
 *   D   a búvár kezdőhelye (a hajó); alatta víz
 *   H   vízszintesen járőröző cápa, jobbra indul; alatta víz
 *   V   függőlegesen járőröző cápa, lefelé indul; alatta víz
 * </pre>
 * A beolvasó szigorú: minden sor egyforma hosszú; pontosan egy D van;
 * legalább egy P; ismeretlen karakter hiba. A hibaüzenet megmondja, melyik
 * sorban mi a baj.
 *
 * A 10 beépített pálya az src/main/resources/levels mappában van
 * (level01.txt ... level10.txt), és a load metódussal tölthető be: a Maven
 * a jar-ba csomagolja őket, így a program bárhonnan indítva megtalálja.
 * A LevelTest ellenőrzi, hogy mind a 10 betölthető és végigjátszható.
 */
public final class Level {

    /** A beépített pályák száma. */
    public static final int LEVEL_COUNT = 10;

    private static final char START = 'D';
    private static final char HORIZONTAL_SHARK = 'H';
    private static final char VERTICAL_SHARK = 'V';

    private final Tile[][] tiles;
    private final Position start;
    private final List<Shark> sharks;

    private Level(Tile[][] tiles, Position start, List<Shark> sharks) {
        this.tiles = tiles;
        this.start = start;
        this.sharks = sharks;
    }

    /**
     * Betölti a megadott sorszámú beépített pályát.
     *
     * @param number a pálya sorszáma, 1 .. LEVEL_COUNT
     * @return a pálya
     * @throws IllegalArgumentException ha nincs ilyen pálya, vagy a fájl hibás
     */
    public static Level load(int number) {
        if (number < 1 || number > LEVEL_COUNT) {
            throw new IllegalArgumentException("No such level: " + number + " (1.." + LEVEL_COUNT + ")");
        }
        String name = String.format("/levels/level%02d.txt", number);
        try (InputStream in = Level.class.getResourceAsStream(name)) {
            if (in == null) {
                throw new IllegalArgumentException("Missing level file: " + name);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return parse(reader.lines().toList());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read the level file: " + name, e);
        }
    }

    /**
     * Mind a 10 beépített pálya, sorban.
     */
    public static List<Level> loadAll() {
        List<Level> levels = new ArrayList<>();
        for (int number = 1; number <= LEVEL_COUNT; number++) {
            levels.add(load(number));
        }
        return levels;
    }

    /**
     * Pálya a szöveges sorokból (a tesztek így adnak meg kis pályákat).
     *
     * @param lines a sorok, felülről lefelé
     * @return a pálya
     * @throws IllegalArgumentException ha a sorok nem egyforma hosszúak,
     * ismeretlen karakter van bennük, nem pontosan egy kezdőhely van, vagy
     * nincs gyöngy
     */
    public static Level parse(List<String> lines) {
        if (lines.isEmpty() || lines.get(0).isEmpty()) {
            throw new IllegalArgumentException("Empty level");
        }
        int rows = lines.size();
        int cols = lines.get(0).length();
        Tile[][] tiles = new Tile[rows][cols];
        Position start = null;
        List<Shark> sharks = new ArrayList<>();
        int pearls = 0;

        for (int row = 0; row < rows; row++) {
            String line = lines.get(row);
            if (line.length() != cols) {
                throw new IllegalArgumentException("Line " + (row + 1) + ": " + line.length()
                        + " characters, the first line has " + cols);
            }
            for (int col = 0; col < cols; col++) {
                char c = line.charAt(col);
                Position position = new Position(row, col);
                switch (c) {
                    case '#' -> tiles[row][col] = Tile.ROCK;
                    case '.' -> tiles[row][col] = Tile.WATER;
                    case 'P' -> {
                        tiles[row][col] = Tile.PEARL;
                        pearls++;
                    }
                    case START -> {
                        if (start != null) {
                            throw new IllegalArgumentException("Line " + (row + 1) + ": more than one start (D)");
                        }
                        start = position;
                        tiles[row][col] = Tile.WATER;
                    }
                    case HORIZONTAL_SHARK -> {
                        sharks.add(new Shark(position, Direction.RIGHT));
                        tiles[row][col] = Tile.WATER;
                    }
                    case VERTICAL_SHARK -> {
                        sharks.add(new Shark(position, Direction.DOWN));
                        tiles[row][col] = Tile.WATER;
                    }
                    default -> throw new IllegalArgumentException("Line " + (row + 1) + ", character "
                            + (col + 1) + ": unknown symbol '" + c + "'");
                }
            }
        }
        if (start == null) {
            throw new IllegalArgumentException("No start (D)");
        }
        if (pearls == 0) {
            throw new IllegalArgumentException("No pearl (P)");
        }
        return new Level(tiles, start, sharks);
    }

    /**
     * Végigjátszható-e a pálya: a kezdőhelyről minden gyöngy elérhető
     * sziklán át nem vezető úton. Szélességi bejárás (BFS) a kezdőhelyről:
     * ha minden gyöngy a bejárt mezők között van, a pálya jó.
     *
     * @return igaz, ha minden gyöngy elérhető
     */
    public boolean isPlayable() {
        Set<Position> visited = new HashSet<>();
        Queue<Position> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            Position current = queue.remove();
            for (Direction direction : Direction.values()) {
                Position next = current.step(direction);
                if (contains(next) && getTile(next).isPassable() && visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                if (tiles[row][col] == Tile.PEARL && !visited.contains(new Position(row, col))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return a mezők másolata; a Game ezen könyveli a felvett gyöngyöket,
     * a Level változatlan marad
     */
    public Tile[][] copyTiles() {
        Tile[][] copy = new Tile[getRows()][];
        for (int row = 0; row < getRows(); row++) {
            copy[row] = tiles[row].clone();
        }
        return copy;
    }

    /**
     * @return a cápák a kezdőhelyükön, új példányokként (a Game mozgatja
     * őket, a Level cápái nem változnak)
     */
    public List<Shark> newSharks() {
        List<Shark> copy = new ArrayList<>();
        for (Shark shark : sharks) {
            copy.add(new Shark(shark.getPosition(), shark.getDirection()));
        }
        return copy;
    }

    public boolean contains(Position position) {
        return position.row() >= 0 && position.row() < getRows()
                && position.col() >= 0 && position.col() < getCols();
    }

    public Tile getTile(Position position) {
        return tiles[position.row()][position.col()];
    }

    public int getRows() {
        return tiles.length;
    }

    public int getCols() {
        return tiles[0].length;
    }

    public Position getStart() {
        return start;
    }

    /**
     * @return a cápák kezdőhelye és iránya, csak olvasásra
     */
    public List<Shark> getSharks() {
        return Collections.unmodifiableList(sharks);
    }

    /**
     * @return a gyöngyök száma a kiinduló állapotban
     */
    public int getPearlCount() {
        int count = 0;
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                if (tile == Tile.PEARL) {
                    count++;
                }
            }
        }
        return count;
    }

}
