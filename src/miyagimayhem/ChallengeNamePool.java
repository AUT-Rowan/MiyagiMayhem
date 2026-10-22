package miyagimayhem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Hands out challenge names one category at a time without repeating any
 * name until every name in that category has been used once. Once a
 * category's pool is exhausted, it reshuffles and starts a fresh cycle.
 * One instance is shared across an entire Simulation run.
 */
public class ChallengeNamePool {

    private final Map<String, List<String>> remaining = new HashMap<>();
    private final Random rng = new Random();

    public String draw(String category) {
        List<String> pool = remaining.get(category);

        if (pool == null || pool.isEmpty()) {
            pool = new ArrayList<>(FlavorTextGenerator.getChallengeNames(category));
            Collections.shuffle(pool, rng);
            remaining.put(category, pool);
        }

        return pool.remove(pool.size() - 1);
    }
}
