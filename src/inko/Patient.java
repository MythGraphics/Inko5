/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 6.1.0
 *
 */

import static inko.InkoType.*;
import static inko.PatientField.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Patient implements Comparable<Patient>, HasArtikel {

    // Formatter für die verschiedenen Anwendungsfälle
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter REDUCED_FORMATTER = DateTimeFormatter.ofPattern("MM.yyyy");
    public static final DateTimeFormatter SQL_FORMATTER     = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final LocalDate EOT = LocalDate.of(2999, 12, 31);
    public static final LocalDate DEFAULT_DATE = LocalDate.of(1900, 1, 1);

    public final static String ARTIKEL_SEPARATOR = ", ";

    // Einmalig als statische Konstante definieren (sucht alles zwischen zwei ⚕)
    private final static Pattern TEMPLATE_PATTERN = Pattern.compile("⚕([^⚕]+)⚕");

    private int id                                  = -1;
    private String lastName                         = "";
    private String firstName                        = "";
    private String street                           = "";
    private Integer zipCode                         = 0;
    private String city                             = "";
    private LocalDate birthDate                     = DEFAULT_DATE;
    private Integer healthInsurerIK                 = 0;
    private String insurenceNumber                  = ""; // als String, da sie immer mit einem Buchstaben beginnt
    private String phoneNumber                      = "";
    private String comment                          = "";
    private LocalDate prescriptionDate              = DEFAULT_DATE;
    private LocalDate firstSupplyDate               = DEFAULT_DATE;
    private LocalDate prescriptionExpiringDate      = DEFAULT_DATE;
    private LocalDate bindingExpiringDate           = DEFAULT_DATE;
    private LocalDate coPaymentFreeUntil            = DEFAULT_DATE;
    private InkoType type                           = InkoType.SAUGEND;
    private String besonderheiten                   = "";
    private final ArrayList<Artikel> artikelList    = new ArrayList<>();
    private ArrayList<Integer> artikelIdList        = new ArrayList<>();
    private ArrayList<Integer> mengenList           = new ArrayList<>();
    private boolean deliver                         = false;
    private boolean paused                          = false;
    private boolean modified                        = false; // NUR für DB-Einträge

    public Patient() {}

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return modified;
    }

    public void setComment(String newComment) {
        this.comment = (newComment == null) ? "" : newComment;
    }

    public String getComment() {
        return comment;
    }

    /**
     * Prüft, ob die Bindungserklärung im nächsten Monat ausläuft.
     * @return WAHR oder FALSCH
     */
    public boolean isBindingExpiringSoon() {
        LocalDate threshold = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        return bindingExpiringDate != null && bindingExpiringDate.isBefore(threshold);
    }

    /**
     * Prüft, ob Rezept/Genehmigung im nächsten Monat ausläuft.
     * @return WAHR oder FALSCH
     */
    public boolean isPrescriptionExpiringSoon() {
        LocalDate threshold = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        return prescriptionExpiringDate != null && prescriptionExpiringDate.isBefore(threshold);
    }

    /**
     * Prüft, ob die Bindungserklärung bereits abgelaufen ist.
     * @return WAHR oder FALSCH
     */
    public boolean isBindingExpired() {
        return bindingExpiringDate != null && bindingExpiringDate.isBefore( LocalDate.now() );
    }

    /**
     * Prüft, ob Rezept/Genehmigung bereits abgelaufen ist.
     * @return WAHR oder FALSCH
     */
    public boolean isPrescriptionExpired() {
        return prescriptionExpiringDate != null && prescriptionExpiringDate.isBefore( LocalDate.now() );
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFullName() {
        return lastName + ", " + firstName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public LocalDate getFirstSupplyDate() {
        return firstSupplyDate;
    }

    public LocalDate getPrescriptionExpiringDate() {
        return prescriptionExpiringDate;
    }

    public LocalDate getBindingExpiringDate() {
        return bindingExpiringDate;
    }

    public LocalDate getCoPaymentFreeUntil() {
        return coPaymentFreeUntil;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public boolean isCoPaymentFree() {
        return coPaymentFreeUntil.isAfter( LocalDate.now() );
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public Location getCityAsLocation() {
        return new Location( getCity() );
    }

    public String getInsurenceNumber() {
        return insurenceNumber;
    }

    public String getHealthInsurer() {
        return String.valueOf( InsurenceCompany.getByIK( healthInsurerIK ));
    }

    public int getHealthInsurerIK() {
        return healthInsurerIK;
    }

    public InkoType getType() {
        return type;
    }

    public void setType(InkoType type) {
        this.type = type;
        setModified(true);
    }

    public void setBesonderheiten(String str) {
        this.besonderheiten = str;
    }

    public String getBesonderheiten() {
        return besonderheiten;
    }

    public String getACTK() {
        try {
            InsurenceCompany ic = InsurenceCompany.getByIK(healthInsurerIK);
            if (ic == null) {
                return "";
            }
            String s = "AC/TK ";
            switch (type) {
                case ABLEITEND:
                    return s + InsurenceCompany.ACTK_ABLEITEND[ic.id];
                case SAUGEND:
                    return s + InsurenceCompany.ACTK_SAUGEND[ic.id];
                case SAUGEND_KIND:
                    return s + InsurenceCompany.ACTK_SAUGEND_KIND[ic.id];
                default:
                    return "";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }

    /**
     * Postleitzahl (PLZ)
     * @return PLZ
     */
    public int getZipCode() {
        return zipCode;
    }

    public boolean toDeliver() {
        return deliver;
    }

    public Artikel getArtikel(int id) {
        try {
            return artikelList.get(id);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Artikel> getArtikelList() {
        return artikelList;
    }

    @Override
    public String getArtikelListAsString() {
        return artikelList.stream()
                          .map(Artikel::getFullArtikelString)
                          .collect( Collectors.joining( ARTIKEL_SEPARATOR ));
    }

    /**
     * Aktualisiert <code>artikelIdList</code>, <code>mengenList</code> und setzt <code>modified</code> auf TRUE.
     */
    public void refreshArtikelList() {
        artikelIdList.clear();
        mengenList.clear();
        for ( Artikel a : getArtikelList() ) {
            artikelIdList.add( a.getId() );
            mengenList.add( a.getMenge() );
        }
        setModified(true);
    }

    /**
     * Inititalisiert <code>artikelList</code> aus <code>artikelIdList</code> und <code>mengenList</code>.
     * @param allArtikelList Liste aller verfügbaren Himis
     */
    public void initArtikelList(List<Artikel> allArtikelList) {
        Artikel a;
        for (int j = 0; j < artikelIdList.size(); ++j) {
            for (int i = 0; i < allArtikelList.size(); ++i) {
                if ( allArtikelList.get( i ).getId() == artikelIdList.get( j )) {
                    a = allArtikelList.get(i).clone();
                    a.setMenge( mengenList.get( j ));
                    artikelList.add(a);
                }
            }
        }
    }

    @Override
    public int compareTo(Patient other) {
        if (other == null) {
            return 1;
        }
        return Comparator.comparing(Patient::getLastName)
                         .thenComparing(Patient::getFirstName)
                         .compare(this, other);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ( !( obj instanceof Patient )) {
            return false;
        }
        Patient patient = (Patient) obj;
        return Objects.equals(lastName, patient.lastName)   &&
               Objects.equals(firstName, patient.firstName) &&
               Objects.equals(birthDate, patient.birthDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastName, firstName, birthDate);
    }

    @Override
    public String toString() {
        return getFullName();
    }

    /**
     * Erzeugt ein Patient-Objekt aus dem Datensatz der ADG-Liste (Legacy-Support).
     * @param row Ein Array mit Strings
     * @return Ein initialisiertes Patient-Objekt
     */
    static Patient fromADG(String[] row) {
        Patient p = new Patient();
        try {
            p.set( FAMILIENNAME,    row[0] );
            p.set( VORNAME,         row[1] );
            p.set( STRASSE,         row[2] );
            p.set( PLZ,             row[3] == null ? 0 : Integer.valueOf( row[3] ));
            p.set( ORT,             row[4] );
            p.set( GEBURTSDATUM,    parseDate( row[5] ));
            p.set( KK_IK,           row[6] == null ? 0 : Integer.valueOf( row[6] ));
            p.set( KV_NUMMER,       row[7] );
            p.set( TELEFON,         row[8] );
            p.set( BEFREIUNGSDATUM, parseDate( row[9] ));
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println( e.toString() + ": (" + p.getFullName() + ")" );
        }
        return p;
    }

    /**
     * Hilfsmethode zum sicheren Parsen von Datums-Strings.
     * @param dateStr
     * @return date as LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        if ( dateStr == null || dateStr.isEmpty() ) {
            return DEFAULT_DATE;
        }
        if ( dateStr.contains( "/" )) {
            dateStr = dateStr.replaceAll("/", ".");
        }
        if ( dateStr.contains( "," )) {
            dateStr = dateStr.replaceAll(",", ".");
        }
        try {
            if ( dateStr.matches( "\\d{4}\\-\\d{2}\\-\\d{2}" )) {
                return LocalDate.parse( dateStr, DateTimeFormatter.ofPattern( "yyyy-MM-dd" ));
            }
            if ( dateStr.matches( "\\d{2}\\.\\d{2}\\.\\d{4}" )) {
                return LocalDate.parse( dateStr, DateTimeFormatter.ofPattern( "dd.MM.yyyy" ));
            }
            if ( dateStr.matches( "\\d{2}\\.\\d{4}" )) {
                return LocalDate.parse( dateStr, DateTimeFormatter.ofPattern( "MM.yyyy" ));
            }
            if ( dateStr.matches( "\\d{2}\\.\\d{2}" )) {
                return LocalDate.parse( dateStr, DateTimeFormatter.ofPattern( "MM.yy" ));
            }
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            System.err.println( e.getMessage() );
            System.err.println( "Datumsangabe '" + e.getParsedString() + "' unverständlich" );
            return DEFAULT_DATE;
        }
    }

    public String getRawMengenList() {
        return mengenList.stream()
                         .map(String::valueOf)
                         .collect( Collectors.joining( "," ));
    }

    public String getRawArtikelList() {
        return artikelIdList.stream()
                          .map(String::valueOf)
                          .collect( Collectors.joining( "," ))
        ;
    }

    public void setArtikelIdList(String rawstr) {
        artikelIdList = parseAsList(rawstr);
    }

    public void setMengenList(String rawstr) {
        mengenList = parseAsList(rawstr);
    }

    private static ArrayList<Integer> parseAsList(String str) {
        ArrayList<Integer> list = new ArrayList<>();
        if ( str == null || str.isEmpty() ) {
            return list;
        }
        for ( String token : str.split( ",\\s*" )) {
            list.add( Integer.valueOf( token ));
        }
        return list;
    }

    /**
     * Hilfsmethode, um das PreparedStatement mit Werten aus dem Patient-Objekt zu füllen.
     * Mappt Java-Typen (LocalDate, String, etc.) auf SQL-Typen.
     */
    static int fillPreparedStatement(PreparedStatement pstmt, Patient p, List<PatientField> fields)
    throws SQLException {
        int i = 1;
        for (PatientField field : fields) {
            Object value = p.get(field);

            if (value == null) {
                pstmt.setNull(i, Types.NULL);
            }
            // Datumskonvertierung (LocalDate -> java.sql.Date)
            else if (value instanceof LocalDate) {
                pstmt.setDate( i, java.sql.Date.valueOf( (LocalDate) value ));
            }
            // Patiententyp (Enum -> char/String Code)
            else if (field == PatientField.TYP) {
                pstmt.setString( i, String.valueOf( value ));
            }
            // Integer
            else if (value instanceof Integer) {
                pstmt.setInt( i, (Integer) value );
            }
            // Boolean
            else if (value instanceof Boolean) {
                pstmt.setBoolean( i, (Boolean) value );
            }
            // Fallback für alles andere (Strings)
            else {
                pstmt.setString( i, value.toString() );
            }
            ++i;
        }
        return i;
    }

    static void setStatementParam(PreparedStatement pstmt, int sqlIndex, Patient p, PatientField field)
    throws SQLException {
        Object value = p.get(field);
        if (value == null) {
            pstmt.setNull(sqlIndex, Types.NULL);
            return;
        }
        switch (field) {
            case GEBURTSDATUM:
            case RX_DATUM:
            case ERSTBELIEFERUNG:
            case ENDE_GENEHMIGUNG:
            case ENDE_BINDUNG:
            case BEFREIUNGSDATUM:
                LocalDate ld = (LocalDate) value;
                pstmt.setDate( sqlIndex, java.sql.Date.valueOf( ld ));
                break;
            case ID:
            case KK_IK:
                pstmt.setInt( sqlIndex, (Integer) value );
                break;
            case LIEFERN:
            case PAUSE:
                pstmt.setBoolean( sqlIndex, (Boolean) value );
                break;
            default:
                pstmt.setString( sqlIndex, value.toString() );
                break;
        }
    }

    /**
     * Erzeugt ein Patient-Objekt direkt aus einem ResultSet.
     * @param rs ResultSet
     * @return Datenbank-Eintrag als neues Patient-Objekt
     * @throws SQLException
     */
    public static Patient fromResultSet(ResultSet rs) throws SQLException {
        Patient p = new Patient();

        // Basisfelder
        p.set( ID,                                  rs.getInt(      ID.getDBName()              ));
        p.set( FAMILIENNAME,                        rs.getString(   FAMILIENNAME.getDBName()    ));
        p.set( VORNAME,                             rs.getString(   VORNAME.getDBName()         ));
        p.set( STRASSE,                             rs.getString(   STRASSE.getDBName()         ));
        p.set( PLZ,                                 rs.getInt(      PLZ.getDBName()             ));
        p.set( ORT,                                 rs.getString(   ORT.getDBName()             ));

        // Datumsfelder mit Konvertierung von SQL-Date zu LocalDate
        p.set( GEBURTSDATUM,        toLocalDate(    rs.getDate(     GEBURTSDATUM.getDBName()    )));
        p.set( RX_DATUM,            toLocalDate(    rs.getDate(     RX_DATUM.getDBName()        )));
        p.set( ERSTBELIEFERUNG,     toLocalDate(    rs.getDate(     ERSTBELIEFERUNG.getDBName() )));
        p.set( ENDE_GENEHMIGUNG,    toLocalDate(    rs.getDate(     ENDE_GENEHMIGUNG.getDBName())));
        p.set( ENDE_BINDUNG,        toLocalDate(    rs.getDate(     ENDE_BINDUNG.getDBName()    )));
        p.set( BEFREIUNGSDATUM,     toLocalDate(    rs.getDate(     BEFREIUNGSDATUM.getDBName() )));

        // Sonstige Felder
        p.set( KK_IK,                               rs.getInt(      KK_IK.getDBName()           ));
        p.set( KV_NUMMER,                           rs.getString(   KV_NUMMER.getDBName()       ));
        p.set( TELEFON,                             rs.getString(   TELEFON.getDBName()         ));
        p.setComment(                               rs.getString(   KOMMENTAR.getDBName()       ));
        p.set( LIEFERN,                             rs.getBoolean(  LIEFERN.getDBName()         ));
        p.set( PAUSE,                               rs.getBoolean(  PAUSE.getDBName()           ));

        // Artikel-Listen als Row-Daten
        p.setArtikelIdList(                         rs.getString(   ARTIKELLISTE.getDBName()    ));
        p.setMengenList(                            rs.getString(   ARTIKELMENGE.getDBName()    ));

        String typeCode =                           rs.getString(   TYP.getDBName()             );
        if ( typeCode != null && !typeCode.isEmpty() ) {
            p.setType( InkoType.fromCode( typeCode.charAt(0) ));
        }

        p.setModified(false);
        return p;
    }

    private static LocalDate toLocalDate(java.sql.Date sqlDate) {
        return (sqlDate != null) ? sqlDate.toLocalDate() : DEFAULT_DATE;
    }

    /**
     * Ersetzt alle Platzhalter in einem einzelnen String.
     * @param line Zeile
     * @param p Patient
     * @return Aufgelöster String
     */
    public static String replaceTemplate(String line, Patient p) {
        if ( line == null || !line.contains( "⚕" )) {
            return line;
        }
        StringBuilder sb = new StringBuilder();
        Matcher matcher = TEMPLATE_PATTERN.matcher(line);
        int lastCursor = 0;

        // alle Zeilen durchgehen
        while ( matcher.find() ) {
            // Text vor dem Platzhalter anfügen
            sb.append( line, lastCursor, matcher.start() );
            // Den Platzhalter-Text extrahieren (z.B. "name")
            String tag = matcher.group(1);
            // Den passenden Wert aus dem Patienten-Objekt holen
            String replacement = getReplacementForTag(tag, p);
            // Wert einfügen (null-safe)
            sb.append( replacement != null ? escapeXml( replacement ) : "" );
            lastCursor = matcher.end();
        }
        // Rest der Zeile anfügen
        sb.append( line.substring( lastCursor ));
        return sb.toString();
    }

    /**
     * Sucht basierend auf dem Tag im PatientField-Enum nach dem Wert.
     */
    private static String getReplacementForTag(String tag, Patient p) {
        // Feld suchen, dessen Template-String "⚕" + tag + "⚕" entspricht.
        PatientField field = TAG_MAP.get(tag);
        return (field != null) ? p.getFormattedValue(field) : null;
    }

    /**
     * Sonderzeichen für XML maskieren (z.B. & zu &amp;)
     */
    private static String escapeXml(String str) {
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }

    public void set(PatientField field, Object value) throws NoSuchElementException {
        if (value == null) {
            return;
        }
        // wenn es bereits ein String ist, vorhandene Logik nutzen
        if (value instanceof String) {
            setValueAsString(field, (String) value);
            return;
        }
        switch (field) {
            case ID:                id                          = ((Number) value).intValue();              break;
            case PLZ:               zipCode                     = ((Number) value).intValue();              break;
            case GEBURTSDATUM:      birthDate                   = (LocalDate)           value;              break;
            case KK_IK:             healthInsurerIK             = ((Number) value).intValue();              break;
            case RX_DATUM:          prescriptionDate            = (LocalDate)           value;              break;
            case ERSTBELIEFERUNG:   firstSupplyDate             = (LocalDate)           value;              break;
            case ENDE_GENEHMIGUNG:  prescriptionExpiringDate    = (LocalDate)           value;              break;
            case ENDE_BINDUNG:      bindingExpiringDate         = (LocalDate)           value;              break;
            case BEFREIUNGSDATUM:   coPaymentFreeUntil          = (LocalDate)           value;              break;
            case TYP:               type                        = InkoType.fromCode(((Character) value));   break;
            case LIEFERN:           deliver                     = (Boolean)             value;              break;
            case PAUSE:             paused                      = (Boolean)             value;              break;
            default: throw new NoSuchElementException("Feld \"" + field + "\" unbekannt.");
        }
        setModified(true);
    }

    public void setValueAsString(PatientField field, String value) throws
        NoSuchElementException, NumberFormatException, DateTimeParseException {
        if (value == null) {
            return;
        }
        switch (field) {
            case ID:                id                          = Integer.parseInt(value);                  break;
            case FAMILIENNAME:      lastName                    = value;                                    break;
            case VORNAME:           firstName                   = value;                                    break;
            case STRASSE:           street                      = value;                                    break;
            case PLZ:               zipCode                     = Integer.valueOf(value);                   break;
            case ORT:               city                        = value;                                    break;
            case GEBURTSDATUM:      birthDate                   = LocalDate.parse(value);                   break;
            case KK_IK:             healthInsurerIK             = Integer.valueOf(value);                   break;
            case KV_NUMMER:         insurenceNumber             = value;                                    break;
            case TELEFON:           phoneNumber                 = value;                                    break;
            case KOMMENTAR:         comment                     = value;                                    break;
            case RX_DATUM:          prescriptionDate            = LocalDate.parse(value);                   break;
            case ERSTBELIEFERUNG:   firstSupplyDate             = LocalDate.parse(value);                   break;
            case ENDE_GENEHMIGUNG:  prescriptionExpiringDate    = LocalDate.parse(value);                   break;
            case ENDE_BINDUNG:      bindingExpiringDate         = LocalDate.parse(value);                   break;
            case LIEFERN:           deliver                     = Boolean.parseBoolean(value);              break;
            case PAUSE:             paused                      = Boolean.parseBoolean(value);              break;
            case BEFREIUNGSDATUM:   coPaymentFreeUntil          = LocalDate.parse(value);                   break;
            case TYP:               type                        = InkoType.fromCode( value.charAt( 0 ));    break;
            case ARTIKELMENGE:      setMengenList(value);                                                   break;
            case ARTIKELLISTE:      setArtikelIdList(value);                                                break;
            case BESONDERHEITEN:    besonderheiten              = value;                                    break;
            default: throw new NoSuchElementException("Feld \"" + field + "\" unbekannt.");
        }
        setModified(true);
    }

    public String getValue(PatientField field) {
        return get(field).toString();
    }

    public String getFormattedValue(PatientField field) {
        Object obj = get(field);
        if (obj instanceof LocalDate) {
            return ((ChronoLocalDate) obj).format(DEFAULT_FORMATTER);
        }
        return getValue(field);
    }

    public Object get(PatientField field) {
        switch (field) {
            case ID:                return id;
            case FAMILIENNAME:      return lastName;
            case VORNAME:           return firstName;
            case STRASSE:           return street;
            case PLZ:               return zipCode;
            case ORT:               return city;
            case GEBURTSDATUM:      return birthDate;
            case KK_IK:             return healthInsurerIK;
            case KV_NUMMER:         return insurenceNumber;
            case TELEFON:           return phoneNumber;
            case KOMMENTAR:         return comment;
            case RX_DATUM:          return prescriptionDate;
            case ERSTBELIEFERUNG:   return firstSupplyDate;
            case ENDE_GENEHMIGUNG:  return prescriptionExpiringDate;
            case ENDE_BINDUNG:      return bindingExpiringDate;
            case LIEFERN:           return deliver;
            case PAUSE:             return paused;
            case BEFREIUNGSDATUM:   return coPaymentFreeUntil;
            case TYP:               return type.getCode();
            case ARTIKELMENGE:      return getRawMengenList();
            case ARTIKELLISTE:      return getRawArtikelList();
            case BESONDERHEITEN:    return getBesonderheiten();
            case ACTK:              return getACTK();
            case KK_NAME:           return getHealthInsurer();
            case TYPE_LABEL:        return type.getLabel();
            case HIMI:              return getArtikelListAsString();
            default:                return null;
        }
    }

}
