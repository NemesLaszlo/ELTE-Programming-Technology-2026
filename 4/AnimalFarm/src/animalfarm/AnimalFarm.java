package animalfarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * Az állatfarm nyilvántartása: beolvassa az állatokat egy szövegfájlból, és
 * kigyűjti a kórosan sovány, illetve a sokat fogyasztó állatokat.
 *
 * @author pinter
 */
public class AnimalFarm {

    /** Ennél több élelem (dkg) elfogyasztása számít nagy fogyasztásnak: 1 kg. */
    private static final int BIG_EATER_LIMIT_DKG = 100;

    private final List<Animal> animals = new ArrayList<>();

    /**
     * Beolvassa az állatokat a megadott fájlból.
     *
     * A fájlt sorról sorra dolgozzuk fel, minden sort önállóan, ezért a fájl
     * első sorában lévő darabszámra (n) valójában nincs szükségünk. Ha mégis
     * ott van, akkor csak ellenőrzésre használjuk.
     *
     * Hiba esetén a nyilvántartás változatlan marad, mert az állatokat csak a
     * sikeres beolvasás végén vesszük fel.
     *
     * @param filename a beolvasandó fájl neve
     * @throws IOException ha a fájl nem található vagy nem olvasható
     * @throws InvalidInputException ha a fájl tartalma hibás
     */
    public void read(String filename) throws IOException, InvalidInputException {
        List<Animal> loaded = new ArrayList<>();
        // null, amíg nem találtunk darabszámot a fájlban
        Integer declaredCount = null;

        // A try-with-resources a blokk végén hiba esetén is bezárja a fájlt.
        // Nincs catch ág, mert itt a try egyetlen feladata a fájl lezárása:
        // a hibát ez a metódus nem tudja értelmesen kezelni, ezért a fejlécben
        // lévő throws miatt a kivétel továbbmegy a hívóhoz (a Main kapja el).
        // A fájl ilyenkor is bezáródik, még a kivétel továbbdobása előtt.
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filename), StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            // Sorról sorra haladunk: a readLine null értéket ad a fájl végén.
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                // A Scanner egymás után adja vissza a sor mezőit. A mezőket
                // szóközök és tabulátorok választják el, akárhány állhat
                // belőlük egymás mellett.
                Scanner fields = new Scanner(line);
                // Az állatok sora betűvel kezdődik, ezért ha az első adatsor
                // számmal indul, akkor az a darabszám.
                boolean isCountLine = declaredCount == null && loaded.isEmpty() && fields.hasNextInt();
                if (isCountLine) {
                    declaredCount = fields.nextInt();
                } else {
                    loaded.add(parseAnimal(fields, lineNumber));
                }
            }
        }

        if (declaredCount != null && declaredCount != loaded.size()) {
            throw new InvalidInputException("The file declares " + declaredCount
                    + " animals, but contains " + loaded.size());
        }
        checkUniqueNames(loaded);
        animals.addAll(loaded);
    }

    /**
     * Egyetlen sor feldolgozása. A sor felépítése: fajta, név, súly, étkezések
     * száma, majd az egyes étkezések mennyisége.
     *
     * A beolvasás közben többféle hiba történhet. Mindegyiket a saját
     * kivételünkké alakítjuk, és mellétesszük a sor számát, hogy a felhasználó
     * tudja, hol keresse a hibát.
     */
    private static Animal parseAnimal(Scanner fields, int lineNumber) throws InvalidInputException {
        try {
            String kind = fields.next();
            String name = fields.next();
            int weight = fields.nextInt();
            Animal animal = createAnimal(kind, name, weight);

            int numMeals = fields.nextInt();
            for (int i = 0; i < numMeals; i++) {
                animal.addMeal(fields.nextInt());
            }
            if (fields.hasNext()) {
                throw new IllegalArgumentException("More data than expected: " + fields.next());
            }
            return animal;
        } catch (InputMismatchException ex) {
            // A nextInt dobja, ha nem szám következik. A hibás mező ilyenkor
            // a Scannerben marad, ezért a next metódussal még kiolvasható.
            throw new InvalidInputException(lineNumber,
                    "Expected a whole number, but found: " + fields.next(), ex);
        } catch (NoSuchElementException ex) {
            // A next és a nextInt dobja, ha a sor véget ért.
            throw new InvalidInputException(lineNumber,
                    "Data is missing from the end of the line", ex);
        } catch (IllegalArgumentException ex) {
            // Ezt a saját ellenőrzéseink dobják, pl. az Animal osztályban.
            throw new InvalidInputException(lineNumber, ex.getMessage(), ex);
        }
    }

    /**
     * "Gyártó" metódus: a fajta betűjele alapján létrehozza a megfelelő
     * leszármazott osztály egy példányát.
     */
    private static Animal createAnimal(String kind, String name, int weight) {
        return switch (kind) {
            case Cow.KIND -> new Cow(name, weight);
            case Horse.KIND -> new Horse(name, weight);
            case Goat.KIND -> new Goat(name, weight);
            case Emu.KIND -> new Emu(name, weight);
            default -> throw new IllegalArgumentException("Unknown animal kind: " + kind);
        };
    }

    /**
     * A név azonosítja az állatot, ezért nem szerepelhet kétszer.
     */
    private void checkUniqueNames(List<Animal> loaded) throws InvalidInputException {
        Set<String> names = new HashSet<>();
        for (Animal animal : animals) {
            names.add(animal.getName());
        }
        for (Animal animal : loaded) {
            // A halmaz add metódusa hamisat ad, ha az elem már benne volt.
            if (!names.add(animal.getName())) {
                throw new InvalidInputException("Duplicate name: " + animal.getName());
            }
        }
    }

    /**
     * @return a kórosan sovány állatok
     */
    public List<Animal> getMalnourished() {
        List<Animal> result = new ArrayList<>();
        for (Animal animal : animals) {
            if (animal.isMalnourished()) {
                result.add(animal);
            }
        }
        return result;
    }

    /**
     * @return azok az állatok, amelyek több, mint 1 kg élelmet fogyasztottak
     */
    public List<Animal> getBigEaters() {
        List<Animal> result = new ArrayList<>();
        for (Animal animal : animals) {
            if (animal.consumedMore(BIG_EATER_LIMIT_DKG)) {
                result.add(animal);
            }
        }
        return result;
    }

    /**
     * Kiírja az összes állatot, majd a kórosan soványakat és a nagy
     * fogyasztókat.
     */
    public void report() {
        printAnimals("Animals on the farm:", animals);
        printAnimals("Malnourished animals:", getMalnourished());
        printAnimals("Animals that consumed more than 1 kg of food:", getBigEaters());
    }

    private static void printAnimals(String title, List<Animal> list) {
        System.out.println(title);
        // Üres lista esetén is legyen egyértelmű a kimenet.
        if (list.isEmpty()) {
            System.out.println("(none)");
        }
        for (Animal animal : list) {
            System.out.println(animal);
        }
    }

    /**
     * Kiüríti a nyilvántartást.
     */
    public void clear() {
        animals.clear();
    }

}
