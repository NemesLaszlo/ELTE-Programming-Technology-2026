package calc;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Az egyszerűsített számológép felülete (az MV "V" betűje: a nézet).
 *
 * A nézet felépíti a felületet, az eseménykezelői beolvassák az operandusokat,
 * meghívják a modellt (CalcModel), majd a refresh() a modell állapota alapján
 * frissíti a felületet. A nézet tehát egyben vezérlő is (ld. README, MV).
 *
 * Az ablak felépítése (komponensfa):
 * <pre>
 * JFrame
 * ├── JMenuBar: Calc menü (Clear, Use result as A, Exit)
 * └── content pane (BorderLayout)
 *     ├── CENTER: numPanel (BorderLayout)
 *     │   ├── WEST:   labelPanel (GridLayout, 1 oszlop) – címkék
 *     │   └── CENTER: fieldPanel (GridLayout, 1 oszlop) – szövegmezők
 *     └── SOUTH:  buttonPanel (GridLayout, 1 sor) – műveletenként egy gomb
 * </pre>
 *
 * @author bli
 */
public class CalcGUI {

    /** A komponensek közötti távolság pixelben. */
    private static final int GAP = 5;

    /** A modell: itt történik a számolás, és itt van az eredmény. */
    private final CalcModel model = new CalcModel();

    private final JFrame frame;
    private final JTextField operand1;
    private final JTextField operand2;
    private final JTextField result;

