/*
 *
 */

package inko;


/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 2.0.2
 *
 */

import static inko.ArtikelField.*;
import static inko.ArtikelType.*;
import static inko.Size.KEINE;
import static inko.Size.parseSize;
import java.util.Objects;
import java.util.StringTokenizer;

public class Artikel implements Comparable, Cloneable {

    private Size size = KEINE;
    private ArtikelType type = KEINER;
    private String name = "[unbenannt]";
    private int id = -1;
    private int packQuantity = 0;
    private int pzn = 0;
    private int menge = 0;

    private Artikel(int id, int menge, String name, Size size, ArtikelType type, int packQuantity, int pzn) {
        this.id             = id;
        this.menge          = menge;
        this.name           = name;
        this.size           = size;
        this.type           = type;
        this.packQuantity   = packQuantity;
        this.pzn            = pzn;
    }

    public Artikel(int menge, String name, Size size, ArtikelType type, int packQuantity, int pzn) {
        this(-1, menge, name, size, type, packQuantity, pzn);
    }

    public Artikel(String name, Size size, ArtikelType type, int packQuantity, int pzn) {
        this(-1, 1, name, size, type, packQuantity, pzn);
    }

    public Artikel(String name, int packQuantity, int pzn) {
        this(-1, 1, name, KEINE, KEINER, packQuantity, pzn);
    }

    public Artikel(String name, int pzn) {
        // über diesen Konstruktor wird ID=PZN gesetzt
        this(pzn, 1, name, KEINE, KEINER, 1, pzn);
    }

    public Artikel() {}

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Size getSize() {
        return size;
    }

    public void setType(ArtikelType type) {
        this.type = type;
    }

    public ArtikelType getType() {
        return type;
    }

    public String getSizeString() {
        if ( size == null || size == KEINE ) {
            return "";
        } else {
            return size.toString();
        }
    }

    /**
     * Gibt die Artikelbezeichnung ohne Mengenangabe zurück.
     * @return Bezeichnung des Hilfsmittels
     */
    public String getReducedArtikelName() {
        if (type == KOMMENTAR) {
            return name;
        }
        return name + " " + getSizeString() + " " + packQuantity + " St. " + type.getFullName();
    }

    /**
     * Gibt die Artikelbezeichnung mit Mengenangabe zurück.
     * @return Bezeichnung des Hilfsmittels
     */
    public String getFullArtikelName() {
        if (type == KOMMENTAR) {
            return name;
        }
        return menge + "x " + getReducedArtikelName();
    }

    /**
     * Gibt die Artikelbezeichnung mit Menge und PZN zurück.
     * @return Bezeichnung des Artikels
     */
    public String getFullArtikelString() {
        return getFullArtikelName() + getPznString();
    }

    /**
     * Gibt die Artikelbezeichnung ohne Mengenangabe und mit PZN zurück.
     * @return Bezeichnung des Artikels
     */
    public String getReducedArtikelString() {
        return getReducedArtikelName() + getPznString();
    }

    private String getPznString() {
        if (type == KOMMENTAR) {
            return "";
        }
        return " (" + pzn + ")";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPackQuantity() {
        return packQuantity;
    }

    public void setPackQuantity(int packQuantity) {
        this.packQuantity = packQuantity;
    }

    public int getPZN() {
        return pzn;
    }

    public void setPZN(int pzn) {
        this.pzn = pzn;
    }

    public void setMenge(int menge) {
        this.menge = menge;
    }

    public int getMenge() {
        return menge;
    }

    /**
     * Gibt die Artikelbezeichnung ohne Menge mit PZN zurück.
     * Dient der Ausgabe innerhalb der GUI.
     * @return Vollstäbdige Bezeichnung des Artikels
     */
    @Override
    public String toString() {
        return getReducedArtikelString();
    }

    @Override
    public Artikel clone() {
        try { super.clone(); } catch (CloneNotSupportedException e) {}
        Artikel h = new Artikel(menge, name, size, type, packQuantity, pzn);
        h.setId( getId() );
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ( obj == null || getClass() != obj.getClass() ) {
            return false;
        }
        Artikel himi = (Artikel) obj;
        return pzn == himi.pzn; // Vergleich basierend auf der PZN
    }

    @Override
    public int hashCode() {
        return Objects.hash(pzn);
    }

    public void set(ArtikelField field, Object value) {
        if (value == null) {
            return;
        }
        switch (field) {
            case ID:            id              = ((Number) value).intValue();  break;
            case NAME:          name            = (String) value;               break;
            case PZN:           pzn             = ((Number) value).intValue();  break;
            case SIZE:          size            = parseSize((String) value);    break;
            case PACK_QUANTITY: packQuantity    = ((Number) value).intValue();  break;
            case TYPE:          type            = parseType((String) value);    break;
        }
    }

    public Object get(ArtikelField field) {
        if (field == null) {
            return null;
        }
        switch (field) {
            case ID:            return getId();
            case NAME:          return getName();
            case PZN:           return getPZN();
            case SIZE:          return getSize();
            case PACK_QUANTITY: return getPackQuantity();
            case TYPE:          return getType();
            default: return null;
        }
    }

    public static int parseUnsignedInt(String str) {
        int i = 0;
        for (
            int pointer = 0;
            pointer < str.length() && Character.isDigit( str.charAt( pointer ));
            ++pointer
        ) {
            i = 10*i + Character.getNumericValue( str.charAt( pointer ));
        }
        return i;
    }

    /**
     * Sortiert alphabetisch nach Artikel-Name.
     * @param o Ein Artikel-Objekt, mit dem verglichen werden soll.
     * @return -1 wenn kleiner, 0 wenn gleich, +1 wenn größer
     */
    @Override
    public int compareTo(Object o) {
        Artikel himi2 = (Artikel) o;
        return getReducedArtikelName().compareTo( himi2.getReducedArtikelName() );
    }

    public static Artikel parseArtikelString(String str) {
        int menge = 0, packQuantity = 0, pzn = 0;
        Size size = null;
        StringTokenizer t;
        try {
            t = new StringTokenizer(str, " .x", false);
        } catch (NullPointerException e) {
            return new Artikel();
        }
        StringBuilder sb = new StringBuilder();
        String tmp;
        while ( t.hasMoreTokens() ) {
            tmp = t.nextToken();
            if ( menge == 0 && Character.isDigit( tmp.charAt( 0 ))) {
                menge = parseUnsignedInt(tmp);
                continue;
            }
            if ( packQuantity == 0 && Character.isDigit( tmp.charAt( 0 ))) {
                packQuantity = parseUnsignedInt(tmp);
                continue;
            }
            if ( tmp.length() <= 2 ) {
                size = parseSize(tmp);
                continue;
            }
            sb.append(tmp);
            sb.append(" ");
        }
        String name = sb.toString();
        if ( name.contains( "(" )) {
            pzn = Integer.parseUnsignedInt(
                str.substring( str.indexOf( '(' )+1, str.indexOf( ')' ))
            );
            name = name.substring( 0, name.indexOf( '(' ));
        }
        name = name.trim();
        return new Artikel( menge, name, size, KEINER, packQuantity, pzn );
    }

}
