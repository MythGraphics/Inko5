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

    private List<Patient> patients; // Backup der kompletten Liste

    public PatientComboBoxModel(List<Patient> patients) {
        this.patients = patients;
        for (Patient p : patients) {
            addElement(p);
        }
    }

    public void filter(String searchText) {
        removeAllElements();
        String searchLower = searchText.toLowerCase();
        for (Patient p : patients) {
            if ( p.getFullName().toLowerCase().contains( searchLower )) {
                addElement(p);
            }
        }
        // Falls keine Treffer, bleibt die Liste leer
    }

    /**
     * Aktualisiert das Model mit neuen Patienten-Daten.
     * @param newPatients
     */
    public void refresh(List<Patient> newPatients) {
        this.patients = newPatients;
        removeAllElements();
        for (Patient p : newPatients) {
            addElement(p);
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

    /**
     * Returns the unfiltered, cached List processed by this model.
     * @return cached list
     */
    public List<Patient> getList() {
        return patients;
    }

    public String getSelectedItemName() {
        Patient p = (Patient) super.getSelectedItem();
        return p.getFullName();
    }

}
