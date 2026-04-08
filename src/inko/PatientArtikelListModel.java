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

import javax.swing.AbstractListModel;

public class PatientArtikelListModel extends AbstractListModel<Artikel> implements HasPatient {

    private Patient patient;

    public PatientArtikelListModel() {}

    @Override
    public void setPatient(Patient patient) {
        this.patient = patient;
        fireContentsChanged( this, 0, getSize() );
    }

    @Override
    public Patient getPatient() {
        return patient;
    }

    @Override
    public int getSize() {
        return (patient != null) ? patient.getArtikelList().size() : 0;
    }

    @Override
    public Artikel getElementAt(int index) {
        try {
            return patient.getArtikelList().get(index);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void set(int index, Artikel himi) {
        if (patient != null) {
            patient.getArtikelList().set(index, himi);
            patient.refreshArtikelList();
            fireContentsChanged(himi, index, index);
        }
    }

    public void addElement(Artikel himi) {
        if (patient != null) {
            patient.getArtikelList().add(himi);
            patient.refreshArtikelList();
            int index = patient.getArtikelList().size()-1;
            fireIntervalAdded(this, index, index);
        }
    }

    public void remove(int index) {
        if ( patient != null && index >= 0 && index < patient.getArtikelList().size() ) {
            patient.getArtikelList().remove(index);
            patient.refreshArtikelList();
            fireIntervalRemoved(this, index, index);
        }
    }

    public int moveElementUp(int index) {
        if ( patient != null && index > 0 && index < patient.getArtikelList().size() ) {
            // Element in der Liste tauschen
            Artikel himi = patient.getArtikelList().remove(index);
            patient.getArtikelList().add(index-1, himi);

            patient.refreshArtikelList(); // aktualisieren
            fireContentsChanged(this, index-1, index); // GUI benachrichtigen
            return index-1;
        } else {
            return index;
        }
    }

    public int moveElementDown(int index) {
        if ( patient != null && index >= 0 && index < patient.getArtikelList().size() - 1 ) {
            // Element in der Liste tauschen
            Artikel himi = patient.getArtikelList().remove(index);
            patient.getArtikelList().add(index+1, himi);

            patient.refreshArtikelList(); // aktualisieren
            fireContentsChanged(this, index, index+1); // GUI benachrichtigen
            return index+1;
        } else {
            return index;
        }
    }

}
