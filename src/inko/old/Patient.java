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
import inko.PatientField;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.AbstractListModel;

public class Patient extends inko.Patient {

    private final ArrayList<Artikel> artikelList = new ArrayList<>();

    public Patient() {}

    /**
     * Factory-Methode, um ein PatientOld-Objekt aus einem Standard-Patienten zu erstellen.
     * @param base Das Basis-Patientenobjekt
     * @param artikelModel Die (alternative) Artikelliste
     * @return Eine neue Instanz von PatientOld
     */
    public static Patient fromPatient(inko.Patient base, AbstractListModel<Artikel> artikelModel) {
        if (base == null) {
            return null;
        }
        Patient old = new Patient();
        for ( PatientField field : PatientField.DB_FIELDS ) {
            old.set(field, base.get( field ));
        }
        old.setModified(true);
        if (artikelModel != null) {
            old.updateArtikelList(artikelModel);
        }
        return old;
    }

    public void setArtikel(String namelist, String pznlist) throws IllegalArgumentException {
        artikelList.clear();
        StringTokenizer tname = new StringTokenizer(namelist, "\n", false);
        StringTokenizer tpzn  = new StringTokenizer(pznlist, "\n", false);
        if ( tname.countTokens() != tpzn.countTokens() ) {
            throw new IllegalArgumentException(
                "String \"namelist\" und \"pznlist\" ungleich in der Anzahl ihrer Zeilen."
            );
        }
        while ( tname.hasMoreTokens() ) {
            artikelList.add( new Artikel( tname.nextToken(), Integer.parseInt( tpzn.nextToken() )));
        }
    }

    public void addArtikel(String name, int pzn) {
        Artikel artikel = Artikel.parseArtikelString(name);
        artikel.setPZN(pzn);
        artikel.setId(pzn);
        artikelList.add(artikel);
    }

    public void updateArtikelList(AbstractListModel<Artikel> model) {
        artikelList.clear();
        for (int i = 0; i < model.getSize(); ++i) {
            artikelList.add( model.getElementAt( i ));
        }
        setModified(true);
    }

    @Override
    public ArrayList<Artikel> getArtikelList() {
        return artikelList;
    }

    @Override
    public String getArtikelListAsString() {
        StringBuilder sb = new StringBuilder();
        for ( Artikel a : getArtikelList() ) {
            sb.append( a.getFullArtikelString() ).append(ARTIKEL_SEPARATOR);
        }
        return sb.toString();
    }

}
