package miyagimayhem.db;

import java.util.List;

/**
 * Data access for saved rosters (named groups of contestant names).
 */
public interface RosterDao {

    /** Names of all saved rosters, alphabetical. */
    List<String> findAllNames();

    /** The contestant names in a roster, in the order they were saved. Empty if it doesn't exist. */
    List<String> findMembers(String rosterName);

    /** Saves a roster, replacing any existing roster with the same name (ignoring case). */
    void save(String rosterName, List<String> members);

    void delete(String rosterName);
}
