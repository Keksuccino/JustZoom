package de.keksuccino.justzoom.compat.gui.tabs;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;

class TabManagerTest {

    @Test
    void removesOldWidgetsBeforeAddingNewWidgetsAndLaysOutTheSelection() {
        List<String> events = new ArrayList<>();
        TabManager manager = new TabManager(widget -> events.add("add"), widget -> events.add("remove"), () -> events.add("sound"));
        FakeTab first = new FakeTab();
        FakeTab second = new FakeTab();
        Tab.Area area = new Tab.Area(0, 24, 320, 180);
        manager.setTabArea(area);
        manager.setCurrentTab(first, false);
        manager.setCurrentTab(second, true);
        assertEquals(List.of("add", "remove", "add", "sound"), events);
        assertSame(second, manager.getCurrentTab());
        assertSame(area, second.area);
        assertEquals(1, first.layouts);
        assertEquals(1, second.layouts);
    }

    @Test
    void repeatedSelectionIsANoOpAndResizeAndTickOnlyReachTheCurrentTab() {
        int[] callbacks = {0};
        TabManager manager = new TabManager(widget -> callbacks[0]++, widget -> callbacks[0]++, () -> callbacks[0]++);
        FakeTab first = new FakeTab();
        manager.tickCurrent();
        assertNull(manager.getCurrentTab());
        manager.setCurrentTab(first, false);
        manager.setCurrentTab(first, true);
        assertEquals(1, callbacks[0]);
        manager.setTabArea(new Tab.Area(0, 24, 640, 400));
        manager.tickCurrent();
        assertEquals(1, first.layouts);
        assertEquals(1, first.ticks);
        assertEquals(640, first.area.width());
    }

    @Test
    void rejectsNullTabBeforeRemovingTheCurrentSelection() {
        TabManager manager = new TabManager(widget -> {}, widget -> fail(), () -> {});
        FakeTab tab = new FakeTab();
        manager.setCurrentTab(tab, false);
        assertThrows(NullPointerException.class, () -> manager.setCurrentTab(null, false));
        assertSame(tab, manager.getCurrentTab());
    }

    private static final class FakeTab implements Tab {

        private Tab.Area area;
        private int layouts;
        private int ticks;

        @Override
        public Component getTabTitle() {
            return Component.empty();
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> consumer) {
            consumer.accept(null);
        }

        @Override
        public void doLayout(Tab.Area area) {
            this.area = area;
            this.layouts++;
        }

        @Override
        public void tick() {
            this.ticks++;
        }

    }

}
