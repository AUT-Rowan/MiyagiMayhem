package miyagimayhem.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ContestantTest {

    @Test
    public void randomStatsStayInRange() {
        for (int i = 0; i < 200; i++) {
            Contestant c = new Contestant("Ann");
            for (int stat : new int[] {c.getStrength(), c.getIntelligence(), c.getSocial(), c.getLuck()}) {
                assertTrue(stat >= Contestant.MIN_STAT && stat <= Contestant.MAX_STAT);
            }
        }
    }

    @Test
    public void startsActiveWithoutImmunityOrAlly() {
        Contestant c = new Contestant("Ann", 5, 5, 5, 5);
        assertFalse(c.isEliminated());
        assertFalse(c.hasImmunity());
        assertFalse(c.isAlliedWith("Bob"));
    }

    @Test
    public void immunityCanBeGrantedAndCleared() {
        Contestant c = new Contestant("Ann", 5, 5, 5, 5);
        c.grantImmunity();
        assertTrue(c.hasImmunity());
        c.clearImmunity();
        assertFalse(c.hasImmunity());
    }

    @Test
    public void allianceIgnoresCase() {
        Contestant c = new Contestant("Ann", 5, 5, 5, 5);
        c.setAlly("Bob");
        assertTrue(c.isAlliedWith("bOB"));
    }

    @Test
    public void scoreWithZeroLuckEqualsTheChosenStat() {
        Contestant c = new Contestant("Ann", 8, 3, 6, 0);
        assertEquals(8, c.rollScore("strength"));
        assertEquals(3, c.rollScore("intelligence"));
        assertEquals(6, c.rollScore("social"));
    }

    @Test
    public void scoreNeverExceedsStatPlusLuck() {
        Contestant c = new Contestant("Ann", 4, 4, 4, 6);
        for (int i = 0; i < 200; i++) {
            int score = c.rollScore("strength");
            assertTrue(score >= 4 && score <= 10);
        }
    }
}
