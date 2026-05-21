package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 3.0.0
 *
 */

import static inko.InkoType.*;

/*
 * A  - ableitend
 * S  - saugend
 * KS - saugend (Kind)
 */

public enum InsurenceCompany {

    UNKNOWN         ("Unbekannt",           000000000),
    AOK_SA          ("AOK Sachsen-Anhalt",  101097008),
    IKK_GESUND_PLUS ("IKK gesund plus",     101202961),
    BARMER          ("Barmer",              100980006);

    public final static String NO_DATA = "keine Daten";

    public final static String[] INFO_ABLEITEND = {
        NO_DATA,
        "kein Vertrag - Lieferausschluss!",
        "kein Vertrag - Lieferausschluss!",
        "Verordnung für max. 3 Monate; max. 8 St./Monat\nVWKZ 00 Erstversorgung, 04 Folgeversorgung"
    };
    public final static String[] INFO_SAUGEND = {
        NO_DATA,
        "Dauerverordnung: max. 24 Monate,\nHiMi-Nr. Pauschale: 1500001000\nPreis: 23,30€",
        "Dauerverordnung: max. 12 Monate,\nHiMi-Nr. Pauschale: 1599993008\nPreis: 23,30€",
        "kein Vertrag - Lieferausschluss!"
    };
    public final static String[] INFO_SAUGEND_KIND = {
        NO_DATA,
        "4-12 Jahre,\nDauerverordnung: max. 24 Monate,\nHiMi-Nr. Pauschale: 1500001005\nPreis: 35,00€",
        "4-12 Jahre,\nDauerverordnung: max. 12 Monate,\nHiMi-Nr. Pauschale: 1599993010\nPreis: 35,00€",
        "kein Vertrag - Lieferausschluss!"
    };
    public final static String ACTK = "AC/TK ";
    public final static String[] ACTK_ABLEITEND = {
        "",
        "1514327",
        "1991716",
        "-"
    };
    public final static String[] ACTK_SAUGEND = {
        "",
        "1114322",
        "1514742",
        ""
    };
    public final static String[] ACTK_SAUGEND_KIND = ACTK_SAUGEND;

    final String name;
    final int ik;

    InsurenceCompany(String name, int ik) {
        this.name = name;
        this.ik = ik;
    }

    public int getIndex() {
        return ordinal();
    }

    public String getName() {
        return name;
    }

    public int getIk() {
        return ik;
    }

    public String getACTK(InkoType type) {
        switch (type) {
            case ABLEITEND:
                return ACTK + ACTK_ABLEITEND[ordinal()];
            case SAUGEND:
                return ACTK + ACTK_SAUGEND[ordinal()];
            case SAUGEND_KIND:
                return ACTK + ACTK_SAUGEND_KIND[ordinal()];
        }
        return NO_DATA;
    }

    public String getInfo(InkoType type) {
        switch (type) {
            case ABLEITEND:
                return INFO_ABLEITEND[ordinal()];
            case SAUGEND:
                return INFO_SAUGEND[ordinal()];
            case SAUGEND_KIND:
                return INFO_SAUGEND_KIND[ordinal()];
        }
        return NO_DATA;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Gibt die Krankenkasse anhand des gegebenen IK zurück.
     *
     * @param   ik  Institutionskennzeichen
     * @return  Krankenkasse
     */
    public final static InsurenceCompany getByIK(int ik) {
        for ( InsurenceCompany ic : values() ) {
            if ( ik == ic.getIk() ) {
                return ic;
            }
        }
        return UNKNOWN;
    }

    /**
     * Gibt die Krankenkasse anhand des gegebenen Namens zurück.
     *
     * @param   name    Name der Krankenkasse
     * @return  Krankenkasse
     */
    public final static InsurenceCompany getByName(String name) {
        if ( name == null || name.isEmpty() ) {
            return UNKNOWN;
        }
        for ( InsurenceCompany ic : values() ) {
            if ( name.equalsIgnoreCase( ic.getName() )) {
                return ic;
            }
        }
        return UNKNOWN;
    }

}
