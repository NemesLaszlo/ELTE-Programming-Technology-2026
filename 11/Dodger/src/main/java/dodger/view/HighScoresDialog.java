package dodger.view;

import dodger.model.HighScoreDto;
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
 * A ranglista ablaka: egy táblázat (JTable) a legjobb eredményekkel. Az
 * adatokat készen kapja (a DodgerGUI kéri le a backendtől, DTO-ként): ez az osztály
 * nem tud az adatbázisról.
 */
@SuppressWarnings("serial") // a JDialog Serializable, de az ablakot nem mentjük fájlba
public final class HighScoresDialog extends JDialog {

    private static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * @param owner a főablak; a dialógus fölötte, középen jelenik meg
     * @param highScores az eredmények a legjobbal az elején
     */
    public HighScoresDialog(Frame owner, List<HighScoreDto> highScores) {
        super(owner, "High scores", true);

        String[] columns = {"#", "Name", "Score", "Date"};
        // A DefaultTableModel alapból szerkeszthetővé teszi a cellákat; itt
        // csak megjelenítünk, ezért az isCellEditable mindig hamis.
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (HighScoreDto highScore : highScores) {
            model.addRow(new Object[]{
                highScore.rank(), highScore.name(), highScore.score(), highScore.createdAt().format(DATE_FORMAT)
            });
        }
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.setPreferredScrollableViewportSize(new Dimension(400, 180));

        if (highScores.isEmpty()) {
            add(new JLabel("No high scores yet. Be the first!", JLabel.CENTER), BorderLayout.NORTH);
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

}
