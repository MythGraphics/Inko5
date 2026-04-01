/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 2.5.3
 *
 */

import java.util.ArrayList;
import java.util.Arrays;

public class Location implements Comparable<Location> {

    public static ArrayList<String> AUSWÄRTS = new ArrayList<>( Arrays.asList( // Tour1; wird nicht verwendet
        "Schwiesau",
        "Kakerbeck",
        "Trippigleben",
        "Köckte",
        "Dannefeld",
        "Kusey",
        "Kunrau",
        "Wendze",
        "Neuferchau",
        "Jahrstedt",
        "Apenburg",
        "Steimke"
    ));
    public static ArrayList<String> ORTE = new ArrayList<>( Arrays.asList( // Tour2
        "Klötze",
        "Poppau",
        "Beetzendorf",
        "Nesenitz",
        "Rohrberg",
        "Siedengrieben",
        "Hohentramm",
        "Jüber",
        "Jübar"
    ));

    private final String loc;
    private final int loci;

    public Location(String s) {
        this.loc = cleanString(s);
        loci = getLocI(loc);
    }

    public Location(Patient p) {
        this( p.getCity() );
    }

    private int getLocI(String s) {
        for (int i = 0; i < ORTE.size(); ++i) {
            if ( s.contains( ORTE.get( i ))) {
                return i;
            }
        }
        return ORTE.size();
    }

    private static String cleanString(String s) {
        if ( s.contains("OT") ) {
            s = s.substring( s.indexOf("OT")+3, s.length() ); // entfernt "OT" & nachfolgendes Leerzeichen
        }
        return s;
    }

    /**
     * Returns the relativ position of these Location for comparison.
     * @return The relativ postion as int.
     */
    public int getLocI() {
        return loci;
    }

    /**
     * Prüft, ob diese Location zu "local" (Tour 2) gehört.
     * @return TRUE oder FALSE
     */
    public boolean isLocal() {
        return ( loci < ORTE.size() );
    }

    /**
     * Prüft, ob diese Location zu "LOCAL" (Tour 2) gehört.
     * Methode entfernt ein etwaiges Ortsteil-Segment
     * @param s Prüf-String
     * @return TRUE oder FALSE
     */
    public static boolean isLocal(String s) {
        s = cleanString(s);
        for (String x : ORTE) {
            if ( s.contains(x) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Liefert einen Rückgabewert, der auswärtige Orte <b>vor lokale Orte</b> sortiert.
     * Gehören beide Orte zu LOCAL oder sind unbekannt, ist die Rückgabe 0.
     * Die Reihenfolge in LOCAL ist für den Vergleich <b>nicht</b> relevant.
     * @param l Vergleichs-Location
     * @return Integer zwischen -1 und +1
     */
    @Override
    public int compareTo(Location l) {
        if ( l == null) { throw new NullPointerException(); }
        int x = 0;
        if ( !isLocal() && l.isLocal() ) {
            x = -1;
        } else if ( isLocal() && !l.isLocal() ) {
            x = 1;
        }
        return x;
    }

    @Override
    public String toString() {
        return loc;
    }

}
