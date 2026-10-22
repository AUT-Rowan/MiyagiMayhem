package miyagimayhem.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton that owns the one connection to the embedded Apache Derby
 * database. Derby runs inside this program's own JVM, so there is no
 * database server to start: the first call to getConnection() creates the
 * database folder, builds the tables and loads the seed data if needed.
 *
 * All the connection settings live here and nowhere else, the DAO classes
 * just ask this class for a connection.
 */
public final class DatabaseManager {

    private static final String DEFAULT_PATH = "database/miyagiDB";
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static DatabaseManager instance;
    private static String databasePath = DEFAULT_PATH;

    private final String path;
    private Connection connection;

    private DatabaseManager(String path) {
        this.path = path;
    }

    /** The shared instance, created on first use. */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager(databasePath);
        }
        return instance;
    }

    /**
     * Points the manager at a different database folder. Used by the unit
     * tests so they run against a throwaway database instead of the real one.
     */
    public static synchronized void useDatabasePath(String newPath) {
        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
        databasePath = newPath;
    }

    /** Returns the open connection, connecting and setting up the tables the first time. */
    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = open();
            }
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException(explain(e), e);
        }
    }

    private Connection open() throws SQLException {
        prepareFolder();
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("The Derby driver (derby-10.14.2.0.jar) is missing from the classpath.", e);
        }
        Connection created = DriverManager.getConnection("jdbc:derby:" + path + ";create=true");
        DatabaseInitializer.initialize(created);
        return created;
    }

    /** Makes sure the folder holding the database exists and keeps derby.log inside it. */
    private void prepareFolder() {
        File parent = new File(path).getAbsoluteFile().getParentFile();
        if (parent != null) {
            parent.mkdirs();
            if (System.getProperty("derby.stream.error.file") == null) {
                System.setProperty("derby.stream.error.file", new File(parent, "derby.log").getPath());
            }
        }
    }

    /** Closes the connection and cleanly shuts this database down. */
    public synchronized void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // closing on the way out, nothing useful to do if it fails
        }
        connection = null;
        try {
            DriverManager.getConnection("jdbc:derby:" + path + ";shutdown=true");
        } catch (SQLException e) {
            // Derby always reports a successful single-database shutdown as SQLState 08006
        }
    }

    private String explain(SQLException e) {
        if ("XJ040".equals(e.getSQLState()) || "XSDB6".equals(e.getSQLState())
                || (e.getNextException() != null && "XSDB6".equals(e.getNextException().getSQLState()))) {
            return "The database is already in use. Close any other copy of Miyagi Mayhem and try again.";
        }
        return "Could not open the database: " + e.getMessage();
    }
}
