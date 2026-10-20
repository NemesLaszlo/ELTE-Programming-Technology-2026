# 7. hét – A modell és a nézet szétválasztása

Az előző héten megismerkedtünk a Swinggel, és a `calc`, `qwerty` projektekben már láttuk, hogy a számolást / a szöveg kezelését külön osztályba, a **modellbe** tettük. Ezen a héten ez a szétválasztás a téma: egy kész, működő, de egy osztályba zsúfolt programot alakítunk át **modell–nézet** (MV) szerkezetűre, és megnézzük, mit nyerünk vele. Ugyanezt kell majd megtenni a 2. beadandó játékával is.

## Áttekintés

| Mappa          | Típus | Téma                                                                                   |
| -------------- | ----- | -------------------------------------------------------------------------------------- |
| `Flashcards`   | Példa | Tanulókártyák – **kiinduló változat**: az állapot, a szabályok és a felület egy osztályban |
| `FlashcardsMV` | Példa | Ugyanez **MV-ben**: `FlashcardsModel` Swing nélkül, a nézet egyetlen `refresh()`-sel frissül, JUnit tesztek a modellhez |

A két program **kívülről pontosan ugyanaz**: ugyanaz az ablak, ugyanazok a gombok, ugyanúgy viselkedik. Csak a kód szervezése más. Érdemes a két `FlashcardsGUI` osztályt egymás mellé téve olvasni.

## A program – Tanulókártyák

Egy szövegfájlban kérdés–válasz párok (tanulókártyák) vannak. A program sorban mutatja a kártyákat: először a kérdést, majd kérésre a választ; a felhasználó eldönti, hogy tudta-e a választ, és ennek megfelelően kap pontot. A végén látjuk, hány kártyát tudtunk.

```
┌ Flashcards ───────────────────────────────────────────────┐
│ File                                                      │  ← menüsor: Open... (Ctrl+O), Exit (Ctrl+Q)
├───────────────────────────────────────────────────────────┤
│ Card 2 / 11      Score: 1 / 1                             │  ← állapotsor (NORTH)
│ ┌ Question ─────────────────────────────────────────────┐ │
│ │ What is an abstract method?                           │ │  ← a kártya szövege (CENTER, JTextArea)
│ │                                                       │ │     a keret címe: Question / Answer
│ └───────────────────────────────────────────────────────┘ │
│   [Reset]  [Toggle Q/A]  [Wrong answer]  [Good answer]    │  ← gombsor (SOUTH, FlowLayout)
└───────────────────────────────────────────────────────────┘
```

### A fájl szerkezete

Minden sor egy kártya; a kérdést és a választ a `~` karakter választja el. Az üres sorokat a program átugorja. Egy példafájl (`cards.txt`) mindkét projekt mappájában megtalálható.

```
What is an abstract method?~A method without a body (implementation); the subclasses must implement it.
Which Swing class represents a top-level window?~JFrame
```

A program hibaüzenetet ad, ha a fájl nem olvasható, egy sora nem `kérdés~válasz` alakú (a hibaüzenet megmondja, melyik sor), vagy egyetlen kártyát sem tartalmaz. Hibás fájl esetén a korábban betöltött kártyák megmaradnak.

### Használat

1. *File → Open...* (Ctrl+O): a fájlválasztó a projekt mappájában indul, válasszuk ki a `cards.txt`-t.
2. A kártya kérdését látjuk. *Toggle Q/A*: megmutatja a választ (és vissza).
3. *Good answer* / *Wrong answer*: továbblépünk a következő kártyára, jó válaszért egy pont jár. A következő kártya mindig a kérdésével kezdődik.
4. Az utolsó kártya után a program kiírja az eredményt. *Reset*: elölről kezdjük ugyanazokkal a kártyákkal.

Csak az a gomb nyomható meg, aminek az adott állapotban értelme van: fájl nélkül egyik sem, a végén csak a *Reset*.

## Kiinduló változat – minden a GUI-ban

**Projekt:** `Flashcards`

| Osztály         | Feladat                                                                                            |
| --------------- | -------------------------------------------------------------------------------------------------- |
| `Flashcards`    | belépési pont, az EDT-n létrehozza a felületet                                                     |
| `Card`          | egy kártya: kérdés + válasz (rekord)                                                               |
| `FlashcardsGUI` | **minden más:** a kártyák listája, a pontszám, a szabályok, a fájl beolvasása és a felület felépítése, frissítése |

