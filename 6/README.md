# 6. hét – GUI programozás Swinggel

Ezen a héten elkezdjük a grafikus felhasználói felületű (GUI) alkalmazások fejlesztését. A félév hátralévő részében – így a 2. és a 3. beadandóban is – **Swinget** használunk.

## Áttekintés

| Mappa    | Típus   | Téma                                                                          |
| -------- | ------- | ----------------------------------------------------------------------------- |
| `calc`   | Példa   | Egyszerűsített számológép – szövegmezők, gombok, menü, hibakezelés            |
| `calcMVC` | Példa (Maven) | Ugyanez a számológép MVC-ben, bővítve: előzmények, memória, tesztek |
| `qwerty` | Feladat | QWERTY billentyűzet kijelzővel – gombok ciklusból, többféle eseménykezelő     |

## Miért Swing?

- **A JDK része**, nem kell hozzá külső könyvtár, NetBeansben azonnal használható.
- **Átlátható:** a felületet kódból építjük fel, minden lépés látszik, nincs mögötte "varázslat".
- **Éles rendszerekben is bevált:** évtizedekig használták, és sok helyen ma is használják. Itthon például a NAV nyomtatványkitöltő programja (ÁNYK) Java alapú asztali alkalmazás; nemzetközi példa a JetBrains IntelliJ IDEA (és az erre épülő Android Studio) vagy maga a NetBeans, amelyek felülete Swingre épül. Banki, tőzsdei és vállalati belső rendszerekben is gyakori volt.
- **A tanult fogalmak átvihetők.** A technológia más, de a gondolkodásmód ugyanaz a weben (HTML/JS, React), más asztali keretrendszerekben (JavaFX, WPF, Qt) és mobilon is: komponensekből fát építünk, egy elrendezés (layout) helyezi el őket, és eseménykezelők reagálnak a felhasználó műveleteire.

| Swing                                   | Web (HTML / JS / React)                   | Más asztali keretrendszerek            |
| --------------------------------------- | ----------------------------------------- | -------------------------------------- |
| `JFrame`, `JPanel` (konténer)           | `<body>`, `<div>`                         | Window, Stage, Panel                   |
| `JButton`, `JTextField`, `JLabel`       | `<button>`, `<input>`, `<span>`           | Button, TextBox / TextField, Label     |
| layout manager (`BorderLayout`, ...)    | CSS flexbox / grid                        | Grid, StackPanel, HBox / VBox          |
| `addActionListener(...)`                | `addEventListener("click", ...)`, `onClick` | Click esemény, `setOnAction(...)`    |
| Event Dispatch Thread (EDT)             | a böngésző event loopja                   | UI szál                                |

## Swing alapok – amire figyelni kell

### 1. Komponensfa

Egy Swing felület konténerek és komponensek fája. A legfelső szint az ablak (`JFrame`); ennek a *tartalompaneljébe* (content pane) kerülnek a panelek (`JPanel`), azokba pedig a gombok, szövegmezők, címkék.

```java
import javax.swing.*;

public class Hello {

    public static void main(String[] args) {
        // A felületet mindig az eseménykezelő szálon építjük fel (ld. 4. pont).
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hello");                     // az ablak
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JLabel label = new JLabel("Hello!");
            JButton button = new JButton("Click me");
            button.addActionListener(e -> label.setText("Hello, Swing!"));

            JPanel panel = new JPanel();                             // konténer
            panel.add(label);
            panel.add(button);
            frame.add(panel);           // valójában a tartalompanelbe kerül

            frame.pack();                       // méret a tartalom alapján
            frame.setLocationRelativeTo(null);  // a képernyő közepére
            frame.setVisible(true);             // mindig a legvégén!
        });
    }
}
```

A fenti program komponensfája:

```
JFrame "Hello"
└── content pane (BorderLayout)
    └── JPanel (FlowLayout)
        ├── JLabel "Hello!"
        └── JButton "Click me"
```

- Egy komponensnek **csak egy szülője** lehet. Ha egy már máshol lévő komponenst újra hozzáadunk valahová, az átkerül, nem másolódik.
- A felületet érdemes előbb lerajzolni papírra, és a fát ennek alapján felépíteni: melyik panel melyik régióba kerül, és milyen elrendezéssel.

### 2. Elrendezéskezelők (layout manager)

A komponensek helyét és méretét nem pixelre adjuk meg, hanem a konténer elrendezéskezelője számolja ki. Így az ablak átméretezésekor is rendezett marad a felület.

