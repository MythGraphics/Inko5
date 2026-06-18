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
        if ( getSize() > 0 ) {
            fireIntervalRemoved( this, 0, getSize()-1 );
        }
        this.patient = patient;
        if ( getSize() > 0 ) {
            fireIntervalAdded(   this, 0, getSize()-1 );
        }
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

    public Artikel set(int index, Artikel artikel) {
        if (patient != null) {
            Artikel old = patient.getArtikelList().set(index, artikel);
            patient.refreshArtikelList();
            fireContentsChanged(artikel, index, index);
            return old;
        }
        return null;
    }

    public void addElement(Artikel artikel) {
        if (patient != null) {
            patient.getArtikelList().add(artikel);
            patient.refreshArtikelList();
            int index = patient.getArtikelList().size()-1;
            fireIntervalAdded(this, index, index);
        }
    }

    public Artikel remove(int index) {
        if ( patient != null && index >= 0 && index < patient.getArtikelList().size() ) {
            Artikel old = patient.getArtikelList().remove(index);
            patient.refreshArtikelList();
            fireIntervalRemoved(this, index, index);
            return old;
        }
        return null;
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
