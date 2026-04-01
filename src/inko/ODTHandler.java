/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.3.1
 *
 */

import io.Reader;
import io.Writer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ODTHandler {

    public final static String SOURCE = "content.xml";

    private ODTHandler() {}

    public static File getContentFile(ZipFile odtfile, File target) throws IOException {
        target.createNewFile();
        ZipEntry entry = odtfile.getEntry(SOURCE);
        if ( io.IO.extract(odtfile, entry, target) ) {
            return target;
        } else {
            return null;
        }
    }

    public static void zip(String odtfn, String newODTfn, String xmlfn) throws IOException {
        // fn == filename
        Writer.write(
            Reader.getBinaryReader( new File( odtfn )),
            Writer.getBinaryWriter( new File( newODTfn ))
        ); // kopiert die Vorlage
        Process p1 = Runtime.getRuntime().exec( new String[] { MainFrame.SEVENZIP, "u", newODTfn, xmlfn });
        try {
            int exitCode = p1.waitFor();
            if (exitCode != 0) {
                throw new IOException("7-Zip Fehler Code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Runtime.getRuntime().exec( new String[]{ MainFrame.OO, newODTfn });
    }

    public static void replaceData(String templatePath, String outputPath, Patient p, String zipExe, String ooExe)
    throws IOException {
        File tempXml = File.createTempFile("content_processed_", ".xml");
        try {
            try ( ZipFile zipFile = new ZipFile( templatePath );
                BufferedReader in = new BufferedReader( new InputStreamReader(
                    zipFile.getInputStream( zipFile.getEntry( "content.xml" )), StandardCharsets.UTF_8 ));
                PrintWriter out = new PrintWriter( new OutputStreamWriter(
                    new FileOutputStream(tempXml), StandardCharsets.UTF_8 ))) {
                String line;
                while (( line = in.readLine() ) != null ) {
                    // Platzhalter ersetzen
                    out.println( Patient.replaceTemplate( line, p ));
                }
                out.flush();
            }
            // Injection-Prozess starten
            executeZipAndOpen(templatePath, outputPath, tempXml.getAbsolutePath(), zipExe, ooExe);
        } finally {
            if ( tempXml.exists() ) {
                tempXml.delete();
            }
        }
    }

    private static void executeZipAndOpen(String template, String output, String xmlPath, String zipExe, String ooExe)
    throws IOException {
        File templateFile = new File(template);
        File outputFile = new File(output);
        // Vorlage an den Zielort kopieren (mit NIO Files viel schneller)
        Files.copy( templateFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
        // 7-Zip aufrufen, um content.xml im ODT zu ersetzen
        // Umbenennen der tempXml in "content.xml" um, da 7z den Dateinamen im Archiv beibehält
        File targetXml = new File( outputFile.getParent(), "content.xml" );
        new File(xmlPath).renameTo(targetXml);

        ProcessBuilder zipPb = new ProcessBuilder( zipExe, "u", output, targetXml.getName() );
        zipPb.directory( outputFile.getParentFile() ); // Im Ordner der Datei ausführen

        try {
            Process zipProcess = zipPb.start();
            int exitCode = zipProcess.waitFor();
            if (exitCode == 0) {
                // Wenn 7-Zip Erfolg meldet, OpenOffice starten
                new ProcessBuilder(ooExe, output).start();
            } else {
                throw new IOException("7-Zip Fehler: Prozess beendet mit Code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Prozess wurde unterbrochen", e);
        } finally {
            targetXml.delete();
        }
    }

}
