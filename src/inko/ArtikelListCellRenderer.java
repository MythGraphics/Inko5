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

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class ArtikelListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        String text;
        Artikel artikel = (Artikel) value;
        if ( artikel.getType() == ArtikelType.KOMMENTAR ) {
            text = artikel.getName();
        } else {
            text = artikel.getFullArtikelString();
        }
        label.setText(text);

        return label;
    }

}
