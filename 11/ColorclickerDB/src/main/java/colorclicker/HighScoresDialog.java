package colorclicker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * A ranglista ablaka egy táblamérethez: táblázat (JTable) a legjobb
 * időkkel. Az adatokat DTO-ként készen kapja; nem tud az adatbázisról.
 */
@SuppressWarnings("serial") // a JDialog Serializable, de az ablakot nem mentjük fájlba
public final class HighScoresDialog extends JDialog {

    private static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * @param owner a főablak
     * @param size a tábla mérete (a címhez)
     * @param highScores az eredmények a legjobbal az elején
     */
    public HighScoresDialog(Frame owner, int size, List<HighScoreDto> highScores) {
        super(owner, "Best times - " + size + "x" + size, true);

        String[] columns = {"#", "Name", "Time", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (HighScoreDto highScore : highScores) {
            model.addRow(new Object[]{
                highScore.rank(), highScore.name(), formatTime(highScore.millis()),
                highScore.createdAt().format(DATE_FORMAT)
            });
        }
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.setPreferredScrollableViewportSize(new Dimension(400, 180));

        if (highScores.isEmpty()) {
            add(new JLabel("No results for this size yet.", JLabel.CENTER), BorderLayout.NORTH);
        }
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.addActionListener(e -> dispose());
        JPanel buttons = new JPanel();
        buttons.add(ok);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * @param millis az idő ezredmásodpercben
     * @return pl. "1:02.345" (perc:másodperc.ezred)
     */
    static String formatTime(long millis) {
        return String.format("%d:%02d.%03d", millis / 60_000, (millis / 1000) % 60, millis % 1000);
    }

}
