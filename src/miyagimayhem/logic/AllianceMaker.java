package miyagimayhem.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import miyagimayhem.model.Contestant;

/**
 * Randomly pairs some contestants into two-person alliances at the start
 * of a game. Not every contestant gets an ally.
 */
public final class AllianceMaker {

    private AllianceMaker() {
    }

    public static void formRandomAlliances(List<Contestant> contestants, Random rng) {
        List<Contestant> unallied = new ArrayList<>(contestants);
        while (unallied.size() >= 2) {
            // ~50% chance any given pair actually forms an alliance
            if (rng.nextBoolean()) {
                Contestant a = unallied.remove(rng.nextInt(unallied.size()));
                Contestant b = unallied.remove(rng.nextInt(unallied.size()));
                a.setAlly(b.getName());
                b.setAlly(a.getName());
            } else {
                // skip this one, still remove to make progress
                unallied.remove(0);
            }
        }
    }
}