| Elrendezés    | Működés                                                                                     | Mire jó?                                    |
| ------------- | ------------------------------------------------------------------------------------------- | ------------------------------------------- |
| `BorderLayout` | 5 régió: `NORTH`, `SOUTH`, `EAST`, `WEST`, `CENTER`. A `CENTER` kapja a maradék helyet.   | az ablak fő felosztása (a content pane alapértelmezése) |
| `FlowLayout`  | egymás után, balról jobbra, ha nem fér ki, új sorba tör                                     | gombsor, eszköztár (a `JPanel` alapértelmezése) |
| `GridLayout`  | `rows × cols` egyforma méretű cella, sorfolytonosan töltjük fel                             | **játéktábla**, számbillentyűzet            |
| `BoxLayout`   | egyetlen sor vagy oszlop, a komponensek megtartják a saját méretüket                        | űrlapok, függőleges listák                  |

Összetett felületet **egymásba ágyazott panelekkel** építünk: minden panel kap egy egyszerű elrendezést.

```java
JPanel content = new JPanel(new BorderLayout());
content.add(display, BorderLayout.NORTH);      // felül a kijelző
content.add(boardPanel, BorderLayout.CENTER);  // középen a tábla (GridLayout)
content.add(statusLabel, BorderLayout.SOUTH);  // alul egy állapotsor
frame.setContentPane(content);
```

Figyeljünk rá:

- `BorderLayout`-ban **régiónként egy** komponens lehet. Ha egy régióba többet szeretnénk tenni, tegyünk oda egy panelt, és abba a komponenseket.
- A `GridLayout(0, 2)` jelentése: 2 oszlop, a sorok száma pedig annyi, amennyi kell.
- Kerüljük a `setLayout(null)` + `setBounds(...)` abszolút pozícionálást: más képernyőn, más betűmérettel szétesik a felület.
- Méret: `frame.pack()` a komponensek kívánt mérete alapján állítja be az ablakot. Ha egy komponens méretét szeretnénk befolyásolni (pl. a tábla mezőiét), a `setPreferredSize(...)` metódust használjuk, ne az ablak `setSize`-át.

### 3. Eseménykezelés

A GUI programok **eseményvezéreltek**: a `main` felépíti a felületet és véget ér, utána a program csak akkor csinál valamit, ha esemény történik (kattintás, billentyűleütés, menüválasztás). Egy eseményhez **eseménykezelőt** (listener) rendelünk – gomboknál és menüpontoknál az `ActionListener` interfészt.

Ugyanazt az eseménykezelőt négyféleképpen írhatjuk meg. A `qwerty` projektben mind a négy forma szerepel:

```java
// 1. Névvel rendelkező (belső) osztály – ha paramétere van, vagy több helyen használjuk
class LetterListener implements ActionListener {
    private final String letter;

    LetterListener(String letter) {
        this.letter = letter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        display.setText(display.getText() + letter);
    }
}
button.addActionListener(new LetterListener("Q"));

// 2. Névtelen (anonim) osztály – a lambdák előtti (Java 8 előtti) szokásos forma
exitItem.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }
});

// 3. Lambda – rövid, egyszer használt kódhoz a legtömörebb
clearButton.addActionListener(e -> display.setText(""));

// 4. Metódusreferencia – ha a metódus paramétere (ActionEvent) egyezik az actionPerformed-ével
backButton.addActionListener(this::backspace);   // private void backspace(ActionEvent e)
```

- A belső osztály és a lambda **eléri a külső osztály mezőit** (pl. `display`), ezért nem kell mindent paraméterként átadni.
- Lambdában csak *effektíve final* lokális változót használhatunk. Ciklusban ezért másolatot készítünk a ciklusváltozóról (ld. lent, a beadandós táblánál).
- Az `e.getSource()` megadja, melyik komponens váltotta ki az eseményt – de olvashatóbb, ha az eseménykezelő eleve tudja, mihez tartozik (paraméter, lambda).

**Mit jelent a `->` és a `::`?** Az `addActionListener` egy *eseménykezelő objektumot* vár: valamit, aminek van `actionPerformed` metódusa. Mivel az `ActionListener` interfésznek csak ez az egy metódusa van (*funkcionális interfész*), elég megadni a metódus törzsét – ez a **lambda**: `e -> display.setText("")`. A nyíl bal oldalán a paraméter (az esemény, `e`), jobb oldalán a végrehajtandó kód. A **metódusreferencia** (`this::backspace`) még rövidebb: nem írunk törzset, csak megnevezünk egy már létező metódust, amit az eseménykor meg kell hívni. A `::` előtt az áll, *kinek* a metódusa (`this` – a saját objektumunk, de lehet más objektum is, pl. `model::clearHistory`). Fontos: a `this::backspace` **nem hívja meg** a metódust, csak átadja, hogy majd a gomb megnyomásakor hívódjon meg.

