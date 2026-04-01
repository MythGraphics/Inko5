/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.1.0
 *
 */

public enum ParameterEnum {
    ID                      ("ID", "⚕nr⚕"),
    FAMILIENNAME            ("Familienname", "⚕name⚕"),
    VORNAME                 ("Vorname", "⚕vorname⚕"),
    STRASSE                 ("Straße", "⚕straße⚕"),
    PLZ                     ("PLZ", "⚕plz⚕"),
    WOHNORT                 ("Wohnort", "⚕ort⚕"),
    GEBURTSDATUM            ("Geburtsdatum", "⚕gdatum⚕"),
    KRANKENKASSE            ("Krankenkasse (IK)", "⚕kk⚕"),
    KVNUMMER                ("KV-Nummer", "⚕kvn⚕"),
    TELENUMMER              ("Telefon-Nummer", "⚕tel⚕"),
    KOMMENTAR               ("Kommentar", "⚕kommentar⚕"),
    RXDATUM                 ("Rezeptdatum", "⚕rxdatum⚕"),
    ERSTBELIEFERUNG         ("Erstbelieferung", "⚕erstbelieferung⚕"),
    GENEHMIGUNGSZEITRAUM    ("Genehmigungszeitraum", "⚕gzeit⚕"),
    BINDUNGSZEITRAUM        ("Bindungszeitraum", "⚕bzeit⚕"),
    LIEFERN                 ("liefern", "⚕liefern⚕"),
    FREI                    ("frei", "⚕frei⚕"),
    TYPE                    ("Typ", "⚕typ⚕"),

    HIMI_MENGEN_LIST        ("Mengenliste", "⚕mengen_liste⚕"),
    HIMI_ARIKEL_LIST        ("Artikelliste", "⚕artikel_liste⚕"),
    HIMI_LIST               ("Hilfsmittelliste", "⚕hm⚕"),

    BESONDERHEITEN          ("Besonderheiten", "⚕besonderheiten⚕"),
    ACTK                    ("ACTK", "⚕actk⚕"),
    KRANKENKASSENNAME       ("Krankenkassenname", "⚕kk_name⚕"),
    TYPE_STRING             ("Typ (String)", "⚕typ_str⚕");

    final String name;
    final String template_identifier;

    public final static char TEMPLATE_MODIFIER = '⚕';

    ParameterEnum(String name, String template_identifier) {
        this.name = name;
        this.template_identifier = template_identifier;
    }

    public String getName() {
        return name;
    }

    public String getTemplateIdentifier() {
        return template_identifier;
    }

}
