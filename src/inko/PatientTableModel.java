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

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class PatientTableModel extends AbstractTableModel implements HasPatient {

    private final List<PatientField> fields;
    private final String[] columnNames = {"Feld", "Eingabe"};

    private Patient patient;

    public PatientTableModel() {
        this.fields = PatientField.BASIC_FIELDS;
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
            if ( patient.get(field) != null && patient.get(field).equals( value )) {
                return;
            }
            try {
                patient.set(field, value);
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
