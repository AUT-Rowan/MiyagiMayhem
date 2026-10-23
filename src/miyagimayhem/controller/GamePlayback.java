package miyagimayhem.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import miyagimayhem.model.Contestant;
import miyagimayhem.model.GameSession;
import miyagimayhem.model.LineStyle;
import miyagimayhem.model.NarratedLine;

/**
 * Keeps track of how much of a finished game the viewer has seen so far, and
 * where each contestant stands at that point. It has no Swing code, so the
 * game screen only has to draw what this class reports.
 */
public class GamePlayback {

    private final GameSession session;
    private final Map<String, ContestantStatus> statuses = new LinkedHashMap<>();
    private int nextLine;

    public GamePlayback(GameSession session) {
        this.session = session;
        for (Contestant contestant : session.getContestants()) {
            statuses.put(contestant.getName(), ContestantStatus.ACTIVE);
        }
    }

    public GameSession getSession() {
        return session;
    }

    public boolean isFinished() {
        return nextLine >= session.getLines().size();
    }

    /** Contestant names in roster order. */
    public List<String> getContestantNames() {
        return new ArrayList<>(statuses.keySet());
    }

    public ContestantStatus getStatus(String name) {
        return statuses.getOrDefault(name, ContestantStatus.ACTIVE);
    }

    /**
     * Reveals lines up to and including the next one that waits for the
     * viewer (headers and blank lines come along with the line after them).
     * Returns the lines that were just revealed.
     */
    public List<NarratedLine> revealNext() {
        List<NarratedLine> revealed = new ArrayList<>();
        List<NarratedLine> lines = session.getLines();
        boolean stop = false;
        while (!stop && nextLine < lines.size()) {
            NarratedLine line = lines.get(nextLine++);
            apply(line);
            revealed.add(line);
            stop = line.isPause();
        }
        if (isFinished()) {
            markWinner();
        }
        return revealed;
    }

    /** Reveals everything that is left. */
    public List<NarratedLine> revealAll() {
        List<NarratedLine> revealed = new ArrayList<>();
        while (!isFinished()) {
            revealed.addAll(revealNext());
        }
        return Collections.unmodifiableList(revealed);
    }

    private void apply(NarratedLine line) {
        if (line.getStyle() == LineStyle.ROUND) {
            clearImmunity();
        }
        String subject = line.getSubject();
        if (subject == null || !statuses.containsKey(subject)) {
            return;
        }
        switch (line.getTag()) {
            case ELIMINATED:
                statuses.put(subject, ContestantStatus.OUT);
                break;
            case IMMUNITY_GRANTED:
                if (statuses.get(subject) != ContestantStatus.OUT) {
                    statuses.put(subject, ContestantStatus.IMMUNE);
                }
                break;
            case IMMUNITY_CLEARED:
                if (statuses.get(subject) == ContestantStatus.IMMUNE) {
                    statuses.put(subject, ContestantStatus.ACTIVE);
                }
                break;
            default:
                break;
        }
    }

    private void clearImmunity() {
        for (Map.Entry<String, ContestantStatus> entry : statuses.entrySet()) {
            if (entry.getValue() == ContestantStatus.IMMUNE) {
                entry.setValue(ContestantStatus.ACTIVE);
            }
        }
    }

    private void markWinner() {
        String winner = session.getWinnerName();
        if (statuses.containsKey(winner)) {
            statuses.put(winner, ContestantStatus.WINNER);
        }
    }
}
