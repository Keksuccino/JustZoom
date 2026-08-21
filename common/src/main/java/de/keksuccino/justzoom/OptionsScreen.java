package de.keksuccino.justzoom;

import com.mojang.blaze3d.platform.InputConstants;
import de.keksuccino.justzoom.platform.Services;
import de.keksuccino.justzoom.util.config.ConfigValue;
import de.keksuccino.konkrete.math.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class OptionsScreen extends Screen {

    protected static final int BUTTON_HEIGHT = 20;
    protected static final int BUTTON_ROW_MAX_WIDTH = 360;
    protected static final int CYCLE_VALUE_COLOR = 0xFFAA00;
    protected static final int DISABLED_CYCLE_VALUE_COLOR = 0xFF5555;
    protected static final int FLOAT_INPUT_GAP = 5;
    protected static final int FLOAT_INPUT_MIN_WIDTH = 40;
    protected static final int KEYBIND_RESET_BUTTON_WIDTH = 50;
    protected static final int KEYBIND_GAP = 5;
    protected static final int OPTION_ROW_ADVANCE = 26;
    protected static final int OPTION_SECTION_PADDING_TOP = 10;
    protected static final Identifier TAB_HEADER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/tab_header_background.png");
    protected static final KeybindSetting ZOOM_KEYBIND = new KeybindSetting(KeyMappings.KEY_TOGGLE_ZOOM, "justzoom.options.zoom_keybind", "justzoom.options.zoom_keybind.desc");
    protected static final KeybindSetting ZOOM_IN_KEYBIND = new KeybindSetting(KeyMappings.KEY_ZOOM_IN, "justzoom.options.zoom_in_keybind", "justzoom.options.zoom_in_keybind.desc");
    protected static final KeybindSetting ZOOM_OUT_KEYBIND = new KeybindSetting(KeyMappings.KEY_ZOOM_OUT, "justzoom.options.zoom_out_keybind", "justzoom.options.zoom_out_keybind.desc");
    protected static final List<KeybindSetting> KEYBIND_SETTINGS = List.of(ZOOM_KEYBIND, ZOOM_IN_KEYBIND, ZOOM_OUT_KEYBIND);

    @Nullable
    protected Screen parent;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    @Nullable
    private MenuTabBar tabNavigationBar;
    @Nullable
    private KeyMapping waitingForKeybind;
    private final List<Button> fullWidthOptionButtons = new ArrayList<>();
    private final List<FloatInputControl> floatInputControls = new ArrayList<>();
    private final List<KeybindControl> keybindControls = new ArrayList<>();

    public OptionsScreen(@Nullable Screen parent) {
        super(Component.translatable("justzoom.options"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        this.layout.removeChildren();
        this.fullWidthOptionButtons.clear();
        this.floatInputControls.clear();
        this.keybindControls.clear();

        OptionsTab generalTab = this.buildGeneralTab();
        OptionsTab controlsTab = this.buildControlsTab();
        this.tabNavigationBar = MenuTabBar.builder(this.tabManager, this.width).addTabs(generalTab, controlsTab).build();
        this.addRenderableWidget(this.tabNavigationBar);

        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, ignored -> this.onClose()).size(150, BUTTON_HEIGHT).build());
        this.layout.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            this.addRenderableWidget(widget);
        });

        this.updateControlWidths();
        this.updateKeybindButtons();
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();

    }

    @NotNull
    protected OptionsTab buildGeneralTab() {
        OptionsTab tab = new OptionsTab(Component.translatable("justzoom.options.tab.general"));
        this.addFloatInput(tab, JustZoom.getOptions().baseMagnification, "justzoom.options.base_magnification");
        this.addFloatInput(tab, JustZoom.getOptions().scrollMagnificationMultiplier, "justzoom.options.scroll_magnification_multiplier");
        this.addFullWidthOption(tab, this.buildToggleButton(JustZoom.getOptions().smoothZoomInOut, "justzoom.options.smooth_zoom_in_out"), settings -> settings.paddingTop(OPTION_SECTION_PADDING_TOP));
        this.addFullWidthOption(tab, this.buildToggleButton(JustZoom.getOptions().smoothCameraOnZoom, "justzoom.options.smooth_camera_movement_on_zoom"));
        this.addFullWidthOption(tab, this.buildToggleButton(JustZoom.getOptions().normalizeMouseSensitivityOnZoom, "justzoom.options.normalize_mouse_sensitivity_on_zoom"));
        this.addFullWidthOption(tab, this.buildToggleButton(JustZoom.getOptions().allowZoomInMirroredView, "justzoom.options.allow_zoom_in_mirrored_view"));
        this.addFullWidthOption(tab, this.buildToggleButton(JustZoom.getOptions().improveThirdPersonZoom, "justzoom.options.improve_third_person_zoom"));
        this.addFullWidthOption(tab, this.buildToggleButton(JustZoom.getOptions().hideArmsWhenZooming, "justzoom.options.hide_arms_when_zooming"));
        this.addFullWidthOption(tab, this.buildShowHudButton());
        this.addFullWidthOption(tab, this.buildToggleButton(JustZoom.getOptions().resetZoomFactorOnStopZooming, "justzoom.options.reset_zoom_factor_when_stop_zooming"));
        this.addFullWidthOption(tab, this.buildToggleButton(JustZoom.getOptions().useJustZoomForSpyglass, "justzoom.options.use_just_zoom_for_spyglass"), settings -> settings.paddingTop(OPTION_SECTION_PADDING_TOP));
        this.addFullWidthOption(tab, this.buildSpyglassOverlayButton());
        this.addFullWidthOption(tab, this.buildSpyglassSoundsButton());
        return tab;
    }

    @NotNull
    protected OptionsTab buildControlsTab() {
        OptionsTab tab = new OptionsTab(Component.translatable("justzoom.options.tab.controls"));
        for (KeybindSetting setting : KEYBIND_SETTINGS) {
            this.addKeybindRow(tab, setting);
        }
        return tab;
    }

    protected void addFloatInput(@NotNull OptionsTab tab, @NotNull ConfigValue<Float> option, @NotNull String labelBaseKey) {
        Component label = Component.translatable(labelBaseKey);
        Tooltip tooltip = Tooltip.create(Component.translatable(labelBaseKey + ".desc"));
        StringWidget labelWidget = new StringWidget(label, this.font);
        labelWidget.setTooltip(tooltip);

        EditBox input = new EditBox(this.font, FLOAT_INPUT_MIN_WIDTH, BUTTON_HEIGHT, label);
        input.setValue(Float.toString(option.getValue()));
        input.setResponder(value -> {
            if (MathUtils.isFloat(value)) {
                option.setValue(Float.parseFloat(value));
            }
        });
        input.setTooltip(tooltip);

        LinearLayout row = LinearLayout.horizontal().spacing(FLOAT_INPUT_GAP);
        row.addChild(labelWidget, settings -> settings.alignVerticallyMiddle());
        row.addChild(input);
        this.floatInputControls.add(new FloatInputControl(labelWidget, input, labelWidget.getWidth()));
        tab.addChild(row);
    }

    protected void addFullWidthOption(@NotNull OptionsTab tab, @NotNull Button button) {
        this.fullWidthOptionButtons.add(button);
        tab.addChild(button);
    }

    protected void addFullWidthOption(@NotNull OptionsTab tab, @NotNull Button button, @NotNull Consumer<LayoutSettings> settings) {
        this.fullWidthOptionButtons.add(button);
        tab.addChild(button, settings);
    }

    @NotNull
    protected Button buildToggleButton(@NotNull ConfigValue<Boolean> option, @NotNull String labelBaseKey) {
        Button button = Button.builder(this.toggleMessage(option, labelBaseKey), pressedButton -> {
            option.setValue(!option.getValue());
            pressedButton.setMessage(this.toggleMessage(option, labelBaseKey));
        }).size(this.getButtonWidth(), BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable(labelBaseKey + ".desc"))).build();
        return button;
    }

    @NotNull
    protected Button buildShowHudButton() {
        ConfigValue<ShowHudMode> option = JustZoom.getOptions().showHud;
        return this.buildCycleButton(option, ShowHudMode::next, this::showHudMessage, "justzoom.options.show_hud.desc");
    }

    @NotNull
    protected Button buildSpyglassOverlayButton() {
        ConfigValue<SpyglassOverlayMode> option = JustZoom.getOptions().spyglassOverlay;
        return this.buildCycleButton(option, SpyglassOverlayMode::next, this::spyglassOverlayMessage, "justzoom.options.spyglass_overlay.desc");
    }

    @NotNull
    protected Button buildSpyglassSoundsButton() {
        ConfigValue<SpyglassSoundsMode> option = JustZoom.getOptions().spyglassSounds;
        return this.buildCycleButton(option, SpyglassSoundsMode::next, this::spyglassSoundsMessage, "justzoom.options.spyglass_sounds.desc");
    }

    @NotNull
    protected <T> Button buildCycleButton(@NotNull ConfigValue<T> option, @NotNull UnaryOperator<T> nextValue, @NotNull Function<T, Component> messageFactory, @NotNull String descriptionKey) {
        Button button = Button.builder(messageFactory.apply(option.getValue()), pressedButton -> {
            option.update(nextValue);
            pressedButton.setMessage(messageFactory.apply(option.getValue()));
        }).size(this.getButtonWidth(), BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable(descriptionKey))).build();
        return button;
    }

    protected void addKeybindRow(@NotNull OptionsTab tab, @NotNull KeybindSetting setting) {
        Button keybindButton = this.buildKeybindButton(setting);
        Button keybindResetButton = this.buildKeybindResetButton(setting);
        this.keybindControls.add(new KeybindControl(setting, keybindButton, keybindResetButton));
        tab.addChild(this.buildKeybindRowLayout(keybindButton, keybindResetButton));
    }

    @NotNull
    protected Button buildKeybindButton(@NotNull KeybindSetting setting) {
        KeyMapping keyMapping = setting.keyMapping();
        return Button.builder(Component.empty(), ignored -> {
            this.waitingForKeybind = keyMapping;
            this.updateKeybindButtons();
        }).size(this.getButtonWidth() - KEYBIND_RESET_BUTTON_WIDTH - KEYBIND_GAP, BUTTON_HEIGHT).createNarration(defaultNarrationSupplier -> keyMapping.isUnbound() ? Component.translatable("narrator.controls.unbound", Component.translatable(keyMapping.getName())) : Component.translatable("narrator.controls.bound", Component.translatable(keyMapping.getName()), defaultNarrationSupplier.get())).build();
    }

    @NotNull
    protected Button buildKeybindResetButton(@NotNull KeybindSetting setting) {
        KeyMapping keyMapping = setting.keyMapping();
        return Button.builder(Component.translatable("controls.reset"), ignored -> {
            Services.PLATFORM.setKeyMappingKey(keyMapping, keyMapping.getDefaultKey());
            this.afterKeybindChanged();
        }).size(KEYBIND_RESET_BUTTON_WIDTH, BUTTON_HEIGHT).createNarration(defaultNarrationSupplier -> Component.translatable("narrator.controls.reset", Component.translatable(keyMapping.getName()))).build();
    }

    protected void updateControlWidths() {
        int rowWidth = this.getButtonWidth();
        for (Button button : this.fullWidthOptionButtons) {
            button.setWidth(rowWidth);
        }
        for (FloatInputControl control : this.floatInputControls) {
            FloatInputWidths widths = calculateFloatInputWidths(rowWidth, control.preferredLabelWidth());
            control.label().setWidth(widths.labelWidth());
            control.input().setWidth(widths.inputWidth());
        }
        for (KeybindControl control : this.keybindControls) {
            control.keybindButton().setWidth(rowWidth - KEYBIND_RESET_BUTTON_WIDTH - KEYBIND_GAP);
            control.resetButton().setWidth(KEYBIND_RESET_BUTTON_WIDTH);
        }
    }

    protected void updateKeybindButtons() {
        for (KeybindControl control : this.keybindControls) {
            KeyMapping keyMapping = control.setting().keyMapping();
            control.keybindButton().setMessage(this.keybindMessage(control.setting()));
            control.keybindButton().setTooltip(this.keybindTooltip(control.setting()));
            control.resetButton().active = !keyMapping.isDefault();
        }
    }

    @NotNull
    protected Component toggleMessage(@NotNull ConfigValue<Boolean> option, @NotNull String labelBaseKey) {
        Component value = Component.translatable(option.getValue() ? "justzoom.options.toggle.enabled" : "justzoom.options.toggle.disabled").withStyle(Style.EMPTY.withColor(option.getValue() ? ChatFormatting.GREEN : ChatFormatting.RED));
        return Component.translatable(labelBaseKey, value);
    }

    @NotNull
    protected Component showHudMessage(@NotNull ShowHudMode mode) {
        Component value = Component.translatable(mode.getTranslationKey()).withStyle(Style.EMPTY.withColor(showHudValueColor(mode)));
        return Component.translatable("justzoom.options.show_hud", value);
    }

    static int showHudValueColor(@NotNull ShowHudMode mode) {
        return spyglassCycleValueColor(mode == ShowHudMode.NEVER);
    }

    @NotNull
    protected Component spyglassOverlayMessage(@NotNull SpyglassOverlayMode mode) {
        Component value = Component.translatable(mode.getTranslationKey()).withStyle(Style.EMPTY.withColor(spyglassOverlayValueColor(mode)));
        return Component.translatable("justzoom.options.spyglass_overlay", value);
    }

    static int spyglassOverlayValueColor(@NotNull SpyglassOverlayMode mode) {
        return spyglassCycleValueColor(mode == SpyglassOverlayMode.DISABLED);
    }

    @NotNull
    protected Component spyglassSoundsMessage(@NotNull SpyglassSoundsMode mode) {
        Component value = Component.translatable(mode.getTranslationKey()).withStyle(Style.EMPTY.withColor(spyglassSoundsValueColor(mode)));
        return Component.translatable("justzoom.options.spyglass_sounds", value);
    }

    static int spyglassSoundsValueColor(@NotNull SpyglassSoundsMode mode) {
        return spyglassCycleValueColor(mode == SpyglassSoundsMode.DISABLED);
    }

    private static int spyglassCycleValueColor(boolean disabled) {
        return disabled ? DISABLED_CYCLE_VALUE_COLOR : CYCLE_VALUE_COLOR;
    }

    @NotNull
    protected Component keybindMessage(@NotNull KeybindSetting setting) {
        KeyMapping keyMapping = setting.keyMapping();
        Component value = keyMapping.getTranslatedKeyMessage().copy().withStyle(Style.EMPTY.withColor(CYCLE_VALUE_COLOR));
        Component message = Component.translatable(setting.labelKey(), value);
        if (this.waitingForKeybind == keyMapping) {
            return Component.literal("> ").append(message.copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW);
        }
        if (this.hasKeybindCollision(keyMapping)) {
            return Component.literal("[ ").append(message.copy().withStyle(ChatFormatting.WHITE)).append(" ]").withStyle(ChatFormatting.YELLOW);
        }
        return message;
    }

    @Nullable
    protected Tooltip keybindTooltip(@NotNull KeybindSetting setting) {
        KeyMapping keyMapping = setting.keyMapping();
        if (!this.hasKeybindCollision(keyMapping)) {
            return Tooltip.create(Component.translatable(setting.descriptionKey()));
        }

        MutableComponent collisions = Component.empty();
        boolean first = true;
        if (this.minecraft != null) {
            for (KeyMapping otherKey : this.minecraft.options.keyMappings) {
                if (otherKey != keyMapping && keyMapping.same(otherKey) && (!otherKey.isDefault() || !keyMapping.isDefault())) {
                    if (!first) {
                        collisions.append(", ");
                    }
                    collisions.append(Component.translatable(otherKey.getName()));
                    first = false;
                }
            }
        }
        return Tooltip.create(Component.translatable("justzoom.options.keybind.duplicate_desc", collisions));
    }

    @NotNull
    protected LinearLayout buildKeybindRowLayout(@NotNull Button keyButton, @NotNull Button resetButton) {
        int rowWidth = this.getButtonWidth();
        keyButton.setWidth(rowWidth - resetButton.getWidth() - KEYBIND_GAP);
        LinearLayout row = LinearLayout.horizontal().spacing(KEYBIND_GAP);
        row.addChild(keyButton);
        row.addChild(resetButton);
        return row;
    }

    protected int getButtonWidth() {
        return Math.min(BUTTON_ROW_MAX_WIDTH, this.width - 40);
    }

    protected boolean hasKeybindCollision(@NotNull KeyMapping keyMapping) {
        return this.minecraft != null && hasKeybindCollision(keyMapping, this.minecraft.options.keyMappings);
    }

    static boolean hasKeybindCollision(@NotNull KeyMapping keyMapping, KeyMapping @NotNull [] keyMappings) {
        if (keyMapping.isUnbound()) {
            return false;
        }
        for (KeyMapping otherKey : keyMappings) {
            if (otherKey != keyMapping && keyMapping.same(otherKey) && (!otherKey.isDefault() || !keyMapping.isDefault())) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    static FloatInputWidths calculateFloatInputWidths(int rowWidth, int preferredLabelWidth) {
        // Keep the complete row at the button width so its centered layout shares both button edges.
        int availableWidth = Math.max(2, rowWidth - FLOAT_INPUT_GAP);
        int labelWidth = Math.min(Math.max(1, preferredLabelWidth), Math.max(1, availableWidth - FLOAT_INPUT_MIN_WIDTH));
        return new FloatInputWidths(labelWidth, availableWidth - labelWidth);
    }

    protected void afterKeybindChanged() {
        this.waitingForKeybind = null;
        KeyMapping.resetMapping();
        if (this.minecraft != null) {
            this.minecraft.options.save();
        }
        this.updateKeybindButtons();
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
        if (this.waitingForKeybind != null) {
            Services.PLATFORM.setKeyMappingKey(this.waitingForKeybind, InputConstants.Type.MOUSE.getOrCreate(event.button()));
            this.afterKeybindChanged();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (this.waitingForKeybind != null && KeyMappings.isZoomAdjustment(this.waitingForKeybind) && KeyMappings.hasMouseWheelDirection(deltaY)) {
            Services.PLATFORM.setKeyMappingKey(this.waitingForKeybind, KeyMappings.getMouseWheelKey(deltaY));
            this.afterKeybindChanged();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        if (this.waitingForKeybind != null) {
            Services.PLATFORM.setKeyMappingKey(this.waitingForKeybind, event.isEscape() ? InputConstants.UNKNOWN : InputConstants.getKey(event));
            this.afterKeybindChanged();
            return true;
        }
        if (this.tabNavigationBar != null && this.tabNavigationBar.keyPressed(event)) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar == null) {
            return;
        }
        this.tabNavigationBar.arrangeElements(this.width);
        int tabAreaTop = this.tabNavigationBar.getRectangle().bottom();
        ScreenRectangle tabArea = new ScreenRectangle(0, tabAreaTop, this.width, Math.max(0, this.height - this.layout.getFooterHeight() - tabAreaTop));
        this.tabManager.setTabArea(tabArea);
        this.layout.setHeaderHeight(tabAreaTop);
        this.layout.arrangeElements();
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight(), 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    @Override
    protected void extractMenuBackground(@NotNull GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TAB_HEADER_BACKGROUND, 0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.extractMenuBackground(graphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    protected record KeybindSetting(@NotNull KeyMapping keyMapping, @NotNull String labelKey, @NotNull String descriptionKey) {
    }

    static record FloatInputWidths(int labelWidth, int inputWidth) {
    }

    private record FloatInputControl(@NotNull StringWidget label, @NotNull EditBox input, int preferredLabelWidth) {
    }

    private record KeybindControl(@NotNull KeybindSetting setting, @NotNull Button keybindButton, @NotNull Button resetButton) {
    }

    protected class OptionsTab implements Tab {

        private final Component title;
        private final LinearLayout optionsLayout;
        private final ScrollableLayout scrollableLayout;

        protected OptionsTab(@NotNull Component title) {
            this.title = title;
            this.optionsLayout = LinearLayout.vertical().spacing(OPTION_ROW_ADVANCE - BUTTON_HEIGHT);
            this.optionsLayout.defaultCellSetting().alignHorizontallyCenter();
            this.scrollableLayout = new ScrollableLayout(OptionsScreen.this.minecraft, this.optionsLayout, BUTTON_HEIGHT, ScrollableLayout.ReserveStrategy.BOTH);
            this.scrollableLayout.setScrollbarSpacing(2);
        }

        protected void addChild(@NotNull LayoutElement child) {
            this.optionsLayout.addChild(child);
        }

        protected void addChild(@NotNull LayoutElement child, @NotNull Consumer<LayoutSettings> settings) {
            this.optionsLayout.addChild(child, settings);
        }

        @Override
        public Component getTabTitle() {
            return this.title;
        }

        @Override
        public Component getTabExtraNarration() {
            return Component.empty();
        }

        @Override
        public void visitChildren(@NotNull Consumer<AbstractWidget> childrenConsumer) {
            // ScrollableLayout caches its content widgets, so refresh before TabManager registers it.
            this.scrollableLayout.arrangeElements();
            this.scrollableLayout.visitWidgets(childrenConsumer);
        }

        @Override
        public void doLayout(@NotNull ScreenRectangle screenRectangle) {
            OptionsScreen.this.updateControlWidths();
            int topY = Math.max(screenRectangle.top() + 4, Math.min(50, screenRectangle.bottom() - BUTTON_HEIGHT));
            this.scrollableLayout.setMinWidth(OptionsScreen.this.getButtonWidth());
            this.scrollableLayout.arrangeElements();
            this.scrollableLayout.setMaxHeight(Math.max(BUTTON_HEIGHT, screenRectangle.bottom() - topY));
            this.scrollableLayout.setPosition((OptionsScreen.this.width - this.scrollableLayout.getWidth()) / 2, topY);
        }

        @Override
        public Layout getLayout() {
            return this.scrollableLayout;
        }

    }

}
