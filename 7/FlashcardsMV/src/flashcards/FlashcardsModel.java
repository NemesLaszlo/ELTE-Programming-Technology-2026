package flashcards;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Flashcards program MODELLJE (az MV "M" betűje): a kártyák, az aktuális
 * kártya, a pontszám, és a gyakorlás szabályai.
 *
 * A modell semmit nem tud a felületről, nem használ Swinget. A GUI
 * eseménykezelői hívják a metódusait, majd a GUI a modell állapota alapján
 * frissíti magát (ld. FlashcardsGUI.refresh). Mivel nincs benne Swing,
 * felület nélkül, JUnit-tal tesztelhető (ld. test/flashcards/FlashcardsModelTest).
 *
 * A gyakorlás menete: a kártyákat sorban vesszük. Mindegyiknél először a
 * kérdést mutatjuk, a válasz megnézhető (toggle), majd a felhasználó eldönti,
 * hogy tudta-e (answer). Jó válaszért egy pont jár. Ha minden kártyán túl
 * vagyunk, a gyakorlás véget ért (isFinished); a reset elölről kezdi.
 *
 * @author pinter
 */
public class FlashcardsModel {

    /** A fájlban a kérdést és a választ elválasztó karakter. */
    public static final String SEPARATOR = "~";

    /** A betöltött kártyák; üres, amíg nem töltöttünk be fájlt. */
    private List<Card> cards = new ArrayList<>();
    /**
     * Az eddig megválaszolt kártyák száma. Mivel a kártyákat sorban vesszük,
     * ez egyben az aktuális kártya indexe is: az aktuális kártya a
     * cards.get(answered). Induláskor 0, azaz az első kártya; ha megegyezik a
     * kártyák számával, nincs több kártya. Így egyetlen számlálót kell
     * karbantartani, nem kell egy indexet és egy darabszámot szinkronban
     * tartani (ld. answer).
     */
    private int answered;
    /** Igaz, ha az aktuális kártya kérdését mutatjuk, hamis, ha a válaszát. */
    private boolean showingQuestion = true;
    /** A jó válaszok száma. */
    private int score;

    /**
     * Beolvassa a kártyákat egy fájlból, és ezekkel elölről kezdi a
     * gyakorlást. Ha a fájl nem olvasható vagy hibás, a korábbi kártyák
     * változatlanul megmaradnak.
     *
     * @param file a kártyákat tartalmazó fájl
     * @throws IOException ha a fájl nem olvasható, vagy a tartalma hibás
     * (ld. readCards)
     */
    public void load(Path file) throws IOException {
        load(readCards(file));
    }

    /**
     * Ezekkel a kártyákkal elölről kezdi a gyakorlást. A lista másolatát
     * tároljuk, így a hívó későbbi módosításai nem hatnak a modellre.
     *
     * @param newCards a kártyák
     */
    public void load(List<Card> newCards) {
        cards = new ArrayList<>(newCards);
        reset();
    }

