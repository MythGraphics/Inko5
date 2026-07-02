package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 3.0.0
 *
 */

import static inko.PatientField.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.*;
import static java.sql.Types.BLOB;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import net.Login;

public class DBio extends SQLConnection {

    public final static String DB               = "inkodb";
    public final static String TABLE_PATIENT    = "patienten";
    public final static String TABLE_ARTIKEL    = "artikel";
    public final static String TABLE_SIGNATURE  = "signature";
    public final static String TABLE_APP        = "inkoapp";
    public final static String[] SQL_APP_FIELD  = {"orte", "artikelpass", "sign_apo"};
    public final static String[] SQL_APP_TYPE   = {"text", "text", "mediumblob"};

    private List<Artikel> artikelCache;

    public DBio(String host, int port, Login login) {
        super(host, port, login, DB);
    }

    public int savePatient(Patient p) {
        if ( p.getId() == -1 ) {
            return insertPatient(p);
        } else {
            return updatePatient(p);
        }
    }

    public static ArrayList<String> parseString(String str) {
        Scanner scanner = new Scanner(str);
        ArrayList<String> list = new ArrayList<>();
        while ( scanner.hasNext() ) {
            list.add( scanner.next() );
        }
        return list;
    }

    public ArrayList<String> getOrte() {
        String sql = "SELECT " + SQL_APP_FIELD[0] + " FROM " + TABLE_APP;
        try ( Statement stmt = getConnection().createStatement();
              ResultSet result = stmt.executeQuery(sql)
            ) {
            if ( result.next() ) {
                return parseString( result.getString( SQL_APP_FIELD[0] ));
            }
        } catch (SQLException e) {
            exHandling(e);
        }
        return new ArrayList<>();
    }

    public char[] getArtikelPasswordHash() {
        String sql = "SELECT " + SQL_APP_FIELD[1] + " FROM " + TABLE_APP;
        try ( Statement stmt = getConnection().createStatement();
              ResultSet result = stmt.executeQuery(sql)
            ) {
            if ( result.next() ) {
                return result.getString( SQL_APP_FIELD[1] ).toCharArray();
            }
        } catch (SQLException e) {
            exHandling(e);
        }
        return new char[]{0};
    }

