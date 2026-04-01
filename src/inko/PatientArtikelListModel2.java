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

public class PatientArtikelListModel2 extends PatientArtikelListModel {

    private final DBio io;

    public PatientArtikelListModel2(DBio io) {
        this.io = io;
    }

    public void setPatient(int id) {
        super.setPatient( io.getPatientById( id ));
    }

}
