/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 3.1.0
 *
 */

import dataformat.xml.html.TableRow;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class HTMLMaker {

    public final static String HEADERCELL_START = "<th>";
    public final static String HEADERCELL_END   = "</th>";
    public final static String TABLEROW_START   = "<tr>";
    public final static String TABLEROW_END     = "</tr>";
    public final static String CELL_START       = "<td>";
    public final static String CELLCLASS_START  = "<td class=\"";
    public final static String CELL_END         = "</td>";
    public final static String NEWLINE          = "<br />";

    public final static String HEADER_LIEFERLISTE =
        "<tr>" +
        "<th class=\"nr\">Nr.</th>" +
        "<th class=\"frei\">F</th>" +
        "<th class=\"name\">Name, Vorname</th>" +
        "<th class=\"anschrift\">Anschrift</th>" +
        "<th class=\"ort\">Ort</th>" +
        "<th class=\"kommentar\">Kommentar</th>" +
        "<th class=\"tel\">Tel.-Nr.</th>" +
        "<th class=\"artikel\">Artikel</th>" +
        "<th class=\"preis\">Preis</th>" +
        "</tr>"
    ;
    public final static String MARKER = "<!-- Data here -->";

    public final static int FAELLIGKEITSLISTE      = 3;
    public final static int FAELLIGKEITSLISTE_RX   = 31;
    public final static int FAELLIGKEITSLISTE_BIND = 32;

    public final static String IOERROR = "Erstellen der Liste fehlgeschlagen (I/O Fehler)";

    private final File template;
    private final File target;

    public HTMLMaker(String template, String target) {
        this.template   = new File(template);
        this.target     = new File(target);
    }

    public static void makeHTMLFile(File target, String content) throws IOException {
        target.createNewFile();
        try ( PrintWriter out = io.Writer.getTextWriter( target )) {
            out.println(content);
            out.flush();
        }
        try { Desktop.getDesktop().browse( target.toURI() ); }
        catch (IOException e) { e.printStackTrace(); }
        try { Thread.sleep(500); }
        catch (InterruptedException e) {}
    }

    private static String getLeadingWhitespace(String line) {
        return line.substring( 0, line.indexOf( '<' ));
    }

    public void makeDeckblatt(Patient p) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = io.Reader.getTextReader( template )) {
            while ( in.ready() ) {
                String line = in.readLine();
                if ( !line.contains( MARKER )) {
                    line = Patient.replaceTemplate(line, p);
                    sb.append(line).append("\n");
                    continue;
                }
                String leader = getLeadingWhitespace(line);
                for ( Artikel h : p.getArtikelList() ) {
                    sb.append(leader).append("<table><tr><th class=\"largebold\">");
                    sb.append( h.getFullArtikelName() );
                    sb.append("</th><th class=\"smallbold\">");
                    sb.append( h.getPZN() );
                    sb.append("</th></tr></table>").append("\n");
                }
            }
        }
        makeHTMLFile( target, sb.toString() );
    }

    public void makeErweiterteArtikelliste(List<? extends Patient> pats) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = io.Reader.getTextReader( template )) {
            while ( in.ready() ) {
                String line = in.readLine();
                if ( !line.contains( MARKER )) {
                    sb.append(line).append("\n");
                    continue;
                }
                String leader = getLeadingWhitespace(line);
                for (Patient p : pats) {
                    if ( p.isPaused() ) {
                        continue;
                    }
                    String artikel = p.getArtikelListAsString().replaceAll(Patient.ARTIKEL_SEPARATOR, "<br>");
                    sb.append(leader).append("<tr><td class=\"name\">");
                    sb.append( p.getFullName() );
                    sb.append("</td><td class=\"artikel\">");
                    sb.append(artikel);
                    sb.append("</td></tr>").append("\n");
                }
            }
        }
        makeHTMLFile( target, sb.toString() );
    }

    public void makeArtikelListe(List<? extends Patient> pats, List<Artikel> artikelList)
    throws IOException, IllegalArgumentException {
        HashMap<Integer, Integer> mergeMap = new HashMap<>(); // Artikel.ID -> Artikel-Menge pro Patient
        HashMap<Integer, String> stringMap = new HashMap<>(); // Artikel.ID -> Artikel.NAME
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = io.Reader.getTextReader( template )) {
            while ( in.ready() ) {
                String line = in.readLine();
                if ( !line.contains( MARKER )) {
                    sb.append(line).append("\n");
                    continue;
                }
                String leader = getLeadingWhitespace(line);
                for ( Patient p : pats ) {
                    if ( p.isPaused() ) {
                        continue;
                    }
                    for ( Artikel a : p.getArtikelList() ) {
                        mergeMap.merge( a.getId(), a.getMenge(), Integer::sum );
                    }
                }
                for ( Artikel a : artikelList ) {
                    stringMap.putIfAbsent( a.getId(), a.getReducedArtikelName() );
                }
                TreeMap<Integer, String> sortedMap = new TreeMap<>(stringMap);
                for ( Map.Entry<Integer, String> entry : sortedMap.entrySet() ) {
                    Integer i = mergeMap.get( entry.getKey() );
                    if ( i == null || i == 0 ) {
                        // Menge == 0 -> Eintrag übererspringen
                        continue;
                    }
                    sb.append(leader).append("<tr><td class=\"menge\">");
                    sb.append( mergeMap.get( entry.getKey() )).append("x");
                    sb.append("</td><td class=\"artikel\">");
                    sb.append( entry.getValue() );
                    sb.append("</td></tr>").append("\n");
                }
            }
        }
        makeHTMLFile( target, sb.toString() );
    }

    public void makeFaelligkeitsliste(List<Patient> pats, int listType) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = io.Reader.getTextReader( template )) {
            while ( in.ready() ) {
                String line = in.readLine();
                if ( !line.contains( MARKER )) {
                    sb.append(line).append("\n");
                    continue;
                }
                String leader = getLeadingWhitespace(line);
                for (Patient p : pats) {
                    if ( p.isPaused() ) {
                        continue;
                    }
                    String[] content = null;
                    switch (listType) {
                        case FAELLIGKEITSLISTE:
                            if ( p.isBindingExpiringSoon() || p.isPrescriptionExpiringSoon() ) {
                                content = new String[] {
                                    p.getLastName()  + ", " + p.getFirstName(),
                                    p.getBindingExpiringDate().format(Patient.DEFAULT_FORMATTER),
                                    p.getPrescriptionExpiringDate().format(Patient.DEFAULT_FORMATTER)
                                };
                            }
                            break;
                        case FAELLIGKEITSLISTE_RX:
                            if ( p.isPrescriptionExpiringSoon() ) {
                                content = new String[] {
                                    p.getLastName()  + ", " + p.getFirstName(),
                                    p.getPrescriptionExpiringDate().format(Patient.DEFAULT_FORMATTER)
                                };
                            }
                            break;
                        case FAELLIGKEITSLISTE_BIND:
                            if ( p.isPrescriptionExpiringSoon() ) {
                                content = new String[] {
                                    p.getLastName()  + ", " + p.getFirstName(),
                                    p.getPrescriptionExpiringDate().format(Patient.DEFAULT_FORMATTER)
                                };
                            }
                            break;
                    }
                    if (content != null) {
                        sb.append(leader).append( new TableRow( content, null, false ).toString() );
                    }
                    sb.append("\n");
                }
            }
        }
        makeHTMLFile( target, sb.toString() );
    }

    public void makeLieferliste(List<? extends Patient> pats) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = io.Reader.getTextReader( template )) {
            while ( in.ready() ) {
                String line = in.readLine();
                if ( !line.contains( MARKER )) {
                    sb.append(line);
                    sb.append("\n");
                    continue;
                }
                String leader = getLeadingWhitespace(line);
                String bigleader = "    " + leader;
                List<Patient> list = new ArrayList<>();
                for (Patient p : pats) {
                    if ( !p.isPaused() && p.toDeliver() ) {
                        list.add(p);
                    }
                }
                sb.append(leader).append("<b>Liste 1 / Tour 1:</b>").append("\n");
                sb.append(leader).append("<table>").append("\n");
                sb.append(bigleader).append(HEADER_LIEFERLISTE).append("\n");

                // sortieren nach Ort
                list.sort(( Patient p1, Patient p2 ) ->
                    new Location( p1 ).compareTo( new Location( p2 ))
                );
                // in HTML formatieren
                boolean flag = true;
                String frei = "";
                String artikelList;
                Patient p;
                for ( int i = 0; i < list.size(); ++i ) {
                    artikelList = list.get(i).getArtikelListAsString();
                    p = list.get(i);
                    if ( p.isCoPaymentFree() ) {
                        frei = "X";
                    }
                    String[] content = {
                        String.valueOf(i+1), // Zeilen-Nummerierung
                        frei, // Status der Zuzahlungsbefreiung
                        p.getFullName(),
                        p.getStreet(),
                        p.getCity(),
                        p.getComment(),
                        p.getPhoneNumber(),
                        artikelList.replaceAll(Patient.ARTIKEL_SEPARATOR, "<br>"),
                        "" // leere Spalte für Preis
                    };
                    if ( flag && p.getCityAsLocation().isLocal() ) {
                        sb.append(leader).append("</table>").append("\n");
                        sb.append(leader).append("<br />" ).append("\n");
                        sb.append(leader).append("<b>Liste 2 / Tour 2:</b>").append("\n");
                        sb.append(leader).append("<table>").append("\n");
                        sb.append(bigleader).append(HEADER_LIEFERLISTE).append("\n");
                        flag = false;
                    }
                    sb.append(leader).append(TABLEROW_START);
                    for (int j = 0; j < content.length; ++j) {
                        if ( j == 5 ) { // "Kommentar-Spalte"
                            sb.append("<td class=\"klein\">");
                        } else {
                            sb.append(CELL_START);
                        }
                        sb.append(content[j]);
                        sb.append(CELL_END);
                    }
                    sb.append(TABLEROW_END).append("\n");
                }
                sb.append(leader).append("</table>").append("\n");
            }
        }
        makeHTMLFile( target, sb.toString() );
    }

    public void makePatientenliste(List<Patient> pats) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = io.Reader.getTextReader( template )) {
            while ( in.ready() ) {
                String line = in.readLine();
                if ( !line.contains( MARKER )) {
                    sb.append(line).append("\n");
                    continue;
                }
                String leader = getLeadingWhitespace(line);
                for (Patient p : pats) {
                    if ( p.isPaused() ) {
                        continue;
                    }
                    sb.append(leader).append("<tr><td class=\"small\">");
                    if ( p.isCoPaymentFree() ) {
                        sb.append("X");
                    }
                    sb.append("</td><td class=\"small\">");
                    sb.append( String.valueOf( p.getType().getCode() ).toUpperCase() );
                    sb.append("</td><td class=\"name\">");
                    sb.append( p.getFullName() );
                    sb.append("</td><td></td><td></td><td></td><td></td><td></td><td></td></tr>").append("\n");
                }
            }
        }
        makeHTMLFile( target, sb.toString() );
    }

}
