package calc.view;

import calc.model.Operation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

/**
 * A számológép NÉZETE (az MVC "V" betűje): felépíti a felületet, és
 * megjeleníti, amit a vezérlő kér tőle.
 *
 * A nézet nem ismeri a modellt és a vezérlőt. Csak szövegeket jelenít meg
 * (a számok formázása a vezérlő dolga), és az eseményeket továbbadja annak,
 * aki feliratkozott rájuk az onXxx metódusokkal. A gombokat, menüpontokat
 * nem adjuk ki, így a vezérlő nem függ attól, hogy egy funkció gomb,
 * menüpont vagy billentyűparancs a felületen – ezt a nézet szabadon
 * megváltoztathatja. A modellből egyedül az Operation felsorolást használja,
 * a műveleti gombok feliratához.
 *
 * A metódusok három csoportba tartoznak:
 * <ul>
 * <li>a felület felépítése (konstruktor, createXxx): egyszer fut le,
 * induláskor;</li>
 * <li>események (onXxx): a vezérlő ezekkel iratkozik fel a felhasználói
 * eseményekre;</li>
 * <li>megjelenítés (showXxx, setXxx, getXxx): a vezérlő ezekkel írja és
 * olvassa a felületet.</li>
 * </ul>
 *
 * Az ablak felépítése (komponensfa):
 * <pre>
 * JFrame
 * ├── JMenuBar: Calc, Edit, Help menü
 * └── content pane (BorderLayout)
 *     ├── WEST:   calculatorPanel (BorderLayout, a NORTH régióban, hogy ne nyúljon meg)
 *     │   ├── címkék és mezők (A, B, Result)
 *     │   ├── műveleti gombok (GridLayout, 1 sor)
 *     │   └── memóriagombok (GridLayout, 1 sor)
 *     ├── CENTER: az előzmények listája görgetősávval (ez nő az ablakkal)
 *     └── SOUTH:  állapotsor (üzenet balra, memória jobbra)
 * </pre>
 *
 * @author bli
 */
public class CalculatorView {

    /**
     * Az operandusmezők azonosítói. Ezzel mondja meg a vezérlő, melyik
     * mezőt olvassa, írja vagy jelölje hibásnak – a JTextField objektumokat
     * nem kell kiadnunk.
     */
    public enum Operand {
        A, B
    }

    /** A komponensek közötti távolság pixelben. */
    private static final int GAP = 5;
    /** A szövegmezők szélessége oszlopokban (nagyjából karakterszám). */
    private static final int FIELD_WIDTH = 18;
    /** Az előzménylista kívánt szélessége; a magasságát az ablak adja. */
    private static final Dimension HISTORY_SIZE = new Dimension(220, 0);
    /** A hibás mező háttérszíne. */
    private static final Color ERROR_BACKGROUND = new Color(255, 225, 225);
    /** A hibaüzenet betűszíne. */
    private static final Color ERROR_FOREGROUND = new Color(180, 0, 0);

    /** Az ablak. */
    private final JFrame frame;
    /** Az A és a B operandus szövegmezője. */
    private final Map<Operand, JTextField> operandFields = new EnumMap<>(Operand.class);
    /** Az eredmény mezője, csak olvasható. */
    private final JTextField resultField = new JTextField(FIELD_WIDTH);
    /** Műveletenként egy gomb, hogy az onOperation fel tudja iratkoztatni őket. */
    private final Map<Operation, JButton> operationButtons = new EnumMap<>(Operation.class);

    // A memória gombjai.
    private final JButton memoryClearButton = new JButton("MC");
    private final JButton memoryRecallButton = new JButton("MR");
    private final JButton memoryAddButton = new JButton("M+");
    private final JButton memorySubtractButton = new JButton("M−");

    // A JList adatait egy saját modell (ListModel) tárolja – a Swing maga is
    // a modell–nézet szétválasztást követi. A lista ezt a modellt jeleníti meg.
    private final DefaultListModel<String> historyListModel = new DefaultListModel<>();
    private final JList<String> historyList = new JList<>(historyListModel);

    /** Az állapotsor bal oldala: tájékoztató vagy hibaüzenet. */
    private final JLabel statusLabel = new JLabel(" ");
    /** Az állapotsor jobb oldala: a memória tartalma. */
    private final JLabel memoryLabel = new JLabel();

    // A menüpontok, amelyekre a vezérlő feliratkozik.
    private final JMenuItem exitItem = new JMenuItem("Exit");
    private final JMenuItem copyResultItem = new JMenuItem("Copy result");
    private final JMenuItem useResultItem = new JMenuItem("Use result as A");
    private final JMenuItem swapItem = new JMenuItem("Swap A and B");
    private final JMenuItem clearItem = new JMenuItem("Clear");
    private final JMenuItem clearHistoryItem = new JMenuItem("Clear history");

    // Az eredeti színek, hogy a hibajelzés után vissza tudjuk állítani őket.
    private final Color normalFieldBackground;
    private final Color normalStatusForeground;

    /** Az utoljára fókuszt kapott operandusmező: az MR ide írja az értéket. */
    private Operand activeOperand = Operand.A;

    // ==================================================================
    // A felület felépítése: induláskor, egyszer fut le.
    // ==================================================================