> **Beadandóhoz:** a dokumentációban fel kell sorolni az *esemény – eseménykezelő* párosításokat. Ehhez jó minta a lenti táblázatok formája: melyik komponens melyik eseményére melyik kezelő fut, és az mit csinál.

### 4. Az eseménykezelő szál (EDT)

A Swing **nem szálbiztos**: minden komponenst egyetlen szálon, az *Event Dispatch Thread*-en (EDT) szabad létrehozni és módosítani. Az eseménykezelők automatikusan ezen futnak.

- A `main`-ben ezért `SwingUtilities.invokeLater(...)` segítségével építjük fel a felületet.
- Az eseménykezelő legyen **gyors**. Amíg fut, a felület nem rajzolódik újra és nem reagál – egy `Thread.sleep` vagy egy hosszú ciklus "lefagyasztja" az ablakot.
- Időzített, ismétlődő feladathoz (animáció, óra, számláló) a `javax.swing.Timer` való, ez is az EDT-n futtatja a kódot:

  ```java
  Timer timer = new Timer(1000, e -> secondsLabel.setText(String.valueOf(++seconds)));
  timer.start();   // másodpercenként lefut; timer.stop() állítja le
  ```

### 5. Gyakori hibák

- **`setVisible(true)` túl korán.** Ha utána adunk komponenseket az ablakhoz, nem jelennek meg biztosan. A `setVisible(true)` legyen a legvégén, előtte `pack()`.
- **Hiányzó `EXIT_ON_CLOSE`.** Alapértelmezésben az ablak bezárása csak elrejti az ablakot, a program tovább fut.
- **Komponens cseréje látható ablakban.** Ha futás közben veszünk fel vagy veszünk le komponenst (pl. új játéknál új táblát), utána `revalidate()` és `repaint()` kell – vagy `pack()`, ha az ablak méretének is igazodnia kell.
- **A szövegmező tartalma mindig `String`.** Számmá alakítani nekünk kell, és a `NumberFormatException`-t kezelni (ld. `calc`).
- **Szövegek összehasonlítása `==`-vel.** A `getText()` eredményét `equals`-szal hasonlítsuk össze.
- **Üzenetablak szülő nélkül.** A `JOptionPane.showMessageDialog(frame, ...)` első paramétere a szülőablak: fölötte jelenik meg, és amíg nyitva van, a főablak nem kezelhető (modális).
- **Minden logika a GUI-ban.** Ha a játékszabályok az eseménykezelőkben vannak, a kód gyorsan átláthatatlanná válik és nem tesztelhető. A számolás / játéklogika kerüljön külön osztályba (modell), a GUI csak megjelenít és továbbít (ld. a következő pontot, a `calc` – `Operation` párost, illetve a 7. heti `FlashcardsMV`-t).

### 6. Kódszervezés: modell–nézet (MV) és modell–nézet–vezérlő (MVC)

Egy GUI program kódja három dolgot csinál: **tárolja az állapotot és alkalmazza a szabályokat** (modell), **megjeleníti** azt (nézet), és **reagál a felhasználóra** (vezérlő). Ha ezek egy osztályban keverednek, a program nehezen érthető, nehezen módosítható és nem tesztelhető. A két legelterjedtebb szétválasztás:

- **MV (modell–nézet):** a modell egy sima Java osztály, Swing nélkül. A nézet felépíti a felületet, az eseménykezelői meghívják a modellt, majd a nézet újrarajzolja magát a modell alapján. A nézet tehát egyben vezérlő is.
- **MVC (modell–nézet–vezérlő):** a vezérlőt is külön osztályba tesszük. A nézet csak a komponenseket építi fel és jeleníti meg az adatokat; a vezérlő köti össze az eseményeket a modell műveleteivel. A modell értesíti a feliratkozókat, ha megváltozott (*observer* minta), így a nézet magától frissül.

Mindkettőt egy egyszerű számlálón mutatjuk be: egy `+1` és egy `Reset` gomb, valamint egy címke, ami az aktuális értéket mutatja.

**MV** – a modell semmit nem tud a felületről:

```java
public class CounterModel {

    private int value;

    public void increment() {
        value++;
    }

    public void reset() {
        value = 0;
    }

    public int getValue() {
        return value;
    }
}
```