A `FlashcardsGUI` mezői közül négy nem komponens, hanem **állapot**: a kártyák listája (`cards`), a megválaszolt kártyák száma (`answered`), hogy kérdést vagy választ mutatunk (`showingQuestion`), és a pontszám (`score`). A **szabályok** az eseménykezelőkben vannak: az `answer` metódus dönti el, hogy jár-e pont, hogy van-e még kártya, és hogy a következő kártya kérdéssel kezdődik.

Ez a program működik, és egy ilyen kis feladatnál még át is látható. A gondok akkor jönnek elő, amikor bővíteni vagy tesztelni szeretnénk:

- **Nem tesztelhető.** Ha ellenőrizni szeretnénk, hogy három kártya után tényleg véget ér a gyakorlás, vagy hogy rossz válaszért nem jár pont, csak az ablakot kattintgatva tehetjük meg. JUnit tesztet nem tudunk írni, mert a szabályok a Swing komponensekhez kötődnek (`display.setText`, `statusLabel.setText`).
- **A felület frissítése szétszóródik.** Három frissítő metódus van (`updateDisplay`, `updateStatus`, `updateButtons`), és minden eseménykezelőnek tudnia kell, melyeket kell meghívnia. A *Toggle* csak a kijelzőt frissíti, az *Answer* mindhármat. Egy új funkciónál (pl. a kártyák megkeverése) könnyű egyet kifelejteni, és a felület ellentmondásba kerül az állapottal.
- **Összekeveredik, ami nem tartozik össze.** A fájl beolvasása (`readCards`) és a `~` elválasztó semmit nem tud a felületről, mégis a GUI osztályban van. Ha egy parancssoros vagy webes változatot szeretnénk, mindent újra kellene írni.

## MV változat – modell és nézet

**Projekt:** `FlashcardsMV`

| Osztály           | Réteg  | Feladat                                                                                            |
| ----------------- | ------ | -------------------------------------------------------------------------------------------------- |
| `Flashcards`      | –      | belépési pont, az EDT-n létrehozza a felületet                                                     |
| `Card`            | modell | egy kártya: kérdés + válasz (rekord)                                                               |
| `FlashcardsModel` | modell | a kártyák, az aktuális kártya, a pontszám és a **szabályok**; a fájl beolvasása – **Swing nélkül** |
| `FlashcardsGUI`   | nézet  | a felület felépítése és az eseménykezelők; minden esemény után a `refresh()` a modellből rajzolja újra a felületet |

### A modell

A modell egy sima Java osztály. Az állapotot tárolja, és a műveletei a szabályok szerint módosítják:

| Művelet                     | Mit csinál                                                                                   |
| --------------------------- | -------------------------------------------------------------------------------------------- |
| `load(Path)` / `load(List)` | beolvassa a kártyákat (fájlból vagy listából), és elölről kezdi a gyakorlást                 |
| `readCards(Path)` (statikus) | beolvas egy fájlt; hibás sornál `IOException`-t dob a sor számával                          |
| `reset()`                   | elölről kezdi ugyanazokkal a kártyákkal: első kártya, kérdés, nulla pont                     |
| `toggle()`                  | átvált az aktuális kártya kérdése és válasza között                                          |
| `answer(boolean correct)`   | megválaszolja az aktuális kártyát: jó válaszért pont, továbblép, a következő kérdéssel kezdődik |

| Lekérdezés                                   | Mit ad vissza                                                                |
| -------------------------------------------- | ---------------------------------------------------------------------------- |
| `hasCards()`                                 | vannak-e betöltött kártyák                                                   |
| `hasCurrentCard()`                           | van-e aktuális kártya (vannak kártyák, és nem válaszoltuk meg mindet)        |
| `isFinished()`                               | megválaszoltuk-e mindet                                                      |
| `getCurrentCard()`, `getCurrentText()`       | az aktuális kártya; a kérdése vagy a válasza, attól függően, melyiket mutatjuk |
| `isShowingQuestion()`                        | kérdést mutatunk-e                                                           |
| `getAnsweredCount()`, `getCardCount()`, `getScore()` | hányadik kártyánál járunk, hány kártya van, hány pontunk van         |

Két tervezési döntésre érdemes felfigyelni:

- A **megválaszolt kártyák száma egyben az aktuális kártya indexe** (`answered`), mert a kártyákat sorban vesszük. Nem tárolunk két, egymással szinkronban tartandó számlálót (ld. lent, *Hogyan lépünk tovább?*).
- Ha nincs aktuális kártya, a `toggle()`, az `answer()` és a `getCurrentCard()` **`IllegalStateException`-t dob**, nem csinál csendben semmit. Így a hiba azonnal kiderül (a tesztben is), a nézet pedig egyszerűen nem engedi megnyomni a gombot, ha nincs értelme (`setEnabled(false)`).

