#!/bin/sh
# Launches a dev client that never shows a window or takes focus. See TESTING_GUIDELINES.md.
# Usage: sh offscreen_client.sh <loader module> [additional Gradle arguments]
# Set JAVA_HOME to the project's Java version before running it.
# Set JUSTZOOM_TEST_RUN_DIR to a folder name in the project root to run a separate game instance instead of run_client.

if [ "$#" -lt 1 ]; then
    echo "Usage: sh offscreen_client.sh <loader module> [additional Gradle arguments]" >&2
    exit 2
fi
LOADER="$1"
shift
cd "$(dirname "$0")" || exit 1

# Config overrides for the run, restored when the script exits:
# - (Neo)Forge opens its early loading window before any mod code runs, so only its FML config can keep it closed.
# - The game's master volume is 0, so test clients stay silent.
# - The accessibility onboarding screen (Minecraft 1.20 and newer) is skipped, because it would wait for input on a new game instance.
RUN_DIR="${JUSTZOOM_TEST_RUN_DIR:-run_client}"
FML_CONFIG="$RUN_DIR/config/fml.toml"
GAME_OPTIONS="$RUN_DIR/options.txt"

set_config_line() {
    # Replaces the line of the given key (or appends it), keeping the rest of the file untouched.
    # Keys end with a space, "=" or ":", which covers TOML and the game's options.txt.
    mkdir -p "$(dirname "$1")"
    if [ -f "$1" ] && grep -q "^$2[ =:]" "$1"; then
        awk -v key="$2" -v line="$3" 'index($0, key) == 1 && substr($0, length(key) + 1, 1) ~ /[ =:]/ { print line; next } { print }' "$1" > "$1.offscreen-tmp" && mv "$1.offscreen-tmp" "$1"
    else
        echo "$3" >> "$1"
    fi
}

backup_config() {
    # Has to run once per file before its lines get overridden.
    # A backup left behind by a killed run still holds the original file.
    if [ -f "$1.offscreen-backup" ]; then
        mv "$1.offscreen-backup" "$1"
    fi
    if [ -f "$1" ]; then
        cp "$1" "$1.offscreen-backup"
    fi
}

restore_config() {
    # Usage: restore_config <file> [<key> <default line>]...
    file="$1"
    shift
    if [ -f "$file.offscreen-backup" ]; then
        mv "$file.offscreen-backup" "$file"
        return
    fi
    # The file didn't exist before, so it gets the values the game or loader would have created it with
    while [ "$#" -ge 2 ]; do
        set_config_line "$file" "$1" "$2"
        shift 2
    done
}

restore_all() {
    restore_config "$GAME_OPTIONS" soundCategory_master "soundCategory_master:1.0" onboardAccessibility "onboardAccessibility:true"
    if [ "$LOADER" != "fabric" ]; then
        restore_config "$FML_CONFIG" earlyWindowControl "earlyWindowControl = true"
    fi
}

# ForgeGradle names the client run task "Client"
CLIENT_TASK="runClient"
if [ "$LOADER" = "forge" ]; then
    CLIENT_TASK="Client"
fi

trap restore_all EXIT
trap 'exit 130' INT TERM
backup_config "$GAME_OPTIONS"
set_config_line "$GAME_OPTIONS" soundCategory_master "soundCategory_master:0.0"
set_config_line "$GAME_OPTIONS" onboardAccessibility "onboardAccessibility:false"
if [ "$LOADER" != "fabric" ]; then
    backup_config "$FML_CONFIG"
    set_config_line "$FML_CONFIG" earlyWindowControl "earlyWindowControl = false"
fi

# The loader build scripts read the game directory of the client run from this property
JUSTZOOM_TEST_OFFSCREEN=true sh gradlew ":$LOADER:$CLIENT_TASK" "-Pjustzoom_client_run_dir=$RUN_DIR" "$@"
