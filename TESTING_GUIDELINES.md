# Testing Guidelines

## Staying off the screen

Tests must never get in the way of the user. No test, unit test or in-game test, may open a window, put an icon in the Dock,
take the window focus, move or lock the mouse cursor or play audible sound.

- **Unit tests** run headless: the root `build.gradle` gives every `Test` task `java.awt.headless=true` and `apple.awt.UIElement=true`.
  Without headless mode, macOS puts the test JVM in the Dock as soon as a test loads AWT (fonts, images). Never remove these properties,
  and never start GLFW, Swing or anything else that opens a window from a unit test.
- **In-game tests** only run in off-screen test clients, see below. Never launch a normal client (`runClient`) for testing.
- To check a new kind of test, watch `lsappinfo list` while it runs. No process of the test may show up there with `type="Foreground"`,
  which is what gets a Dock icon, and `lsappinfo front` must never switch to it.

## Off-screen test clients

Test clients must never show their window on screen, take focus away from the user or touch the mouse cursor. Just Zoom has a dev-only
off-screen mode for that, so always launch test clients through `offscreen_client.sh`:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 25) sh offscreen_client.sh fabric
JAVA_HOME=$(/usr/libexec/java_home -v 25) sh offscreen_client.sh neoforge
```

- The script runs the loader's client task (`runClient`, or `Client` for Forge) with the `JUSTZOOM_TEST_OFFSCREEN=true` environment variable.
  Extra arguments get passed on to Gradle.
- By default the client uses the `run_client` game directory. To test with a separate, clean game instance, set `JUSTZOOM_TEST_RUN_DIR`
  to a folder name in the project root, for example `JUSTZOOM_TEST_RUN_DIR=run_client_test`. The script passes it to the loader build
  scripts as the `justzoom_client_run_dir` Gradle property.
- For (Neo)Forge, it sets `earlyWindowControl = false` in `<game directory>/config/fml.toml` for the run.
  The early loading window opens before any mod code runs, so the FML config is the only way to keep it closed.
- It sets `soundCategory_master:0.0` in `<game directory>/options.txt` for the run, so test clients stay silent.
- It also sets `onboardAccessibility:false` there, so the accessibility onboarding screen of a new game instance (Minecraft 1.20
  and newer) never waits for input.
- All of these files get restored when the script exits.
- The game log contains `Off-screen test client mode is active!` when the mode works.
- For startup checks (for example that all mixins applied), also set `JUSTZOOM_TEST_STOP_AT_TITLE=true`. The game then stops on its own
  once the title screen is open and done loading, and logs `[CHECK] title screen reached: PASS` right before.

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 25) JUSTZOOM_TEST_STOP_AT_TITLE=true sh offscreen_client.sh fabric --no-daemon
```

### How it works

- `util/window/OffscreenTestClient` reads the environment variables. The mode only works in a development environment.
- `MixinGLX` disables the macOS menu bar before GLFW starts, so the game gets no Dock icon and can't get activated.
- `MixinWindow` creates the game window hidden and unfocused, and keeps it windowed, because GLFW always shows fullscreen windows.
- `MixinMinecraft` skips the `glfwShowWindow()` call Minecraft makes after creating the window, and runs the title screen stop of
  `JUSTZOOM_TEST_STOP_AT_TITLE` at the end of every client tick.
- `MixinInputConstants` skips the game's mouse grab and release, so the real cursor never gets moved or locked,
  for example when a test enters or leaves a world.

### Writing in-game tests

- The game still renders into its render targets, so screenshots (`Screenshot.grab()`, `Screenshot.takeScreenshot()`) work as usual.
- The hidden window gets no input from the OS. Simulate input through the game's real input handlers instead, for example
  `KeyboardHandler#keyPress` (private, so call it through an invoker mixin) with the key of the `KeyMapping` you want to trigger.
- Mouse moves only reach the game while `Minecraft#isWindowActive()` is true (`MouseHandler#onMove` and `#handleAccumulatedMovement`
  check it). It reads `Window#isFocused()`, which starts as `true` and only changes through focus events of the window, so simulated
  moves work as they are. If a test sees them ignored, check `isWindowActive()` first. Mouse buttons and scrolling (which adjusts the
  zoom factor) don't depend on it.
- The game still keeps track of the mouse being grabbed or released, only the real cursor stays untouched.
  Never call window or cursor functions of GLFW from test code (showing, raising or focusing the window, moving the cursor).
- There is no window to close, so test code has to call `Minecraft#stop()` when it's done, or use `JUSTZOOM_TEST_STOP_AT_TITLE` for
  startup checks. If a run hangs, stop its Gradle process.
