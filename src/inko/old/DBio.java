/*
 *
 */

package inko.old;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.0
 *
 */

import inko.PatientField;
import static inko.SQLConnection.exHandling;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.Login;

public class DBio extends inko.DBio {

    public final static String[] SQL_HIMI_FIELD = {
        "id", "p_id", "artikel1", "pzn1", "artikel2", "pzn2", "artikel3", "pzn3", "artikel4", "pzn4", "artikel5", "pzn5"
    };
    public final static String[] SQL_HIMI_TYPE = {
        "int unsigned not null auto_increment primary key", "int unsigned not null", "text", "int unsigned", "text",
        "int unsigned", "text", "int unsigned", "text", "int unsigned", "text", "int unsigned"
    };
    public final static String TABLE_HIMI = "himi";

    private final static String HIMI_INSERT_COLUMNS = Arrays.stream(SQL_HIMI_FIELD)
        .skip(1)
        .map(f -> f)
        .collect( Collectors.joining( ", " ));
    private final static String HIMI_INSERT_PLACEHOLDERS = Arrays.stream(SQL_HIMI_FIELD)
        .skip(1)
        .map(f -> "?")
        .collect( Collectors.joining( ", " ));
    private final static String HIMI_UPDATE = Arrays.stream(SQL_HIMI_FIELD)
        .skip(2)
        .map(f -> f + " = ?")
        .collect( Collectors.joining( ", " ));
    private final static String JOIN  =
        " LEFT JOIN " + TABLE_HIMI + " ON " + TABLE_PATIENT + ".ID=" + TABLE_HIMI + "." + SQL_HIMI_FIELD[1]
    ;

    public DBio(String host, int port, Login login) {
        super(host, port, login);
    }

    public Patient getOldPatientByID(int id) {
        Patient p = null;
        String sql = "SELECT * FROM " + TABLE_PATIENT + JOIN + " WHERE " + TABLE_PATIENT + ".ID = ?";
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            pstmt.setInt(1, id);
            try ( ResultSet rs = pstmt.executeQuery() ) {
                if ( rs.next() ) {
                    p = Patient.fromPatient( loadPatient( rs ), null );
                    addHimi2Patient(rs, p);
                }
            }
        } catch (SQLException e) {
            exHandling(e);
        }
        return p;
    }

    private void addHimi2Patient(ResultSet result, Patient p) throws SQLException {
        for (int i = 2; i < SQL_HIMI_FIELD.length; i+=2) {
            String name = result.getString( SQL_HIMI_FIELD[i] );
            if ( result.wasNull() ) {
                return;
            }
            int pzn = result.getInt( SQL_HIMI_FIELD[i+1] );
            if ( result.wasNull() ) {
                pzn = 0;
            }
            p.addArtikel(name, pzn);
        }
    }

    private static int fillPreparedStatement(PreparedStatement pstmt, List<Object> fields, int startIndex)
    throws SQLException {
        int i = startIndex;
        for (Object value : fields) {
            if (value == null) {
                pstmt.setNull(i, Types.NULL);
            }
            // Integer
            else if (value instanceof Integer) {
                pstmt.setInt( i, (Integer) value );
            }
            // String (default)
            else {
                pstmt.setString( i, value.toString() );
            }
            ++i;
        }
        return i;
    }

    @Override
    public int insertPatient(inko.Patient p) {
        int affected = super.insertPatient(p);
        if (affected == 0) {
            return 0;
        }

        String sql = "INSERT INTO " + TABLE_HIMI + " (" + HIMI_INSERT_COLUMNS + ") " +
                     "VALUES (" + HIMI_INSERT_PLACEHOLDERS + ")";
        List<Object> list = new ArrayList<>();
        p.getArtikelList().forEach( entry -> {
            list.add( entry.getName() );
            list.add( entry.getPZN() );
        });

        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            pstmt.setInt( 1, p.getId() ); // p_id
            int nextIndex = fillPreparedStatement(pstmt, list, 2);
            int totalParameterCount = pstmt.getParameterMetaData().getParameterCount();
            for ( ; nextIndex <= totalParameterCount; nextIndex++) {
                // Hier wird jeder verbleibende Platzhalter mit NULL gefüllt
                pstmt.setNull(nextIndex, Types.NULL);
            }
            affected += pstmt.executeUpdate();
            return affected;
        } catch (SQLException e) {
            exHandling(e);
            return 0;
        }
    }

    @Override
    public int updatePatient(inko.Patient p) {
        int affected = super.updatePatient(p);
        if (affected == 0) {
            return 0;
        }

        String sql = "UPDATE " + TABLE_HIMI + " SET " + HIMI_UPDATE + " WHERE " + SQL_HIMI_FIELD[1] + " = ?";
        List<Object> list = new ArrayList<>();
        p.getArtikelList().forEach( entry -> {
            list.add( entry.getFullArtikelName() );
            list.add( entry.getPZN() );
        });

        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            int nextIndex = fillPreparedStatement(pstmt, list, 1);
            int totalParameterCount = pstmt.getParameterMetaData().getParameterCount();
            for ( ; nextIndex <= totalParameterCount-1; nextIndex++) {
                // Hier wird jeder verbleibende Platzhalter bis auf den Letzten mit NULL gefüllt
                pstmt.setNull(nextIndex, Types.NULL);
            }
            pstmt.setInt( nextIndex, p.getId() ); // p_id
            affected += pstmt.executeUpdate();
            return affected;
        } catch (SQLException e) {
            exHandling(e);
            return 0;
        }
    }

    public ArrayList<Patient> getOldPatientList() {
        String sql = "select * from " + TABLE_PATIENT + JOIN + " order by " +
                     PatientField.FAMILIENNAME.getDBName() + " asc, " +
                     PatientField.VORNAME.getDBName() + " asc";
//      System.out.println(sql);                                                                                        // debug
        ArrayList<Patient> list = new ArrayList<>();
        Patient p;
        try ( PreparedStatement pstmt = getConnection().prepareStatement( sql )) {
            try ( ResultSet rs = pstmt.executeQuery() ) {
                while ( rs.next() ) {
                    p = Patient.fromPatient( loadPatient( rs ), null );
                    addHimi2Patient(rs, p);
                    list.add(p);
                }
            }
            return list;
        } catch (SQLException e) {
            exHandling(e);
            return null;
        }
    }

}
