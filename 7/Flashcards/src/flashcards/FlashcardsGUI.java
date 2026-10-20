package flashcards;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A Flashcards program felülete – KIINDULÓ változat: minden egy osztályban.
 *
 * Ez az osztály egyszerre
 * - tárolja a program állapotát (a kártyákat, a megválaszolt kártyák számát,
 *   a pontszámot, és hogy kérdést vagy választ mutatunk),
 * - tartalmazza a szabályokat (mikor lépünk tovább, mikor jár pont, mikor ér
 *   véget a gyakorlás, milyen a fájl szerkezete),
 * - és felépíti, frissíti a felületet.
 *
 * Működik, de nehezen karbantartható:
 * - a szabályok csak az ablakon keresztül, kattintgatva próbálhatók ki, JUnit
 *   teszt nem írható rájuk, mert a Swing komponensektől nem választhatók el;
 * - minden eseménykezelőnek tudnia kell, mely komponenseket kell frissítenie
 *   (kijelző, állapotsor, gombok) – könnyű egyet kifelejteni;
 * - a fájl beolvasása és a felület kódja összekeveredik.
 *
 * A FlashcardsMV projektben ugyanez a program a modell–nézet (MV) szerkezet
 * szerint készült el: az állapot és a szabályok a FlashcardsModel osztályba
 * kerültek, a nézet pedig egyetlen refresh() metódussal rajzolja újra magát.
 * Érdemes a két FlashcardsGUI-t egymás mellé téve összehasonlítani: a
 * felület ugyanaz, csak a kód szervezése más.
 *
 * Az ablak felépítése (komponensfa):
 * <pre>
 * JFrame
 * ├── JMenuBar: File menü (Open..., Exit)
 * └── content pane (BorderLayout)
 *     ├── NORTH:  statusLabel – hányadik kártyánál járunk, pontszám
 *     ├── CENTER: displayPane (JScrollPane) – a kártya szövege (JTextArea),
 *     │           a keret címe mutatja, hogy kérdést vagy választ látunk
 *     └── SOUTH:  buttonPanel (FlowLayout) – Reset, Toggle Q/A, Wrong answer, Good answer
 * </pre>
 *
 * @author pinter
 */
public class FlashcardsGUI {

    private static final int DISPLAY_ROWS = 6;
    private static final int DISPLAY_COLUMNS = 40;
    private static final float DISPLAY_FONT_SIZE = 16f;
    private static final int GAP = 5;
    /** A fájlban a kérdést és a választ elválasztó karakter. */
    private static final String SEPARATOR = "~";

    // ----- Az állapot. A FlashcardsMV-ben ez a modellbe (FlashcardsModel) kerül. -----

    /** A betöltött kártyák; üres, amíg nem nyitottunk meg fájlt. */
    private List<Card> cards = new ArrayList<>();
    /** Az eddig megválaszolt kártyák száma; egyben az aktuális kártya indexe. */
    private int answered;
    /** Igaz, ha az aktuális kártya kérdését mutatjuk, hamis, ha a válaszát. */
    private boolean showingQuestion = true;
    /** A jó válaszok száma. */
    private int score;

    // ----- A felület -----

    private final JFrame frame;
    private final JTextArea display;
    private final JScrollPane displayPane;
    private final TitledBorder displayBorder;
    private final JLabel statusLabel;
    private final JButton resetButton;
    private final JButton toggleButton;
    private final JButton wrongButton;
    private final JButton goodButton;
    private final JFileChooser fileChooser;