#### Hogyan lépünk tovább?

```java
public void answer(boolean correct) {
    requireCurrentCard();
    if (correct) {
        score++;
    }
    answered++;               // továbblépés: a következő kártya a cards.get(answered)
    showingQuestion = true;   // az új kártyán megint a kérdés látszik
}
```

Az `answered++` a továbblépés. Nincs külön „aktuális kártya index” mező: a megválaszolt kártyák száma egyben az aktuális kártya indexe is.

- **Az aktuális kártya** mindig `cards.get(answered)`. Induláskor `answered == 0`, ez az első kártya.
- **Továbblépés:** az `answer` megnöveli az `answered` értékét, ezért a `getCurrentCard()` innentől a következő kártyát adja. Nem kell semmit külön „betölteni”.
- **Kérdéssel kezdünk:** a `showingQuestion = true` miatt az új kártyán megint a kérdés látszik, akkor is, ha az előzőnél épp a választ néztük.
- **A vége:** amikor `answered == cards.size()`, nincs több kártya. A `hasCurrentCard()` ekkor hamis, az `isFinished()` igaz. A `requireCurrentCard()` ezért dob kivételt, ha a végén még egyszer hívnánk az `answer`-t.

Egy példa három kártyával:

| Hívás           | `answered` | aktuális kártya   | `score` |
| --------------- | ---------- | ----------------- | ------- |
| `load(...)`     | 0          | 1. kártya kérdése | 0       |
| `answer(true)`  | 1          | 2. kártya kérdése | 1       |
| `answer(false)` | 2          | 3. kártya kérdése | 1       |
| `answer(true)`  | 3          | nincs, vége       | 2       |

A nézet ebből semmit nem tud: csak meghívja a `model.answer(correct)`-et, aztán a `refresh()` megkérdezi a modellt, hogy van-e aktuális kártya, és annak a szövegét írja ki. Ezért elég egy számláló, és nem kell két értéket, egy indexet és egy darabszámot szinkronban tartani.

A `readCards` **statikus** metódus: nem függ a modell állapotától, egy fájlból egy listát készít. Így külön tesztelhető, és a `load(Path)` csak akkor cseréli le a kártyákat, ha a beolvasás sikerült. Ezért marad meg a régi kártyacsomag, ha hibás fájlt nyitunk meg.

### A nézet

A nézet felépíti a felületet, az eseménykezelői **csak továbbítanak** a modellnek, és minden esemény után a `refresh()` fut le:

```java
toggleButton.addActionListener(e -> {
    model.toggle();
    refresh();
});
```

A `refresh()` az **egyetlen hely**, ahol a nézet a modellt olvassa, és mindent beállít belőle: a kártya szövegét, a keret címét (*Question* / *Answer*), az állapotsort és a gombok engedélyezettségét.

```java
private void refresh() {
    if (model.hasCurrentCard()) {
        display.setText(model.getCurrentText());
        displayBorder.setTitle(model.isShowingQuestion() ? "Question" : "Answer");
        statusLabel.setText("Card " + (model.getAnsweredCount() + 1) + " / " + model.getCardCount()
                + "      Score: " + model.getScore() + " / " + model.getAnsweredCount());
    } else if (model.isFinished()) {
        // ... az eredmény kiírása
    } else {
        // ... "nyissunk meg egy fájlt"
    }
    resetButton.setEnabled(model.hasCards());
    toggleButton.setEnabled(model.hasCurrentCard());
    wrongButton.setEnabled(model.hasCurrentCard());
    goodButton.setEnabled(model.hasCurrentCard());
    displayPane.repaint();
}
```

Így a felület **soha nem kerülhet ellentmondásba** a modellel: nem gombonként, "kézzel" tartjuk szinkronban, hanem minden esemény után a teljes felületet újrarajzoljuk az állapotból. Egy ilyen kis felületnél ez semmibe nem kerül; a beadandó néhány tucat gombjánál sem.

A nézetben **nincs szabály**: nem tudja, hány kártya van, nem dönti el, jár-e pont – ezt mind a modelltől kérdezi. Ha egy `if` a nézetben az állapotról szól (és nem arról, hogy mit hogyan rajzoljunk), az valószínűleg a modellbe való.

### Esemény – eseménykezelő párosítások

