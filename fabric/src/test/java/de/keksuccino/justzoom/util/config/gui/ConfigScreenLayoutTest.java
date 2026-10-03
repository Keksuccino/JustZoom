package de.keksuccino.justzoom.util.config.gui;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigScreenLayoutTest {

    @Test
    void footerAndContentUseVanillaLayoutAtDifferentScreenSizes() {
        for (int height : List.of(200, 240, 333)) {
            TestScreen screen = new TestScreen(480, height);
            HeaderAndFooterLayout nativeLayout = new HeaderAndFooterLayout(screen);
            Button nativeFooter = nativeLayout.addToFooter(Button.builder(Component.empty(), ignored -> {}).width(150).build());
            nativeLayout.arrangeElements();

            assertEquals(nativeFooter.getRectangle(), screen.footer().getRectangle());
            assertEquals(height - nativeLayout.getFooterHeight(), screen.optionsList().getBottom());
            assertTrue(screen.optionsList().getY() >= screen.tabBar().getRectangle().bottom());
        }
    }

    @Test
    void resizingPreservesSelectedTabAndItsScrollPosition() {
        TestScreen screen = new TestScreen(480, 240);
        screen.tabBar().selectTab(1, false);
        ConfigOptionsList options = screen.optionsList();
        options.setRows(IntStream.range(0, 30).mapToObj(index -> new ConfigOptionsList.Row(List.of())).toList());
        AbstractSelectionList<?> list = (AbstractSelectionList<?>) options.children().getFirst();
        list.setScrollAmount(52.0D);

        screen.resize(null, 640, 280);

        assertTrue(((TabButton) screen.tabBar().children().get(1)).isSelected());
        assertSame(options, screen.optionsList());
        assertEquals(52.0D, list.getScrollAmount());
        assertEquals(640, options.getWidth());
        assertEquals(247, options.getBottom());
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

        private TestScreen(int width, int height) {
            super(null, Component.empty(), "justzoom.test.layout");
            this.width = width;
            this.height = height;
            this.init();
        }

        @Override
        protected List<OptionsTab> buildTabs() {
            return List.of(this.createTab(Component.empty()), this.createTab(Component.empty()), this.createTab(Component.empty()));
        }

        @Override
        protected void setInitialFocus() {
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