    /**
     * Felépíti és megjeleníti az ablakot.
     */
    public FlashcardsGUI() {
        frame = new JFrame("Flashcards");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        display = new JTextArea(DISPLAY_ROWS, DISPLAY_COLUMNS);
        display.setEditable(false);
        display.setLineWrap(true);
        display.setWrapStyleWord(true);
        display.setFont(display.getFont().deriveFont(DISPLAY_FONT_SIZE));
        displayBorder = BorderFactory.createTitledBorder("");
        displayPane = new JScrollPane(display);
        displayPane.setBorder(displayBorder);

        statusLabel = new JLabel();

        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> reset());
        toggleButton = new JButton("Toggle Q/A");
        toggleButton.setToolTipText("Show the answer / the question");
        toggleButton.addActionListener(e -> toggle());
        wrongButton = new JButton("Wrong answer");
        wrongButton.addActionListener(new AnswerListener(false));
        goodButton = new JButton("Good answer");
        goodButton.addActionListener(new AnswerListener(true));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(resetButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(wrongButton);
        buttonPanel.add(goodButton);

        fileChooser = new JFileChooser(".");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));

        JPanel content = new JPanel(new BorderLayout(GAP, GAP));
        content.setBorder(BorderFactory.createEmptyBorder(GAP * 2, GAP * 2, GAP * 2, GAP * 2));
        content.add(statusLabel, BorderLayout.NORTH);
        content.add(displayPane, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);
        frame.setContentPane(content);

        frame.setJMenuBar(createMenuBar());
        updateDisplay();
        updateStatus();
        updateButtons();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(this::open);
        fileMenu.add(openItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        return menuBar;
    }

    // ----- Eseménykezelők: itt keveredik a szabály és a felület frissítése -----

    /**
     * Fájlválasztó ablakban kiválasztott fájlból betölti a kártyákat.
     */
    private void open(ActionEvent e) {
        if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path file = fileChooser.getSelectedFile().toPath();
        try {
            cards = readCards(file);
            reset();
        } catch (IOException ex) {
            showError("Could not load the cards:\n" + ex.getMessage());
        }
    }

    /**
     * Elölről kezdi a gyakorlást, és frissíti az egész felületet.
     */
    private void reset() {
        answered = 0;
        score = 0;
        showingQuestion = true;
        updateDisplay();
        updateStatus();
        updateButtons();
    }

    /**
     * Átvált a kérdés és a válasz között. Csak a kijelzőt kell frissíteni.
     */
    private void toggle() {
        showingQuestion = !showingQuestion;
        updateDisplay();
    }

    /**
     * Megválaszolja az aktuális kártyát, és továbblép. Itt a szabály ("ha van
     * még kártya", "jó válaszért pont jár") és a felület frissítése egy
     * metódusban van.
     */
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

    // ----- A felület frissítése, részenként -----

    private void updateDisplay() {
        if (answered < cards.size()) {
            Card card = cards.get(answered);
            display.setText(showingQuestion ? card.question() : card.answer());
            displayBorder.setTitle(showingQuestion ? "Question" : "Answer");
        } else if (!cards.isEmpty()) {
            display.setText("The end!\n\nYou knew " + score + " of " + cards.size()
                    + " cards.\nPress Reset to start over, or open another file.");
            displayBorder.setTitle("Finished");
        } else {
            display.setText("Open a cards file: File > Open... (Ctrl+O)\n\n"
                    + "Every line of the file is a card: question" + SEPARATOR + "answer");
            displayBorder.setTitle("Flashcards");
        }
        displayPane.repaint();
    }

    private void updateStatus() {
        if (answered < cards.size()) {
            statusLabel.setText("Card " + (answered + 1) + " / " + cards.size()
                    + "      Score: " + score + " / " + answered);
        } else if (!cards.isEmpty()) {
            statusLabel.setText("Finished      Score: " + score + " / " + cards.size());
        } else {
            statusLabel.setText("No cards loaded");
        }
    }

    private void updateButtons() {
        boolean hasCurrentCard = answered < cards.size();
        resetButton.setEnabled(!cards.isEmpty());
        toggleButton.setEnabled(hasCurrentCard);
        wrongButton.setEnabled(hasCurrentCard);
        goodButton.setEnabled(hasCurrentCard);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ----- Fájlkezelés: a GUI-ban, pedig semmi köze a felülethez -----

    /**
     * Beolvassa a kártyákat egy szövegfájlból. A fájl minden sora egy kártya:
     * {@code kérdés~válasz}. Az üres sorokat átugorjuk.
     *
     * @throws IOException ha a fájl nem olvasható, egy sora nem
     * {@code kérdés~válasz} alakú, vagy a fájl egyetlen kártyát sem tartalmaz
     */
    private static List<Card> readCards(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<Card> result = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(SEPARATOR);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new IOException("Line " + (i + 1) + " is not in the form question"
                        + SEPARATOR + "answer: " + line);
            }
            result.add(new Card(parts[0].trim(), parts[1].trim()));
        }
        if (result.isEmpty()) {
            throw new IOException("The file does not contain any cards");
        }
        return result;
    }

    /**
     * A két válaszgomb eseménykezelője. Névvel rendelkező belső osztály, mert
     * paramétere van (jó vagy rossz válasz), és két gomb is használja.
     */
    class AnswerListener implements ActionListener {

        private final boolean correct;

        public AnswerListener(boolean correct) {
            this.correct = correct;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            answer(correct);
        }

    }

}
