package miyagimayhem.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import miyagimayhem.model.LineTag;
import miyagimayhem.model.NarratedLine;
import org.junit.Test;

public class NarratorTest {

    private static class Collector implements NarrationListener {
        final List<NarratedLine> lines = new ArrayList<>();

        @Override
        public void onLine(NarratedLine line) {
            lines.add(line);
        }
    }

    @Test
    public void allListenersReceiveEachLine() {
        Narrator narrator = new Narrator();
        Collector first = new Collector();
        Collector second = new Collector();
        narrator.addListener(first);
        narrator.addListener(second);
        narrator.log("hello");
        assertEquals(1, first.lines.size());
        assertEquals(1, second.lines.size());
    }

    @Test
    public void removedListenerStopsReceiving() {
        Narrator narrator = new Narrator();
        Collector collector = new Collector();
        narrator.addListener(collector);
        narrator.removeListener(collector);
        narrator.log("hello");
        assertTrue(collector.lines.isEmpty());
    }

    @Test
    public void taggedLinesCarryTheirTagSubjectAndRound() {
        Narrator narrator = new Narrator();
        Collector collector = new Collector();
        narrator.addListener(collector);
        narrator.setRound(3);
        narrator.log("Ann has been voted out.", LineTag.ELIMINATED, "Ann");
        NarratedLine line = collector.lines.get(0);
        assertEquals(LineTag.ELIMINATED, line.getTag());
        assertEquals("Ann", line.getSubject());
        assertEquals(3, line.getRound());
        assertTrue(line.isPause());
    }

    @Test
    public void headersAndBlankLinesDoNotPause() {
        Narrator narrator = new Narrator();
        Collector collector = new Collector();
        narrator.addListener(collector);
        narrator.header("=== ROUND 1 ===");
        narrator.blank();
        narrator.log("text");
        assertFalse(collector.lines.get(0).isPause());
        assertFalse(collector.lines.get(1).isPause());
        assertTrue(collector.lines.get(2).isPause());
    }
}
