package de.keksuccino.justzoom.util.window;

import de.keksuccino.justzoom.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * Dev-only launch mode for automated in-game tests, documented in TESTING_GUIDELINES.md in the project root.<br>
 * When the {@value #ENV_VARIABLE} environment variable is "true" in a development environment, the game window gets created
 * hidden and without focus, so test clients never show up on screen or take focus away from the user.
 * The game also never moves or locks the real mouse cursor in this mode (see MixinInputConstants).<br><br>
 *
 * The mode also keeps the window out of fullscreen, because GLFW always shows fullscreen windows.
 * Its window hooks run before Minecraft creates the window, so they must not touch any game state.
 */
public final class OffscreenTestClient {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ENV_VARIABLE = "JUSTZOOM_TEST_OFFSCREEN";
    public static final String STOP_AT_TITLE_ENV_VARIABLE = "JUSTZOOM_TEST_STOP_AT_TITLE";
    // Checks the environment variable first, so normal launches never load the platform services this early
    private static final boolean ENABLED = "true".equalsIgnoreCase(System.getenv(ENV_VARIABLE)) && Services.PLATFORM.isDevelopmentEnvironment();
    private static final boolean STOP_AT_TITLE = ENABLED && "true".equalsIgnoreCase(System.getenv(STOP_AT_TITLE_ENV_VARIABLE));

    private static boolean stopRequested = false;

    static {
        if (ENABLED) LOGGER.info("[JUST ZOOM] Off-screen test client mode is active! The game window stays hidden and unfocused.");
    }

    private OffscreenTestClient() {
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    /**
     * Has to get called right before GLFW gets initialized.<br>
     * Disabling the macOS menu bar keeps GLFW from turning the game into a regular app, so it gets no Dock icon and can't get activated.
     */
    public static void prepareBackendInit() {
        if (!ENABLED) return;
        GLFW.glfwInitHint(GLFW.GLFW_COCOA_MENUBAR, GLFW.GLFW_FALSE);
    }

    /**
     * Has to get called after Minecraft reset the GLFW window hints and before it creates the game window.
     */
    public static void prepareWindowCreation() {
        if (!ENABLED) return;
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
    }

    /**
     * Has to get called at the end of every client tick.<br>
     * When {@value #STOP_AT_TITLE_ENV_VARIABLE} is "true", it stops the game once the title screen is open and done loading,
     * so startup checks end on their own. The hidden window has no close button.
     */
    public static void onClientTick(@NotNull Minecraft minecraft) {
        if (!STOP_AT_TITLE || stopRequested) return;
        if ((minecraft.getOverlay() == null) && (minecraft.screen instanceof TitleScreen)) {
            stopRequested = true;
            LOGGER.info("[JUST ZOOM] [CHECK] title screen reached: PASS - stopping the off-screen test client..");
            minecraft.stop();
        }
    }

}
