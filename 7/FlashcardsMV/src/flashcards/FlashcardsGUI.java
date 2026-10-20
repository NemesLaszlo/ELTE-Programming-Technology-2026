package flashcards;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
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
 * A Flashcards program felülete (az MV "V" betűje: a nézet).
 *
 * A nézet felépíti a felületet, az eseménykezelői meghívják a modellt
 * (FlashcardsModel), majd a refresh() a modell állapota alapján frissíti a
 * teljes felületet: a kártya szövegét, az állapotsort és a gombok
 * engedélyezettségét. A nézetben nincs állapot és nincs szabály: nem tudja,
 * hány kártya van, vagy hogy mikor ér véget a gyakorlás, mindezt a modelltől
 * kérdezi meg.
 *
 * Hasonlítsuk össze a Flashcards projekt FlashcardsGUI osztályával, ahol
 * mindez még egy osztályban van!
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

    /** A modell: a kártyák, az aktuális kártya, a pontszám és a szabályok. */
    private final FlashcardsModel model = new FlashcardsModel();

    private final JFrame frame;
    private final JTextArea display;
    private final JScrollPane displayPane;
    private final TitledBorder displayBorder;
    private final JLabel statusLabel;
    private final JButton resetButton;
    private final JButton toggleButton;
    private final JButton wrongButton;
    private final JButton goodButton;
    /**
     * A fájlválasztó ablak. Egyszer hozzuk létre, így megjegyzi, melyik
     * mappában jártunk legutóbb.
     */
    private final JFileChooser fileChooser;

    /**
     * Felépíti és megjeleníti az ablakot.
     */
    public FlashcardsGUI() {
        frame = new JFrame("Flashcards");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        display = new JTextArea(DISPLAY_ROWS, DISPLAY_COLUMNS);
        // A kártya szövegét csak olvasni lehet, és a hosszú sorok szóhatáron
        // törjenek.
        display.setEditable(false);
        display.setLineWrap(true);
        display.setWrapStyleWord(true);
        display.setFont(display.getFont().deriveFont(DISPLAY_FONT_SIZE));
        // A keret címe ("Question" / "Answer") a refresh-ben változik.
        displayBorder = BorderFactory.createTitledBorder("");
        displayPane = new JScrollPane(display);
        displayPane.setBorder(displayBorder);

        statusLabel = new JLabel();

        // Az eseménykezelők csak továbbítanak: meghívják a modellt, majd a
        // refresh-t. Nincs bennük szabály (pl. "ha van még kártya").
        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            model.reset();
            refresh();
        });
        toggleButton = new JButton("Toggle Q/A");
        toggleButton.setToolTipText("Show the answer / the question");
        toggleButton.addActionListener(e -> {
            model.toggle();
            refresh();
        });
        // A két válaszgomb ugyanazt az eseménykezelő osztályt kapja, más
        // paraméterrel (ld. AnswerListener).
        wrongButton = new JButton("Wrong answer");
        wrongButton.addActionListener(new AnswerListener(false));
        goodButton = new JButton("Good answer");
        goodButton.addActionListener(new AnswerListener(true));

        // A JPanel alapértelmezett elrendezése a FlowLayout: a gombok egymás
        // után, balról jobbra kerülnek a panelre.
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(resetButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(wrongButton);
        buttonPanel.add(goodButton);

        // A fájlválasztó a projekt mappájában (az aktuális munkakönyvtárban)
        // indul, ahol a cards.txt is van.
        fileChooser = new JFileChooser(".");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));

        JPanel content = new JPanel(new BorderLayout(GAP, GAP));
        content.setBorder(BorderFactory.createEmptyBorder(GAP * 2, GAP * 2, GAP * 2, GAP * 2));
        content.add(statusLabel, BorderLayout.NORTH);
        content.add(displayPane, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);
        frame.setContentPane(content);

        frame.setJMenuBar(createMenuBar());
        refresh();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        // Metódusreferencia: az open metódus paramétere ugyanaz, mint az
        // actionPerformed metódusé, ezért közvetlenül átadható.
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

    /**
     * Fájlválasztó ablakban kiválasztott fájlból betölti a kártyákat. A
     * beolvasás és az ellenőrzés a modell dolga; a nézet csak a hibát
     * jeleníti meg. Hibás fájl esetén a korábbi kártyák megmaradnak.
     */
    private void open(ActionEvent e) {
        if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path file = fileChooser.getSelectedFile().toPath();
        try {
            model.load(file);
        } catch (IOException ex) {
            showError("Could not load the cards:\n" + ex.getMessage());
        }
        refresh();
    }

    /**
     * A felületet mindig a modell állapotából rajzoljuk újra: a kártya
     * szövegét és a keret címét, az állapotsort és a gombok engedélyezettségét.
     * Ez az egyetlen hely, ahol a felület a modellt "olvassa", ezért a
     * komponensek soha nem kerülhetnek ellentmondásba egymással vagy a
     * modellel.
     */
    private void refresh() {
        if (model.hasCurrentCard()) {
            display.setText(model.getCurrentText());
            displayBorder.setTitle(model.isShowingQuestion() ? "Question" : "Answer");
            statusLabel.setText("Card " + (model.getAnsweredCount() + 1) + " / " + model.getCardCount()
                    + "      Score: " + model.getScore() + " / " + model.getAnsweredCount());
        } else if (model.isFinished()) {
            display.setText("The end!\n\nYou knew " + model.getScore() + " of " + model.getCardCount()
                    + " cards.\nPress Reset to start over, or open another file.");
            displayBorder.setTitle("Finished");
            statusLabel.setText("Finished      Score: " + model.getScore() + " / " + model.getCardCount());
        } else {
            display.setText("Open a cards file: File > Open... (Ctrl+O)\n\n"
                    + "Every line of the file is a card: question" + FlashcardsModel.SEPARATOR + "answer");
            displayBorder.setTitle("Flashcards");
            statusLabel.setText("No cards loaded");
        }
        // Csak az a gomb nyomható meg, aminek az adott állapotban értelme van.
        resetButton.setEnabled(model.hasCards());
        toggleButton.setEnabled(model.hasCurrentCard());
        wrongButton.setEnabled(model.hasCurrentCard());
        goodButton.setEnabled(model.hasCurrentCard());
        // A keret címének módosítása önmagában nem rajzolja újra a panelt.
        displayPane.repaint();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * A két válaszgomb eseménykezelője. Névvel rendelkező belső osztály, mert
     * paramétere van (jó vagy rossz válasz), és két gomb is használja. Belső
     * osztályként eléri a külső osztály mezőit (model) és metódusait
     * (refresh).
     */
    class AnswerListener implements ActionListener {

        private final boolean correct;

        public AnswerListener(boolean correct) {
            this.correct = correct;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            model.answer(correct);
            refresh();
        }

    }

}