    /**
     * Létrehozza az ablakot az összes komponensével, de még nem jeleníti
     * meg.
     *
     * Felület: elkészül az ablak, benne a mezők, a gombok, az előzmények
     * listája, az állapotsor és a menüsor (ld. az osztály leírásában a
     * komponensfát). Az egyes részeket a createXxx metódusok építik fel.
     *
     * MVC: a Calc hozza létre. Ezután a vezérlő feliratkozik az eseményekre,
     * és csak a végén hívjuk a {@link #show()} metódust.
     */
    public CalculatorView() {
        // Az ablakot is kívülről befelé gondoljuk végig, de belülről kifelé
        // rakjuk össze: előbb elkészülnek a részek (mezők, panelek), és a
        // végén egymásba tesszük őket. Az egész ablak a végén így néz ki:
        //
        //    ┌ Calculator ──────────────────────────────────────────┐
        //    │ Calc  Edit  Help                                     │ menüsor
        //    ├──────────────────────────────────────────────────────┤
        //    │ A:      [__________]            ┌ History ─────────┐ │
        //    │ B:      [__________]            │ 6 × 7 = 42       │ │
        //    │ Result: [__________]            │ 2 + 3 = 5        │ │
        //    │ [+][−][×][÷][^][mod]            │                  │ │
        //    │ [MC][MR][M+][M−]                │                  │ │
        //    │                                 └──────────────────┘ │
        //    │ 6 × 7 = 42                                    M = 12 │ állapotsor
        //    └──────────────────────────────────────────────────────┘

        // 1. lépés: az ablak. A JFrame maga még üres: egy keret címsorral.
        //
        //    ┌ Calculator ──────────── _ □ X ┐
        //    │                               │
        //    └───────────────────────────────┘
        //
        // A jobb felső X-re magától nem záródik be (DO_NOTHING_ON_CLOSE): a
        // bezárást a vezérlő kezeli az onExit-en keresztül.
        frame = new JFrame("Calculator");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // 2. lépés: a szövegmezők. Ezek még nincsenek rajta semmilyen
        // panelen, csak létrejönnek; a helyükre a createCalculatorPanel teszi
        // őket.
        //
        //    A: [__________]   B: [__________]   Result: [__________]
        //       szerkeszthető     szerkeszthető          csak olvasható (szürke)
        //
        // A JTextField(FIELD_WIDTH) paramétere a mező szélessége "oszlopokban"
        // (nagyjából ennyi karakter fér el benne látható módon).
        for (Operand operand : Operand.values()) {
            JTextField field = new JTextField(FIELD_WIDTH);
            // A súgó (tooltip) akkor jelenik meg, ha az egér a mező fölött áll.
            field.setToolTipText("Operand " + operand + " – press Enter to repeat the last operation");
            // Megjegyezzük, melyik mezőben járt utoljára a felhasználó (az MR
            // ide írja a memóriát). Ez tisztán felületi állapot, ezért a
            // nézetben van, nem a modellben.
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    activeOperand = operand;
                }
            });
            operandFields.put(operand, field);
        }
        resultField.setEditable(false);
        // Az eredeti színek megjegyzése. Hibánál a showError átszínezi a
        // hibás mezőt és az állapotsort, a clearError pedig visszaállítja
        // őket – ehhez tudnia kell, mi volt az eredeti szín:
        //
        //    induláskor:     B: [ 5   ]   alapszín (pl. fehér)
        //    hibánál:        B: [ abc ]   halványpiros    <- showError
        //    javítás után:   B: [ 5   ]   újra alapszín   <- clearError
        //
        // Az alapszínt nem írhatjuk be fixen (pl. Color.WHITE), mert azt a
        // kinézet (look and feel) határozza meg: Windowson fehér, de más
        // rendszeren vagy sötét témánál más lehet. Ezért még az első
        // átszínezés előtt megkérdezzük a komponensektől, és eltesszük.
        // Elég az A mezőt megkérdezni, a B mező alapszíne ugyanaz.
        //
        // (A setBackground(null) nem lenne jó visszaállításnak: akkor a mező
        // a szülő panel színét venné át, ami szürke, nem a mező fehérje.)
        normalFieldBackground = operandFields.get(Operand.A).getBackground();
        normalStatusForeground = statusLabel.getForeground();

        // 3. lépés: a tartalompanel, ami az ablak teljes belsejét kitölti.
        // BorderLayout, a régiók között GAP * 2 pixel hellyel, és körben egy
        // üres keret (EmptyBorder), hogy a komponensek ne érjenek az ablak
        // széléig.
        //
        //    content (BorderLayout)
        //    ┌───────────────────────────────────────┐
        //    │ ░░░░░░░░░░ üres keret (margó) ░░░░░░░ │
        //    │ ░ ┌──────┬─────────────────────┐    ░ │
        //    │ ░ │ WEST │       CENTER        │    ░ │
        //    │ ░ ├──────┴─────────────────────┤    ░ │
        //    │ ░ │           SOUTH            │    ░ │
        //    │ ░ └────────────────────────────┘    ░ │
        //    └───────────────────────────────────────┘
        //
        // Az EmptyBorder paraméterei: felül, balra, alul, jobbra (pixelben).
        JPanel content = new JPanel(new BorderLayout(GAP * 2, GAP * 2));
        content.setBorder(BorderFactory.createEmptyBorder(GAP * 2, GAP * 2, GAP, GAP * 2));

        // 4. lépés: a számológép (createCalculatorPanel) egy "csomagoló"
        // panel tetejére kerül.
        //
        //    calculatorWrapper (BorderLayout)
        //    ┌────────────────────┐
        //    │ NORTH:             │
        //    │  A:      [_______] │
        //    │  B:      [_______] │  a számológép csak annyi magas,
        //    │  Result: [_______] │  amennyi kell neki
        //    │  [+][−][×][÷][^]...│
        //    ├────────────────────┤
        //    │                    │  üres: ha az ablakot magasabbra
        //    │                    │  húzzuk, ez a rész nő
        //    └────────────────────┘
        //
        // Ha közvetlenül a WEST régióba tennénk, a régió teljes magasságát
        // kitöltené, és nagyobb ablaknál a mezők és a gombok megnyúlnának.
        JPanel calculatorWrapper = new JPanel(new BorderLayout());
        calculatorWrapper.add(createCalculatorPanel(), BorderLayout.NORTH);

        // 5. lépés: a három fő rész a tartalompanel régióiba kerül.
        //
        //    content
        //    ┌──────────────────────┬──────────────────────┐
        //    │ WEST:                │ CENTER:              │
        //    │ calculatorWrapper    │ előzménylista        │
        //    │ (a számológép)       │ (ez nő az ablakkal)  │
        //    ├──────────────────────┴──────────────────────┤
        //    │ SOUTH: állapotsor                           │
        //    └─────────────────────────────────────────────┘
        //
        // A CENTER megkapja a maradék helyet, ezért ha az ablakot nagyobbra
        // húzzuk, az előzménylista nő, a számológép mérete marad.
        content.add(calculatorWrapper, BorderLayout.WEST);
        content.add(createHistoryPanel(), BorderLayout.CENTER);
        content.add(createStatusBar(), BorderLayout.SOUTH);
        frame.setContentPane(content);

        // 6. lépés: a menüsor. Nem a tartalompanel része, hanem az ablak
        // címsora alá, külön helyre kerül (setJMenuBar).
        //
        //    ┌ Calculator ─────────────────────┐
        //    │ Calc  Edit  Help                │  <- JMenuBar
        //    ├─────────────────────────────────┤
        //    │ content (3-5. lépés)            │
        //    └─────────────────────────────────┘
        frame.setJMenuBar(createMenuBar());

        // Az ablak még nem látszik: a méretét és a megjelenítését a show()
        // intézi, miután a vezérlő mindenre feliratkozott.
    }

    /**
     * Létrehozza az ablak bal oldalát: magát a számológépet.
     *
     * Felület: felül az "A:", "B:" és "Result:" címke a szövegmezőkkel,
     * alattuk egy sorban a műveleti gombok (+ − × ÷ ^ mod), alattuk egy
     * sorban a memóriagombok (MC MR M+ M−). A műveleti gombokat az
     * Operation felsorolásból hozzuk létre, így minden művelethez magától
     * lesz gomb.
     *
     * MVC: nézet, csak a konstruktor hívja. A gombokhoz itt még nem tartozik
     * eseménykezelő – azt a vezérlő adja meg később (onOperation, onMemoryXxx).
     *
     * @return a kész panel
     */
    private JPanel createCalculatorPanel() {
        // A panelt kisebb panelekből rakjuk össze, belülről kifelé haladva.
        // Minden panel egy egyszerű elrendezést kap; a bonyolultabb
        // elrendezés az egymásba ágyazásból jön ki.

        // 1. lépés: két egyoszlopos panel, egy a címkéknek, egy a mezőknek.
        //
        //    labelPanel        fieldPanel
        //    ┌─────────┐       ┌──────────────────┐
        //    │ A:      │       │ [______________] │
        //    │ B:      │       │ [______________] │
        //    │ Result: │       │ [______________] │
        //    └─────────┘       └──────────────────┘
        //
        // A GridLayout(0, 1, GAP, GAP) jelentése: 1 oszlop, a sorok száma 0,
        // azaz "annyi sor, ahány komponenst beleteszünk"; a cellák között
        // vízszintesen és függőlegesen is GAP pixel a távolság. A GridLayout
        // minden cellát egyforma magasra vesz, ezért a két panel sorai
        // pontosan egymás mellé kerülnek: az "A:" címke az A mező mellé.
        //
        // Miért nem egyetlen kétoszlopos GridLayout(0, 2)? Mert abban a
        // két oszlop egyforma széles lenne: a rövid címkék ugyanakkora helyet
        // kapnának, mint a mezők. Így viszont a címkék oszlopa csak olyan
        // széles, amilyen a leghosszabb címke ("Result:").
        JPanel labelPanel = new JPanel(new GridLayout(0, 1, GAP, GAP));
        JPanel fieldPanel = new JPanel(new GridLayout(0, 1, GAP, GAP));
        // Az addLabeledField soronként feltölti a két panelt: a címkét a
        // labelPanel-be, a mezőt a fieldPanel-be teszi, ugyanabba a sorba.
        // A KeyEvent.VK_A a gyorsbillentyű: Alt+A az A mezőre ugrik, és a
        // címkében aláhúzva látszik az "A" betű.
        addLabeledField(labelPanel, fieldPanel, "A:", KeyEvent.VK_A, operandFields.get(Operand.A));
        addLabeledField(labelPanel, fieldPanel, "B:", KeyEvent.VK_B, operandFields.get(Operand.B));
        addLabeledField(labelPanel, fieldPanel, "Result:", KeyEvent.VK_R, resultField);

        // 2. lépés: a két oszlopot egymás mellé tesszük egy BorderLayout-tal.
        //
        //    numPanel (BorderLayout)
        //    ┌──────────┬──────────────────┐
        //    │   WEST   │      CENTER      │
        //    │labelPanel│    fieldPanel    │
        //    └──────────┴──────────────────┘
        //
        // A WEST régió csak annyi széles, amennyi a címkéknek kell; a CENTER
        // megkapja a maradék helyet – így a mezők a lehető legszélesebbek.
        JPanel numPanel = new JPanel(new BorderLayout(GAP, GAP));
        numPanel.add(labelPanel, BorderLayout.WEST);
        numPanel.add(fieldPanel, BorderLayout.CENTER);

        // 3. lépés: a műveleti gombok egy sorban.
        //
        //    operationPanel: [ + ][ − ][ × ][ ÷ ][ ^ ][mod]
        //
        // GridLayout(1, 0): 1 sor, annyi oszlop, ahány gomb – minden gomb
        // egyforma széles. A gombokat a műveletek listájából hozzuk létre,
        // és eltesszük őket az operationButtons-ba, hogy az onOperation
        // később eseménykezelőt rendelhessen hozzájuk.
        JPanel operationPanel = new JPanel(new GridLayout(1, 0, GAP, GAP));
        for (Operation operation : Operation.values()) {
            JButton button = new JButton(operation.getSymbol());
            operationButtons.put(operation, button);
            operationPanel.add(button);
        }

        // 4. lépés: a memóriagombok egy sorban, ugyanígy.
        //
        //    memoryPanel: [ MC ][ MR ][ M+ ][ M− ]
        //
        // A setToolTipText szövege akkor jelenik meg, ha az egeret a gomb
        // fölött tartjuk.
        JPanel memoryPanel = new JPanel(new GridLayout(1, 0, GAP, GAP));
        memoryClearButton.setToolTipText("Memory clear");
        memoryRecallButton.setToolTipText("Memory recall – into the last used operand field");
        memoryAddButton.setToolTipText("Add the result to memory");
        memorySubtractButton.setToolTipText("Subtract the result from memory");
        memoryPanel.add(memoryClearButton);
        memoryPanel.add(memoryRecallButton);
        memoryPanel.add(memoryAddButton);
        memoryPanel.add(memorySubtractButton);

        // 5. lépés: a két gombsort egymás alá tesszük.
        //
        //    buttonPanel (GridLayout, 1 oszlop)
        //    ┌───────────────────────────────┐
        //    │ [ + ][ − ][ × ][ ÷ ][ ^ ][mod]│  operationPanel
        //    │ [ MC ][ MR ][ M+ ][ M− ]      │  memoryPanel
        //    └───────────────────────────────┘
        //
        // Egy panel is lehet egy másik panel "komponense": a GridLayout a
        // két gombsort ugyanolyan magasra és szélesre veszi.
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, GAP, GAP));
        buttonPanel.add(operationPanel);
        buttonPanel.add(memoryPanel);

        // 6. lépés: a mezők fölülre, a gombok alulra.
        //
        //    panel (BorderLayout)
        //    ┌───────────────────────────────┐
        //    │ CENTER: numPanel (mezők)      │
        //    ├───────────────────────────────┤
        //    │ SOUTH:  buttonPanel (gombok)  │
        //    └───────────────────────────────┘
        //
        // A BorderLayout(GAP, GAP * 2) második paramétere a függőleges
        // távolság a régiók között: a gombok egy kicsit távolabb kerülnek a
        // mezőktől. A kész panelt a konstruktor az ablak bal oldalára teszi.
        JPanel panel = new JPanel(new BorderLayout(GAP, GAP * 2));
        panel.add(numPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Egy címkét és a hozzá tartozó szövegmezőt tesz a felületre.
     *
     * Felület: a címke a bal oszlopba, a mező a jobb oszlopba kerül. A
     * címkében aláhúzva látszik a gyorsbillentyű betűje: Alt + ez a betű a
     * mezőre ugrik (setLabelFor + setDisplayedMnemonic).
     *
     * MVC: nézet, segédmetódus a createCalculatorPanel-hez.
     */
    private static void addLabeledField(JPanel labelPanel, JPanel fieldPanel,
            String text, int mnemonic, JTextField field) {
        // 1. lépés: a címke. A setDisplayedMnemonic aláhúzza a megadott
        // betűt, a setLabelFor összeköti a címkét a mezővel: Alt + a betű
        // ezért a mezőre ugrik (és a képernyőolvasó is tudja, mi a mező neve).
        //
        //    A:   <- az "A" alá van húzva: Alt+A
        JLabel label = new JLabel(text);
        label.setDisplayedMnemonic(mnemonic);
        label.setLabelFor(field);

        // 2. lépés: a címke a bal, a mező a jobb oszlop következő sorába
        // kerül. Mivel mindkét panel GridLayout, és mindkettőbe ugyanannyi
        // elem kerül, a címke pontosan a mezője mellé esik.
        //
        //    labelPanel   fieldPanel
        //    ┌────────┐   ┌──────────────┐
        //    │ ...    │   │ ...          │  korábbi sorok
        //    │ A:     │   │ [__________] │  <- most hozzáadott sor
        //    └────────┘   └──────────────┘
        labelPanel.add(label);
        fieldPanel.add(field);
    }

    /**
     * Létrehozza az előzmények listáját.
     *
     * Felület: az ablak közepén egy "History" feliratú keret, benne egy lista
     * görgetősávval. Ez a rész nő, ha az ablakot nagyobbra húzzuk. A lista
     * eleinte üres, a sorait a {@link #showHistory(List)} tölti fel.
     *
     * MVC: nézet, csak a konstruktor hívja.
     *
     * @return a görgetősávos lista
     */
    private JComponent createHistoryPanel() {
        // 1. lépés: maga a lista. Egyszerre csak egy sor lehet kijelölve.
        // A sorait nem közvetlenül a JList tárolja, hanem a hozzá tartozó
        // historyListModel – ha azt módosítjuk, a lista magától frissül.
        //
        //    historyList
        //    ┌──────────────────┐
        //    │ 6 × 7 = 42       │  <- kijelölt sor (kiemelt háttérrel)
        //    │ 2 + 3 = 5        │
        //    └──────────────────┘
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setToolTipText("Double-click or press Enter to load a calculation");

        // 2. lépés: a listát egy JScrollPane-be tesszük. A JList magától
        // nem görget: ha több sor van, mint amennyi elfér, a JScrollPane
        // tesz mellé görgetősávot.
        //
        //    scrollPane
        //    ┌──────────────────┬─┐
        //    │ 6 × 7 = 42       │▲│
        //    │ 2 + 3 = 5        │█│  <- görgetősáv, csak ha kell
        //    │ ...              │▼│
        //    └──────────────────┴─┘
        //
        // A kívánt szélesség HISTORY_SIZE (220 pixel); a magasságot (0)
        // a BorderLayout CENTER régiója úgyis felülírja: annyi lesz, amennyi
        // hely az ablakban marad.
        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setPreferredSize(HISTORY_SIZE);

        // 3. lépés: feliratos keret (TitledBorder) a görgethető lista köré.
        //
        //    ┌ History ───────────┐
        //    │ 6 × 7 = 42         │
        //    │ 2 + 3 = 5          │
        //    └────────────────────┘
        scrollPane.setBorder(BorderFactory.createTitledBorder("History"));
        return scrollPane;
    }

    /**
     * Létrehozza az állapotsort.
     *
     * Felület: az ablak alján egy sor; bal oldalon az üzenetek (pl. az
     * utolsó számolás vagy egy hibaüzenet), jobb oldalon a memória ("M = 12").
     * A szövegüket a showStatus, a showError és a showMemory állítja be.
     *
     * MVC: nézet, csak a konstruktor hívja.
     *
     * @return az állapotsor panelje
     */
    private JPanel createStatusBar() {
        // Egy BorderLayout két címkével: az üzenet a CENTER-be kerül (ez
        // kapja a teljes maradék szélességet), a memória az EAST-be (ez
        // csak annyi széles, amennyi a szövegének kell, és mindig a jobb
        // szélen marad).
        //
        //    statusBar (BorderLayout)
        //    ┌──────────────────────────────────────┬────────┐
        //    │ CENTER: statusLabel                  │ EAST:  │
        //    │ 6 × 7 = 42                           │ M = 12 │
        //    └──────────────────────────────────────┴────────┘
        //
        // A statusLabel kezdő szövege egy szóköz (" "), nem üres: egy üres
        // JLabel magassága 0 lenne, és az állapotsor csak az első üzenetnél
        // "ugrana" be, megváltoztatva az ablak elrendezését.
        JPanel statusBar = new JPanel(new BorderLayout(GAP, 0));
        statusBar.add(statusLabel, BorderLayout.CENTER);
        statusBar.add(memoryLabel, BorderLayout.EAST);
        return statusBar;
    }

    /**
     * Létrehozza a menüsort.
     *
     * Felület: az ablak tetején a Calc (Exit), az Edit (Copy result, Use
     * result as A, Swap A and B, Clear, Clear history) és a Help (About)
     * menü, a menüpontok mellett a gyorsbillentyűkkel (pl. Ctrl+L). A
     * gyorsbillentyűk Windowson és Linuxon a Ctrl, macOS-en a Cmd billentyűvel
     * működnek – a Java megmondja, melyik a szokásos az adott rendszeren.
     *
     * MVC: nézet, csak a konstruktor hívja. A menüpontokhoz a vezérlő ad
     * eseménykezelőt (onXxx), kivéve az About-ot: a névjegy csak a felületet
     * érinti, nincs köze a modellhez, ezért azt a nézet maga kezeli.
     *
     * @return a kész menüsor
     */
    private JMenuBar createMenuBar() {
        // A menü három szintből áll:
        //   JMenuBar  – a sáv az ablak tetején
        //   JMenu     – egy lenyíló menü a sávon (Calc, Edit, Help)
        //   JMenuItem – egy menüpont a lenyíló menüben (Exit, Clear, ...)
        //
        // Gyorsbillentyű kétféle van:
        //   setMnemonic    – Alt + betű lenyitja a menüt (a betű alá van húzva)
        //   setAccelerator – a menüpont a menü lenyitása nélkül is elérhető,
        //                    pl. Ctrl+L; a menüpont mellett ki is van írva.
        //
        // Windowson / Linuxon Ctrl, macOS-en Cmd a szokásos gyorsbillentyű.
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // 1. lépés: a Calc menü, benne a kilépés.
        //
        //    Calc
        //    ┌──────────────────┐
        //    │ Exit      Ctrl+Q │
        //    └──────────────────┘
        JMenu calcMenu = new JMenu("Calc");
        calcMenu.setMnemonic(KeyEvent.VK_C);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcut));
        calcMenu.add(exitItem);

        // 2. lépés: az Edit menü. Az addSeparator egy vízszintes vonalat tesz
        // a menüpontok közé: az előzmények törlése külön csoportba kerül,
        // mert az mást töröl, mint a Clear.
        //
        //    Edit
        //    ┌───────────────────────────────────┐
        //    │ Copy result          Ctrl+Shift+C │
        //    │ Use result as A      Ctrl+R       │
        //    │ Swap A and B         Ctrl+Shift+S │
        //    │ Clear                Ctrl+L       │
        //    │ ───────────────────────────────── │  <- addSeparator()
        //    │ Clear history        Ctrl+Shift+L │
        //    └───────────────────────────────────┘
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        copyResultItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                shortcut | InputEvent.SHIFT_DOWN_MASK));
        useResultItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut));
        swapItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                shortcut | InputEvent.SHIFT_DOWN_MASK));
        clearItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut));
        clearHistoryItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                shortcut | InputEvent.SHIFT_DOWN_MASK));
        editMenu.add(copyResultItem);
        editMenu.add(useResultItem);
        editMenu.add(swapItem);
        editMenu.add(clearItem);
        editMenu.addSeparator();
        editMenu.add(clearHistoryItem);

        // 3. lépés: a Help menü. Az About menüpont egy felugró ablakot
        // (JOptionPane) nyit; ezt a nézet maga kezeli, mert nincs köze a
        // modellhez.
        //
        //    Help             ┌ About ─────────────────────────────┐
        //    ┌───────┐        │ (i) Calculator – an MVC example... │
        //    │ About │  --->  │                            [ OK ]  │
        //    └───────┘        └────────────────────────────────────┘
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Calculator – an MVC example in Swing\nProgramming Technology, ELTE",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        // 4. lépés: a három menü a sávra kerül, balról jobbra, a hozzáadás
        // sorrendjében.
        //
        //    menuBar
        //    ┌─────────────────────────────────┐
        //    │ Calc  Edit  Help                │
        //    └─────────────────────────────────┘
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(calcMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    // ==================================================================
    // Események: a vezérlő ezekkel a metódusokkal iratkozik fel.
    //
    // A paraméter mindig "egy darab kód", amit a nézet majd az esemény
    // bekövetkezésekor lefuttat. A típusa azt mondja meg, milyen adatot kap
    // ez a kód:
    //   Runnable            semmit:        () -> ...      vagy this::clear
    //   Consumer<Operation> egy műveletet: op -> ...      vagy this::calculate
    //   IntConsumer         egy int-et:    index -> ...   vagy this::loadFromHistory
    //
    // Ezek a metódusok nem változtatnak a felület kinézetén, csak bekötik a
    // már meglévő gombokat, menüpontokat a vezérlő kódjához. Mindegyiket a
    // vezérlő konstruktora hívja, egyszer.
    // ==================================================================

    /**
     * Feliratkozás a műveleti gombokra (+ − × ÷ ^ mod).
     *
     * Mindegyik gomb ugyanazt a kezelőt kapja, de más paraméterrel: a gomb
     * megnyomásakor a kezelő megkapja, melyik műveletet választották.
     *
     * MVC: a vezérlő hívja: view.onOperation(this::calculate).
     *
     * @param handler a műveleti gombok eseménykezelője
     */
    public void onOperation(Consumer<Operation> handler) {
        operationButtons.forEach((operation, button)
                -> button.addActionListener(e -> handler.accept(operation)));
    }

    /**
     * Feliratkozás az Enter leütésére az A vagy a B mezőben.
     *
     * MVC: a vezérlő hívja; ilyenkor az utoljára használt műveletet ismétli.
     *
     * @param handler fut, ha valamelyik operandusmezőben Entert nyomnak
     */
    public void onEnterInOperand(Runnable handler) {
        for (JTextField field : operandFields.values()) {
            // A JTextField Enter leütésekor ActionEventet küld.
            field.addActionListener(e -> handler.run());
        }
    }

    /**
     * Feliratkozás az Edit → Copy result menüpontra (Ctrl+Shift+C).
     *
     * MVC: a vezérlő hívja; a kezelő a vágólapra másolja az eredményt.
     */
    public void onCopyResult(Runnable handler) {
        bind(copyResultItem, handler);
    }

    /**
     * Feliratkozás az Edit → Use result as A menüpontra (Ctrl+R).
     *
     * MVC: a vezérlő hívja; a kezelő az eredményt átírja az A mezőbe.
     */
    public void onUseResult(Runnable handler) {
        bind(useResultItem, handler);
    }

    /**
     * Feliratkozás az Edit → Swap A and B menüpontra (Ctrl+Shift+S).
     *
     * MVC: a vezérlő hívja; a kezelő felcseréli a két mező tartalmát.
     */
    public void onSwap(Runnable handler) {
        bind(swapItem, handler);
    }

    /**
     * Feliratkozás az Edit → Clear menüpontra (Ctrl+L).
     *
     * MVC: a vezérlő hívja; a kezelő kiüríti a mezőket.
     */
    public void onClear(Runnable handler) {
        bind(clearItem, handler);
    }

    /**
     * Feliratkozás az Edit → Clear history menüpontra (Ctrl+Shift+L).
     *
     * MVC: a vezérlő hívja, és egyenesen a modell metódusát köti be
     * (model::clearHistory). A lista ürítése ezután a modell értesítésén
     * keresztül történik meg.
     */
    public void onClearHistory(Runnable handler) {
        bind(clearHistoryItem, handler);
    }

    /**
     * Feliratkozás az MC gombra (a memória törlése).
     *
     * MVC: a vezérlő hívja, és a modell metódusát köti be (model::clearMemory).
     */
    public void onMemoryClear(Runnable handler) {
        bind(memoryClearButton, handler);
    }

    /**
     * Feliratkozás az MR gombra (a memória visszahívása).
     *
     * MVC: a vezérlő hívja; a kezelő a memória tartalmát az utoljára
     * használt operandusmezőbe írja.
     */
    public void onMemoryRecall(Runnable handler) {
        bind(memoryRecallButton, handler);
    }

    /**
     * Feliratkozás az M+ gombra (az eredmény hozzáadása a memóriához).
     *
     * MVC: a vezérlő hívja.
     */
    public void onMemoryAdd(Runnable handler) {
        bind(memoryAddButton, handler);
    }

    /**
     * Feliratkozás az M− gombra (az eredmény kivonása a memóriából).
     *
     * MVC: a vezérlő hívja.
     */
    public void onMemorySubtract(Runnable handler) {
        bind(memorySubtractButton, handler);
    }

    /**
     * Feliratkozás az előzmények egy sorának betöltésére: dupla kattintás a
     * soron, vagy Enter a kijelölt soron.
     *
     * Itt két különböző felhasználói műveletből lesz egyetlen esemény: a
     * vezérlőnek nem kell tudnia, hogy egérrel vagy billentyűzettel
     * választották-e ki a sort, csak azt kapja meg, hányadikat.
     *
     * MVC: a vezérlő hívja; a kezelő visszatölti a számolást a mezőkbe.
     *
     * @param handler megkapja a kiválasztott sor indexét
     */
    public void onHistorySelected(IntConsumer handler) {
        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = historyList.locationToIndex(e.getPoint());
                // A locationToIndex a lista alatti üres részre is a legközelebbi
                // sort adja, ezért megnézzük, hogy tényleg a sorra kattintottak-e.
                boolean onRow = index >= 0 && historyList.getCellBounds(index, index).contains(e.getPoint());
                if (e.getClickCount() == 2 && onRow) {
                    handler.accept(index);
                }
            }
        });
        historyList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "load");
        historyList.getActionMap().put("load", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = historyList.getSelectedIndex();
                if (index >= 0) {
                    handler.accept(index);
                }
            }
        });
    }

    /**
     * Feliratkozás a kilépésre: a Calc → Exit menüpont (Ctrl+Q), vagy az
     * ablak bezárása a jobb felső X-szel.
     *
     * Itt is két felhasználói műveletből lesz egyetlen esemény.
     *
     * MVC: a vezérlő hívja (view::close). A vezérlő dönti el, mi történjen
     * kilépéskor – később pl. itt lehetne rákérdezni a mentésre.
     *
     * @param handler fut, ha a felhasználó ki akar lépni
     */
    public void onExit(Runnable handler) {
        bind(exitItem, handler);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handler.run();
            }
        });
    }

    /**
     * Egy gombhoz vagy menüponthoz eseménykezelőt rendel. Az ActionEvent
     * paraméterre a vezérlőnek nincs szüksége, ezért egy egyszerű
     * Runnable-t futtatunk.
     *
     * MVC: nézet, segédmetódus az onXxx metódusokhoz.
     */
    private static void bind(AbstractButton button, Runnable handler) {
        button.addActionListener(e -> handler.run());
    }

    // ==================================================================
    // Megjelenítés: a vezérlő ezekkel írja és olvassa a felületet.
    // ==================================================================

    /**
     * Méretezi, középre teszi és megjeleníti az ablakot.
     *
     * Felület: ekkor jelenik meg az ablak a képernyőn. Kisebbre nem lehet
     * húzni, mint amekkorán minden kifér.
     *
     * MVC: a Calc hívja, miután a vezérlő már mindenre feliratkozott.
     */
    public void show() {
        // 1. lépés: pack – az ablak pont akkora lesz, amekkorát a benne lévő
        // komponensek kérnek (a mezők szélessége, a gombok mérete, a lista
        // HISTORY_SIZE-a alapján). Ezért nem kell kézzel méretet megadni.
        frame.pack();

        // 2. lépés: ennél kisebbre ne lehessen összehúzni, különben a mezők
        // és a gombok összenyomódnának. Nagyobbra lehet: ilyenkor az
        // előzménylista nő (ld. a konstruktor 5. lépését).
        //
        //    kisebbre nem:              nagyobbra igen:
        //    ┌──────────────┐           ┌──────────────┬──────────────┐
        //    │ számol. │list│           │ számológép   │ lista        │
        //    └──────────────┘           │              │ (nagyobb)    │
        //                               └──────────────┴──────────────┘
        frame.setMinimumSize(frame.getSize());

        // 3. lépés: a képernyő közepére (a null azt jelenti: nem egy másik
        // ablakhoz, hanem a képernyőhöz képest).
        frame.setLocationRelativeTo(null);

        // 4. lépés: az ablak megjelenik. Ez mindig a legutolsó lépés.
        frame.setVisible(true);
    }

    /**
     * Bezárja az ablakot. Ha nincs több ablak, a program magától leáll.
     *
     * Felület: eltűnik az ablak.
     *
     * MVC: a vezérlő hívja kilépéskor (ld. onExit).
     */
    public void close() {
        frame.dispose();
    }

    /**
     * Kiolvassa egy operandusmező tartalmát.
     *
     * MVC: a vezérlő hívja számolás előtt. A nézet szöveget ad vissza,
     * számmá a vezérlő alakítja.
     *
     * @param operand melyik mező (A vagy B)
     * @return a mezőbe írt szöveg, változtatás nélkül
     */
    public String getOperandText(Operand operand) {
        return operandFields.get(operand).getText();
    }

    /**
     * Beírja a szöveget egy operandusmezőbe (a korábbi tartalmat lecseréli).
     *
     * Felület: megváltozik az A vagy a B mező tartalma.
     *
     * MVC: a vezérlő hívja, pl. törléskor, cserénél, előzmény betöltésekor.
     *
     * @param operand melyik mező (A vagy B)
     * @param text az új tartalom
     */
    public void setOperandText(Operand operand, String text) {
        operandFields.get(operand).setText(text);
    }

    /**
     * Megadja, melyik operandusmezőben járt utoljára a felhasználó.
     *
     * MVC: a vezérlő hívja az MR-nél, hogy tudja, hova írja a memóriát.
     *
     * @return az utoljára fókuszt kapott operandusmező
     */
    public Operand getActiveOperand() {
        return activeOperand;
    }

    /**
     * A billentyűzet fókuszát egy operandusmezőbe teszi.
     *
     * Felület: a mezőben villogni kezd a kurzor, a felhasználó azonnal
     * gépelhet bele.
     *
     * MVC: a vezérlő hívja, pl. törlés után az A mezőre.
     *
     * @param operand melyik mező (A vagy B)
     */
    public void focusOperand(Operand operand) {
        operandFields.get(operand).requestFocusInWindow();
    }

    /**
     * Kiírja az eredményt.
     *
     * Felület: megváltozik a Result mező tartalma (üres szöveggel törlődik).
     *
     * MVC: a vezérlő hívja, a már szöveggé formázott eredménnyel.
     *
     * @param text a kiírandó eredmény
     */
    public void showResult(String text) {
        resultField.setText(text);
    }

    /**
     * Lecseréli az előzmények listájának tartalmát.
     *
     * Felület: a jobb oldali History listában a megadott sorok jelennek meg,
     * ebben a sorrendben. A JList magától újrarajzolja magát, mert a saját
     * modelljét (historyListModel) módosítjuk.
     *
     * MVC: a vezérlő render() metódusa hívja, valahányszor a modell jelzi,
     * hogy megváltozott.
     *
     * @param entries az előzmények sorai, pl. "6 × 7 = 42"
     */
    public void showHistory(List<String> entries) {
        historyListModel.clear();
        historyListModel.addAll(entries);
    }

    /**
     * Kiírja a memória tartalmát.
     *
     * Felület: az állapotsor jobb oldalán megjelenik az "M = ..." felirat,
     * vagy eltűnik, ha a memória üres.
     *
     * MVC: a vezérlő render() metódusa hívja, a modell változásakor.
     *
     * @param value a memória tartalma szövegként, vagy null, ha üres
     */
    public void showMemory(String value) {
        memoryLabel.setText(value == null ? "" : "M = " + value);
    }

    /**
     * Tájékoztató üzenetet ír ki, és eltünteti az esetleges korábbi
     * hibajelzést.
     *
     * Felület: az állapotsor bal oldalán, normál színnel megjelenik az
     * üzenet (pl. "6 × 7 = 42"); a mezők piros háttere visszaáll.
     *
     * MVC: a vezérlő hívja minden sikeres művelet után.
     *
     * @param message az üzenet
     */
    public void showStatus(String message) {
        clearError();
        statusLabel.setText(message);
    }

    /**
     * Hibát jelez. Felugró ablak helyett az állapotsorban jelezzük, így nem
     * kell minden elírás után leokézni.
     *
     * Felület: az állapotsorban piros betűvel megjelenik az üzenet. Ha a
     * hiba egy mezőhöz tartozik, annak a háttere halványpiros lesz, a fókusz
     * oda kerül, és a tartalma kijelölődik, hogy azonnal átírható legyen.
     *
     * MVC: a vezérlő hívja, ha a bemenet hibás vagy a modell kivételt dobott.
     *
     * @param operand a hibás mező, vagy null, ha a hiba nem egy mezőhöz
     * tartozik (pl. nullával osztás)
     * @param message a hibaüzenet
     */
    public void showError(Operand operand, String message) {
        // Így néz ki egy hibás B mező után:
        //
        //    A:      [ 6         ]
        //    B:      [▓abc▓▓▓▓▓▓▓]   <- halványpiros háttér, a szöveg kijelölve,
        //    Result: [           ]      a kurzor itt van: rögtön átírható
        //    ...
        //    Operand B is not a valid number          <- piros betű
        //
        // Előbb eltüntetjük az esetleges korábbi hibajelzést, hogy egyszerre
        // mindig csak egy mező legyen pirosra színezve.
        clearError();
        statusLabel.setForeground(ERROR_FOREGROUND);
        statusLabel.setText(message);
        if (operand != null) {
            JTextField field = operandFields.get(operand);
            field.setBackground(ERROR_BACKGROUND);
            field.requestFocusInWindow();
            field.selectAll();
        }
    }

    /**
     * Eltünteti a hibajelzést.
     *
     * Felület: az állapotsor betűszíne és a mezők háttérszíne visszaáll az
     * eredetire.
     *
     * MVC: nézet, a showStatus és a showError hívja.
     */
    private void clearError() {
        statusLabel.setForeground(normalStatusForeground);
        for (JTextField field : operandFields.values()) {
            field.setBackground(normalFieldBackground);
        }
    }

    /**
     * A szöveget a rendszer vágólapjára másolja (utána Ctrl+V-vel bárhová
     * beilleszthető).
     *
     * MVC: a vezérlő hívja. A vágólap a felülethez (az operációs rendszer
     * grafikus környezetéhez) tartozik, ezért van a nézetben.
     *
     * @param text a másolandó szöveg
     * @return hamis, ha a vágólap éppen nem elérhető (pl. egy másik program
     * zárolja)
     */
    public boolean copyToClipboard(String text) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            return true;
        } catch (IllegalStateException ex) {
            return false;
        }
    }

}