    /**
     * Beolvassa a kártyákat egy szövegfájlból. A fájl minden sora egy kártya:
     * {@code kérdés~válasz}. Az üres sorokat átugorjuk.
     *
     * Ez a metódus statikus: nem függ a modell állapotától, csak egy fájlból
     * készít egy listát. Így külön is tesztelhető, és a GUI a modell
     * módosítása nélkül is ellenőrizhet egy fájlt.
     *
     * @param file a fájl
     * @return a beolvasott kártyák a fájlbeli sorrendben
     * @throws IOException ha a fájl nem olvasható, egy sora nem
     * {@code kérdés~válasz} alakú, vagy a fájl egyetlen kártyát sem tartalmaz
     */
    public static List<Card> readCards(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(SEPARATOR);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                // A sorszám a hibaüzenetben: 1-től számozzuk, mint a
                // szerkesztők.
                throw new IOException("Line " + (i + 1) + " is not in the form question"
                        + SEPARATOR + "answer: " + line);
            }
            cards.add(new Card(parts[0].trim(), parts[1].trim()));
        }
        if (cards.isEmpty()) {
            throw new IOException("The file does not contain any cards");
        }
        return cards;
    }

    /**
     * Elölről kezdi a gyakorlást ugyanezekkel a kártyákkal: az első kártya
     * kérdésénél, nulla ponttal.
     */
    public void reset() {
        answered = 0;
        score = 0;
        showingQuestion = true;
    }

    /**
     * Átvált az aktuális kártya kérdése és válasza között.
     *
     * @throws IllegalStateException ha nincs aktuális kártya
     */
    public void toggle() {
        requireCurrentCard();
        showingQuestion = !showingQuestion;
    }

    /**
     * Megválaszolja az aktuális kártyát, és továbblép a következőre, amelynek
     * ismét a kérdését mutatjuk.
     *
     * Hogyan lépünk tovább? Az {@code answered++} a továbblépés. Nincs külön
     * "aktuális kártya" mező: az aktuális kártya mindig
     * {@code cards.get(answered)}, ezért ha a megválaszolt kártyák száma
     * eggyel nő, a getCurrentCard() már a következő kártyát adja. Nem kell
     * semmit külön "betölteni". Ha az answered eléri a kártyák számát, nincs
     * több kártya: hasCurrentCard() hamis, isFinished() igaz, és a következő
     * answer() hívás kivételt dob.
     *
     * Példa három kártyával:
     * <pre>
     * hívás          answered   aktuális kártya      score
     * load(...)         0       1. kártya kérdése      0
     * answer(true)      1       2. kártya kérdése      1
     * answer(false)     2       3. kártya kérdése      1
     * answer(true)      3       nincs, vége            2
     * </pre>
     *
     * @param correct igaz, ha a felhasználó tudta a választ (egy pont jár)
     * @throws IllegalStateException ha nincs aktuális kártya
     */
    public void answer(boolean correct) {
        requireCurrentCard();
        if (correct) {
            score++;
        }
        // Továbblépés: a következő kártya a cards.get(answered) lesz.
        answered++;
        // Az új kártyán megint a kérdés látszik, akkor is, ha az előzőnél
        // épp a választ néztük.
        showingQuestion = true;
    }

    /**
     * @return igaz, ha vannak betöltött kártyák
     */
    public boolean hasCards() {
        return !cards.isEmpty();
    }

    /**
     * @return igaz, ha van aktuális kártya, azaz vannak kártyák, és még nem
     * válaszoltuk meg mindet
     */
    public boolean hasCurrentCard() {
        return answered < cards.size();
    }

    /**
     * @return igaz, ha minden kártyát megválaszoltunk
     */
    public boolean isFinished() {
        return hasCards() && !hasCurrentCard();
    }

    /**
     * @return az aktuális kártya
     * @throws IllegalStateException ha nincs aktuális kártya
     */
    public Card getCurrentCard() {
        requireCurrentCard();
        return cards.get(answered);
    }

    /**
     * @return az aktuális kártya kérdése vagy válasza, attól függően, hogy
     * melyiket mutatjuk
     * @throws IllegalStateException ha nincs aktuális kártya
     */
    public String getCurrentText() {
        Card card = getCurrentCard();
        return showingQuestion ? card.question() : card.answer();
    }

    /**
     * @return igaz, ha az aktuális kártya kérdését mutatjuk, hamis, ha a
     * válaszát
     */
    public boolean isShowingQuestion() {
        return showingQuestion;
    }

    /**
     * @return az eddig megválaszolt kártyák száma (0-tól a kártyák számáig)
     */
    public int getAnsweredCount() {
        return answered;
    }

    /**
     * @return a betöltött kártyák száma
     */
    public int getCardCount() {
        return cards.size();
    }

    /**
     * @return a jó válaszok száma
     */
    public int getScore() {
        return score;
    }

    /**
     * @return a betöltött kártyák, csak olvasható listaként
     */
    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    private void requireCurrentCard() {
        if (!hasCurrentCard()) {
            throw new IllegalStateException("There is no current card");
        }
    }

}
