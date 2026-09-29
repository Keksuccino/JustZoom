package de.keksuccino.justzoom.util.config.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConfigTabTest {

    @Test
    void capturesRowsAddedAfterTabCreationAndReusesContainerOnReselection() throws Exception {
        // Screen's constructor needs a live client. Only the outer reference is needed to exercise
        // the real OptionsTab and vanilla scroll container; no rendering or Minecraft methods run.
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        TestScreen screen = (TestScreen) ((Unsafe) unsafeField.get(null)).allocateInstance(TestScreen.class);
        ConfigScreen.OptionsTab tab = screen.createTab(Component.empty());
        Button first = Button.builder(Component.empty(), ignored -> {}).build();
        Button second = Button.builder(Component.empty(), ignored -> {}).build();
        tab.addChild(first);
        tab.addChild(second);

        List<AbstractWidget> widgets = new ArrayList<>();
        tab.visitChildren(widgets::add);
        assertEquals(1, widgets.size());
        ContainerEventHandler container = (ContainerEventHandler) widgets.getFirst();
        assertEquals(List.of(first, second), container.children());

        List<AbstractWidget> revisited = new ArrayList<>();
        tab.visitChildren(revisited::add);
        assertEquals(1, revisited.size());
        assertSame(container, revisited.getFirst());
        assertEquals(List.of(first, second), container.children());
    }

    private static final class TestScreen extends ConfigScreen {

        private TestScreen() {
            super(null, Component.empty(), "test");
        }

        @Override
        protected List<OptionsTab> buildTabs() {
            return List.of();
        }

    }

}
