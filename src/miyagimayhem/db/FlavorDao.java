package miyagimayhem.db;

import java.util.List;
import java.util.Map;
import miyagimayhem.model.FlavorLine;

/**
 * Data access for the editable show text (challenge names, reactions, quotes).
 */
public interface FlavorDao {

    /** Every line, grouped by kind (PHYSICAL, LOSER, QUOTE and so on). */
    Map<String, List<String>> findAllGrouped();

    /** All lines of one kind, in the order they were added. */
    List<FlavorLine> findByKind(String kind);

    void add(String kind, String text);

    void delete(int id);
}
