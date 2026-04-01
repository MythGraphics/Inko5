/*
 *
 */

package inko.old;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.0
 *
 */

import inko.Artikel;
import javax.swing.AbstractListModel;

public class PatientArtikelListModel extends AbstractListModel<Artikel> {

    private final DBio io;

    private Patient patient;

    public PatientArtikelListModel(DBio io) {
        this.io = io;
    }

    public void setPatient(int id) {
        this.patient = io.getOldPatientByID(id);
        fireContentsChanged( this, 0, getSize() );
    }

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

    public void set(int index, Artikel artikel) {
        if (patient != null) {
            patient.getArtikelList().set(index, artikel);
            fireContentsChanged(artikel, index, index);
        }
    }

    public void addElement(Artikel artikel) {
        if (patient != null) {
            patient.getArtikelList().add(artikel);
            int index = patient.getArtikelList().size()-1;
            fireIntervalAdded(this, index, index);
        }
    }

    public void remove(int index) {
        if ( patient != null && index >= 0 && index < patient.getArtikelList().size() ) {
            patient.getArtikelList().remove(index);
            fireIntervalRemoved(this, index, index);
        }
    }

}