| Esemény                      | Eseménykezelő                              | Tevékenység                                                      |
| ---------------------------- | ------------------------------------------ | ---------------------------------------------------------------- |
| *File → Open...* (Ctrl+O)    | `this::open` (metódusreferencia)           | fájlválasztó; `model.load(file)`; hiba esetén hibaüzenet; `refresh()` |
| *File → Exit* (Ctrl+Q)       | lambda                                     | kilép a programból                                               |
| *Reset* gomb                 | lambda                                     | `model.reset()`, `refresh()`                                     |
| *Toggle Q/A* gomb            | lambda                                     | `model.toggle()`, `refresh()`                                    |
| *Wrong answer* / *Good answer* gomb | `AnswerListener(correct)` (belső osztály) | `model.answer(correct)`, `refresh()`                       |

### Tesztek

Mivel a modellben nincs Swing, a szabályok ablak nélkül, JUnit-tal ellenőrizhetők: `test/flashcards/FlashcardsModelTest.java` (JUnit 4, mint a 4. heti `Race`). NetBeansben **Alt+F6** futtatja.

```java
@Test
public void answeringAllCardsFinishesThePractice() {
    model.load(List.of(new Card("Q1", "A1"), new Card("Q2", "A2"), new Card("Q3", "A3")));

    model.answer(true);
    model.answer(false);
    model.answer(true);

    assertTrue(model.isFinished());
    assertFalse(model.hasCurrentCard());
    assertEquals(2, model.getScore());
}
```

A tesztek három csoportban vannak: állapot kártyák nélkül, a gyakorlás menete (továbblépés, pontozás, a gyakorlás vége, `reset`), és a fájl beolvasása (üres sorok, hibás sor sorszámmal, üres és hiányzó fájl). A fájlos tesztek egy ideiglenes mappába (`TemporaryFolder`) írnak, ami a teszt után törlődik. Az utolsó teszt a projekt mellé tett `cards.txt`-t olvassa be, így az is kiderül, ha a példafájl romlik el.

Ugyanezeket a teszteket a `Flashcards` projektben **nem lehetne megírni**: ott a `FlashcardsGUI` konstruktora ablakot nyit, és a szabályok a komponensek frissítésével keverednek.

## Hogyan válasszuk szét? – lépésről lépésre

Ahogy a `Flashcards`-ból a `FlashcardsMV` lett. Ugyanígy érdemes eljárni, ha a beadandó játéka egy osztályban készült el, vagy ha eleve így tervezzük meg.

1. **Keressük meg az állapotot.** Melyik mezők *nem* Swing komponensek? Itt: `cards`, `answered`, `showingQuestion`, `score`. Ezek lesznek a modell mezői. Mindegyik `private`, a nézet csak lekérdező metódusokon keresztül látja őket.

2. **Keressük meg a szabályokat.** Melyik kódrészek módosítják ezeket a mezőket, és milyen feltételekkel? Ezek lesznek a modell műveletei (`reset`, `toggle`, `answer`, `load`). Az eseménykezelőből csak a modell hívása és a `refresh()` marad:

   ```java
   // Flashcards (előtte): szabály + frissítés egy helyen
   private void answer(boolean correct) {
       if (answered < cards.size()) {
           if (correct) {
               score++;
           }
           answered++;
           showingQuestion = true;
           updateDisplay();
           updateStatus();
           updateButtons();
       }
   }

   // FlashcardsMV (utána): a szabály a modellben...
   public void answer(boolean correct) {
       requireCurrentCard();
       if (correct) {
           score++;
       }
       answered++;
       showingQuestion = true;
   }

   // ...az eseménykezelő csak továbbít
   model.answer(correct);
   refresh();
   ```

3. **Ami nem a felületről szól, az is a modellbe megy.** A fájl beolvasása (`readCards`), a `~` elválasztó, a hibaüzenetek szövege a hibás sorról – ezek akkor is kellenének, ha holnap lecserélnénk a felületet.

4. **Egyetlen `refresh()`.** A szétszórt frissítő metódusok helyett egy metódus rajzolja újra a teljes felületet a modellből. Minden eseménykezelő ezt hívja a végén. A nézet ettől "buta" lesz, és ez jó: nincs mit elrontani benne.

5. **Írjunk teszteket a modellhez.** Most már lehet. A tesztek egyben dokumentálják is a szabályokat: a `theNextCardAlwaysStartsWithItsQuestion` neve többet mond, mint egy komment.

