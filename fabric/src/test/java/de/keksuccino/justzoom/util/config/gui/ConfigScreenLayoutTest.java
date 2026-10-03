package de.keksuccino.justzoom.util.config.gui;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigScreenLayoutTest {

    @Test
    void footerUsesTheNativeCreateWorldLayout() {
        for (int height : List.of(200, 240, 333)) {
            TestScreen screen = new TestScreen(480, height);
            Button nativeFooter = Button.builder(Component.empty(), ignored -> {}).width(150).build();
            FrameLayout.centerInRectangle(nativeFooter, 0, height - 36, 480, 36);

            assertEquals(nativeFooter.getRectangle(), screen.footer().getRectangle());
            assertEquals(height - 36, screen.optionsList().getY() + screen.optionsList().getHeight());
            assertTrue(screen.optionsList().getY() >= screen.tabBar().getRectangle().bottom());
        }
    }

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
        assertEquals(244, options.getY() + options.getHeight());
    }

    @Test
    void rebuildingWidgetsDoesNotAccumulateFooterControls() {
        TestScreen screen = new TestScreen(480, 240);
        int childCount = screen.children().size();

        screen.rebuild();
        screen.rebuild();

        assertEquals(childCount, screen.children().size());
        assertEquals(1, screen.children().stream().filter(Button.class::isInstance).count());
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

        private Button footer() {
            return this.children().stream().filter(Button.class::isInstance).map(Button.class::cast).findFirst().orElseThrow();
        }

        private ConfigOptionsList optionsList() {
            return this.children().stream().filter(ConfigOptionsList.class::isInstance).map(ConfigOptionsList.class::cast).findFirst().orElseThrow();
        }

    }

}
