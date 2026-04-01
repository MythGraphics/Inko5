/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.2
 *
 */

public enum ArtikelType {

    KEINER      (""),
    KLETTIS     ("Klettis"),
    SLIPS       ("Slips/Mobihosen"),
    VORLAGEN    ("Vorlagen"),
    KOMMENTAR   ("");

    private final String name;

    ArtikelType(String name) {
        this.name = name;
    }

    public String getFullName() {
        return name;
    }

    // toString() absichtlich nicht überschrieben, da für die Datenbank this.name() besser ist.

    public static ArtikelType parseType(String s) {
        if ( s == null || s.isEmpty() ) {
            return KEINER;
        }
        for ( ArtikelType aType : ArtikelType.values() ) {
            if ( s.equalsIgnoreCase( aType.name() )) {
                return aType;
            }
        }
        return KEINER;
    }

}