    /**
     * Felépíti és megjeleníti az ablakot.
     *
     * @param fieldWidth a szövegmezők szélessége oszlopokban
     */
    public CalcGUI(int fieldWidth) {
        frame = new JFrame("Calculator");
        // Enélkül a bezárás csak elrejti az ablakot, a program tovább futna.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        operand1 = new JTextField(fieldWidth);
        operand2 = new JTextField(fieldWidth);
        result = new JTextField(fieldWidth);
        // Az eredményt csak a program írhatja, a felhasználó nem.
        result.setEditable(false);

        // A címkék és a mezők külön-külön egy-egy egyoszlopos GridLayout-ba
        // kerülnek (a sorok száma 0, azaz annyi sor, amennyi kell). Egyetlen
        // kétoszlopos GridLayout-nál a címkék oszlopa ugyanolyan széles lenne,
        // mint a mezőké, így viszont a címkék csak annyi helyet kapnak a WEST
        // régióban, amennyi kell, a többi a mezőké (CENTER).
        JPanel labelPanel = new JPanel(new GridLayout(0, 1, GAP, GAP));
        labelPanel.add(new JLabel("A:"));
        labelPanel.add(new JLabel("B:"));
        labelPanel.add(new JLabel("Result:"));

        JPanel fieldPanel = new JPanel(new GridLayout(0, 1, GAP, GAP));
        fieldPanel.add(operand1);
        fieldPanel.add(operand2);
        fieldPanel.add(result);

        JPanel numPanel = new JPanel(new BorderLayout(GAP, GAP));
        numPanel.add(labelPanel, BorderLayout.WEST);
        numPanel.add(fieldPanel, BorderLayout.CENTER);

        // A gombokat nem egyesével írjuk meg, hanem a műveletek listájából
        // hozzuk létre őket. Mindegyik ugyanazt az eseménykezelő osztályt
        // kapja, csak más paraméterrel.
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, GAP, GAP));
        for (Operation operation : Operation.values()) {
            JButton button = new JButton(operation.getSymbol());
            button.addActionListener(new CalcActionListener(operation));
            buttonPanel.add(button);
        }

        // Saját tartalompanelt használunk, hogy legyen egy kis margó az
        // ablak széle és a komponensek között.
        JPanel content = new JPanel(new BorderLayout(GAP, GAP));
        content.setBorder(BorderFactory.createEmptyBorder(GAP * 2, GAP * 2, GAP * 2, GAP * 2));
        content.add(numPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);
        frame.setContentPane(content);

        frame.setJMenuBar(createMenuBar());

        // A pack a komponensek kívánt mérete alapján állítja be az ablak
        // méretét. A setVisible mindig a legvégén legyen, amikor az ablak
        // már teljesen fel van építve.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Létrehozza a menüsort. A menüpontok eseménykezelői lambdák. A lambda
     * egy névtelen, helyben megírt függvény: e -> clear() jelentése "ha
     * jön egy e esemény, hívd meg a clear()-t". Rövid, egyszer használt
     * kódnál ez a legtömörebb forma.
     */
    private JMenuBar createMenuBar() {
        JMenu calcMenu = new JMenu("Calc");
        // Alt+C lenyitja a menüt.
        calcMenu.setMnemonic(KeyEvent.VK_C);

        JMenuItem clearItem = new JMenuItem("Clear");
        // Gyorsbillentyű: a menü lenyitása nélkül is működik.
        clearItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        clearItem.addActionListener(e -> clear());
        calcMenu.add(clearItem);

        JMenuItem useResultItem = new JMenuItem("Use result as A");
        useResultItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        useResultItem.addActionListener(e -> useResult());
        calcMenu.add(useResultItem);

        calcMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> System.exit(0));
        calcMenu.add(exitItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(calcMenu);
        return menuBar;
    }

    /**
     * Kiüríti mindhárom mezőt.
     */
    private void clear() {
        operand1.setText("");
        operand2.setText("");
        model.clearResult();
        refresh();
        operand1.requestFocusInWindow();
    }

    /**
     * Az eredményt átírja az első operandusba, így láncolhatók a műveletek.
     */
    private void useResult() {
        if (!model.hasResult()) {
            return;
        }
        operand1.setText(format(model.getResult()));
        operand2.setText("");
        model.clearResult();
        refresh();
        operand2.requestFocusInWindow();
    }

    /**
     * Az eredménymezőt mindig a modell állapotából írjuk ki, nem "kézzel".
     */
    private void refresh() {
        result.setText(model.hasResult() ? format(model.getResult()) : "");
    }

    /**
     * Beolvas egy operandust a szövegmezőből. Tizedesvesszőt és tizedespontot
     * is elfogadunk, mert magyar billentyűzeten jellemzően vesszőt írunk.
     *
     * @param field a szövegmező
     * @param name az operandus neve a hibaüzenethez
     * @return a beolvasott szám
     * @throws NumberFormatException ha a mező üres, vagy nem véges számot
     * tartalmaz
     */
    private static double parseOperand(JTextField field, String name) {
        String text = field.getText().trim().replace(',', '.');
        if (text.isEmpty()) {
            throw new NumberFormatException("Operand " + name + " is missing");
        }
        double value;
        try {
            value = Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            value = Double.NaN;
        }
        // A parseDouble a "NaN" és az "Infinity" szöveget is elfogadja,
        // ezeket sem tekintjük számnak.
        if (!Double.isFinite(value)) {
            throw new NumberFormatException("Operand " + name
                    + " is not a valid number: " + field.getText());
        }
        return value;
    }

    /**
     * Egész eredménynél ne jelenjen meg a felesleges ".0" (pl. 2 + 3 = 5).
     */
    private static String format(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private void showError(String message) {
        // Az első paraméter a szülő komponens: a párbeszédablak felette
        // jelenik meg, és amíg nyitva van, a főablak nem kezelhető.
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * A műveleti gombok eseménykezelője. Névvel rendelkező belső osztály,
     * mert paramétere (a művelet) van, és több gomb is használja. Belső
     * osztályként eléri a külső osztály mezőit (model, operand1, operand2).
     *
     * Csak továbbít: beolvassa az operandusokat, a számolást a modellre bízza,
     * végül frissíti a felületet.
     */
    class CalcActionListener implements ActionListener {

        private final Operation operation;

        public CalcActionListener(Operation operation) {
            this.operation = operation;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                double a = parseOperand(operand1, "A");
                double b = parseOperand(operand2, "B");
                model.calculate(a, operation, b);
                refresh();
            } catch (NumberFormatException | ArithmeticException ex) {
                // Hibás bemenet vagy értelmezhetetlen eredmény: ne maradjon
                // a mezőben egy korábbi, már nem érvényes eredmény.
                model.clearResult();
                refresh();
                showError(ex.getMessage());
            }
        }
    }

}
