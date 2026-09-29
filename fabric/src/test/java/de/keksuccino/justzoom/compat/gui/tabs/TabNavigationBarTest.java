package de.keksuccino.justzoom.compat.gui.tabs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TabNavigationBarTest {

    @Test
    void cyclesInBothDirectionsAndWrapsAtEitherEnd() {
        assertEquals(1, TabNavigationBar.shortcutTabIndex(258, true, false, 0, 3));
        assertEquals(0, TabNavigationBar.shortcutTabIndex(258, true, false, 2, 3));
        assertEquals(2, TabNavigationBar.shortcutTabIndex(258, true, true, 0, 3));
        assertEquals(0, TabNavigationBar.shortcutTabIndex(258, true, true, 0, 1));
    }

    @Test
    void numberShortcutsClampToTheLastExistingTab() {
        assertEquals(0, TabNavigationBar.shortcutTabIndex(49, true, false, 1, 3));
        assertEquals(1, TabNavigationBar.shortcutTabIndex(50, true, false, 0, 3));
        assertEquals(2, TabNavigationBar.shortcutTabIndex(57, true, false, 0, 3));
    }

    @Test
    void leavesUnrelatedKeysAndMissingSelectionsToTheScreen() {
        assertEquals(-1, TabNavigationBar.shortcutTabIndex(258, false, false, 0, 3));
        assertEquals(-1, TabNavigationBar.shortcutTabIndex(65, true, false, 0, 3));
        assertEquals(-1, TabNavigationBar.shortcutTabIndex(258, true, false, -1, 3));
        assertEquals(-1, TabNavigationBar.shortcutTabIndex(49, true, false, 0, 0));
    }

}
