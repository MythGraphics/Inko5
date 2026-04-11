/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.1.0
 *
 */

import static inko.Patient.DEFAULT_FORMATTER;
import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import static java.time.temporal.ChronoField.YEAR;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

public class LocalDateCellEditor extends DefaultCellEditor {

    private final JTextField textField;

    public LocalDateCellEditor() {
        super( new JTextField() );
        this.textField = (JTextField) getComponent();
    }

    @Override
    public Object getCellEditorValue() {
        String text = textField.getText().trim();
        if ( text.isEmpty() ) {
            return null;
        }
        try {
            // ISO Format yyyy-MM-dd
            if ( text.contains( "-" )) {
                return LocalDate.parse(text);
            }
            // deutsche Formate mit Punkt
            if ( text.contains( "." )) {
                if ( text.split("\\.").length == 3 ) {
                    // dd.MM.yyyy
                    return LocalDate.parse(text, DEFAULT_FORMATTER);
                }
                if ( text.split("\\.").length == 2 &&
                     text.matches( "^(0[0-2]|1[0-9])\\.(\\d{2}|\\d{4})$" )) {
                        // MM.yyyy, M.yyyy, MM.yy
                        return parseGermanShortDate(text);
                }
            }
            // deutsche Formate ohne Punkt (MMyyyy, MMyy)
            if ( text.matches( "^(\\d{4}|\\d{6})$" )) {
                return parseGermanShortDate( text.substring( 0, 2 ) + "." + text.substring( 2 ));
            }
            if ( text.equalsIgnoreCase( "heute" )) {
                return LocalDate.now();
            }
            if ( text.equalsIgnoreCase( "aktuell" ) || text.equalsIgnoreCase( "dieser" )) {
                return YearMonth.now().atEndOfMonth();
            }
            if ( text.equalsIgnoreCase( "gestern" )) {
                return LocalDate.now().minusDays(1);
            }
            if ( text.equalsIgnoreCase( "vorgestern" )) {
                return LocalDate.now().minusDays(2);
            }
            // nix passt
            return null;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static LocalDate parseGermanShortDate(String text) {
        // MM.yyyy, M.yyyy, MM.yy
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("M.")
            .appendValueReduced(YEAR, 2, 4, 2000)
            .toFormatter();
        return YearMonth.parse(text, formatter).atEndOfMonth(); // den letzten Tag des Monats berechnen
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JTextField tf = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
        if (value instanceof LocalDate) {
            tf.setText(( (ChronoLocalDate) value ).format( DEFAULT_FORMATTER ));
        } else {
            tf.setText("");
        }
        return tf;
    }

    @Override
    public boolean stopCellEditing() {
        // Validierung vor dem Schließen der Zelle
        try {
            getCellEditorValue();
            return super.stopCellEditing();
        } catch (Exception e) {
            textField.setBackground(Color.PINK);
            return false; // verhindert das Verlassen der Zelle bei Fehlern
        }
    }

}