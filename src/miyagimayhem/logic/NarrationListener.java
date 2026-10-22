package miyagimayhem.logic;

import miyagimayhem.model.NarratedLine;

/**
 * Observer interface. Anything that wants to hear the game's narration
 * (a transcript recorder, a test, a GUI) implements this and registers
 * itself with the Narrator.
 */
public interface NarrationListener {

    void onLine(NarratedLine line);
}
