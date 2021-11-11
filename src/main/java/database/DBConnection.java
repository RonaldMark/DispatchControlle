package database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import main.App;
import utils.Logging;

/**
 *
 * @author ronald.langat
 */
public final class DBConnection {

    String DATABASE_IP = "";
    String DATABASE_PORT = "";
    String DATABASE_NAME = App.DATABASE_NAME;
    String DATABASE_SERVER_TIME_ZONE = "";

    String url = "";
    String driverName = App.DATABASE_DRIVER;

    Statement stmt;
    ResultSet rs;

    private final Logging logger;

    Connection con = null;

    public DBConnection() {
        logger = new Logging();
        stmt = null;
        rs = null;
        url = System.getProperty("user.dir") + File.separator + "database" + File.separator + "" + DATABASE_NAME + "";
    }

    public Connection getConnection() {
        // Create database URL
        //String inURL = "jdbc:h2:tcp://localhost/" + url;
       // String inURL = "jdbc:h2:file:" + url;

        try {
            // Class.forName(driverName).newInstance();
            con = DriverManager.getConnection("jdbc:h2:file:" + url, "sa", "sa");
        } catch (SQLException ex) {
            logger.applicationLog(logger.logPreString() + "DB Exception  - " + ex.getMessage() + "\n\n", "", 4);
        }
        return con;
    }

    public ResultSet query_all(final String query) {
        try {
            con = getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
        } catch (SQLException ex) {
            logger.applicationLog(logger.logPreString() + "DB Exception  - " + ex.getMessage() + "\n\n", "", 4);
        }
        return rs;
    }

    public int rowCount(final String query) {
        int count = 0;

        rs = query_all(query);
        try {
            while (rs.next()) {
                ++count;
            }
        } catch (SQLException ex) {
            logger.applicationLog(logger.logPreString() + "DB Exception  - " + ex.getMessage() + "\n\n", "", 4);
        }

        return count;
    }

    public int executeQuery(final String query) {
        int i = 0;
        try {
            con = getConnection();
            stmt = con.createStatement();
            i = stmt.executeUpdate(query);
        } catch (SQLException ex) {
            logger.applicationLog(logger.logPreString() + "DB Exception  - " + ex.getMessage() + "\n\n", "", 4);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                logger.applicationLog(logger.logPreString() + "DB Exception  - " + ex.getMessage() + "\n\n", "", 4);
            }
        }

        return i;
    }

    public void closeConn() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            logger.applicationLog(logger.logPreString() + "DB Exception  - " + e.getMessage() + "\n\n", "", 4);
        }
    }
}
