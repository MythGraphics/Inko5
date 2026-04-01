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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class PatientListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Patient) {
            Patient p = (Patient) value;
            String text = p.toString();

            // neue Patienten hervorheben
            if ( p.getId() <= 0 ) {
                setForeground(Color.RED);
                setText( "[neu] " + p.getFullName() );
                return this;
            }

            // geänderte Patienten hervorheben
            if ( p.isModified() ) {
                setText(text + " (geändert)");
                setForeground(Color.BLUE);
                setFont( getFont().deriveFont( Font.ITALIC ));
            } else {
                setText(text);
                if (!isSelected) {
                    setForeground( list.getForeground() );
                }
            }
        }
        return this;
    }

}