    public int insertArtikel(Artikel artikel) {
        String sql = "INSERT INTO " + TABLE_ARTIKEL+ " (" + ArtikelField.INSERT_COLUMNS + ") " +
                     "VALUES (" + ArtikelField.INSERT_PLACEHOLDERS + ")";
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql, Statement.RETURN_GENERATED_KEYS )) {
            pstmt.setString( 1, artikel.getName() );
            pstmt.setInt(    2, artikel.getPZN() );
            pstmt.setString( 3, artikel.getSize().name() );
            pstmt.setInt(    4, artikel.getPackQuantity() );
            pstmt.setString( 5, artikel.getType().name() );
            int affected = pstmt.executeUpdate();
            // vom Server vergebene ID setzen
            try ( ResultSet rs = pstmt.getGeneratedKeys() ) {
                if ( rs.next() ) {
                    artikel.setId( rs.getInt( 1 ));
                }
            }
            return affected;
        } catch (SQLException e) {
            exHandling(e);
            return 0;
        }
        finally {
            deleteArtikelCache();
        }
    }

    public Artikel getArtikelById(int id) {
        String sql = "SELECT * FROM " + TABLE_ARTIKEL + " WHERE id = ?";
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            pstmt.setInt(1, id);
            try ( ResultSet rs = pstmt.executeQuery() ) {
                if ( rs.next() ) {
                    return loadArtikel(rs);
                }
            }
        } catch (SQLException e) {
            exHandling(e);
        }
        return null;
    }

    private Artikel loadArtikel(ResultSet rs) throws SQLException {
        Artikel himi = new Artikel();
        for ( ArtikelField field : ArtikelField.values() ) {
            String colName = field.getDBName();
            // prüfen, ob die Spalte überhaupt im ResultSet vorhanden ist
            Object value = rs.getObject(colName);
            if (value == null) {
                continue; // Feld bleibt beim Default-Wert
            }
            himi.set(field, value);
        }
        return himi;
    }

    public int updateArtikel(Artikel artikel) {
        int affected = 0;
        String sql = "UPDATE " + TABLE_ARTIKEL + " SET " +
                     ArtikelField.NAME.getDBName()             + "= ?, " +
                     ArtikelField.PZN.getDBName()              + "= ?, " +
                     ArtikelField.SIZE.getDBName()             + "= ?, " +
                     ArtikelField.PACK_QUANTITY.getDBName()    + "= ?, " +
                     ArtikelField.TYPE.getDBName()             + "= ? " +
                     "WHERE " + ArtikelField.ID.getDBName()    + "= ?";

        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            pstmt.setString(1, artikel.getName() );
            pstmt.setInt(   2, artikel.getPZN() );
            pstmt.setString(3, artikel.getSize().name() );
            pstmt.setInt(   4, artikel.getPackQuantity() );
            pstmt.setString(5, artikel.getType().name() );
            pstmt.setInt(   6, artikel.getId() );
            affected = pstmt.executeUpdate();
        } catch (SQLException e) {
            exHandling(e);
        }
        updateHimiCache(artikel);
        return affected;
    }

    private void updateHimiCache(Artikel artikel) {
        if (artikelCache == null) {
            return;
        }
        artikelCache.stream()
                    .filter( h -> h.getId() == artikel.getId() )
                    .findFirst()
                    .ifPresent( h -> artikelCache.set( artikelCache.indexOf( h ), artikel ));
    }

    public boolean deleteHimi(int id) {
        deleteArtikelCache();
        String sql = "DELETE FROM " + TABLE_ARTIKEL + " WHERE id = ?";
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            exHandling(e);
        }
        return false;
    }

    public int updateOrte(String s) {
        return write(
            "UPDATE " + TABLE_APP + " SET " + SQL_APP_FIELD[0] + " = ('" + s.replaceAll("\n", " ") + "');"
        );
    }

    public Patient loadPatient(ResultSet rs) throws SQLException {
        // siehe Patient.fromResultSet
        Patient p = new Patient();
        for (PatientField field : DB_FIELDS) {
            String colName = field.getDBName();
            // prüfen, ob die Spalte überhaupt im ResultSet vorhanden ist
            Object value = rs.getObject(colName);
            if (value == null) {
                continue; // Feld bleibt beim Default-Wert
            }
            // Typsichere Konvertierung
            if (value instanceof Date) {
                p.set( field, ((Date) value).toLocalDate() );
            }
            else {
                p.set(field, value);
            }
        }
        p.setId( rs.getInt( ID.getDBName() ));
        p.setSignatureMap( getSignatureMap( p ));
        p.buildArtikelList(artikelCache);
        p.setModified(false);
        return p;
    }

    public Patient getPatientById(int id) {
        String sql = "SELECT * FROM " + TABLE_PATIENT + " WHERE id = ?";
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            pstmt.setInt(1, id);
            try ( ResultSet rs = pstmt.executeQuery() ) {
                if ( rs.next() ) {
                    return loadPatient(rs);
                }
            }
        } catch (SQLException e) {
            exHandling(e);
        }
        return null;
    }

    public int updateArtikel(Patient p) {
        String sql = "UPDATE " + TABLE_PATIENT + " SET " +
                     ARTIKELLISTE.getDBName() + "='" +
                     p.getRawArtikelList() + "'," +
                     MENGENLISTE.getDBName() + "='" +
                     p.getRawMengenList() + "' WHERE " +
                     ID.getDBName() + "='" + p.getId() + "'";
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            exHandling(e);
        }
        return 0;
    }

    private void loadArtikel(Patient p) throws SQLException {
        if ( artikelCache == null ) {
            artikelCache = getArtikelList();
        }
        p.buildArtikelList(artikelCache);
    }

    public void deleteArtikelCache() {
        artikelCache = null;
    }

    public List<Artikel> getArtikelList() {
        if ( artikelCache != null ) {
            return artikelCache;
        }
        String sql = "SELECT * FROM " + TABLE_ARTIKEL + " ORDER BY " + ArtikelField.NAME.getDBName() + " ASC";
        ArrayList<Artikel> list = new ArrayList<>();
        try ( PreparedStatement pstmt = getConnection().prepareStatement(sql);
              ResultSet rs = pstmt.executeQuery();
            ) {
            while ( rs.next() ) {
                Artikel artikel = new Artikel();
                for ( ArtikelField field : ArtikelField.values() ) {
                    artikel.set( field, rs.getObject( field.getDBName() ));
                }
                list.add(artikel);
            }
            artikelCache = list;
        } catch (SQLException e) {
            exHandling(e);
        }
        return list;
    }

    public ArrayList<Patient> getPatientList() {
        String sql = "SELECT * FROM " + TABLE_PATIENT + " ORDER BY " +
                     FAMILIENNAME.getDBName() + " ASC, " +
                     VORNAME.getDBName() + " ASC";
        ArrayList<Patient> list = new ArrayList<>();
        try ( PreparedStatement pstmt = getConnection().prepareStatement(sql);
              ResultSet rs = pstmt.executeQuery();
            ) {
            if ( rs == null ) {
                return list;
            }
            while ( rs.next() ) {
                Patient p = Patient.fromResultSet(rs);
                loadArtikel(p);
                list.add(p);
            }
        } catch (SQLException e) {
            exHandling(e);
        }
        return list;
    }

    public void initPatientComboBoxModel(DefaultComboBoxModel<String> model) {
        model.removeAllElements();
        String sql = "SELECT " +
            FAMILIENNAME.getDBName() + "," +
            VORNAME.getDBName() + " " +
            "FROM " + TABLE_PATIENT;
        try ( PreparedStatement pstmt = getConnection().prepareStatement(sql);
              ResultSet rs = pstmt.executeQuery();
            ) {
            while ( rs.next() ) {
                model.addElement( rs.getString( 1 ) + ", " + rs.getString( 2 ));
            }
        } catch (SQLException e) {
            exHandling(e);
            model.addElement("[no data]");
        }
    }

    public int insertPatient(Patient p) {
        String sql = "INSERT INTO " + TABLE_PATIENT + " (" + INSERT_COLUMNS + ") " +
                     "VALUES (" + INSERT_PLACEHOLDERS + ")";
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql, Statement.RETURN_GENERATED_KEYS )) {
            Patient.fillPreparedStatement(pstmt, p, INSERT_FIELDS);
            int affected = pstmt.executeUpdate();
            // vom Server vergebene ID setzen
            try ( ResultSet rs = pstmt.getGeneratedKeys() ) {
                if ( rs.next() ) {
                    p.setId( rs.getInt( 1 ));
                }
                p.setModified(false);
            }
            return affected;
        } catch (SQLException e) {
            exHandling(e);
            return 0;
        }
    }

    public int updatePatient(Patient p) {
        String setClause = INSERT_FIELDS
            .stream()
            .map(f -> f.getDBName() + " = ?")
            .collect( Collectors.joining( ", " ));
        String sql = "UPDATE " + TABLE_PATIENT + " SET " + setClause + " WHERE id = ?";
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            int nextIndex = Patient.fillPreparedStatement(pstmt, p, INSERT_FIELDS);
            pstmt.setInt( nextIndex, p.getId() );
            int affected = pstmt.executeUpdate();
            p.setModified(false);
            return affected;
        } catch (SQLException e) {
            exHandling(e);
            return 0;
        }
    }

    public int updatePatient(Patient p, List<PatientField> fieldsToUpdate) {
        return updatePatient(p, fieldsToUpdate.toArray( new PatientField[0] ));
    }

    public int updatePatient(Patient p, PatientField[] fieldsToUpdate) {
        if (fieldsToUpdate == null || fieldsToUpdate.length == 0) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("UPDATE " + TABLE_PATIENT + " SET ");
        for (int i = 0; i < fieldsToUpdate.length; i++) {
            sql.append( fieldsToUpdate[i].getDBName() ).append(" = ?");
            if (i < fieldsToUpdate.length-1) {
                sql.append(", ");
            }
        }
        sql.append(" WHERE ").append( ID.getDBName() ).append(" = ?");

        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql.toString() )) {
            for (int i = 0; i < fieldsToUpdate.length; i++) {
                Patient.setStatementParam( pstmt, i+1, p, fieldsToUpdate[i] );
            }
            pstmt.setInt( fieldsToUpdate.length+1, p.getId() );
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                p.setModified(false);
            }
            return affected;
        } catch (SQLException e) {
            exHandling(e);
            return -1;
        }
    }

    public boolean deletePatient(int id) {
        String sql = "DELETE FROM " + TABLE_PATIENT + " WHERE id = ?";
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            exHandling(e);
        }
        return false;
    }

    public BufferedImage getApoSign() {
        String sql = "SELECT " + SQL_APP_FIELD[2] + " FROM " + TABLE_APP;
        try ( PreparedStatement pstmt = getConnection().prepareStatement(sql);
              ResultSet rs = pstmt.executeQuery();
            ) {
            if ( rs.next() ) {
                return SignatureServer.convertBytesToImage( rs.getBytes( SQL_APP_FIELD[2] ));
            }
        } catch (SQLException e) {
            exHandling(e);
        } catch (IOException e) {
            System.err.println( "Message: " + e.getMessage() );
        }
        return null;
    }

    public Map<SignableDocument, Signature> getSignatureMap(Patient p) {
        Map<SignableDocument, Signature> map = new HashMap<>();
        String sql = "SELECT * FROM " + TABLE_SIGNATURE + " WHERE p_id = ?";
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            SignableDocument type;
            BufferedImage img;
            Date date;
            pstmt.setInt( 1, p.getId() );
            try ( ResultSet rs = pstmt.executeQuery() ) {
                if ( rs.next() ) {
                    type = SignableDocument.values()[0];
                    img  = SignatureServer.convertBytesToImage( rs.getBytes( 2 ));
                    date = rs.getDate(3);
                    map.put( type, new Signature( type, img, date ));

                    type = SignableDocument.values()[1];
                    img  = SignatureServer.convertBytesToImage( rs.getBytes( 4 ));
                    date = rs.getDate(5);
                    map.put( type, new Signature( type, img, date ));

                    type = SignableDocument.values()[2];
                    img  = SignatureServer.convertBytesToImage( rs.getBytes( 6 ));
                    date = rs.getDate(7);
                    map.put( type, new Signature( type, img, date ));
                }
            }
        } catch (SQLException e) {
            exHandling(e);
        } catch (IOException e) {
            System.err.println( e.getMessage() );
        }
        return map;
    }

    public void updateSignature(Patient p, Signature sign) {
        String sql = "REPLACE INTO " + TABLE_SIGNATURE + " (" +
                     SignatureField.PATIENT_ID.getDBName() + ", " +
                     sign.getDocumentType().getSignField().getDBName() + ", " +
                     sign.getDocumentType().getDateField().getDBName() +
                     ") VALUES (?, ?, ?)";
        System.out.println(sql); // debug
        if ( sign.getSign() == null ) {
            try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
                pstmt.setInt(  1, p.getId() );
                pstmt.setNull( 2, BLOB );
                pstmt.setDate( 3, Date.valueOf( sign.getDate() ));
                pstmt.executeUpdate();
            } catch (SQLException e) {
                exHandling(e);
            }
        } else {
            try ( ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
                // BufferedImage zurück in komprimierte PNG-Bytes wandeln
                ImageIO.write( sign.getSign(), "png", out );
                byte[] imageBytes = out.toByteArray();

                try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
                    pstmt.setInt(          1, p.getId() );
                    // ByteArrayInputStream übergibt die Bytes direkt an das BLOB-Feld
                    pstmt.setBinaryStream( 2, new ByteArrayInputStream( imageBytes ), imageBytes.length );
                    pstmt.setDate(         3, Date.valueOf( sign.getDate() ));
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                exHandling(e);
            } catch (IOException e) {
                System.err.println( "Message: " + e.getMessage() );
            }
        }
    }

    static void firstRun_DB(SQLConnection io) {
        System.out.println( io.write( "CREATE DATABASE " + DB + ";" ));
        System.out.println( io.write( "USE " + DB + ";" ));
    }

    static void firstRun_App(SQLConnection io) {
        String orte = String.join( " ", Location.ORTE );
        StringBuilder sb = new StringBuilder( "CREATE TABLE IF NOT EXISTS " );
        sb.append( TABLE_APP );
        sb.append( " (" );
        for (int i = 0; i < SQL_APP_FIELD.length; ++i) {
            sb.append( SQL_APP_FIELD[i] );
            sb.append( " " );
            sb.append( SQL_APP_TYPE[i] );
            sb.append( "," );
        }
        sb.deleteCharAt( sb.length()-1 );
        sb.append( ") VALUES ('" );
        sb.append( orte );
        sb.append( "','" );
        sb.append( "0" );
        sb.append( "');" );
        System.out.println( io.write( sb.toString() ));
    }

    static void firstRun_Patienten(SQLConnection io) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sb.append(TABLE_PATIENT);
        sb.append(" (");
        for ( PatientField field : DB_FIELDS ) {
            sb.append( field.getDBName() );
            sb.append( " " );
            sb.append( field.getDBType() );
            sb.append( "," );
        }
        sb.deleteCharAt( sb.length()-1 );
        sb.append( ");" );
        System.out.println( io.write( sb.toString() ));
    }

    static void firstRun_Signature(SQLConnection io) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sb.append(TABLE_SIGNATURE);
        sb.append(" (");
        for ( SignatureField field : SignatureField.values() ) {
            sb.append( field.getDBName() );
            sb.append( " " );
            sb.append( field.getDBType() );
            sb.append( "," );
        }
        sb.deleteCharAt( sb.length()-1 );
        sb.append( ");" );
        System.out.println( io.write( sb.toString() ));
    }

    static void firstRun_Artikel(SQLConnection io) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sb.append(TABLE_ARTIKEL);
        sb.append(" (");
        for ( ArtikelField field : ArtikelField.values() ) {
            sb.append( field.getDBName() );
            sb.append( " " );
            sb.append( field.getDBType() );
            sb.append( "," );
        }
        sb.deleteCharAt( sb.length()-1 );
        sb.append( ");" );
        System.out.println( io.write( sb.toString() ));
    }

    static void firstRun(String user, char[] pass, String server) {
        try ( SQLConnection io = new SQLConnection(
            server, SQLConnection.DEFAULT_PORT, new Login( user, new String( pass )), null
        )) {
            io.connect();
            firstRun_DB(io);
            firstRun_App(io);
            firstRun_Artikel(io);
            firstRun_Signature(io);
            firstRun_Patienten(io);
        }
    }

    static void updateDB(String user, char[] pass, String server) {
        try ( SQLConnection con = new SQLConnection(
            server, SQLConnection.DEFAULT_PORT, new Login( user, new String( pass )), null
        )) {
            con.connect();
            DatabaseMetaData metaData = con.getConnection().getMetaData();
            ResultSet columns = metaData.getColumns(null, null, TABLE_PATIENT, null);
            List<PatientField> dbFields = PatientField.DB_FIELDS;
            Statement stmt = con.getConnection().createStatement();
            columns.next(); // erste Spalte überspringen (primary key)
            // index 0 überspringen (primary key)
            for (int index = 1; columns.next() && index < dbFields.size(); ++index) {
                String oldColumnName = columns.getString("COLUMN_NAME");
                PatientField targetField = dbFields.get(index);

                // SQL-Befehl für diese Spalte bauen
                String sql = String.format(
                    "ALTER TABLE %s CHANGE `%s` `%s` %s",
                    TABLE_PATIENT,
                    oldColumnName,
                    targetField.getDBName(),
                    targetField.getDBType()
                );

                System.out.println(sql);
                System.out.println( oldColumnName + " -> " + targetField.getDBName() );
                // stmt.execute(sql); // Zum Ausführen auskommentieren
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
