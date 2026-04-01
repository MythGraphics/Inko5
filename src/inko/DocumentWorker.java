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
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

public class DocumentWorker extends SwingWorker<Boolean, String> {

    private final String template;
    private final String targetPath;
    private final Patient patient;
    private final MainFrame mainFrame;

    public DocumentWorker(String template, String targetPath, Patient patient, MainFrame mainFrame) {
        this.template = template;
        this.targetPath = targetPath;
        this.patient = patient;
        this.mainFrame = mainFrame;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        publish( "Generiere Dokument für " + patient.getLastName() + " ..." );
        ODTHandler.replaceData(template, targetPath, patient, MainFrame.SEVENZIP, MainFrame.OO);
        return true;
    }

    @Override
    protected void process(List<String> chunks) {
        // läuft im UI-Thread und kann das Statusfeld sicher updaten
        for (String message : chunks) {
            mainFrame.statusField.showMessage(message);
        }
    }

    @Override
    protected void done() {
        try {
            if ( get() ) {
                mainFrame.statusField.showSuccess("Dokument erfolgreich erstellt und geöffnet.");
            }
        } catch (InterruptedException | ExecutionException e) {
            mainFrame.statusField.showError( "Fehler: " + e.getMessage() );
            e.printStackTrace();
        }
    }

}
