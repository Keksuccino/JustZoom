package de.keksuccino.justzoom.util.config.gui;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigOptionsListTest {

    @Test
    void repeatedTabVisitsPreserveScrollPositionAndRows() {
        ConfigOptionsList wrapper = new ConfigOptionsList(null, 320, 40, 0, 280);
        List<ConfigOptionsList.Row> rows = rows(10);
        wrapper.setRows(rows);
        AbstractSelectionList<?> list = (AbstractSelectionList<?>) wrapper.children().get(0);
        list.setScrollAmount(52.0D);

        wrapper.setRows(rows);

        assertEquals(52.0D, list.getScrollAmount());
        assertEquals(rows, list.children());
    }

    @Test
    void replacingContentResetsScrollAndDragging() {
        ConfigOptionsList wrapper = new ConfigOptionsList(null, 320, 40, 0, 280);
        wrapper.setRows(rows(10));
        AbstractSelectionList<?> list = (AbstractSelectionList<?>) wrapper.children().get(0);
        list.setScrollAmount(52.0D);
        wrapper.setDragging(true);
        List<ConfigOptionsList.Row> replacement = rows(2);

        wrapper.setRows(replacement);

        assertEquals(0.0D, list.getScrollAmount());
        assertEquals(replacement, list.children());
        assertFalse(wrapper.isDragging());
    }

    @Test
    void clearingContentRemovesOldEntries() {
        ConfigOptionsList wrapper = new ConfigOptionsList(null, 320, 40, 0, 280);
        wrapper.setRows(rows(10));
        wrapper.setRows(List.of());
        AbstractSelectionList<?> list = (AbstractSelectionList<?>) wrapper.children().get(0);
        assertTrue(list.children().isEmpty());
        assertEquals(0.0D, list.getScrollAmount());
    }

    @Test
    void resizingPreservesAValidScrollOffset() {
        ConfigOptionsList wrapper = new ConfigOptionsList(null, 320, 40, 0, 280);
        wrapper.setRows(rows(10));
        AbstractSelectionList<?> list = (AbstractSelectionList<?>) wrapper.children().get(0);
        list.setScrollAmount(52.0D);

        wrapper.setBounds(480, 100, 24, 360);

        assertEquals(52.0D, list.getScrollAmount());
    }

    @Test
    void growingTheViewportClampsScrollingToTheRemainingContent() {
        ConfigOptionsList wrapper = new ConfigOptionsList(null, 320, 40, 0, 280);
        wrapper.setRows(rows(10));
        AbstractSelectionList<?> list = (AbstractSelectionList<?>) wrapper.children().get(0);
        list.setScrollAmount(list.getMaxScroll());

        wrapper.setBounds(480, 200, 24, 360);

        assertEquals((double) list.getMaxScroll(), list.getScrollAmount());

        wrapper.setBounds(480, 400, 24, 360);

        assertEquals(0.0D, list.getScrollAmount());
    }

    private static List<ConfigOptionsList.Row> rows(int count) {
        return IntStream.range(0, count).mapToObj(index -> new ConfigOptionsList.Row(List.of())).toList();
    }

}
