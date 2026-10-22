package miyagimayhem.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class RosterValidatorTest {

    @Test
    public void normalizeTrimsCollapsesSpacesAndCapitalises() {
        assertEquals("Mary Jane", RosterValidator.normalize("   mary    Jane  "));
    }

    @Test
    public void normalizeKeepsInnerCapitals() {
        assertEquals("McDonald", RosterValidator.normalize("mcDonald"));
    }

    @Test
    public void acceptsAnOrdinaryName() {
        assertNull(RosterValidator.validateName("Aiko", new ArrayList<String>()));
    }

    @Test
    public void rejectsEmptyName() {
        assertNotNull(RosterValidator.validateName("   ", new ArrayList<String>()));
    }

    @Test
    public void rejectsTooLongName() {
        String tooLong = "A".repeat(RosterValidator.MAX_NAME_LENGTH + 1);
        assertNotNull(RosterValidator.validateName(tooLong, new ArrayList<String>()));
    }

    @Test
    public void rejectsDuplicateIgnoringCase() {
        List<String> existing = Arrays.asList("Aiko", "Ben");
        assertNotNull(RosterValidator.validateName("aIKO", existing));
    }

    @Test
    public void rejectsCharactersThatBreakSavedRosters() {
        assertNotNull(RosterValidator.validateName("A,B", new ArrayList<String>()));
        assertNotNull(RosterValidator.validateName("A|B", new ArrayList<String>()));
    }

    @Test
    public void rejectsNameWhenRosterIsFull() {
        List<String> full = new ArrayList<>();
        for (int i = 0; i < RosterValidator.MAX_CONTESTANTS; i++) {
            full.add("Player" + i);
        }
        assertNotNull(RosterValidator.validateName("Extra", full));
    }

    @Test
    public void rosterNeedsBetweenMinAndMaxContestants() {
        assertNotNull(RosterValidator.validateRoster(Arrays.asList("A", "B")));
        assertNull(RosterValidator.validateRoster(Arrays.asList("A", "B", "C")));
        List<String> tooMany = new ArrayList<>();
        for (int i = 0; i <= RosterValidator.MAX_CONTESTANTS; i++) {
            tooMany.add("P" + i);
        }
        assertNotNull(RosterValidator.validateRoster(tooMany));
    }

    @Test
    public void rosterNameMustBeSensible() {
        assertNull(RosterValidator.validateRosterName("Friday crew"));
        assertNotNull(RosterValidator.validateRosterName(" "));
        assertNotNull(RosterValidator.validateRosterName("a|b"));
        assertTrue(RosterValidator.validateRosterName("x".repeat(31)) != null);
    }
}
