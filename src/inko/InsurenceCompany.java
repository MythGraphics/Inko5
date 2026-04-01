package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 2.0.0
 *
 */

/*
 * A  - ableitend
 * S  - saugend
 * KS - saugend (Kind)
 */

public enum InsurenceCompany {

    UNKNOWN(        0, "Unbekannt",          000000000),
    AOK(            1, "AOK Sachsen-Anhalt", 101097008),
    IKK_GESUND_PLUS(2, "IKK gesund plus",    101202961),
    BARMER(         3, "Barmer",             100980006);

    public final static String[] INFO_ABLEITEND = {
        "",
        "kein Vertrag - Lieferausschluss!",
        "kein Vertrag - Lieferausschluss!",
        "Verordnung für max. 3 Monate; max. 8 St./Monat\nVWKZ 00 Erstversorgung, 04 Folgeversorgung"
    };
    public final static String[] INFO_SAUGEND = {
        "",
        "Dauerverordnung: max. 24 Monate,\nHiMi-Nr. Pauschale: 1500001000\nPreis: 23,30€",
        "Dauerverordnung: max. 12 Monate,\nHiMi-Nr. Pauschale: 1599993008\nPreis: 23,30€",
        "kein Vertrag - Lieferausschluss!"
    };
    public final static String[] INFO_SAUGEND_KIND = {
        "",
        "4-12 Jahre,\nDauerverordnung: max. 24 Monate,\nHiMi-Nr. Pauschale: 1500001005\nPreis: 35,00€",
        "4-12 Jahre,\nDauerverordnung: max. 12 Monate,\nHiMi-Nr. Pauschale: 1599993010\nPreis: 35,00€",
        "kein Vertrag - Lieferausschluss!"
    };
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

    final int id;
    final String name;
    final int ik;

    InsurenceCompany(int id, String name, int ik) {
        this.id = id;
        this.name = name;
        this.ik = ik;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIk() {
        return ik;
    }

    @Override
    public String toString() {
        return name;
    }

    public static InsurenceCompany fromId(int id) {
        for ( InsurenceCompany ic : values() ) {
            if (ic.id == id) {
                return ic;
            }
        }
        return UNKNOWN;
    }

    /**
     * Gibt die Krankenkasse anhand des gegebenen IK zurück.
     *
     * @param   ik  Institutionskennzeichen
     * @return  Krankenkasse
     */
    public final static InsurenceCompany getByIK(int ik) {
        for ( InsurenceCompany ic : InsurenceCompany.values() ) {
            if ( ik == ic.getIk() ) {
                return ic;
            }
        }
        return null;
    }

    /**
     * Gibt die Krankenkasse anhand des gegebenen Namens zurück.
     *
     * @param   name    Name der Krankenkasse
     * @return  Krankenkasse
     */
    public final static InsurenceCompany getByName(String name) {
        if ( name == null || name.isEmpty() ) {
            return null;
        }
        for ( InsurenceCompany ic : InsurenceCompany.values() ) {
            if ( name.equalsIgnoreCase( ic.getName() )) {
                return ic;
            }
        }
        return null;
    }

}
