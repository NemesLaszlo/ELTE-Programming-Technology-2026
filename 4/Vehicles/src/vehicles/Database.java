package vehicles;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * A gépjárművek nyilvántartása: beolvassa őket egy szövegfájlból, és
 * statisztikákat készít róluk.
 *
 * @author bli
 */
public class Database {

    private static final List<String> CATEGORIES
            = List.of(Car.CATEGORY, Bus.CATEGORY, Truck.CATEGORY);

    private final List<Vehicle> vehicles = new ArrayList<>();

    /**
     * Beolvassa a gépjárműveket a megadott fájlból.
     *
     * A fájlt sorról sorra dolgozzuk fel, minden sort önállóan, ezért a fájl
     * első sorában lévő darabszámra (n) valójában nincs szükségünk. Ha mégis
     * ott van, akkor csak ellenőrzésre használjuk.
     *
     * Hiba esetén a nyilvántartás változatlan marad, mert a járműveket csak a
     * sikeres beolvasás végén vesszük fel.
     *
     * @param filename a beolvasandó fájl neve
     * @throws IOException ha a fájl nem található vagy nem olvasható
     * @throws InvalidInputException ha a fájl tartalma hibás
     */
    public void read(String filename) throws IOException, InvalidInputException {
        List<Vehicle> loaded = new ArrayList<>();
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
                // A járművek sora betűvel kezdődik, ezért ha az első adatsor
                // számmal indul, akkor az a darabszám.
                boolean isCountLine = declaredCount == null && loaded.isEmpty() && fields.hasNextInt();
                if (isCountLine) {
                    declaredCount = fields.nextInt();
                } else {
                    loaded.add(parseVehicle(fields, lineNumber));
                }
            }
        }

        if (declaredCount != null && declaredCount != loaded.size()) {
            throw new InvalidInputException("The file declares " + declaredCount
                    + " vehicles, but contains " + loaded.size());
        }
        checkUniquePlates(loaded);
        vehicles.addAll(loaded);
    }

    /**
     * Egyetlen sor feldolgozása. A sor felépítése: típus, rendszám, tankolások
     * száma, majd a tankolt mennyiségek.
     *
     * A beolvasás közben többféle hiba történhet. Mindegyiket a saját
     * kivételünkké alakítjuk, és mellétesszük a sor számát, hogy a felhasználó
     * tudja, hol keresse a hibát.
     */
    private static Vehicle parseVehicle(Scanner fields, int lineNumber) throws InvalidInputException {
        try {
            String type = fields.next();
            String plate = fields.next();
            Vehicle vehicle = createVehicle(type, plate);

            int numRefuels = fields.nextInt();
            for (int i = 0; i < numRefuels; i++) {
                vehicle.addRefuel(fields.nextInt());
            }
            if (fields.hasNext()) {
                throw new IllegalArgumentException("More data than expected: " + fields.next());
            }
            return vehicle;
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
            // Ezt a saját ellenőrzéseink dobják, pl. a Vehicle osztályban.
            throw new InvalidInputException(lineNumber, ex.getMessage(), ex);
        }
    }

    /**
     * "Gyártó" metódus: a típus betűjele alapján létrehozza a megfelelő
     * leszármazott osztály egy példányát.
     */
    private static Vehicle createVehicle(String type, String plate) {
        return switch (type) {
            case Car.CATEGORY -> new Car(plate);
            case Bus.CATEGORY -> new Bus(plate);
            case Truck.CATEGORY -> new Truck(plate);
            default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
        };
    }

    /**
     * A rendszám azonosítja a járművet, ezért nem szerepelhet kétszer.
     */
    private void checkUniquePlates(List<Vehicle> loaded) throws InvalidInputException {
        Set<String> plates = new HashSet<>();
        for (Vehicle vehicle : vehicles) {
            plates.add(vehicle.getPlate());
        }
        for (Vehicle vehicle : loaded) {
            // A halmaz add metódusa hamisat ad, ha az elem már benne volt.
            if (!plates.add(vehicle.getPlate())) {
                throw new InvalidInputException("Duplicate plate: " + vehicle.getPlate());
            }
        }
    }

    /**
     * Kiírja a járműveket, a járművenkénti átlagos tankolást, valamint
     * kategóriánként a legtöbbet/legkevesebbet és a legtöbbször/legkevesebbszer
     * tankoló járművet.
     */
    public void report() {
        System.out.println("Vehicles in the database:");
        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);
        }
        System.out.println("Mean refuels: ");
        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle.getPlate() + ": " + vehicle.meanRefuels());
        }
        for (String category : CATEGORIES) {
            System.out.println("Refuels in category " + category + ":");
            reportCategory(collectCategory(category));
        }
    }

    private void reportCategory(List<Vehicle> categoryVehicles) {
        // Üres kategóriában nincs se legnagyobb, se legkisebb elem.
        if (categoryVehicles.isEmpty()) {
            System.out.println("No vehicles in this category.");
            return;
        }
        // A Comparator mondja meg, mi alapján hasonlítjuk össze a járműveket.
        Comparator<Vehicle> byAmount = Comparator.comparingInt(Vehicle::sumRefuels);
        Comparator<Vehicle> byCount = Comparator.comparingInt(Vehicle::numRefuels);
        System.out.println("Most fuel refueled: " + Collections.max(categoryVehicles, byAmount));
        System.out.println("Least fuel refueled: " + Collections.min(categoryVehicles, byAmount));
        System.out.println("Most times refueled: " + Collections.max(categoryVehicles, byCount));
        System.out.println("Least times refueled: " + Collections.min(categoryVehicles, byCount));
    }

    /**
     * @param category a keresett kategória betűjele
     * @return a megadott kategóriába tartozó járművek
     */
    public List<Vehicle> collectCategory(String category) {
        List<Vehicle> categoryVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getCategory().equals(category)) {
                categoryVehicles.add(vehicle);
            }
        }
        return categoryVehicles;
    }

    /**
     * Kiüríti a nyilvántartást.
     */
    public void clear() {
        vehicles.clear();
    }

}
