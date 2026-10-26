## About

**Just Zoom** adds lots of new features and improvements to the Spyglass, and also adds a standalone zooming mode that does not require the Spyglass to work.

## What The Mod Gives You

- **Full Spyglass Integration**: All of Just Zoom's features are available for both the Spyglass and the standalone zoom (can be disabled in settings).
- **Mouse Wheel Zooming**: Adjust the zoom level by using your mouse wheel!
- **Increased Zoom Level**: Zoom in a _looooot_ further than with the Vanilla Spyglass!
- **Zooming in Third-Person**: Normally you can't zoom while in third-person, but Just Zoom got your back!
- **Hiding the HUD While Zooming**: You probably never noticed, but the HUD stays visible while zooming. Now that you will constantly notice and be annoyed by it, install Just Zoom to get back your peace of mind.
- **Just Works, But Also Highly Customizable**: It doesn't matter if you want a mod that simply _works_ out of the box, or if you want to customize the hell out of your zooming experience, Just Zoom works for both types of people.
- **Settings That Make Sense**: Unlike other mods, Just Zoom explains all its settings in a way that everyone understands, and it uses common everyday-units like seconds and percentage, instead of over-complicated stuff like "zoom scaling multipliers".
- **Removing The Spyglass Overlay**: ..or adding it to the standalone zoom. No matter what your preferences are, you can do it with Just Zoom.
- **Making the Spyglass Shut Up**: _Click-click-click, clack-clack-clack_, or simply no sound at all, if you don't feel like it right now.
- **And much more!**

_And yes, it also allows you to zoom in on your face and the back of your head, if you, for some reason, want that._

[![youtube](https://github.com/Keksuccino/JustZoom/blob/main/assets/youtube.png?raw=true)](https://www.youtube.com/watch?v=NB6pGFpioRs)

## How to Use

Grab a **Spyglass** and zoom in, that's it. Nothing to configure, if you just need a better zooming experience.

Use your **mouse wheel** to adjust the zoom level.

Or press **Z** by default (**Y** on some keyboard layouts), for the **standalone zoom**.

If you want to configure the mod, press **B** by default, to open the mod's settings. You can also open the settings via Mod Menu in Fabric, and via the Mods menu on (Neo)Forge.

## Disabling Standalone Zoom on Servers

Server owners can turn off zooming with the zoom key, so players can only zoom with a spyglass.

1. Install Just Zoom (3.1.0 or newer) and Konkrete on your server.
2. Start the server once, then stop it.
3. Open `config/justzoom/config.json` and set `allow_standalone_zoom` to `false`.
4. Start the server again. Changes only apply after a restart.

Players who press the zoom key now get a chat message telling them to use a spyglass instead. Zooming with a spyglass keeps working as usual.

Just Zoom stays optional: players without the mod can still join your server, and players with the mod can still join servers without it, where all zoom features work normally.

For singleplayer and LAN worlds, the same option in your own game's `config/justzoom/config.json` applies.
