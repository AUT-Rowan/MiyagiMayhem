package miyagimayhem.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import miyagimayhem.logic.RosterValidator;
import org.junit.Before;
import org.junit.Test;

public class RosterEditorTest {

    private RosterEditor editor;

    @Before
    public void createEditor() {
        editor = new RosterEditor();
    }

    @Test
    public void addedNamesAreTidiedUpAndKeptInOrder() {
        assertNull(editor.add("  ann  "));
        assertNull(editor.add("bob"));
        assertEquals(Arrays.asList("Ann", "Bob"), editor.getNames());
    }

    @Test
    public void refusedNameIsNotAddedAndComesWithAMessage() {
        editor.add("Ann");
        assertNotNull(editor.add("ANN"));
        assertNotNull(editor.add(""));
        assertEquals(1, editor.size());
    }

    @Test
    public void cannotAddMoreThanTheMaximum() {
        for (int i = 0; i < RosterValidator.MAX_CONTESTANTS; i++) {
            assertNull(editor.add("Player" + i));
        }
        assertNotNull(editor.add("OneTooMany"));
        assertEquals(RosterValidator.MAX_CONTESTANTS, editor.size());
    }

    @Test
    public void readyOnlyWithAtLeastThreeNames() {
        editor.add("A");
        editor.add("B");
        assertFalse(editor.isReady());
        assertNotNull(editor.checkReady());
        editor.add("C");
        assertTrue(editor.isReady());
        assertNull(editor.checkReady());
    }

    @Test
    public void removeAtIgnoresBadIndexes() {
        editor.add("A");
        editor.add("B");
        editor.removeAt(-1);
        editor.removeAt(5);
        assertEquals(2, editor.size());
        editor.removeAt(0);
        assertEquals(Arrays.asList("B"), editor.getNames());
    }

    @Test
    public void replaceWithSwapsTheWholeList() {
        editor.add("Old");
        editor.replaceWith(Arrays.asList("X", "Y", "Z"));
        assertEquals(Arrays.asList("X", "Y", "Z"), editor.getNames());
        editor.clear();
        assertEquals(0, editor.size());
    }

    @Test
    public void getNamesReturnsACopy() {
        editor.add("A");
        List<String> copy = editor.getNames();
        copy.add("Sneaky");
        assertEquals(1, editor.size());
    }
}
