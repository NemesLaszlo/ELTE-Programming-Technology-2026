package calc.model;

/**
 * Egy elvégzett számolás: a két operandus, a művelet és az eredmény.
 *
 * A record egy tömör adatosztály (Java 16 óta): a fordító elkészíti hozzá a
 * konstruktort, a lekérdező metódusokat (pl. left(), result()), valamint az
 * equals, a hashCode és a toString metódust. A mezői nem módosíthatók.
 *
 * MVC: a modell hozza létre minden sikeres számolásnál, és ilyenekből áll
 * az előzmények listája. A vezérlő ebből készíti a felületen megjelenő
 * szöveget (pl. "6 × 7 = 42").
 *
 * @param left a bal oldali operandus
 * @param operation a művelet
 * @param right a jobb oldali operandus
 * @param result az eredmény
 *
 * @author bli
 */
public record Calculation(double left, Operation operation, double right, double result) {
}