```java
public class CounterGUI {

    private final CounterModel model = new CounterModel();
    private final JLabel valueLabel = new JLabel();

    public CounterGUI() {
        JButton incButton = new JButton("+1");
        // Esemény -> a modell módosítása -> a nézet frissítése.
        incButton.addActionListener(e -> {
            model.increment();
            refresh();
        });
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            model.reset();
            refresh();
        });

        JFrame frame = new JFrame("Counter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.add(valueLabel);
        panel.add(incButton);
        panel.add(resetButton);
        frame.add(panel);
        refresh();
        frame.pack();
        frame.setVisible(true);
    }

    /** A felületet mindig a modell állapotából rajzoljuk újra. */
    private void refresh() {
        valueLabel.setText("Value: " + model.getValue());
    }
}
```

**MVC** – a modell értesít, ha változott; a nézet csak megjelenít; a vezérlő köt össze mindent:

```java
public class CounterModel {

    private int value;
    private final List<Runnable> listeners = new ArrayList<>();

    /** Feliratkozás: a listener minden változás után lefut. */
    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    public void increment() {
        value++;
        fireChanged();
    }

    public void reset() {
        value = 0;
        fireChanged();
    }

    public int getValue() {
        return value;
    }

    private void fireChanged() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
```

```java
public class CounterView {

    private final JLabel valueLabel = new JLabel();
    private final JButton incButton = new JButton("+1");
    private final JButton resetButton = new JButton("Reset");

    public CounterView() {
        JFrame frame = new JFrame("Counter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.add(valueLabel);
        panel.add(incButton);
        panel.add(resetButton);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    /** A nézet nem ismeri a modellt: csak azt jeleníti meg, amit kap. */
    public void showValue(int value) {
        valueLabel.setText("Value: " + value);
    }

    public JButton getIncButton() {
        return incButton;
    }

    public JButton getResetButton() {
        return resetButton;
    }
}
```

```java
public class CounterController {

    public CounterController(CounterModel model, CounterView view) {
        // Felhasználói esemény -> modellművelet
        view.getIncButton().addActionListener(e -> model.increment());
        view.getResetButton().addActionListener(e -> model.reset());
        // Modellváltozás -> a nézet frissítése
        model.addChangeListener(() -> view.showValue(model.getValue()));
        view.showValue(model.getValue());
    }
}
```

```java
SwingUtilities.invokeLater(() -> new CounterController(new CounterModel(), new CounterView()));
```

|                          | MV                                           | MVC                                                       |
| ------------------------ | -------------------------------------------- | --------------------------------------------------------- |
| Osztályok                | modell + nézet                               | modell + nézet + vezérlő                                  |
| Ki kezeli az eseményeket? | a nézet                                     | a vezérlő                                                 |
| Ki frissíti a felületet? | a nézet, minden esemény után (`refresh()`)   | a modell értesítése alapján, automatikusan                |
| Mikor jó?                | kisebb programok, **a beadandókhoz bőven elég** | nagyobb programok, több nézet ugyanarra a modellre (pl. tábla + pontszám panel) |

**Hova kerül a számolás / a játékszabály?** A modellbe – akkor is, ha MVC-ben a vezérlő "irányít". A vezérlő csak közvetít: beolvas a nézetből, meghívja a modellt, és továbbadja az eredményt a nézetnek. Ökölszabály: ha egy kód akkor is kellene, ha holnap lecserélnénk a felületet, az a modellbe való.

Egy teljes, kibővített MVC példa a `calcMVC` projekt (ld. lent).

Mindkét esetben a lényeg ugyanaz: **a modellben nincs Swing**, ezért külön, felület nélkül is tesztelhető JUnit-tal (ld. 4. hét), és a felület lecserélhető anélkül, hogy a szabályokhoz hozzányúlnánk. Maga a Swing is ezt a gondolatot követi: a `JTable` adatait egy `TableModel`, a `JList` elemeit egy `ListModel` tárolja, a komponens csak megjeleníti őket. Ugyanez a szétválasztás jelenik meg a weben is (pl. Reactben az állapot és a komponensek), és a legtöbb asztali és mobil keretrendszerben (MVC, MVVM).

## Példa – Egyszerűsített számológép

**Projekt:** `calc`

Az egyszerűsített számológép, amit a félév elején átfutottunk. A számológépnek három szövegmezője van a két operandus és az eredmény tárolására, öt gomb pedig a különböző műveletek (összeadás, kivonás, szorzás, osztás, hatványozás) végrehajtására szolgál. A program ellenőrzi a bemenetet, és hibaüzenetet ad, ha az hibás. A programból menüből is ki lehet lépni.

