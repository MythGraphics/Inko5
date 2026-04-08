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

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import static java.time.temporal.ChronoField.YEAR;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class PatientTableModel extends AbstractTableModel implements HasPatient {

    private final List<PatientField> fields;
    private final String[] columnNames = {"Feld", "Eingabe"};

    private Patient patient;

    public PatientTableModel() {
        this.fields = PatientField.UI_FIELDS;
    }

    public PatientTableModel(List<PatientField> fields) {
        this.fields = fields;
    }

    @Override
    public int getRowCount() {
        return fields.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PatientField field = fields.get(rowIndex);
        if (columnIndex == 0) {
            return field.getUIName();
        }
        return patient.get(field);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            PatientField field = fields.get(rowIndex);
            // Double-Check: Wenn der Wert gleich bleibt, nichts tun
            if ( patient.get(field) != null && patient.get(field).equals( value )) {
                return;
            }
            try {
                Object valueToSet = value;
                // Logik für mm.yyyy Formatierung
                if ( value instanceof String && field.getType() == LocalDate.class ) {
                    String input = ((String) value).trim();
                    // Prüfen, ob die Eingabe einem sinnigen Monat.Jahr Muster entspricht
                    // Regex für: (1 oder 01) . (24 oder 2024)
                    // ^(0?[1-9]|1[0-2])  -> Monat (1-12, optional mit 0)
                    // \\.                -> Punkt
                    // (\\d{2}|\\d{4})$   -> Entweder 2 oder 4 Ziffern am Ende
                    if ( input.matches( "^(0?[1-9]|1[0-2])\\.(\\d{2}|\\d{4})$" ) &&
                         PatientField.SHORT_DATE_FIELDS.contains( field )) {
                            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                .appendPattern("M.")
                                .appendValueReduced(YEAR, 2, 4, 2000)
                                .toFormatter();
                            YearMonth ym = YearMonth.parse(input, formatter);
                            valueToSet = ym.atEndOfMonth(); // den letzten Tag des Monats berechnen
                    }
                }
                patient.set(field, valueToSet);
                super.fireTableCellUpdated(rowIndex, columnIndex);
            } catch (NumberFormatException | DateTimeParseException | NoSuchElementException e) {
                JOptionPane.showMessageDialog( null, "Fehler: " + e.getMessage() );
            }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            return fields.get(rowIndex) != PatientField.ID; // ID darf nicht editierbar sein
        }
        return columnIndex == 1; // nur die Eingabe-Spalte ist editierbar
    }

    @Override
    public void setPatient(Patient patient) {
        this.patient = patient;
        super.fireTableDataChanged();
    }

    @Override
    public Patient getPatient() {
        return patient;
    }

}
