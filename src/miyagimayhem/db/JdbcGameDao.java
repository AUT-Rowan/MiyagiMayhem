package miyagimayhem.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import miyagimayhem.model.GameResult;
import miyagimayhem.model.LineStyle;
import miyagimayhem.model.LineTag;
import miyagimayhem.model.NarratedLine;

/**
 * JDBC implementation of GameDao for the embedded Derby database.
 */
public class JdbcGameDao implements GameDao {

    private final DatabaseManager database;

    public JdbcGameDao(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public int save(String winner, String contestants, int rounds, List<NarratedLine> transcript) {
        Connection connection = database.getConnection();
        try {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                int gameId = insertGame(connection, winner, contestants, rounds);
                insertTranscript(connection, gameId, transcript);
                connection.commit();
                return gameId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save the finished game.", e);
        }
    }

    private int insertGame(Connection connection, String winner, String contestants, int rounds)
            throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO GAME (WINNER, CONTESTANTS, ROUNDS) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, winner);
            insert.setString(2, contestants);
            insert.setInt(3, rounds);
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void insertTranscript(Connection connection, int gameId, List<NarratedLine> transcript)
            throws SQLException {
        String sql = "INSERT INTO GAME_LOG (GAME_ID, LINE_NO, ROUND_NO, STYLE, PAUSE_AFTER, TEXT) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            int lineNo = 0;
            for (NarratedLine line : transcript) {
                insert.setInt(1, gameId);
                insert.setInt(2, lineNo++);
                insert.setInt(3, line.getRound());
                insert.setString(4, line.getStyle().name());
                insert.setBoolean(5, line.isPause());
                insert.setString(6, line.getText());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    @Override
    public List<GameResult> findAll() {
        String sql = "SELECT ID, PLAYED_AT, WINNER, CONTESTANTS, ROUNDS FROM GAME ORDER BY PLAYED_AT DESC, ID DESC";
        List<GameResult> games = new ArrayList<>();
        try (Statement statement = database.getConnection().createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next()) {
                games.add(new GameResult(rows.getInt("ID"), rows.getTimestamp("PLAYED_AT"),
                        rows.getString("WINNER"), rows.getString("CONTESTANTS"), rows.getInt("ROUNDS")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load the game history.", e);
        }
        return games;
    }

    @Override
    public List<NarratedLine> findTranscript(int gameId) {
        String sql = "SELECT ROUND_NO, STYLE, PAUSE_AFTER, TEXT FROM GAME_LOG WHERE GAME_ID = ? ORDER BY LINE_NO";
        List<NarratedLine> transcript = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, gameId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    transcript.add(new NarratedLine(rows.getString("TEXT"),
                            LineStyle.valueOf(rows.getString("STYLE")), LineTag.NONE, null,
                            rows.getInt("ROUND_NO"), rows.getBoolean("PAUSE_AFTER")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load that game's transcript.", e);
        }
        return transcript;
    }

    @Override
    public Map<String, Integer> findWinCounts() {
        String sql = "SELECT WINNER, COUNT(*) AS WINS FROM GAME GROUP BY WINNER ORDER BY WINS DESC, WINNER ASC";
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (Statement statement = database.getConnection().createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next()) {
                counts.put(rows.getString("WINNER"), rows.getInt("WINS"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load the leaderboard.", e);
        }
        return counts;
    }

    @Override
    public void deleteAll() {
        try (Statement statement = database.getConnection().createStatement()) {
            // Transcripts are removed automatically by ON DELETE CASCADE.
            statement.executeUpdate("DELETE FROM GAME");
        } catch (SQLException e) {
            throw new DataAccessException("Could not clear the game history.", e);
        }
    }
}