```
┌ Calculator ─────────────────────┐
│ Calc                            │  ← menüsor
├─────────────────────────────────┤
│ A:      [_____________________] │
│ B:      [_____________________] │  ← címkék (WEST) és mezők (CENTER)
│ Result: [_____________________] │
│ [ + ] [ - ] [ * ] [ / ] [ ^ ]   │  ← gombsor (SOUTH, GridLayout)
└─────────────────────────────────┘
```

### Felépítés

| Osztály     | Feladat                                                                               |
| ----------- | ------------------------------------------------------------------------------------- |
| `Calc`      | belépési pont, az EDT-n létrehozza a felületet                                       |
| `CalcGUI`   | a felület felépítése és az eseménykezelők                                              |
| `Operation` | a műveletek (`enum`): jel + számoló függvény – itt van a "logika", nem a GUI-ban     |

A gombokat nem egyesével írjuk meg: a program végigmegy az `Operation` értékein, és mindegyikhez létrehoz egy gombot ugyanazzal az eseménykezelő osztállyal, csak más paraméterrel. Új művelethez (pl. maradékos osztás) elég egy új sort felvenni az `Operation`-be, a gomb magától megjelenik.

### Esemény – eseménykezelő párosítások

| Esemény                       | Eseménykezelő              | Tevékenység                                                                   |
| ----------------------------- | -------------------------- | ----------------------------------------------------------------------------- |
| `+`, `-`, `*`, `/`, `^` gomb  | `CalcActionListener(op)`   | beolvassa a két operandust, elvégzi a műveletet, kiírja az eredményt; hiba esetén hibaüzenet |
| *Calc → Clear* (Ctrl+L)       | lambda                     | törli mindhárom mezőt                                                         |
| *Calc → Use result as A* (Ctrl+R) | lambda                 | az eredményt átírja az A mezőbe, így láncolhatók a műveletek                  |
| *Calc → Exit* (Ctrl+Q)        | lambda                     | kilép a programból                                                            |

### Hibakezelés

A program hibaüzenetet ad (és törli a korábbi eredményt), ha

- valamelyik operandus üres vagy nem szám (a `NaN` és az `Infinity` szöveget sem fogadjuk el, pedig a `Double.parseDouble` igen),
- nullával osztunk (lebegőpontos osztásnál ez nem dob kivételt, csak végtelent ad, ezért külön ellenőrizzük),
- az eredmény nem véges valós szám, pl. `(-8) ^ 0.5` vagy túlcsordulás.

Tizedesvesszőt és tizedespontot is elfogadunk (`2,5` és `2.5`), az egész eredmények pedig `.0` nélkül jelennek meg.

## Példa – Számológép MVC-ben

**Projekt:** `calcMVC` – részletes leírás: [`calcMVC/README.md`](calcMVC/README.md)

Ugyanez a számológép modell–nézet–vezérlő szerkezetben, kibővítve, és úgy felépítve, ahogy egy éles alkalmazást építenénk:

- **három réteg, három csomag:** `calc.model` (Swing nélkül), `calc.view`, `calc.controller`; a modell változásakor értesíti a vezérlőt, ami frissíti a nézetet,
- **új funkciók:** maradékos osztás, előzmények (visszatölthetők), memória (`MC`, `MR`, `M+`, `M−`), Enter = az utolsó művelet ismétlése, vágólapra másolás, A és B cseréje,
- **egyszerű modell:** a számolás szabályai (`Operation`), az előzmények és a memória – a számolás a modellben van, nem a vezérlőben,
- **hibák az állapotsorban** felugró ablak helyett, a hibás mező megjelölésével,
- **JUnit tesztek** a modellhez, ablak nélkül,
- a README-je röviden elmagyarázza a lambdákat és a metódusreferenciákat (`this::calculate`, `model::clearHistory`),
- **Maven projekt:** NetBeansben *File → Open Project*, majd a `calcMVC` mappa; futtatás **F6**, tesztek **Alt+F6**.

A `calc` és a `calcMVC` összevetése jól mutatja, mit nyerünk a szétválasztással: a számolás szabályai felület nélkül tesztelhetők, és a felület átrendezése (pl. a gombok helyett menü) nem érinti a modellt.

## Feladat – QWERTY billentyűzet

**Projekt:** `qwerty`

Készítsünk egy QWERTY billentyűzetet és hozzá egy kijelzőt (ami egy szövegdoboz)! A billentyűzeten a `Q`, `W`, `E`, `R`, `T`, `Y` gombok, a Backspace, és egy `CLR` gomb van, ami törli a kijelző tartalmát.

