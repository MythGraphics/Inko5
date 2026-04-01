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
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

public class LocalDateCellEditor extends DefaultCellEditor {

    private final JTextField textField;
    private final DateTimeFormatter german = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
            // Versuch 1: Deutsches Format dd.MM.yyyy
            if ( text.contains( "." )) {
                if ( text.split("\\.").length == 2 ) {
                    // MM.yy
                    return LocalDate.parse( text, DateTimeFormatter.ofPattern( "MM.yy" ));
                }
                return LocalDate.parse(text, german);
            }
            // Versuch 2: ISO Format yyyy-MM-dd
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        JTextField tf = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
        if (value instanceof LocalDate) {
            tf.setText(( (ChronoLocalDate) value ).format( german ));
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