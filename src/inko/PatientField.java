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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Definiert alle verfügbaren Felder eines Patienten inklusive ihrer
 * SQL-Spaltennamen, UI-Bezeichner (deutsche Bezeichnung), Datentypen und Dokument-Templates.
 */
public enum PatientField {

    // --- DATENBANK FELDER ---
    ID("id", "int unsigned not null auto_increment primary key", "ID", Integer.class, "⚕nr⚕"),
    FAMILIENNAME("last_name", "tinytext", "Familienname", String.class, "⚕name⚕"),
    VORNAME("first_name", "tinytext", "Vorname", String.class, "⚕vorname⚕"),
    STRASSE("street", "tinytext", "Straße", String.class, "⚕straße⚕"),
    PLZ("postcode", "int(5) unsigned", "PLZ", String.class, "⚕plz⚕"),
    ORT("city", "tinytext", "Wohnort", String.class, "⚕ort⚕"),
    GEBURTSDATUM("birthday", "date", "Geburtsdatum", LocalDate.class, "⚕gdatum⚕"),
    KK_IK("kk_ik", "int(9) unsigned", "IK Krankenkassen", Integer.class, "⚕kk⚕"),
    KV_NUMMER("kv_number", "tinytext", "KV-Nummer", String.class, "⚕kvn⚕"),
    TELEFON("telefon", "tinytext", "Telefon-Nummer", String.class, "⚕tel⚕"),
    KOMMENTAR("comment", "text", "Kommentar", String.class, "⚕kommentar⚕"),
    RX_DATUM("rx_date", "date", "Rezeptdatum", LocalDate.class, "⚕rxdatum⚕"),
    ERSTBELIEFERUNG("erstbelieferung", "date", "Erstbelieferung", LocalDate.class, "⚕erstbelieferung⚕"),
    ENDE_GENEHMIGUNG("ende_genehmigung", "date", "Ende Genehmigungszeitraum", LocalDate.class, "⚕gzeit⚕"),
    ENDE_BINDUNG("ende_bindung", "date", "Ende Bindungszeitraum", LocalDate.class, "⚕bzeit⚕"),
    LIEFERN("liefern", "boolean", "Liefern?", Boolean.class, "⚕liefern⚕"),
    BEFREIUNGSDATUM("befreiungsdatum", "date", "befreit bis", LocalDate.class, "⚕frei⚕"),
    TYP("typ", "character(1)", "Typ-Zeichen", String.class, "⚕typ⚕"), // PatientType.getCode()
    ARTIKELMENGE("artikelmenge", "tinytext", "Artikelmengen", String.class, null), // Liste wird als Strings in DB gespeichert
    ARTIKELLISTE("artikelliste", "tinytext", "Artikelliste", String.class, null), // Liste wird als Strings in DB gespeichert
    PAUSE("pause", "boolean", "Patient pausiert?", Boolean.class, "⚕pause⚕"),

    // --- TEMPLATE FELDER (existieren so nicht in der DB) ---
    BESONDERHEITEN(null, null, "Besonderheiten", String.class, "⚕besonderheiten⚕"),
    ACTK(null, null, "ACTK", String.class, "⚕actk⚕"),
    KK_NAME(null, null, "Krankenkasse", String.class, "⚕kk_name⚕"),
    TYPE_LABEL(null, null, "Typ", String.class, "⚕typ_str⚕"), // PatientType.getLabel()
    HIMI(null, null, "Hilfsmittel", String.class, "⚕hm⚕"); // Himi-String via getHimiListAsString()

    // Performance-Optimierung: Statische Listen für Java 8
    public final static List<PatientField> DB_FIELDS;
    public final static List<PatientField> INSERT_FIELDS;
    public final static List<PatientField> UI_FIELDS;
    public final static List<PatientField> SHORT_DATE_FIELDS;
    public final static List<PatientField> ADG_FIELDS;
    public final static String UI_FIELD_STRING;
    public final static String INSERT_COLUMNS;
    public final static String INSERT_PLACEHOLDERS;
    public final static Map<String, PatientField> TAG_MAP =
        Arrays.stream( PatientField.values() )
              .filter( f -> f.getTemplate() != null )
              .collect( Collectors.toMap(
                  f -> f.getTemplate().replace("⚕", ""), // "⚕name⚕" -> "name"
                  f -> f
    ));

    static {
        // alle Felder, die eine Spalte in der DB haben
        DB_FIELDS = Collections.unmodifiableList(
            Arrays.stream( values() )
                  .filter(f -> f.dbName != null)
                  .collect( Collectors.toList() )
        );
        // alle DB-Felder außer ID (für Insert)
        INSERT_FIELDS = Collections.unmodifiableList(
            DB_FIELDS.stream()
                     .filter(f -> f != ID)
                     .collect( Collectors.toList() )
        );
        // alle Felder der UI-Tabelle, die das verkürzte Datum (MM.JJJJ) anzeigen sollen
        SHORT_DATE_FIELDS = Collections.unmodifiableList( Arrays.asList(
            ENDE_GENEHMIGUNG,
            ENDE_BINDUNG,
            BEFREIUNGSDATUM
        ));
        // alle DB-Felder der UI-Tabelle
        UI_FIELDS = Collections.unmodifiableList( Arrays.asList(
            ID,
            FAMILIENNAME,
            VORNAME,
            STRASSE,
            PLZ,
            ORT,
            GEBURTSDATUM,
            KK_IK,
            KV_NUMMER,
            TELEFON,
            KOMMENTAR,
            RX_DATUM,
            ERSTBELIEFERUNG,
            ENDE_GENEHMIGUNG,
            ENDE_BINDUNG
        ));
        ADG_FIELDS = Collections.unmodifiableList( Arrays.asList(
            FAMILIENNAME,
            VORNAME,
            STRASSE,
            PLZ,
            ORT,
            GEBURTSDATUM,
            KK_IK,
            KV_NUMMER,
            TELEFON,
            BEFREIUNGSDATUM
        ));

        UI_FIELD_STRING = INSERT_FIELDS.stream()
            .map(f -> f.uiName)
            .collect( Collectors.joining( "\n" ));

        INSERT_COLUMNS = INSERT_FIELDS.stream()
            .map(f -> f.dbName)
            .collect( Collectors.joining( ", " ));

        INSERT_PLACEHOLDERS = INSERT_FIELDS.stream()
            .map(f -> "?")
            .collect( Collectors.joining( ", " ));
    }

    private final String dbName;
    private final String dbType;
    private final String uiName;
    private final Class<?> type;
    private final String template;

    PatientField(String dbName, String dbType, String uiName, Class<?> type, String template) {
        this.dbName = dbName;
        this.dbType = dbType;
        this.uiName = uiName;
        this.type = type;
        this.template = template;
    }

    public String getDBName() {
        return dbName;
    }

    public String getDBType() {
        return dbType;
    }

    public String getUIName() {
        return uiName;
    }

    public Class<?> getType() {
        return type;
    }

    public String getTemplate() {
        return template;
    }

}