> Próbáljuk meg előbb önállóan megoldani, csak utána nézzük meg a megoldást!

```
┌ QWERTY ─────────────────────────────────────────────┐
│ QWERTY                                              │  ← menüsor
├─────────────────────────────────────────────────────┤
│ [ QWERTY                                          ] │  ← kijelző (NORTH)
│ [Q] [W] [E] [R] [T] [Y] [Back] [CLR] [Caps]         │  ← gombok (CENTER, FlowLayout)
│ Characters: 6                                       │  ← állapotsor (SOUTH)
└─────────────────────────────────────────────────────┘
```

### A megoldás

- A betűgombokat egy tömbből, **ciklusban** hozzuk létre – ugyanígy készül majd a beadandók játéktáblája is.
- A kijelző nem szerkeszthető (`setEditable(false)`), rá csak a gombokkal lehet írni.
- A kijelző minden módosítása egyetlen metóduson (`setDisplayText`) megy keresztül, így az állapotsor karakterszámlálója mindig naprakész.
- Az eseménykezelők a négy lehetséges formában készültek (ld. fent), hogy mindegyikre legyen példa.

A feladatleíráson túli kiegészítések:

- **Caps** kapcsológomb (`JToggleButton`): kis- és nagybetűk között vált, a gombok felirata is követi.
- **Fizikai billentyűzet:** a `Q`…`Y`, a Backspace és az Esc billentyű is működik, mintha a gombot nyomtuk volna meg. Ehhez az ablak `InputMap`-jébe és `ActionMap`-jébe vesszük fel a billentyűket (*key binding*), és a gomb `doClick()` metódusát hívjuk:

  ```java
  JRootPane root = frame.getRootPane();
  root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "clear");
  root.getActionMap().put("clear", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
          clearButton.doClick();
      }
  });
  ```

  A billentyűkezelésnek ez a javasolt módja Swingben. A `KeyListener` csak a fókuszban lévő komponensen működik, ezért könnyen "elveszik". A beadandóknál is jól jön, ha nyilakkal szeretnénk irányítani.

### Esemény – eseménykezelő párosítások

| Esemény                                | Eseménykezelő                    | Tevékenység                                     |
| -------------------------------------- | -------------------------------- | ----------------------------------------------- |
| `Q`…`Y` gomb vagy billentyű            | `QwertyListener(letter)` (belső osztály) | a betűt (a Caps szerinti alakban) a kijelző végére írja |
| `Back` gomb vagy Backspace             | `this::backspace` (metódusreferencia)   | törli az utolsó karaktert, ha van              |
| `CLR` gomb vagy Esc                    | lambda                           | törli a kijelzőt                                |
| `Caps` kapcsoló                        | lambda                           | átírja a betűgombok feliratát kis- / nagybetűre |
| *QWERTY → Exit* (Ctrl+Q)               | névtelen osztály                 | kilép a programból                              |

### Továbbfejlesztési ötletek

- Teljes billentyűzet: a sorokat (`"QWERTYUIOP"`, `"ASDFGHJKL"`, `"ZXCVBNM"`) egy-egy panelbe tegyük, a paneleket pedig egymás alá egy `GridLayout(0, 1)`-be.
- `Space` és `Enter` gomb; az Enter egy `JOptionPane`-ben írja ki a beírt szöveget.
- Legfeljebb 30 karakter fér a kijelzőre; ha megtelt, a betűgombok legyenek letiltva (`setEnabled(false)`).

## Kitekintés – a 2. beadandó

A 2. beadandóban egy kétszemélyes táblás játékot (vagy egy logikai játékot) kell elkészíteni Swinggel. A közös követelmények röviden:

- **Felhasználóbarát** és könnyen kezelhető felület; objektumorientált megoldás (a többrétegű architektúra nem kötelező, de a modell és a nézet szétválasztása erősen ajánlott).
- Megjelenítés **vezérlőkkel** (pl. gombokból álló tábla) vagy **elemi grafikával** (rajzolás).
- Különböző táblaméreteknél az **ablakméret mindig igazodjon** a táblához.
- **Új játék** a menüből, a feladatban megadott táblaméretekkel; a program ismerje fel a játék végét, írja ki a győztest (vagy a döntetlent), majd **automatikusan kezdjen új játékot**.
- **Dokumentáció:** a feladat elemzése, a program szerkezete UML osztálydiagrammal, valamint az esemény – eseménykezelő párosítások és a tevékenység rövid leírása.

### Gyakori építőelemek

