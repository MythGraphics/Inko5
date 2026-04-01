/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.3
 *
 */

import java.io.Closeable;
import java.sql.*;
import net.Login;
import net.ServiceAddress;

public class SQLConnection implements Closeable {

    public final static String MARIADB    = "jdbc:mariadb://";
    public final static int DEFAULT_PORT  = 3306;
    public final static String SQL_STATUS = "Statuscode SQL-Server: ";

    private final String url;
    private final Login login;
    private Connection con;
    private Statement statement;
    private String db;

    public SQLConnection(String host, int port, Login login, String db) {
        this.url = MARIADB + host + ":" + port + "/";
        this.login = login;
        this.db = db;
    }

    public SQLConnection(ServiceAddress address, Login login, String db) {
        this.url = "jdbc:" + "mariadb" + "://" + address.toString();
        this.login = login;
        this.db = db;
    }

    public SQLConnection(String url, Login login, String db) {
        this.url = url;
        this.login = login;
        this.db = db;
    }

    public void setDatabase(String db) {
        this.db = db;
        if ( con != null ) {
            write( "use " + db + ";" );
        }
    }

    public int getColumnCount(ResultSet result) throws SQLException {
        return result.getMetaData().getColumnCount();
    }

    public boolean connect() {
        try {
            con = DriverManager.getConnection( url, login.getUser(), login.getPass() );
            statement = con.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE );
            if ( db != null) {
                write( "use " + db + ";" );
            }
            return true;
        }
        catch (SQLException e) {
            exHandling(e);
            close();
            return false;
        }
    }

    public ResultSet executeQuery(String query) {
        return read(query);
    }

    public ResultSet read(String query) {
        try {
            return statement.executeQuery(query);
        }
        catch (SQLException e) {
            exHandling(e);
            return null;
        }
    }

    public int insert(String query) {
        return write(query);
    }

    public int update(String query) {
        return write(query);
    }

    public int write(String query) {
        try {
            return statement.executeUpdate(query);
        }
        catch (SQLException e) {
            exHandling(e);
            return -1;
        }
    }

    public int getRow(ResultSet result) {
        try {
            result.last();
            int row = result.getRow();
            result.beforeFirst();
            return row;
        }
        catch (SQLException e) {
            exHandling(e);
            return -1;
        }
    }

    public static void exHandling(SQLException e) {
        for ( ; e != null; e = e.getNextException() ) {
            System.err.println( "Message:    " + e.getMessage() );
            System.err.println( "SQL State:  " + e.getSQLState() );
            System.err.println( "Error Code: " + e.getErrorCode() );
        }
    }

    public boolean isConnected() {
        if ( statement == null ) {
            return false;
        }
        return con != null;
    }

    public Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                // Logik zum neu verbinden
                if ( !connect() ) {
                    return null;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return con;
    }

    @Override
    public void close() {
        if ( statement != null ) {
            try { statement.close(); }
            catch (SQLException e) { e.printStackTrace(); }
        }
        if ( con != null ) {
            try { con.close(); }
            catch (SQLException e) { e.printStackTrace(); }
        }
    }

}
