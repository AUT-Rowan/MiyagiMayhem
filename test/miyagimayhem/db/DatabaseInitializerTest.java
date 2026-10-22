package miyagimayhem.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class DatabaseInitializerTest extends DatabaseTestCase {

    @Test
    public void firstRunSeedsDefaultRosters() {
        List<String> rosters = new JdbcRosterDao(database).findAllNames();
        assertTrue(rosters.contains("The Beatles"));
        assertEquals(4, new JdbcRosterDao(database).findMembers("The Beatles").size());
    }

    @Test
    public void firstRunSeedsEveryKindOfShowText() {
        Map<String, List<String>> grouped = new JdbcFlavorDao(database).findAllGrouped();
        assertTrue(grouped.containsKey("PHYSICAL"));
        assertTrue(grouped.containsKey("QUOTE"));
        assertFalse(grouped.get("LOSER").isEmpty());
    }

    @Test
    public void reopeningDoesNotSeedAgain() {
        int rostersBefore = new JdbcRosterDao(database).findAllNames().size();
        int flavorBefore = new JdbcFlavorDao(database).findByKind("QUOTE").size();
        database.shutdown();
        DatabaseManager.useDatabasePath(currentPath());
        DatabaseManager reopened = DatabaseManager.getInstance();
        assertEquals(rostersBefore, new JdbcRosterDao(reopened).findAllNames().size());
        assertEquals(flavorBefore, new JdbcFlavorDao(reopened).findByKind("QUOTE").size());
        database = reopened;
    }

    private String currentPath() {
        return new File(folder.getRoot(), "testDB").getAbsolutePath();
    }
}
