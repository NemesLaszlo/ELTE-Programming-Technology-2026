package qwerty;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

/**
 * Egy egyszerű QWERTY billentyűzet kijelzővel.
 *
 * Az ablak felépítése (komponensfa):
 * <pre>
 * JFrame
 * ├── JMenuBar: QWERTY menü (Exit)
 * └── content pane (BorderLayout)
 *     ├── NORTH:  display – a kijelző (JTextField)
 *     ├── CENTER: keyPanel (FlowLayout) – Q W E R T Y, Back, CLR, Caps
 *     └── SOUTH:  statusLabel – a kijelzőn lévő karakterek száma
 * </pre>
 *
 * Az eseménykezelőket szándékosan négyféleképpen írtuk meg (névvel
 * rendelkező belső osztály, névtelen osztály, lambda, metódusreferencia),
 * hogy mindegyik formára legyen példa. Működésüket tekintve egyenértékűek.
 *
 * @author pinter
 */
public class QwertyGUI {

    private static final String[] LETTERS = {"Q", "W", "E", "R", "T", "Y"};
    private static final int DISPLAY_WIDTH = 30;
    private static final float DISPLAY_FONT_SIZE = 20f;
    private static final int GAP = 5;

    private final JFrame frame;
    private final JTextField display;
    private final JLabel statusLabel;
    private final JToggleButton capsButton;
    /** A betűgombok, hogy a Caps átkapcsolásakor átírhassuk a feliratukat. */
    private final List<JButton> letterButtons = new ArrayList<>();

    /**
     * Felépíti és megjeleníti az ablakot.
     */
    public QwertyGUI() {
        frame = new JFrame("QWERTY");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        display = new JTextField(DISPLAY_WIDTH);
        // A kijelzőre csak a gombokkal lehet írni...
        display.setEditable(false);
        // ...és nem kaphat fókuszt, így nem nyeli el a billentyűleütéseket,
        // azok a gombokhoz jutnak el (ld. bindKey).
        display.setFocusable(false);
        display.setFont(display.getFont().deriveFont(DISPLAY_FONT_SIZE));

        // A JPanel alapértelmezett elrendezése a FlowLayout: a komponensek
        // balról jobbra, egymás után kerülnek a panelre.
        JPanel keyPanel = new JPanel();

        // A Caps gombot előre létrehozzuk, mert a betűgombok eseménykezelője
        // az állapotát használja. A JToggleButton benyomva marad, amíg újra
        // meg nem nyomjuk: az isSelected() adja meg az állapotát.
        capsButton = new JToggleButton("Caps", true);

        for (String letter : LETTERS) {
            JButton button = new JButton(letter);
            // 1. Névvel rendelkező belső osztály: paraméterként megkapja,
            // melyik betűt kell kiírnia.
            button.addActionListener(new QwertyListener(letter));
            bindKey(letter, button);
            letterButtons.add(button);
            keyPanel.add(button);
        }

        JButton backButton = new JButton("Back");
        backButton.setToolTipText("Delete the last character (Backspace)");
        // 2. Metódusreferencia: a backspace metódus paramétere ugyanaz, mint
        // az actionPerformed metódusé, ezért közvetlenül átadható.
        backButton.addActionListener(this::backspace);
        bindKey("BACK_SPACE", backButton);
        keyPanel.add(backButton);

        JButton clearButton = new JButton("CLR");
        clearButton.setToolTipText("Clear the display (Esc)");
        // 3. Lambda: az e paraméter az esemény, itt nincs rá szükségünk.
        clearButton.addActionListener(e -> setDisplayText(""));
        bindKey("ESCAPE", clearButton);
        keyPanel.add(clearButton);

        capsButton.setToolTipText("Switch between upper and lower case letters");
        capsButton.addActionListener(e -> updateLetterCase());
        keyPanel.add(capsButton);

        statusLabel = new JLabel();

        JPanel content = new JPanel(new BorderLayout(GAP, GAP));
        content.setBorder(BorderFactory.createEmptyBorder(GAP * 2, GAP * 2, GAP * 2, GAP * 2));
        content.add(display, BorderLayout.NORTH);
        content.add(keyPanel, BorderLayout.CENTER);
        content.add(statusLabel, BorderLayout.SOUTH);
        frame.setContentPane(content);

        frame.setJMenuBar(createMenuBar());
        setDisplayText("");

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenu qwertyMenu = new JMenu("QWERTY");
        qwertyMenu.setMnemonic(KeyEvent.VK_Q);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        // 4. Névtelen (anonim) osztály: helyben implementáljuk az interfészt.
        // Lambda előtt (Java 8) ez volt a szokásos forma, régebbi kódokban
        // gyakran találkozunk vele.
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        qwertyMenu.add(exitItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(qwertyMenu);
        return menuBar;
    }

    /**
     * A fizikai billentyűzet egy billentyűjét egy gombhoz köti: a billentyű
     * leütése olyan, mintha a gombra kattintottunk volna.
     *
     * A billentyűt az ablak gyökérpaneljének InputMap-jébe vesszük fel. A
     * WHEN_IN_FOCUSED_WINDOW miatt mindegy, melyik komponensen van a fókusz,
     * elég, ha az ablak aktív. Az InputMap a billentyűhöz egy nevet rendel, az
     * ActionMap a névhez a végrehajtandó akciót.
     *
     * @param keyStroke a billentyű neve, pl. "Q", "BACK_SPACE", "ESCAPE"
     * @param button a gomb, amit a billentyű "megnyom"
     */
    private void bindKey(String keyStroke, AbstractButton button) {
        JRootPane root = frame.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyStroke), keyStroke);
        root.getActionMap().put(keyStroke, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // A doClick egy pillanatra benyomottnak is mutatja a gombot,
                // majd lefuttatja az eseménykezelőit.
                button.doClick();
            }
        });
    }

    /**
     * A kijelző minden módosítása ezen a metóduson keresztül történik, így a
     * karakterszámláló mindig naprakész marad.
     */
    private void setDisplayText(String text) {
        display.setText(text);
        statusLabel.setText("Characters: " + text.length());
    }

    /**
     * Törli a kijelzőről az utolsó karaktert, ha van.
     */
    private void backspace(ActionEvent e) {
        String text = display.getText();
        if (!text.isEmpty()) {
            setDisplayText(text.substring(0, text.length() - 1));
        }
    }

    /**
     * A Caps állapotának megfelelően kis- vagy nagybetűssé alakítja a
     * szöveget.
     */
    private String applyCase(String text) {
        return capsButton.isSelected() ? text.toUpperCase() : text.toLowerCase();
    }

    /**
     * A Caps átkapcsolása után a betűgombok feliratát is átírjuk.
     */
    private void updateLetterCase() {
        for (JButton button : letterButtons) {
            button.setText(applyCase(button.getText()));
        }
    }

    /**
     * A betűgombok eseménykezelője. Névvel rendelkező belső osztály, mert
     * paramétere (a betű) van, és több gomb is használja.
     */
    class QwertyListener implements ActionListener {

        private final String letter;

        public QwertyListener(String letter) {
            this.letter = letter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setDisplayText(display.getText() + applyCase(letter));
        }

    }

}
