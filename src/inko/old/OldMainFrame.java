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
import inko.HTMLMaker;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class OldMainFrame extends inko.MainFrame {

    private final PatientArtikelListModel patientArtikelListModel;

    private JTextField artikelTextField;

    public OldMainFrame() {
        patientArtikelListModel = new PatientArtikelListModel( (inko.old.DBio) pio );
        initComponents();
    }

    public static void main(String args[]) {
        showGUI(new OldMainFrame() );
    }

    @Override
    protected void loadEntry(inko.Patient patient) {
        super.loadEntry(patient);
        if ( patientArtikelListModel == null || patient == null || patient.getId() == -1 ) {
            return;
        }

        patientArtikelListModel.setPatient( patient.getId() );
        loadEntryUpdateUI(patient);
    }

    @Override
    protected inko.DBio getDBio() {
        return new DBio( server, inko.DBio.DEFAULT_PORT, new net.Login( user, pass ));
    }

    @Override
    protected void jArtikelComboBoxEditorKeyReleased(KeyEvent evt) {
        // noop -> den Listener brauchen wir hier nicht
    }

    @Override
    protected void _jAddArtikelButtonActionPerformed(ActionEvent evt) {
        patientArtikelListModel.addElement( Artikel.parseArtikelString( artikelTextField.getText() ));
        patientTableModel.getPatient().setModified(true);
    }

    @Override
    protected void _jRemoveArtikelButtonActionPerformed(ActionEvent evt) {
        int index = jPatientArtikelList.getSelectedIndex();
        if (index > -1) {
            patientArtikelListModel.remove(index);
            patientTableModel.getPatient().setModified(true);
        }
    }

    @Override
    protected void _jChangeArtikelButtonActionPerformed(ActionEvent evt) {
        int index = jPatientArtikelList.getSelectedIndex();
        if (index == -1) {
            return;
        }
        patientTableModel.getPatient().setModified(true);
        patientArtikelListModel.set(index, Artikel.parseArtikelString( artikelTextField.getText() ));
    }

    @Override
    protected void jPatientArtikelListMouseClicked(MouseEvent evt) {
        Artikel artikel = jPatientArtikelList.getSelectedValue();
        if (artikel == null) {
            return;
        }
        artikelTextField.setText( artikel.getFullArtikelString() );
    }

    @Override
    protected void _deckblattButtonActionPerformed(ActionEvent evt) {
        HTMLMaker htmlmaker = new HTMLMaker("deckblatt.html", outpath+"deckblatt.html");
        Patient p = Patient.fromPatient(patientTableModel.getPatient(), patientArtikelListModel);
        try {
            htmlmaker.makeDeckblatt(p);
        } catch (IOException e) {
            statusField.showError(HTMLMaker.IOERROR);
            e.printStackTrace();
        }
    }

    @Override
    protected void _lieferlisteButtonActionPerformed(ActionEvent evt) {
        HTMLMaker htmlmaker = new HTMLMaker("lieferliste.html", outpath+"lieferliste.html");
        List<Patient> list = ((inko.old.DBio) pio).getOldPatientList();
        try {
            htmlmaker.makeLieferliste(list);
        } catch (IOException e) {
            statusField.showError(HTMLMaker.IOERROR);
            e.printStackTrace();
        }
    }

    @Override
    protected void _jButtonArtikelListeActionPerformed(ActionEvent evt) {
        HTMLMaker htmlmaker = new HTMLMaker("himiliste.html", outpath+"himiliste.html");
        List<Patient> patientList = ((inko.old.DBio) pio).getOldPatientList();
        List<Artikel> artikelList = new ArrayList<>();
        for ( Patient p : patientList ) {
            for ( Artikel a : p.getArtikelList() ) {
                artikelList.add(a);
            }
        }
        try {
            htmlmaker.makeArtikelListe(patientList, artikelList);
        } catch (IOException e) {
            statusField.showError(HTMLMaker.IOERROR);
            e.printStackTrace();
        }
    }

    @Override
    protected void _jButtonArtikelListe2ActionPerformed(ActionEvent evt) {
        HTMLMaker htmlmaker = new HTMLMaker("himiliste_erw.html", outpath+"himiliste_erw.html");
        List<Patient> list = ((inko.old.DBio) pio).getOldPatientList();
        try {
            htmlmaker.makeErweiterteArtikelliste(list);
        } catch (IOException e) {
            statusField.showError(HTMLMaker.IOERROR);
            e.printStackTrace();
        }
    }

    @Override
    protected void saveEntry(inko.Patient p) {
        super.saveEntry( Patient.fromPatient( p, patientArtikelListModel ));
        patientTableModel.getPatient().setModified(false);
    }

    @Override
    protected JComboBox<Artikel> getArtikelComboBox() {
        return new JComboBox<Artikel>() {
            @Override
            public void updateUI() {
                setUI( new javax.swing.plaf.basic.BasicComboBoxUI() {
                    @Override
                    protected javax.swing.plaf.basic.ComboPopup createPopup() {
                        return new javax.swing.plaf.basic.BasicComboPopup(comboBox) {
                            @Override
                            public void show() {
                                // Popup nie anzeigen
                            }
        };}});}};
    }

    private void initComponents() {
        jPatientArtikelList.setModel(patientArtikelListModel);
        artikelTextField = (JTextField) jArtikelComboBox.getEditor().getEditorComponent();
        for ( Component comp : jArtikelComboBox.getComponents() ) {
            if (comp instanceof JButton) {
                comp.setEnabled(false);
            }
        }

        jSpinner1.setEnabled(false);
        jCreateArtikelButton.setEnabled(false);

        loadEntry( super.patientTableModel.getPatient() );
    }

}

