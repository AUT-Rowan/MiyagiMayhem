package miyagimayhem.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import miyagimayhem.model.NarratedLine;

/**
 * An Observer that simply remembers every line it hears, in order. After a
 * game finishes, the recorded lines become the transcript that the GUI
 * replays and the database stores.
 */
public class TranscriptRecorder implements NarrationListener {

    private final List<NarratedLine> lines = new ArrayList<>();

    @Override
    public void onLine(NarratedLine line) {
        lines.add(line);
    }

    public List<NarratedLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
}