Ellenőrzés: a modell osztályban **egyetlen `javax.swing` vagy `java.awt` import sem lehet**. Ha van, valami a nézetbe való.

|                              | `Flashcards`                                       | `FlashcardsMV`                                           |
| ---------------------------- | -------------------------------------------------- | -------------------------------------------------------- |
| Az állapot helye             | a GUI mezői                                        | `FlashcardsModel` (private mezők, lekérdező metódusok)   |
| A szabályok helye            | az eseménykezelőkben                               | a modell műveleteiben                                    |
| A felület frissítése         | 3 metódus, az eseménykezelő választ közülük         | egyetlen `refresh()` a modellből                         |
| Fájl beolvasása              | a GUI-ban                                          | a modellben, statikus metódus                            |
| Tesztelhetőség               | csak kattintgatva                                  | 22 JUnit teszt, ablak nélkül                             |
| Osztályok                    | 3                                                  | 4 (+ 1 tesztosztály)                                     |

## Kitekintés – a 2. beadandó

A beadandó játékánál ugyanez a két réteg:

| Flashcards        | Táblás játék                        | Mi van benne                                                            |
| ----------------- | ----------------------------------- | ----------------------------------------------------------------------- |
| `Card`            | `Field`, `Player`, `Position`       | egyszerű adatosztályok, enumok, rekordok                                |
| `FlashcardsModel` | `Board` (`Game`)                    | a tábla állapota, ki következik, a lépés szabályai, a játék vége, a győztes – Swing nélkül |
| `FlashcardsGUI`   | `BoardGUI` / `...GUI`               | gombtábla, menü, állapotsor; az eseménykezelők a modellt hívják, majd `refresh()` |
| `FlashcardsModelTest` | `BoardTest`                     | a szabályok tesztjei: szabálytalan lépés, győzelem felismerése, döntetlen |

Jó jel, ha a `Board` osztályt egy parancssoros programból is lehetne használni – ha a modell így is "játszható", a szétválasztás sikerült. A 8. heti `Amoba` és `Colorclicker` projektek pont így épülnek fel.

## Futtatás

A projektekhez **JDK 17 vagy újabb** szükséges.

- **NetBeans:** nyissuk meg a projektet, majd **F6** (Run Project); a `FlashcardsMV` tesztjei **Alt+F6**. A tesztekhez a NetBeansbe épített JUnit 4 könyvtárat használja a projekt.
- **Parancssorból** (a projekt mappájából, hogy a fájlválasztó a `cards.txt` mellett induljon):

  ```
  javac -d build/classes src/flashcards/*.java
  java -cp build/classes flashcards.Flashcards
  ```

  A `FlashcardsMV` tesztjei parancssorból a JUnit 4 és a Hamcrest jar-ral futtathatók:

  ```
  javac -d build/classes src/flashcards/*.java
  javac -cp "build/classes;junit-4.13.2.jar;hamcrest-core-1.3.jar" -d build/test test/flashcards/*.java
  java -cp "build/classes;build/test;junit-4.13.2.jar;hamcrest-core-1.3.jar" org.junit.runner.JUnitCore flashcards.FlashcardsModelTest
  ```

  (Linuxon és macOS-en a `;` helyett `:` az elválasztó az osztályútvonalban.)

## Tippek

- Már a tervezésnél írjuk fel két oszlopba, mi az **állapot és szabály** (modell), és mi a **megjelenítés** (nézet). Ha egy mező vagy metódus nem fér be egyértelműen valamelyikbe, gondoljuk át újra.
- A modell műveletei legyenek **a feladat nyelvén** megnevezve (`answer`, `toggle`, `step`, `newGame`), ne a felület nyelvén (`buttonClicked`, `onToggle`). A modell nem tud a gombokról.
- A nézet ne tárolja másodszor is azt, amit a modell tud. Ha a nézetnek van egy `int score` mezője a modell mellett, az előbb-utóbb eltér a modelltől.
- Az `IllegalStateException` (nincs aktuális kártya) és az `IllegalArgumentException` (hibás paraméter) a modell "védőkorlátjai": a programozási hibát azonnal jelzik. A felhasználó hibáját (hibás fájl) ellenőrzött kivétellel (`IOException`) jelezzük, amit a nézet üzenetablakban mutat meg.
- Ha a modell értesíteni tudná a nézetet a változásairól, a `refresh()` hívásokat sem kellene kézzel kiírni – ez a 6. heti `calcMVC` *observer* mintája. A beadandóhoz az MV és a kézi `refresh()` bőven elég.
