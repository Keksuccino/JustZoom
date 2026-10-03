package de.keksuccino.justzoom.util.config.gui;

import de.keksuccino.justzoom.compat.gui.ConfigButton;
import de.keksuccino.justzoom.compat.gui.tabs.TabNavigationBar;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigScreenLayoutTest {

    @Test
    void resizingPreservesSelectedTabAndItsScrollPosition() {
        TestScreen screen = new TestScreen(480, 240);
        screen.tabBar().selectTab(1, false);
        ConfigOptionsList options = screen.optionsList();
        options.setRows(IntStream.range(0, 30).mapToObj(index -> new ConfigOptionsList.Row(List.of())).toList());
        AbstractSelectionList<?> list = (AbstractSelectionList<?>) options.children().get(0);
        list.setScrollAmount(52.0D);

        screen.resize(null, 640, 280);

        assertTrue(screen.isTabSelected(screen.tabs.get(1)));
        assertSame(options, screen.optionsList());
        assertEquals(52.0D, list.getScrollAmount());
        assertEquals(640, options.getWidth());
        assertEquals(247, options.y + options.getHeight());
        assertEquals(245, screen.footer().x);
        assertEquals(254, screen.footer().y);
    }

    @Test
    void resizingUpdatesTheExistingTabBarBounds() {
        TestScreen screen = new TestScreen(480, 240);
        TabNavigationBar bar = screen.tabBar();

        screen.resize(null, 320, 240);

        assertSame(bar, screen.tabBar());
        assertTrue(bar.getRectangle().left() >= 0);
        assertTrue(bar.getRectangle().left() + bar.getRectangle().width() <= 320);
        assertEquals(24, bar.getRectangle().bottom());
    }

    @Test
    void rebuildingWidgetsDoesNotAccumulateFooterControls() {
        TestScreen screen = new TestScreen(480, 240);
        int childCount = screen.children().size();

        screen.rebuild();
        screen.rebuild();

        assertEquals(childCount, screen.children().size());
        assertEquals(1, screen.children().stream().filter(ConfigButton.class::isInstance).count());
    }

    private static final class TestScreen extends ConfigScreen {

        private List<OptionsTab> tabs;

        private TestScreen(int width, int height) {
            super(null, Component.empty(), "justzoom.test.layout");
            this.width = width;
            this.height = height;
            this.init();
        }

        @Override
        protected List<OptionsTab> buildTabs() {
            this.tabs = List.of(this.createTab(Component.empty()), this.createTab(Component.empty()), this.createTab(Component.empty()));
            return this.tabs;
        }

        private void rebuild() {
            this.rebuildWidgets();
        }

        private TabNavigationBar tabBar() {
            return this.children().stream().filter(TabNavigationBar.class::isInstance).map(TabNavigationBar.class::cast).findFirst().orElseThrow();
        }

        private ConfigButton footer() {
            return this.children().stream().filter(ConfigButton.class::isInstance).map(ConfigButton.class::cast).findFirst().orElseThrow();
        }

        private ConfigOptionsList optionsList() {
            return this.children().stream().filter(ConfigOptionsList.class::isInstance).map(ConfigOptionsList.class::cast).findFirst().orElseThrow();
        }

    }

}
