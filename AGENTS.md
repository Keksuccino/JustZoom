# Repository Guidelines

## Project Structure & Module Organization
- This workspace is giving you access to multiple "Just Zoom" branches/workspaces, which is a Minecraft Java mod. It uses the MultiLoader layout with shared logic under `common` and loader-specific wrappers under `fabric`, and `forge` or `neoforge` depending on the Minecraft version.
- Place shared Java sources in `common/src/main/java` and assets such as menu JSON, translations, or textures in `common/src/main/resources` so they ship with every loader build.
- Loader-only hooks belong inside each module's `src/main/java` tree; keep local run directories like `run_client` and `run_server` for iterative testing but never depend on them for assets.
- Just Zoom's Gradle projects have an unconventional handling of the `version` variable. In Just Zoom's Gradle projects, this variable is always `1.0.0`, which is intentional and should NEVER be changed.
  - The actual mod version is instead defined in `mod_version`.
  - It is also intentional that some stuff in the project uses the `version` variable.

## Sub-Workspaces
- This primary workspace here is the so-called "agent root", but it only exists as an access point to all sub-workspaces/secondary workspaces you also have access to.
- This workspace gives you access to the actual Just Zoom source workspaces for multiple branches/Minecraft versions.
- The folder name of these should always be something like `justzoom-0.0.0`, with the `0.0.0` replaced with the Minecraft version the workspace is targeting.
- You should avoid writing permanent data to this "root" workspace here. Temporary working data is okay, but clear it up at the end, since your actual work targets will always be the sub-workspaces, where you should actually make changes to/write to.
- The code base between all sub-workspaces should always look as similar/identical as possible, to easily find a specific piece of logic in multiple sub-workspaces.