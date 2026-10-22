package miyagimayhem.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class JdbcRosterDaoTest extends DatabaseTestCase {

    private RosterDao dao;

    @Before
    public void createDao() {
        dao = new JdbcRosterDao(database);
    }

    @Test
    public void savedRosterCanBeLoadedInTheSameOrder() {
        dao.save("My Crew", Arrays.asList("Zed", "Amy", "Max"));
        assertEquals(Arrays.asList("Zed", "Amy", "Max"), dao.findMembers("My Crew"));
    }

    @Test
    public void lookupIgnoresCase() {
        dao.save("My Crew", Arrays.asList("A", "B", "C"));
        assertEquals(3, dao.findMembers("my crew").size());
    }

    @Test
    public void savingTheSameNameReplacesTheOldRoster() {
        dao.save("Crew", Arrays.asList("A", "B", "C"));
        dao.save("CREW", Arrays.asList("X", "Y", "Z", "W"));
        List<String> matching = dao.findAllNames();
        int count = 0;
        for (String name : matching) {
            if (name.equalsIgnoreCase("crew")) {
                count++;
            }
        }
        assertEquals(1, count);
        assertEquals(Arrays.asList("X", "Y", "Z", "W"), dao.findMembers("crew"));
    }

    @Test
    public void deleteRemovesTheRosterAndItsMembers() {
        dao.save("Gone", Arrays.asList("A", "B", "C"));
        dao.delete("Gone");
        assertFalse(dao.findAllNames().contains("Gone"));
        assertTrue(dao.findMembers("Gone").isEmpty());
    }

    @Test
    public void unknownRosterHasNoMembers() {
        assertTrue(dao.findMembers("Does not exist").isEmpty());
    }

    @Test
    public void namesAreReturnedAlphabetically() {
        dao.save("zebras", Arrays.asList("A", "B", "C"));
        dao.save("Aardvarks", Arrays.asList("A", "B", "C"));
        List<String> names = dao.findAllNames();
        assertTrue(names.indexOf("Aardvarks") < names.indexOf("zebras"));
    }
}
