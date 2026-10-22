package miyagimayhem.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import miyagimayhem.model.Contestant;
import org.junit.Test;

public class AllianceMakerTest {

    private static List<Contestant> contestants(int count) {
        List<Contestant> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new Contestant("P" + i));
        }
        return list;
    }

    @Test
    public void alliancesAreAlwaysMutual() {
        for (int seed = 0; seed < 100; seed++) {
            List<Contestant> list = contestants(7);
            AllianceMaker.formRandomAlliances(list, new Random(seed));
            for (Contestant c : list) {
                if (c.getAllyName() != null) {
                    Contestant ally = null;
                    for (Contestant other : list) {
                        if (other.getName().equals(c.getAllyName())) {
                            ally = other;
                        }
                    }
                    assertNotNull(ally);
                    assertEquals(c.getName(), ally.getAllyName());
                    assertFalse(ally == c);
                }
            }
        }
    }

    @Test
    public void nobodyIsAlliedWithTwoPeople() {
        List<Contestant> list = contestants(10);
        AllianceMaker.formRandomAlliances(list, new Random(42));
        int allied = 0;
        for (Contestant c : list) {
            if (c.getAllyName() != null) {
                allied++;
            }
        }
        assertEquals(0, allied % 2);
    }
}
