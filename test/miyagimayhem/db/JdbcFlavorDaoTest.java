package miyagimayhem.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import miyagimayhem.model.FlavorLine;
import org.junit.Before;
import org.junit.Test;

public class JdbcFlavorDaoTest extends DatabaseTestCase {

    private FlavorDao dao;

    @Before
    public void createDao() {
        dao = new JdbcFlavorDao(database);
    }

    @Test
    public void addedLineAppearsAtTheEndOfItsKind() {
        int before = dao.findByKind("QUOTE").size();
        dao.add("QUOTE", "This is the end, my friend.");
        List<FlavorLine> lines = dao.findByKind("QUOTE");
        assertEquals(before + 1, lines.size());
        assertEquals("This is the end, my friend.", lines.get(lines.size() - 1).getText());
    }

    @Test
    public void addedLineAlsoAppearsInTheGroupedView() {
        dao.add("MENTAL", "The Riddle of the Sandwich");
        assertTrue(dao.findAllGrouped().get("MENTAL").contains("The Riddle of the Sandwich"));
    }

    @Test
    public void deleteRemovesOnlyThatLine() {
        dao.add("SOCIAL", "Speed Small-Talk");
        List<FlavorLine> lines = dao.findByKind("SOCIAL");
        FlavorLine target = lines.get(lines.size() - 1);
        dao.delete(target.getId());
        List<FlavorLine> after = dao.findByKind("SOCIAL");
        assertEquals(lines.size() - 1, after.size());
        for (FlavorLine line : after) {
            assertFalse(line.getId() == target.getId());
        }
    }

    @Test
    public void kindsDoNotMix() {
        for (FlavorLine line : dao.findByKind("PHYSICAL")) {
            assertEquals("PHYSICAL", line.getKind());
        }
    }
}
