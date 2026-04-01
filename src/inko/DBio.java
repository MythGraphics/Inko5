package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 2.1.1
 *
 */

import static inko.PatientField.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import net.Login;

public class DBio extends SQLConnection {

    public final static String DB_NAME          = "inkodb";
    public final static String TABLE_PATIENT    = "patienten";
    public final static String TABLE_ARTIKEL    = "artikel";
    public final static String TABLE_DIVERSES   = "inkoapp";

    private final static String[] SQL_DIVERSES_FIELD = {"orte", "artikelpass"};
    private final static String[] SQL_DIVERSES_TYPE  = {"text", "text"};

    private List<Artikel> himiCache;

    public DBio(String host, int port, Login login) {
        super(host, port, login, DB_NAME);
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
        String sql = "SELECT " + SQL_DIVERSES_FIELD[0] + " FROM " + TABLE_DIVERSES;
        try (
            Statement stmt = getConnection().createStatement();
            ResultSet result = stmt.executeQuery(sql)
        ) {
            if ( result.next() ) {
                return parseString( result.getString( SQL_DIVERSES_FIELD[0] ));
            }
        } catch (SQLException e) {
            exHandling(e);
        }
        return new ArrayList<>();
    }

    public char[] getArtikelPasswordHash() {
        String sql = "SELECT " + SQL_DIVERSES_FIELD[1] + " FROM " + TABLE_DIVERSES;
        try (
            Statement stmt = getConnection().createStatement();
            ResultSet result = stmt.executeQuery(sql)
        ) {
            if ( result.next() ) {
                return result.getString( SQL_DIVERSES_FIELD[1] ).toCharArray();
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
        if (himiCache == null) {
            return;
        }
        himiCache.stream()
                 .filter( h -> h.getId() == artikel.getId() )
                 .findFirst()
                 .ifPresent( h -> himiCache.set( himiCache.indexOf(h), artikel ));
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
            "UPDATE " + TABLE_DIVERSES + " SET " + SQL_DIVERSES_FIELD[0] + " = ('" + s.replaceAll("\n", " ") + "');"
        );
    }

    public Patient loadPatient(ResultSet rs) throws SQLException {
        // siehe Patient.fromResultSet
        Patient p = new Patient();
        for ( PatientField field : DB_FIELDS ) {
            String colName = field.getDBName();
            // prüfen, ob die Spalte überhaupt im ResultSet vorhanden ist
            Object value = rs.getObject(colName);
            if (value == null) {
                continue; // Feld bleibt beim Default-Wert
            }
            // Typsichere Konvertierung
            if (value instanceof Date) {
                // Konvertierung: SQL Date -> LocalDate
                p.set( field, ( (Date) value ).toLocalDate() );
            }
            else {
                p.set(field, value);
            }
        }
        p.setId( rs.getInt( ID.getDBName() ));
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
                     ARTIKELMENGE.getDBName() + "='" +
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
        if ( himiCache == null ) {
            himiCache = getArtikelList();
        }
        p.initArtikelList(himiCache);
    }

    public void deleteArtikelCache() {
        himiCache = null;
    }

    public List<Artikel> getArtikelList() {
        if ( himiCache != null ) {
            return himiCache;
        }
        String sql = "SELECT * FROM " + TABLE_ARTIKEL + " ORDER BY " + ArtikelField.NAME.getDBName() + " ASC";
        ArrayList<Artikel> list = new ArrayList<>();
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            ResultSet rs = pstmt.executeQuery();
            while ( rs.next() ) {
                Artikel artikel = new Artikel();
                for ( ArtikelField field : ArtikelField.values() ) {
                    artikel.set( field, rs.getObject( field.getDBName() ));
                }
                list.add(artikel);
            }
            himiCache = list;
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
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            ResultSet rs = pstmt.executeQuery();
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
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            ResultSet rs = pstmt.executeQuery();
            while ( rs.next() ) {
                model.addElement( rs.getString(1) + ", " + rs.getString(2) );
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

    static void firstRun_DB(SQLConnection io) {
        System.out.println( io.write( "CREATE DATABASE " + DB_NAME + ";" ));
        System.out.println( io.write( "USE " + DB_NAME + ";" ));
    }

    static void firstRun_Diverses(SQLConnection io) {
        String orte = String.join( " ", Location.ORTE );
        StringBuilder sb = new StringBuilder( "CREATE TABLE IF NOT EXISTS " );
        sb.append( TABLE_DIVERSES );
        sb.append( " (" );
        for (int i = 0; i < SQL_DIVERSES_FIELD.length; ++i) {
            sb.append( SQL_DIVERSES_FIELD[i] );
            sb.append( " " );
            sb.append( SQL_DIVERSES_TYPE[i] );
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
        for (int i = 0; i < DB_FIELDS.size(); ++i) {
            sb.append( DB_FIELDS.get(i).getDBName() );
            sb.append( " " );
            sb.append( DB_FIELDS.get(i).getDBType() );
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
        for (int i = 0; i < ArtikelField.values().length; ++i) {
            sb.append( ArtikelField.values()[i].getDBName() );
            sb.append( " " );
            sb.append( ArtikelField.values()[i].getDBType() );
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
            firstRun_Diverses(io);
            firstRun_Artikel(io);
            firstRun_Patienten(io);
        }
    }

}
