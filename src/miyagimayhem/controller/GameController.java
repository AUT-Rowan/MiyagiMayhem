package miyagimayhem.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import miyagimayhem.db.DatabaseManager;
import miyagimayhem.db.FlavorDao;
import miyagimayhem.db.GameDao;
import miyagimayhem.db.JdbcFlavorDao;
import miyagimayhem.db.JdbcGameDao;
import miyagimayhem.db.JdbcRosterDao;
import miyagimayhem.db.RosterDao;
import miyagimayhem.logic.FlavorText;
import miyagimayhem.logic.GameRunner;
import miyagimayhem.logic.RosterValidator;
import miyagimayhem.model.FlavorLine;
import miyagimayhem.model.GameResult;
import miyagimayhem.model.GameSession;
import miyagimayhem.model.NarratedLine;

/**
 * The only class the GUI talks to. It joins the game logic to the database
 * so that the screens never touch SQL or the simulation directly.
 *
 * Validation methods return an error message for the user, or null when the
 * input is fine. Database failures are thrown as DataAccessException.
 */
public class GameController {

    private final RosterDao rosterDao;
    private final FlavorDao flavorDao;
    private final GameDao gameDao;

    public GameController(RosterDao rosterDao, FlavorDao flavorDao, GameDao gameDao) {
        this.rosterDao = rosterDao;
        this.flavorDao = flavorDao;
        this.gameDao = gameDao;
    }

    /** A controller wired to the shared embedded database. */
    public static GameController createDefault() {
        DatabaseManager database = DatabaseManager.getInstance();
        return new GameController(new JdbcRosterDao(database), new JdbcFlavorDao(database),
                new JdbcGameDao(database));
    }

    // ---- rosters ----

    public String validateName(String raw, List<String> existing) {
        return RosterValidator.validateName(raw, existing);
    }

    public String normalizeName(String raw) {
        return RosterValidator.normalize(raw);
    }

    public String validateRoster(List<String> names) {
        return RosterValidator.validateRoster(names);
    }

    public List<String> getRosterNames() {
        return rosterDao.findAllNames();
    }

    public List<String> loadRoster(String rosterName) {
        return rosterDao.findMembers(rosterName);
    }

    /** Saves (or replaces) a roster. Returns an error message or null. */
    public String saveRoster(String rosterName, List<String> members) {
        String error = RosterValidator.validateRosterName(rosterName);
        if (error == null) {
            error = RosterValidator.validateRoster(members);
        }
        if (error == null) {
            rosterDao.save(RosterValidator.normalize(rosterName), new ArrayList<>(members));
        }
        return error;
    }

    public void deleteRoster(String rosterName) {
        rosterDao.delete(rosterName);
    }

    // ---- playing ----

    /** Runs a whole game using the show text currently stored in the database. */
    public GameSession play(List<String> names) {
        FlavorText flavor = new FlavorText(flavorDao.findAllGrouped());
        return new GameRunner(flavor).play(names);
    }

    /** Stores a finished game and its transcript. Returns the new game id. */
    public int saveGame(GameSession session) {
        return gameDao.save(session.getWinnerName(), session.getContestantNames(),
                session.getRounds(), session.getLines());
    }

    // ---- history ----

    public List<GameResult> getHistory() {
        return gameDao.findAll();
    }

    public List<NarratedLine> getTranscript(int gameId) {
        return gameDao.findTranscript(gameId);
    }

    public Map<String, Integer> getLeaderboard() {
        return gameDao.findWinCounts();
    }

    public void clearHistory() {
        gameDao.deleteAll();
    }

    // ---- show text ----

    public List<FlavorLine> getFlavorLines(String kind) {
        return flavorDao.findByKind(kind);
    }

    /** Adds a line of show text. Returns an error message or null. */
    public String addFlavorLine(String kind, String text) {
        String error = FlavorText.validateLine(kind, text);
        if (error == null) {
            flavorDao.add(kind, text.trim());
        }
        return error;
    }

    public void deleteFlavorLine(int id) {
        flavorDao.delete(id);
    }

    // ---- lifecycle ----

    public void shutdown() {
        DatabaseManager.getInstance().shutdown();
    }
}
