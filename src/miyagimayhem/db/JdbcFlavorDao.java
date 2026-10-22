package miyagimayhem.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import miyagimayhem.model.FlavorLine;

/**
 * JDBC implementation of FlavorDao for the embedded Derby database.
 */
public class JdbcFlavorDao implements FlavorDao {

    private final DatabaseManager database;

    public JdbcFlavorDao(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public Map<String, List<String>> findAllGrouped() {
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        try (Statement statement = database.getConnection().createStatement();
             ResultSet rows = statement.executeQuery("SELECT KIND, TEXT FROM FLAVOR_LINE ORDER BY ID")) {
            while (rows.next()) {
                grouped.computeIfAbsent(rows.getString("KIND"), k -> new ArrayList<>())
                        .add(rows.getString("TEXT"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load the show text.", e);
        }
        return grouped;
    }

    @Override
    public List<FlavorLine> findByKind(String kind) {
        List<FlavorLine> lines = new ArrayList<>();
        String sql = "SELECT ID, KIND, TEXT FROM FLAVOR_LINE WHERE KIND = ? ORDER BY ID";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, kind);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    lines.add(new FlavorLine(rows.getInt("ID"), rows.getString("KIND"), rows.getString("TEXT")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load the lines for " + kind + ".", e);
        }
        return lines;
    }

    @Override
    public void add(String kind, String text) {
        try (PreparedStatement statement =
                database.getConnection().prepareStatement("INSERT INTO FLAVOR_LINE (KIND, TEXT) VALUES (?, ?)")) {
            statement.setString(1, kind);
            statement.setString(2, text);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not add the new line.", e);
        }
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement statement =
                database.getConnection().prepareStatement("DELETE FROM FLAVOR_LINE WHERE ID = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete the line.", e);
        }
    }
}