A lenti részletek nem kész megoldások, hanem minták. A `Board` a játék modellje (a szabályok), ezt mindenki a saját feladatához írja meg.

**Tábla gombokból.** `GridLayout` + ciklusban létrehozott gombok; mindegyik gomb eseménykezelője tudja a saját sorát és oszlopát.

```java
public class BoardGUI {

    private final Board board;          // a modell: itt vannak a játékszabályok
    private final JButton[][] buttons;
    private final JPanel boardPanel;

    public BoardGUI(int rows, int cols) {
        board = new Board(rows, cols);
        buttons = new JButton[rows][cols];
        boardPanel = new JPanel(new GridLayout(rows, cols));
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(60, 60));
                // A lambdában csak effektíve final változó használható,
                // ezért másolatot készítünk a ciklusváltozókról.
                final int r = row;
                final int c = col;
                button.addActionListener(e -> fieldClicked(r, c));
                buttons[row][col] = button;
                boardPanel.add(button);
            }
        }
        refresh();
    }

    private void fieldClicked(int row, int col) {
        // A GUI nem dönti el, hogy szabályos-e a lépés: megkérdezi a modellt.
        if (board.step(row, col)) {
            refresh();
            checkGameOver();
        }
    }

    /** A tábla állapotát a modell alapján újrarajzolja. */
    private void refresh() {
        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                buttons[row][col].setText(board.get(row, col).toString());
            }
        }
    }

    public JPanel getBoardPanel() {
        return boardPanel;
    }
}
```

**Új játék adott mérettel, igazodó ablak.** A menüpontokat a méretek listájából hozzuk létre; új játéknál a régi táblapanelt kicseréljük, és a `pack()` az új táblához igazítja az ablakot.

```java
private static final int[][] SIZES = {{8, 5}, {10, 6}, {12, 7}};   // oszlop × sor

JMenu newGameMenu = new JMenu("New game");
for (int[] size : SIZES) {
    int cols = size[0];
    int rows = size[1];
    JMenuItem item = new JMenuItem(cols + " x " + rows);
    item.addActionListener(e -> newGame(rows, cols));
    newGameMenu.add(item);
}

private void newGame(int rows, int cols) {
    if (boardGUI != null) {
        frame.getContentPane().remove(boardGUI.getBoardPanel());
    }
    boardGUI = new BoardGUI(rows, cols);
    frame.getContentPane().add(boardGUI.getBoardPanel(), BorderLayout.CENTER);
    frame.pack();   // az ablak az új tábla méretéhez igazodik
}
```

**Állapotsor.** Ki következik, hány lépés van még hátra, mennyi a pontszám – egy `JLabel` a `SOUTH` (vagy `NORTH`) régióban, amit minden lépés után frissítünk.

```java
statusLabel.setText("Next player: " + board.getCurrentPlayer()
        + "   Steps left: " + board.getStepsLeft());
```

**Játék vége, automatikus új játék.** A `showMessageDialog` modális: addig nem tér vissza, amíg a felhasználó le nem okézza – utána kezdhetjük az új játékot.

```java
private void checkGameOver() {
    if (!board.isGameOver()) {
        return;
    }
    Player winner = board.getWinner();          // null, ha döntetlen
    String message = winner == null ? "Draw!" : winner + " won!";
    JOptionPane.showMessageDialog(boardPanel, message, "Game over", JOptionPane.INFORMATION_MESSAGE);
    board.reset();      // új játék ugyanakkora táblán
    refresh();
}
```

**Kijelölés, majd lépés (két kattintás).** Sok feladatban előbb ki kell választani egy saját bábut, utána a célmezőt (vagy az irányt). A kijelölt mezőt egy mezőben tároljuk.

```java
private record Position(int row, int col) { }

private Position selected = null;   // a kijelölt bábu, vagy null, ha nincs kijelölés

private void fieldClicked(int row, int col) {
    if (selected == null) {
        if (board.isOwnPiece(row, col)) {
            selected = new Position(row, col);
            buttons[row][col].setBackground(Color.YELLOW);   // kiemelés
        }
    } else {
        board.move(selected.row(), selected.col(), row, col); // a modell ellenőrzi a szabályokat
        selected = null;
        refresh();          // a kiemelést is eltünteti
        checkGameOver();
    }
}
```

**Színezés.** A mezők színét a `setBackground(...)` állítja. Ha a gomb háttérszíne nem látszik (egyes kinézeteknél, *look and feel*), adjuk hozzá: `button.setOpaque(true); button.setBorderPainted(false);`.

### Melyik feladatban mire figyeljünk?

