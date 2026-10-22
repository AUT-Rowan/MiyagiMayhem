package miyagimayhem.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the database tables the first time the program runs, and fills
 * the new FLAVOR_LINE and ROSTER tables from the seed files bundled in the
 * jar. Each table is only seeded at the moment it is created, so deleting
 * your own data later never brings the defaults back.
 */
final class DatabaseInitializer {

    private static final String FLAVOR_SEED = "/miyagimayhem/db/seed/flavor.txt";
    private static final String ROSTER_SEED = "/miyagimayhem/db/seed/rosters.txt";

    private DatabaseInitializer() {
    }

    static void initialize(Connection connection) throws SQLException {
        boolean rostersCreated = createTable(connection, "ROSTER",
                "CREATE TABLE ROSTER ("
                + "ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                + "NAME VARCHAR(60) NOT NULL UNIQUE)");

        createTable(connection, "ROSTER_MEMBER",
                "CREATE TABLE ROSTER_MEMBER ("
                + "ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                + "ROSTER_ID INT NOT NULL, "
                + "SEQ_NO INT NOT NULL, "
                + "NAME VARCHAR(40) NOT NULL, "
                + "CONSTRAINT FK_MEMBER_ROSTER FOREIGN KEY (ROSTER_ID) REFERENCES ROSTER(ID) ON DELETE CASCADE)");

        boolean flavorCreated = createTable(connection, "FLAVOR_LINE",
                "CREATE TABLE FLAVOR_LINE ("
                + "ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                + "KIND VARCHAR(20) NOT NULL, "
                + "TEXT VARCHAR(400) NOT NULL)");

        createTable(connection, "GAME",
                "CREATE TABLE GAME ("
                + "ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                + "PLAYED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "WINNER VARCHAR(40) NOT NULL, "
                + "CONTESTANTS VARCHAR(400) NOT NULL, "
                + "ROUNDS INT NOT NULL)");

        createTable(connection, "GAME_LOG",
                "CREATE TABLE GAME_LOG ("
                + "ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                + "GAME_ID INT NOT NULL, "
                + "LINE_NO INT NOT NULL, "
                + "ROUND_NO INT NOT NULL, "
                + "STYLE VARCHAR(20) NOT NULL, "
                + "PAUSE_AFTER BOOLEAN NOT NULL, "
                + "TEXT VARCHAR(1000) NOT NULL, "
                + "CONSTRAINT FK_LOG_GAME FOREIGN KEY (GAME_ID) REFERENCES GAME(ID) ON DELETE CASCADE)");

        // Seed only after every table exists, and only the tables that are brand new.
        if (flavorCreated) {
            seedFlavor(connection);
        }
        if (rostersCreated) {
            seedRosters(connection);
        }
    }

    /** Creates the table if it does not exist yet. Returns true if it was created. */
    private static boolean createTable(Connection connection, String name, String sql) throws SQLException {
        if (tableExists(connection, name)) {
            return false;
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
        return true;
    }

    private static boolean tableExists(Connection connection, String name) throws SQLException {
        try (ResultSet tables = connection.getMetaData().getTables(null, null, name, new String[] {"TABLE"})) {
            return tables.next();
        }
    }

    private static void seedFlavor(Connection connection) throws SQLException {
        String sql = "INSERT INTO FLAVOR_LINE (KIND, TEXT) VALUES (?, ?)";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            for (String line : readSeed(FLAVOR_SEED)) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    insert.setString(1, parts[0].trim().toUpperCase());
                    insert.setString(2, parts[1].trim());
                    insert.addBatch();
                }
            }
            insert.executeBatch();
        }
    }

    private static void seedRosters(Connection connection) throws SQLException {
        for (String line : readSeed(ROSTER_SEED)) {
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) {
                List<String> members = new ArrayList<>();
                for (String member : parts[1].split(",")) {
                    members.add(member.trim());
                }
                JdbcRosterDao.insertRoster(connection, parts[0].trim(), members);
            }
        }
    }

    private static List<String> readSeed(String resource) {
        List<String> lines = new ArrayList<>();
        InputStream in = DatabaseInitializer.class.getResourceAsStream(resource);
        if (in == null) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            // A missing or unreadable seed file just means we start with no seed data.
        }
        return lines;
    }
}
