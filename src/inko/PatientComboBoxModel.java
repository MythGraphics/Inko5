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

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class PatientComboBoxModel extends DefaultComboBoxModel<Patient> {

    private List<Patient> patsBackup; // Backup der kompletten Liste

    public PatientComboBoxModel(List<Patient> patients) {
        this.patsBackup = patients;
        for (Patient p : patients) {
            this.addElement(p);
        }
    }

    public void filter(String searchText) {
        this.removeAllElements();
        String searchLower = searchText.toLowerCase();
        for (Patient p : patsBackup) {
            if ( p.getFullName().toLowerCase().contains( searchLower )) {
                this.addElement(p);
            }
        }
        // Falls keine Treffer, bleibt die Liste leer
    }

    /**
     * Aktualisiert das Model mit neuen Patienten-Daten.
     * @param newPatients
     */
    public void refresh(List<Patient> newPatients) {
        this.patsBackup = newPatients;
        this.removeAllElements();
        for (Patient p : newPatients) {
            this.addElement(p);
        }
    }

    public void updateSelectedDisplay(JComboBox jComboBox) {
        Object selected = getSelectedItem();
        if (selected != null) {
            int index = getIndexOf(selected);
            if (index != -1) {
                fireContentsChanged(this, index, index);
            }
        }
        jComboBox.repaint();
    }

    public List<Patient> getList() {
        return patsBackup;
    }

    public String getSelectedItemName() {
        Patient p = (Patient) super.getSelectedItem();
        return p.getFullName();
    }

}
