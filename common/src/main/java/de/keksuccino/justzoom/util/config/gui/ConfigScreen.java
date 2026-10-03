package de.keksuccino.justzoom.util.config.gui;

import com.mojang.blaze3d.platform.InputConstants;
import de.keksuccino.justzoom.platform.Services;
import de.keksuccino.justzoom.util.config.ConfigValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Reusable tabbed options UI for {@link ConfigValue} backed settings.
 *
 * <p>Subclasses only need to create tabs in {@link #buildTabs()} and add controls with the provided
 * templates. Each template writes changes immediately, adds a reset button, handles narrow screens,
 * and refreshes its reset state. The translation prefix supplies {@code .toggle.enabled},
 * {@code .toggle.disabled}, and {@code .keybind.duplicate_desc} translations.</p>
 *
 * <pre>{@code
 * @Override
 * protected List<OptionsTab> buildTabs() {
 *     OptionsTab general = this.createTab(Component.translatable("example.options.general"));
 *     this.addToggleOption(general, options.enabled, "example.options.enabled");
 *     this.addPercentageSliderOption(general, options.strength, "example.options.strength", 0, 100);
 *     this.addFloatInputOption(general, options.multiplier, "example.options.multiplier");
 *     return List.of(general);
 * }
 * }</pre>
 */
public abstract class ConfigScreen extends Screen {

    protected static final int BUTTON_HEIGHT = 20;
    protected static final int BUTTON_ROW_MAX_WIDTH = 360;
    protected static final int CONTROL_GAP = 5;
    protected static final int INPUT_GAP = 5;
    protected static final int INPUT_MIN_WIDTH = 75;
    protected static final int INVALID_INPUT_COLOR = 0xFF000000 | ChatFormatting.RED.getColor();
    protected static final int OPTION_ROW_ADVANCE = 26;
    /** Preferred value color for normal cycle states. */
    protected static final ChatFormatting OPTION_VALUE_COLOR = ChatFormatting.GOLD;
    /** Preferred value color for cycle states that represent disabled or equivalent behavior. */
    protected static final ChatFormatting DISABLED_OPTION_VALUE_COLOR = ChatFormatting.RED;
    protected static final int RESET_BUTTON_WIDTH = 50;
    protected static final int FOOTER_HEIGHT = 36;

    @Nullable
    protected Screen parent;
    private final String translationPrefix;
    private GridLayout bottomButtons;
    private List<OptionsTab> tabs = List.of();
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, widget -> {
        if (this.getFocused() == widget) this.setFocused(null);
        if (widget instanceof net.minecraft.client.gui.components.events.ContainerEventHandler container) {
            container.setFocused(null);
            container.setDragging(false);
        }
        this.removeWidget(widget);
    });
    private TabNavigationBar tabNavigationBar;
    private final List<AbstractWidget> primaryControls = new ArrayList<>();
    private final List<InputControl> inputControls = new ArrayList<>();
    private final List<OptionControl> optionControls = new ArrayList<>();
    private final List<KeybindControl> keybindControls = new ArrayList<>();
    @Nullable
    private KeybindSetting waitingForKeybind;

    protected ConfigScreen(@Nullable Screen parent, @NotNull Component title, @NotNull String translationPrefix) {
        super(Objects.requireNonNull(title));
        this.parent = parent;
        this.translationPrefix = requireTranslationPrefix(translationPrefix);
    }

    /** Creates and populates this screen's tabs in display order. */
    @NotNull
    protected abstract List<OptionsTab> buildTabs();

    @Override
    protected final void init() {
        this.primaryControls.clear();
        this.inputControls.clear();
        this.optionControls.clear();
        this.keybindControls.clear();
        this.waitingForKeybind = null;
        this.beforeBuildTabs();

        this.tabs = List.copyOf(Objects.requireNonNull(this.buildTabs()));
        if (this.tabs.isEmpty()) throw new IllegalStateException("A config screen must contain at least one tab.");
        TabNavigationBar.Builder tabBuilder = TabNavigationBar.builder(this.tabManager, this.width);
        for (OptionsTab tab : this.tabs) tabBuilder.addTabs(tab);
        this.tabNavigationBar = tabBuilder.build();
        this.addRenderableWidget(this.tabNavigationBar);
        this.bottomButtons = new GridLayout();
        this.bottomButtons.addChild(Button.builder(CommonComponents.GUI_DONE, ignored -> this.onClose()).size(150, BUTTON_HEIGHT).build(), 0, 0);
        this.bottomButtons.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            this.addRenderableWidget(widget);
        });
        this.updateControlWidths();
        this.updateOptionResetButtons();
        this.updateKeybindButtons();
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
        this.afterBuildTabs();
    }

    /** Lifecycle hook invoked after old controls are discarded and before {@link #buildTabs()}. */
    protected void beforeBuildTabs() {
    }

    /** Lifecycle hook invoked after all controls are registered and positioned. */
    protected void afterBuildTabs() {
    }

    @NotNull
    protected final OptionsTab createTab(@NotNull Component title) {
        return new OptionsTab(Objects.requireNonNull(title));
    }

    protected final void addToggleOption(@NotNull OptionsTab tab, @NotNull ConfigValue<Boolean> option, @NotNull String labelBaseKey) {
        Button button = this.buildToggleButton(option, labelBaseKey);
        this.addButtonOption(tab, option, button, () -> button.setMessage(this.toggleMessage(option, labelBaseKey)));
    }

    /**
     * Adds a cycling option. The value produced by {@code messageFactory} should preferably use
     * {@link #OPTION_VALUE_COLOR}, or {@link #DISABLED_OPTION_VALUE_COLOR} for a disabled-like state.
     */
    protected final <T> void addCycleOption(@NotNull OptionsTab tab, @NotNull ConfigValue<T> option, @NotNull UnaryOperator<T> nextValue, @NotNull Function<T, Component> messageFactory, @NotNull String descriptionKey) {
        Button button = this.buildCycleButton(option, nextValue, messageFactory, descriptionKey);
        this.addButtonOption(tab, option, button, () -> button.setMessage(messageFactory.apply(option.getValue())));
    }

    /** Adds a custom button-based option while retaining standard sizing and reset behavior. */
    protected final void addButtonOption(@NotNull OptionsTab tab, @NotNull ConfigValue<?> option, @NotNull Button button, @NotNull Runnable refreshControl) {
        Button resetButton = this.buildResetButton(option, button::getMessage, refreshControl);
        this.primaryControls.add(button);
        this.optionControls.add(new OptionControl(() -> isOptionDefault(option), resetButton));
        tab.addChild(this.buildControlRowLayout(button, resetButton));
    }

    /** Adds a whole-number slider. The range must divide evenly by the step. */
    @NotNull
    protected final ConfigSlider<Integer> addIntegerSliderOption(@NotNull OptionsTab tab, @NotNull ConfigValue<Integer> option, int minimum, int maximum, int step, @NotNull Function<Integer, Component> messageFactory, @NotNull String descriptionKey) {
        return this.addIntegerSliderOption(tab, option, minimum, maximum, step, messageFactory, descriptionKey, (slider, value) -> {
        });
    }

    /** Adds a whole-number slider and invokes a callback after a changed value is stored. */
    @NotNull
    protected final ConfigSlider<Integer> addIntegerSliderOption(@NotNull OptionsTab tab, @NotNull ConfigValue<Integer> option, int minimum, int maximum, int step, @NotNull Function<Integer, Component> messageFactory, @NotNull String descriptionKey, @NotNull BiConsumer<ConfigSlider<Integer>, Integer> valueChanged) {
        ConfigSlider<Integer> slider = ConfigSlider.integer(option, minimum, maximum, step, messageFactory, valueChanged, this::updateOptionResetButtons, this.getButtonWidth());
        return this.addSliderOption(tab, option, slider, descriptionKey);
    }

    /** Adds a whole-percentage slider with a standard {@code label: value%} message. */
    @NotNull
    protected final ConfigSlider<Integer> addPercentageSliderOption(@NotNull OptionsTab tab, @NotNull ConfigValue<Integer> option, @NotNull String labelBaseKey, int minimumPercentage, int maximumPercentage) {
        return this.addPercentageSliderOption(tab, option, labelBaseKey, minimumPercentage, maximumPercentage, (slider, value) -> {
        });
    }

    /** Adds a whole-percentage slider and invokes a callback after a changed value is stored. */
    @NotNull
    protected final ConfigSlider<Integer> addPercentageSliderOption(@NotNull OptionsTab tab, @NotNull ConfigValue<Integer> option, @NotNull String labelBaseKey, int minimumPercentage, int maximumPercentage, @NotNull BiConsumer<ConfigSlider<Integer>, Integer> valueChanged) {
        return this.addIntegerSliderOption(tab, option, minimumPercentage, maximumPercentage, 1, value -> Component.translatable(labelBaseKey, Component.literal(value + "%")), labelBaseKey + ".desc", valueChanged);
    }

    /** Adds a stepped float slider. The range must divide evenly by the step. */
    @NotNull
    protected final ConfigSlider<Float> addFloatSliderOption(@NotNull OptionsTab tab, @NotNull ConfigValue<Float> option, float minimum, float maximum, float step, @NotNull Function<Float, Component> messageFactory, @NotNull String descriptionKey) {
        return this.addFloatSliderOption(tab, option, minimum, maximum, step, messageFactory, descriptionKey, (slider, value) -> {
        });
    }

    /** Adds a stepped float slider and invokes a callback after a changed value is stored. */
    @NotNull
    protected final ConfigSlider<Float> addFloatSliderOption(@NotNull OptionsTab tab, @NotNull ConfigValue<Float> option, float minimum, float maximum, float step, @NotNull Function<Float, Component> messageFactory, @NotNull String descriptionKey, @NotNull BiConsumer<ConfigSlider<Float>, Float> valueChanged) {
        ConfigSlider<Float> slider = ConfigSlider.floatingPoint(option, minimum, maximum, step, messageFactory, valueChanged, this::updateOptionResetButtons, this.getButtonWidth());
        return this.addSliderOption(tab, option, slider, descriptionKey);
    }

    /**
     * Adds the float input field used by older versions of Just Zoom. Valid values are stored as the
     * user types, while incomplete or invalid text stays editable without replacing the last valid value.
     */
    @NotNull
    protected final EditBox addFloatInputOption(@NotNull OptionsTab tab, @NotNull ConfigValue<Float> option, @NotNull String labelBaseKey) {
        return this.addInputOption(tab, option, Component.translatable(labelBaseKey), Tooltip.create(Component.translatable(labelBaseKey + ".desc")), value -> Float.toString(value), ConfigScreen::parseFloatInput, value -> {
        });
    }

    /**
     * Adds a generic validated input field. Returning {@link Optional#empty()} keeps the current config
     * value and marks the field invalid; returning a value stores it immediately.
     */
    @NotNull
    protected final <T> EditBox addInputOption(@NotNull OptionsTab tab, @NotNull ConfigValue<T> option, @NotNull Component label, @NotNull Tooltip tooltip, @NotNull Function<T, String> formatter, @NotNull Function<String, Optional<T>> parser) {
        return this.addInputOption(tab, option, label, tooltip, formatter, parser, value -> {
        });
    }

    /**
     * Adds a generic validated input field and invokes a callback after a changed value is stored.
     */
    @NotNull
    protected final <T> EditBox addInputOption(@NotNull OptionsTab tab, @NotNull ConfigValue<T> option, @NotNull Component label, @NotNull Tooltip tooltip, @NotNull Function<T, String> formatter, @NotNull Function<String, Optional<T>> parser, @NotNull Consumer<T> valueChanged) {
        Objects.requireNonNull(tab);
        Objects.requireNonNull(option);
        Objects.requireNonNull(label);
        Objects.requireNonNull(tooltip);
        Objects.requireNonNull(formatter);
        Objects.requireNonNull(parser);
        Objects.requireNonNull(valueChanged);

        StringWidget labelWidget = new StringWidget(label, this.font);
        labelWidget.setTooltip(tooltip);

        EditBox input = new EditBox(this.font, 0, 0, INPUT_MIN_WIDTH, BUTTON_HEIGHT, label);
        input.setValue(Objects.requireNonNull(formatter.apply(option.getValue())));
        input.setTooltip(tooltip);
        input.setResponder(text -> {
            Optional<T> parsedValue = Objects.requireNonNull(parser.apply(text));
            input.setTextColor(parsedValue.isPresent() ? 0xFFE0E0E0 : INVALID_INPUT_COLOR);
            parsedValue.ifPresent(value -> {
                if (!Objects.equals(option.getValue(), value)) {
                    option.setValue(value);
                    if (Objects.equals(option.getValue(), value)) {
                        valueChanged.accept(value);
                    } else {
                        // ConfigValue rolls back failed writes. Mirror that rollback so the field never claims an unsaved value.
                        input.setValue(Objects.requireNonNull(formatter.apply(option.getValue())));
                    }
                }
            });
            this.updateOptionResetButtons();
        });

        Button resetButton = this.buildResetButton(option, () -> label, () -> input.setValue(Objects.requireNonNull(formatter.apply(option.getValue()))));
        this.inputControls.add(new InputControl(labelWidget, input, labelWidget.getWidth()));
        this.optionControls.add(new OptionControl(() -> isInputDefault(option, input.getValue(), parser), resetButton));
        tab.addChild(new ConfigOptionsList.Row(List.of(labelWidget, input, resetButton)));
        return input;
    }

    protected final void addKeybindOption(@NotNull OptionsTab tab, @NotNull KeybindSetting setting) {
        Button keybindButton = this.buildKeybindButton(setting);
        Button resetButton = this.buildKeybindResetButton(setting);
        this.keybindControls.add(new KeybindControl(setting, keybindButton, resetButton));
        tab.addChild(this.buildControlRowLayout(keybindButton, resetButton));
    }

    @NotNull
    protected Button buildToggleButton(@NotNull ConfigValue<Boolean> option, @NotNull String labelBaseKey) {
        Button button = Button.builder(this.toggleMessage(option, labelBaseKey), pressedButton -> {
            option.setValue(!option.getValue());
            pressedButton.setMessage(this.toggleMessage(option, labelBaseKey));
            this.updateOptionResetButtons();
        }).size(this.getButtonWidth(), BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable(labelBaseKey + ".desc"))).build();
        return button;
    }

    /**
     * Builds a cycling button. The value produced by {@code messageFactory} should preferably use
     * {@link #OPTION_VALUE_COLOR}, or {@link #DISABLED_OPTION_VALUE_COLOR} for a disabled-like state.
     */
    @NotNull
    protected <T> Button buildCycleButton(@NotNull ConfigValue<T> option, @NotNull UnaryOperator<T> nextValue, @NotNull Function<T, Component> messageFactory, @NotNull String descriptionKey) {
        Button button = Button.builder(messageFactory.apply(option.getValue()), pressedButton -> {
            option.update(nextValue);
            pressedButton.setMessage(messageFactory.apply(option.getValue()));
            this.updateOptionResetButtons();
        }).size(this.getButtonWidth(), BUTTON_HEIGHT).tooltip(Tooltip.create(Component.translatable(descriptionKey))).build();
        return button;
    }

    @NotNull
    protected Button buildResetButton(@NotNull ConfigValue<?> option, @NotNull Supplier<Component> optionName, @NotNull Runnable refreshControl) {
        return Button.builder(Component.translatable("controls.reset"), ignored -> {
            option.resetToDefault();
            refreshControl.run();
            this.updateOptionResetButtons();
        }).size(RESET_BUTTON_WIDTH, BUTTON_HEIGHT).createNarration(defaultNarrationSupplier -> Component.translatable("narrator.controls.reset", optionName.get())).build();
    }

    @NotNull
    protected Component toggleMessage(@NotNull ConfigValue<Boolean> option, @NotNull String labelBaseKey) {
        boolean enabled = option.getValue();
        Component value = Component.translatable(this.translationPrefix + (enabled ? ".toggle.enabled" : ".toggle.disabled")).withStyle(Style.EMPTY.withColor(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
        return Component.translatable(labelBaseKey, value);
    }

    protected final boolean isTabSelected(@Nullable OptionsTab tab) {
        return tab != null && this.tabManager.getCurrentTab() == tab;
    }

    protected final void updateOptionResetButtons() {
        for (OptionControl control : this.optionControls) {
            control.resetButton().active = !control.defaultState().getAsBoolean();
        }
    }

    protected void updateControlWidths() {
        int rowWidth = this.getButtonWidth();
        int controlWidth = calculatePrimaryControlWidth(rowWidth);
        for (AbstractWidget control : this.primaryControls) {
            control.setWidth(controlWidth);
        }
        for (InputControl control : this.inputControls) {
            InputWidths widths = calculateInputWidths(controlWidth, control.preferredLabelWidth());
            control.label().setWidth(widths.labelWidth());
            control.input().setWidth(widths.inputWidth());
        }
        for (OptionControl control : this.optionControls) {
            control.resetButton().setWidth(RESET_BUTTON_WIDTH);
        }
        for (KeybindControl control : this.keybindControls) {
            control.keybindButton().setWidth(controlWidth);
            control.resetButton().setWidth(RESET_BUTTON_WIDTH);
        }
    }

    protected int getButtonWidth() {
        return Math.min(BUTTON_ROW_MAX_WIDTH, Math.max(RESET_BUTTON_WIDTH + CONTROL_GAP + 1, this.width - 40));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.waitingForKeybind != null) {
            Services.PLATFORM.setKeyMappingKey(this.waitingForKeybind.keyMapping(), InputConstants.Type.MOUSE.getOrCreate(button));
            this.afterKeybindChanged();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (this.waitingForKeybind != null) {
            InputConstants.Key wheelKey = this.waitingForKeybind.getMouseWheelKey(deltaY);
            if (wheelKey != null && wheelKey != InputConstants.UNKNOWN) {
                Services.PLATFORM.setKeyMappingKey(this.waitingForKeybind.keyMapping(), wheelKey);
                this.afterKeybindChanged();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.waitingForKeybind != null) {
            Services.PLATFORM.setKeyMappingKey(this.waitingForKeybind.keyMapping(), keyCode == InputConstants.KEY_ESCAPE ? InputConstants.UNKNOWN : InputConstants.getKey(keyCode, scanCode));
            this.afterKeybindChanged();
            return true;
        }
        if (this.tabNavigationBar != null && this.tabNavigationBar.keyPressed(keyCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar == null || this.bottomButtons == null) return;
        this.tabNavigationBar.setWidth(this.width);
        this.tabNavigationBar.arrangeElements();
        this.bottomButtons.arrangeElements();
        FrameLayout.centerInRectangle(this.bottomButtons, 0, this.height - FOOTER_HEIGHT, this.width, FOOTER_HEIGHT);
        int tabAreaTop = this.tabNavigationBar.getRectangle().bottom();
        // Unlike Create World's fixed grid, scrolling rows must stop before the footer background.
        this.tabManager.setTabArea(new ScreenRectangle(0, tabAreaTop, this.width, Math.max(0, this.height - FOOTER_HEIGHT - tabAreaTop)));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float a) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, a);
        if (this.shouldRenderFooterSeparator()) {
            graphics.blit(CreateWorldScreen.FOOTER_SEPERATOR, 0, Mth.roundToward(this.height - FOOTER_HEIGHT - 2, 2), 0.0F, 0.0F, this.width, 2, 32, 2);
        }
    }

    protected boolean shouldRenderFooterSeparator() {
        return true;
    }

    @Override
    public void renderDirtBackground(@NotNull GuiGraphics graphics) {
        graphics.blit(CreateWorldScreen.LIGHT_DIRT_BACKGROUND, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
    }

    @Override
    public void tick() {
        super.tick();
        this.tabManager.tickCurrent();
        for (InputControl control : this.inputControls) control.input().tick();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @NotNull
    private <T> ConfigSlider<T> addSliderOption(@NotNull OptionsTab tab, @NotNull ConfigValue<T> option, @NotNull ConfigSlider<T> slider, @NotNull String descriptionKey) {
        slider.setTooltip(Tooltip.create(Component.translatable(descriptionKey)));
        Button resetButton = this.buildResetButton(option, slider::getMessage, slider::refreshFromOption);
        this.primaryControls.add(slider);
        this.optionControls.add(new OptionControl(() -> isOptionDefault(option), resetButton));
        tab.addChild(this.buildControlRowLayout(slider, resetButton));
        return slider;
    }

    @NotNull
    private Button buildKeybindButton(@NotNull KeybindSetting setting) {
        KeyMapping keyMapping = setting.keyMapping();
        return Button.builder(Component.empty(), ignored -> {
            this.waitingForKeybind = setting;
            this.updateKeybindButtons();
        }).size(calculatePrimaryControlWidth(this.getButtonWidth()), BUTTON_HEIGHT).createNarration(defaultNarrationSupplier -> keyMapping.isUnbound() ? Component.translatable("narrator.controls.unbound", Component.translatable(keyMapping.getName())) : Component.translatable("narrator.controls.bound", Component.translatable(keyMapping.getName()), defaultNarrationSupplier.get())).build();
    }

    @NotNull
    private Button buildKeybindResetButton(@NotNull KeybindSetting setting) {
        KeyMapping keyMapping = setting.keyMapping();
        return Button.builder(Component.translatable("controls.reset"), ignored -> {
            Services.PLATFORM.setKeyMappingKey(keyMapping, keyMapping.getDefaultKey());
            this.afterKeybindChanged();
        }).size(RESET_BUTTON_WIDTH, BUTTON_HEIGHT).createNarration(defaultNarrationSupplier -> Component.translatable("narrator.controls.reset", Component.translatable(keyMapping.getName()))).build();
    }

    private void updateKeybindButtons() {
        for (KeybindControl control : this.keybindControls) {
            KeyMapping keyMapping = control.setting().keyMapping();
            control.keybindButton().setMessage(this.keybindMessage(control.setting()));
            control.keybindButton().setTooltip(this.keybindTooltip(control.setting()));
            control.resetButton().active = !keyMapping.isDefault();
        }
    }

    @NotNull
    private Component keybindMessage(@NotNull KeybindSetting setting) {
        KeyMapping keyMapping = setting.keyMapping();
        Component value = keyMapping.getTranslatedKeyMessage().copy().withStyle(Style.EMPTY.withColor(OPTION_VALUE_COLOR));
        Component message = Component.translatable(setting.labelKey(), value);
        if (this.waitingForKeybind == setting) {
            return Component.literal("> ").append(message.copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW);
        }
        if (this.hasKeybindCollision(keyMapping)) {
            return Component.literal("[ ").append(message.copy().withStyle(ChatFormatting.WHITE)).append(" ]").withStyle(ChatFormatting.YELLOW);
        }
        return message;
    }

    @Nullable
    private Tooltip keybindTooltip(@NotNull KeybindSetting setting) {
        KeyMapping keyMapping = setting.keyMapping();
        if (!this.hasKeybindCollision(keyMapping)) return Tooltip.create(Component.translatable(setting.descriptionKey()));

        MutableComponent collisions = Component.empty();
        boolean first = true;
        if (this.minecraft != null) {
            for (KeyMapping otherKey : this.minecraft.options.keyMappings) {
                if (otherKey != keyMapping && keyMapping.same(otherKey) && (!otherKey.isDefault() || !keyMapping.isDefault())) {
                    if (!first) collisions.append(", ");
                    collisions.append(Component.translatable(otherKey.getName()));
                    first = false;
                }
            }
        }
        return Tooltip.create(Component.translatable(this.translationPrefix + ".keybind.duplicate_desc", collisions));
    }

    private boolean hasKeybindCollision(@NotNull KeyMapping keyMapping) {
        return this.minecraft != null && hasKeybindCollision(keyMapping, this.minecraft.options.keyMappings);
    }

    private void afterKeybindChanged() {
        this.waitingForKeybind = null;
        KeyMapping.resetMapping();
        if (this.minecraft != null) this.minecraft.options.save();
        this.updateKeybindButtons();
    }

    @NotNull
    private ConfigOptionsList.Row buildControlRowLayout(@NotNull AbstractWidget control, @NotNull Button resetButton) {
        return new ConfigOptionsList.Row(List.of(control, resetButton));
    }

    protected static int calculatePrimaryControlWidth(int rowWidth) {
        return rowWidth - RESET_BUTTON_WIDTH - CONTROL_GAP;
    }

    protected static boolean isOptionDefault(@NotNull ConfigValue<?> option) {
        return Objects.equals(option.getValue(), option.getDefaultValue());
    }

    protected static <T> boolean isInputDefault(@NotNull ConfigValue<T> option, @NotNull String inputValue, @NotNull Function<String, Optional<T>> parser) {
        Optional<T> parsedValue = Objects.requireNonNull(parser.apply(inputValue));
        return isOptionDefault(option) && parsedValue.isPresent() && Objects.equals(option.getDefaultValue(), parsedValue.get());
    }

    @NotNull
    protected static Optional<Float> parseFloatInput(@NotNull String value) {
        try {
            float parsedValue = Float.parseFloat(value);
            return Float.isFinite(parsedValue) ? Optional.of(parsedValue) : Optional.empty();
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    @NotNull
    protected static InputWidths calculateInputWidths(int rowWidth, int preferredLabelWidth) {
        int availableWidth = Math.max(2, rowWidth - INPUT_GAP);
        int labelWidth = Math.min(Math.max(1, preferredLabelWidth), Math.max(1, availableWidth - INPUT_MIN_WIDTH));
        return new InputWidths(labelWidth, availableWidth - labelWidth);
    }

    protected static boolean hasKeybindCollision(@NotNull KeyMapping keyMapping, @NotNull KeyMapping[] keyMappings) {
        if (keyMapping.isUnbound()) return false;
        for (KeyMapping otherKey : keyMappings) {
            if (otherKey != keyMapping && keyMapping.same(otherKey) && (!otherKey.isDefault() || !keyMapping.isDefault())) return true;
        }
        return false;
    }

    @NotNull
    private static String requireTranslationPrefix(@NotNull String translationPrefix) {
        String prefix = Objects.requireNonNull(translationPrefix);
        if (prefix.isBlank()) throw new IllegalArgumentException("The config screen translation prefix cannot be blank.");
        return prefix;
    }

    public record KeybindSetting(@NotNull KeyMapping keyMapping, @NotNull String labelKey, @NotNull String descriptionKey, @Nullable DoubleFunction<InputConstants.Key> mouseWheelKeyFactory) {

        public KeybindSetting {
            Objects.requireNonNull(keyMapping);
            Objects.requireNonNull(labelKey);
            Objects.requireNonNull(descriptionKey);
        }

        public KeybindSetting(@NotNull KeyMapping keyMapping, @NotNull String labelKey, @NotNull String descriptionKey) {
            this(keyMapping, labelKey, descriptionKey, null);
        }

        @Nullable
        InputConstants.Key getMouseWheelKey(double deltaY) {
            return this.mouseWheelKeyFactory != null ? this.mouseWheelKeyFactory.apply(deltaY) : null;
        }

    }

    protected record InputWidths(int labelWidth, int inputWidth) {
    }

    private record InputControl(@NotNull StringWidget label, @NotNull EditBox input, int preferredLabelWidth) {
    }

    private record OptionControl(@NotNull BooleanSupplier defaultState, @NotNull Button resetButton) {
    }

    private record KeybindControl(@NotNull KeybindSetting setting, @NotNull Button keybindButton, @NotNull Button resetButton) {
    }

    protected final class OptionsTab implements Tab {

        private final Component title;
        private final List<ConfigOptionsList.Row> rows = new ArrayList<>();
        private final ConfigOptionsList optionsList;

        private OptionsTab(@NotNull Component title) {
            this.title = title;
            this.optionsList = new ConfigOptionsList(ConfigScreen.this.minecraft, ConfigScreen.this.width, BUTTON_HEIGHT, 0, ConfigScreen.this.getButtonWidth());
        }

        private void addChild(@NotNull ConfigOptionsList.Row row) {
            this.rows.add(row);
        }

        @Override
        public Component getTabTitle() {
            return this.title;
        }

        public Component getTabExtraNarration() {
            return Component.empty();
        }

        @Override
        public void visitChildren(@NotNull Consumer<AbstractWidget> consumer) {
            this.optionsList.setRows(this.rows);
            consumer.accept(this.optionsList);
        }

        @Override
        public void doLayout(@NotNull ScreenRectangle rectangle) {
            ConfigScreen.this.updateControlWidths();
            int top = Math.max(rectangle.top() + 4, Math.min(50, rectangle.bottom() - BUTTON_HEIGHT));
            this.optionsList.setBounds(ConfigScreen.this.width, Math.max(BUTTON_HEIGHT, rectangle.bottom() - top), top, ConfigScreen.this.getButtonWidth());
        }

    }

}
