/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.0
 *
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PatientTableCellRenderer extends DefaultTableCellRenderer {

    public final static Color SENFGELB = new Color(255, 220, 88);
    public final static Color HELLROT  = new Color(255, 200, 200);

    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
    ) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        // Styling für die linke Spalte
        if (column == 0) {
            setBackground( table.getTableHeader().getBackground() );
            setFont( getFont().deriveFont( Font.BOLD ));
            setHorizontalAlignment(RIGHT);
            if (!isSelected) {
                setBackground( new Color( 240, 240, 240 ));
            }
            return this;
        }

        // Logik für die "Eingabe"-Spalte
        setBackground( table.getBackground() );
        setFont( getFont().deriveFont( Font.PLAIN ));
        setHorizontalAlignment(LEFT);

        // Datums-Formatierung
        if (value instanceof LocalDate) {
            setText(( (ChronoLocalDate) value ).format( Patient.DEFAULT_FORMATTER ));
        }
/*
        else if (value instanceof Boolean) {
            setText( (Boolean) value ? "Ja" : "Nein" );
        }
*/
        // hier weitere Formatierungen

        PatientTableModel model = (PatientTableModel) table.getModel();
        Patient p = model.getPatient();
        // Standard-Farben setzen
        if (!isSelected) {
            setBackground( table.getBackground() );
            setForeground( table.getForeground() );
        }
        if (p != null) {
            PatientField field = PatientField.BASIC_FIELDS.get(row);
            if (field == PatientField.ENDE_GENEHMIGUNG) {
                if ( p.isPrescriptionExpired() ) {
                    setBackground(HELLROT);
                } else if ( p.isPrescriptionExpiringSoon() ) {
                    setBackground(SENFGELB);
                }
            } else if (field == PatientField.ENDE_BINDUNG) {
                if ( p.isBindingExpired() ) {
                    setBackground(HELLROT);
                } else if ( p.isBindingExpiringSoon() ) {
                    setBackground(SENFGELB);
                }
            }
        }

        return this;
    }

}
