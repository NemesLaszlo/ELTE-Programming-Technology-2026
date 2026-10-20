package flashcards;

/**
 * Egy tanulókártya: egy kérdés és a hozzá tartozó válasz.
 *
 * Rekord (record): egy olyan osztály rövid írásmódja, amely csak adatot tárol.
 * A fordító elkészíti a konstruktort, a question() és answer() lekérdező
 * metódusokat, valamint az equals, hashCode és toString metódusokat. A mezői
 * véglegesek (final): a kártya a létrehozása után nem módosítható.
 *
 * @author pinter
 */
public record Card(String question, String answer) {

}