| #  | Feladat          | Felület                                         | Mire figyeljünk                                                                 |
| -- | ---------------- | ----------------------------------------------- | ------------------------------------------------------------------------------- |
| 1  | Potyogós amőba   | `n × m` gombtábla (oszlop × sor!)               | a kattintásból elég az oszlop; a "leesés" a modellben; győzelem csak **vízszintesen és átlósan** |
| 2  | Kitolás          | `n × n` gombtábla                               | véletlen kezdőállás (`Random`); kijelölés + irány; láncolt eltolás, lelökés; `5n` kör |
| 3  | Vadászat         | `n × n` gombtábla                               | kijelölés + szomszédos célmező; lépésszámláló kijelzése (`4n`); bekerítés vizsgálata |
| 4  | Kiszúrós amőba   | `n × n` gombtábla                               | ki következik kijelzése; 3-as / 4-es sornál véletlen törlés; 5-ös sor nyer       |
| 5  | Fekete lyuk      | `n × n` gombtábla                               | kijelölés + irány; a hajó addig csúszik, amíg akadályba nem ütközik; lyukba jutott hajók számlálása |
| 6  | 4-es játék       | `n × n` gombtábla, számokkal                    | a mező és 4 szomszédja nő; pontok kijelzése; a mező színe azé, aki 4-esre állította |
| 7  | Áttörés          | `n × n` gombtábla                               | kijelölés + célmező; előre egyenesen / átlósan lép, csak átlósan üt              |
| 8  | Lovagi torna     | `n × n` gombtábla, színezett mezők              | L alakú lépés ellenőrzése; a mező a rálépő színét veszi fel; 4 egyforma szín egymás mellett |
| 9  | Rubik tábla      | `n × n` színes tábla + toló gombok a széleken   | sor / oszlop ciklikus eltolása; lépésszámláló; csíkok vizsgálata                 |
| 10 | Rubik óra        | 3 × 3 óra, köztük 4 gomb                        | tipp: `GridLayout(5, 5)` – órák a páros, gombok a páratlan pozíciókon; növelés modulo 12 |

Javasolt felépítés (MV, ld. a *Swing alapok* 6. pontját):

- **Modell** (`Board`, `Field`, `Player`, ...): a tábla állapota és a szabályok – nem használ Swinget, így könnyen tesztelhető (ld. a 4. heti JUnit teszteket).
- **Nézet** (`...GUI`, `BoardGUI`): a felület felépítése, az események továbbítása a modellnek, és a tábla újrarajzolása a modell alapján.

A következő hetek anyaga közvetlenül ehhez kapcsolódik:

- **7. hét** – `Flashcards`, `FlashcardsMV`: a modell és a nézet szétválasztása.
- **8. hét** – `Amoba`, `Colorclicker`: táblás játékok gombokból, új játék adott mérettel – közvetlen minta a beadandóhoz.
- **10. hét** – `UFOs`: elemi grafika (`paintComponent`) és `Timer`, ha a táblát rajzolni szeretnénk.

## Futtatás

A projektekhez **JDK 17 vagy újabb** szükséges.

- **NetBeans:** nyissuk meg a projektet, majd **F6** (Run Project).
- **Parancssorból** (a projekt mappájából):

  ```
  javac -d build/classes src/calc/*.java
  java -cp build/classes calc.Calc
  ```

  A `qwerty` ugyanígy: `src/qwerty/*.java`, majd `qwerty.Qwerty`.

- **`calcMVC`:** Maven projekt, a futtatását és a tesztelését a saját [README](calcMVC/README.md#futtatás-és-tesztelés)-je írja le.

## Tippek

- Mielőtt kódot írunk, rajzoljuk le a felületet, és jelöljük be rajta a paneleket és az elrendezéseket.
- Az ismétlődő komponenseket (gombok, mezők) mindig ciklusban hozzuk létre, és tároljuk el őket (tömb / lista), ha később módosítani kell őket.
- Egy eseménykezelő csak "továbbítson": olvassa be az adatot, hívja meg a modellt, frissítse a felületet. Ha egy eseménykezelő hosszú, az a logika valószínűleg a modellbe való.
- A felület állapotát mindig a modellből rajzoljuk újra (`refresh()`), ne próbáljuk meg gombonként "kézzel" szinkronban tartani.
- A Swing dokumentációja és példái: [Oracle – Creating a GUI With Swing](https://docs.oracle.com/javase/tutorial/uiswing/), azon belül különösen a *Laying Out Components Within a Container* és a *Writing Event Listeners* fejezet.
