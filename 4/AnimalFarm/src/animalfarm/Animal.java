package animalfarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Az állatok közös ősosztálya.
 *
 * Az osztály absztrakt, ezért közvetlenül nem példányosítható: csak a
 * leszármazottaiból ({@link Cow}, {@link Horse}, {@link Goat}, {@link Emu})
 * hozható létre objektum.
 *
 * @author pinter
 */
public abstract class Animal {

    private final String name;
    private final String kind;
    private final int weight;
    private final List<Integer> meals = new ArrayList<>();

    /**
     * @param name az állat neve, ez azonosítja az állatot
     * @param weight az állat súlya kilogrammban, pozitív szám
     * @param kind a fajta betűjele (T, L, K vagy E)
     * @throws IllegalArgumentException ha a név üres vagy a súly nem pozitív
     */
    protected Animal(String name, int weight, String kind) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The name must not be empty");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("The weight must be positive: " + weight);
        }
        this.name = name;
        this.weight = weight;
        this.kind = kind;
    }

    /**
     * A soványsági küszöb fajtánként eltér, ezért absztrakt metódus: minden
     * leszármazott maga adja meg a saját értékét.
     *
     * @return az a súly (kg), ami alatt az állat kórosan soványnak számít
     */
    public abstract int getMalnourishedThreshold();

    /**
     * Új étkezés rögzítése.
     *
     * @param meal az elfogyasztott élelem dekagrammban, pozitív szám
     * @throws IllegalArgumentException ha a mennyiség nem pozitív
     */
    public void addMeal(int meal) {
        if (meal <= 0) {
            throw new IllegalArgumentException("The meal amount must be positive: " + meal);
        }
        meals.add(meal);
    }

    /**
     * @return igaz, ha az állat súlya kisebb, mint a fajtájához tartozó küszöb
     */
    public boolean isMalnourished() {
        return weight < getMalnourishedThreshold();
    }

    /**
     * @return az aznap elfogyasztott összes élelem dekagrammban
     */
    public int totalConsumed() {
        int sumMeals = 0;
        for (int meal : meals) {
            sumMeals += meal;
        }
        return sumMeals;
    }

    /**
     * @param dkgs a vizsgált mennyiség dekagrammban
     * @return igaz, ha az állat ennél többet fogyasztott
     */
    public boolean consumedMore(int dkgs) {
        return totalConsumed() > dkgs;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public String getKind() {
        return kind;
    }

    /**
     * @return az étkezések csak olvasható listája, így a hívó nem tudja
     * megkerülni az {@link #addMeal(int)} ellenőrzését
     */
    public List<Integer> getMeals() {
        return Collections.unmodifiableList(meals);
    }

    @Override
    public String toString() {
        return "Name: " + name + ", kind: " + kind + ", weight: " + weight + ", meals: " + meals;
    }

}
