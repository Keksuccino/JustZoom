package de.keksuccino.justzoom;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenOptionsToastHandlerTest {

    // Creating any toast initializes Toast.Visibility, which references sound events from the built-in registries.
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void firstZoomKeyUseShowsToast() {
        assertTrue(OpenOptionsToastHandler.shouldShow(false, true, false));
    }

    @Test
    void firstSpyglassUseShowsToast() {
        assertTrue(OpenOptionsToastHandler.shouldShow(false, false, true));
    }

    @Test
    void idleGameplayDoesNotShowToast() {
        assertFalse(OpenOptionsToastHandler.shouldShow(false, false, false));
    }

    @Test
    void toastNeverShowsAgainAfterBeingRecorded() {
        assertFalse(OpenOptionsToastHandler.shouldShow(true, true, false));
        assertFalse(OpenOptionsToastHandler.shouldShow(true, false, true));
    }

    @Test
    void toastStaysVisibleUntilTheDisplayTimeIsOver() {
        TutorialToast toast = createToast();

        toast.update(null, 0L);
        assertEquals(Toast.Visibility.SHOW, toast.getWantedVisibility());
        toast.update(null, OpenOptionsToastHandler.DISPLAY_TIME_MS);
        assertEquals(Toast.Visibility.SHOW, toast.getWantedVisibility());
    }

    @Test
    void toastHidesItselfOnceTheDisplayTimeIsOver() {
        TutorialToast toast = createToast();

        toast.update(null, OpenOptionsToastHandler.DISPLAY_TIME_MS + 1L);
        assertEquals(Toast.Visibility.HIDE, toast.getWantedVisibility());
    }

    @Test
    void boundKeyToastContainsOnlyBlackInstructionTextWithABoldTitleColoredKey() {
        Component keyName = Component.literal("B");

        for (boolean builtInModList : new boolean[] { false, true }) {
            Component toastText = OpenOptionsToastHandler.createToastText(keyName, builtInModList);
            Component highlightedName = assertHighlightedInstruction(toastText, OpenOptionsToastHandler.OPEN_WITH_KEY_TRANSLATION_KEY);
            assertEquals(keyName.getContents(), highlightedName.getContents());
        }
    }

    @Test
    void unboundKeyToastPointsToModMenuWithoutABuiltInModList() {
        Component toastText = OpenOptionsToastHandler.createToastText(null, false);

        Component highlightedName = assertHighlightedInstruction(toastText, OpenOptionsToastHandler.OPEN_WITH_MOD_MENU_TRANSLATION_KEY);
        TranslatableContents nameContents = assertInstanceOf(TranslatableContents.class, highlightedName.getContents());
        assertEquals(OpenOptionsToastHandler.MOD_MENU_NAME_TRANSLATION_KEY, nameContents.getKey());
    }

    @Test
    void unboundKeyToastPointsToTheLoaderModsMenuWithABuiltInModList() {
        Component toastText = OpenOptionsToastHandler.createToastText(null, true);

        Component highlightedName = assertHighlightedInstruction(toastText, OpenOptionsToastHandler.OPEN_WITH_MODS_MENU_TRANSLATION_KEY);
        TranslatableContents nameContents = assertInstanceOf(TranslatableContents.class, highlightedName.getContents());
        assertEquals(OpenOptionsToastHandler.LOADER_MODS_MENU_NAME_TRANSLATION_KEY, nameContents.getKey());
    }

    private static Component assertHighlightedInstruction(Component toastText, String expectedTranslationKey) {
        assertEquals(1, toastText.getSiblings().size());
        Component instruction = toastText.getSiblings().getFirst();
        TextColor instructionColor = instruction.getStyle().getColor();
        assertNotNull(instructionColor);
        assertEquals(OpenOptionsToastHandler.TOAST_TEXT_COLOR, instructionColor.getValue());

        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, instruction.getContents());
        assertEquals(expectedTranslationKey, contents.getKey());
        assertEquals(1, contents.getArgs().length);
        Component highlightedName = assertInstanceOf(Component.class, contents.getArgs()[0]);
        TextColor nameColor = highlightedName.getStyle().getColor();
        assertNotNull(nameColor);
        assertEquals(OpenOptionsToastHandler.TUTORIAL_TITLE_COLOR, nameColor.getValue());
        assertTrue(highlightedName.getStyle().isBold());
        return highlightedName;
    }

    private static TutorialToast createToast() {
        Component toastText = OpenOptionsToastHandler.createToastText(Component.literal("B"), false);
        return OpenOptionsToastHandler.createToast(createFixedWidthFont(), toastText);
    }

    /** Creates a font that measures every glyph with the same width, which is all {@link TutorialToast} needs to wrap its text. */
    private static Font createFixedWidthFont() {
        GlyphInfo glyphInfo = GlyphInfo.simple(6.0F);
        BakedGlyph glyph = new BakedGlyph() {

            @Override
            public GlyphInfo info() {
                return glyphInfo;
            }

            @Override
            public TextRenderable.Styled createGlyph(float x, float y, int color, int shadowColor, Style style, float boldOffset, float shadowOffset) {
                return null;
            }

        };
        GlyphSource glyphSource = new GlyphSource() {

            @Override
            public BakedGlyph getGlyph(int codepoint) {
                return glyph;
            }

            @Override
            public BakedGlyph getRandomGlyph(RandomSource random, int width) {
                return glyph;
            }

        };
        return new Font(new Font.Provider() {

            @Override
            public GlyphSource glyphs(FontDescription font) {
                return glyphSource;
            }

            @Override
            public EffectGlyph effect() {
                throw new UnsupportedOperationException();
            }

        });
    }

}
