package miyagimayhem.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of RosterDao for the embedded Derby database.
 */
public class JdbcRosterDao implements RosterDao {

    private final DatabaseManager database;

    public JdbcRosterDao(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public List<String> findAllNames() {
        String sql = "SELECT NAME FROM ROSTER ORDER BY UPPER(NAME)";
        List<String> names = new ArrayList<>();
        try (Statement statement = database.getConnection().createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next()) {
                names.add(rows.getString("NAME"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load the saved rosters.", e);
        }
        return names;
    }

    @Override
    public List<String> findMembers(String rosterName) {
        String sql = "SELECT M.NAME FROM ROSTER_MEMBER M JOIN ROSTER R ON M.ROSTER_ID = R.ID "
                + "WHERE UPPER(R.NAME) = UPPER(?) ORDER BY M.SEQ_NO";
        List<String> members = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, rosterName);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    members.add(rows.getString("NAME"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load the roster '" + rosterName + "'.", e);
        }
        return members;
    }

    @Override
    public void save(String rosterName, List<String> members) {
        Connection connection = database.getConnection();
        try {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                deleteByName(connection, rosterName);
                insertRoster(connection, rosterName, members);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save the roster '" + rosterName + "'.", e);
        }
    }

    @Override
    public void delete(String rosterName) {
        try {
            deleteByName(database.getConnection(), rosterName);
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete the roster '" + rosterName + "'.", e);
        }
    }

    private static void deleteByName(Connection connection, String rosterName) throws SQLException {
        // Members are removed automatically by ON DELETE CASCADE.
        try (PreparedStatement statement =
                connection.prepareStatement("DELETE FROM ROSTER WHERE UPPER(NAME) = UPPER(?)")) {
            statement.setString(1, rosterName);
            statement.executeUpdate();
        }
    }

    /** Inserts a roster row and its members. Also used to seed the default rosters. */
    static void insertRoster(Connection connection, String rosterName, List<String> members) throws SQLException {
        int rosterId;
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO ROSTER (NAME) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, rosterName);
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                keys.next();
                rosterId = keys.getInt(1);
            }
        }

        try (PreparedStatement insertMember = connection.prepareStatement(
                "INSERT INTO ROSTER_MEMBER (ROSTER_ID, SEQ_NO, NAME) VALUES (?, ?, ?)")) {
            for (int i = 0; i < members.size(); i++) {
                insertMember.setInt(1, rosterId);
                insertMember.setInt(2, i);
                insertMember.setString(3, members.get(i));
                insertMember.addBatch();
            }
            insertMember.executeBatch();
        }
    }
}
