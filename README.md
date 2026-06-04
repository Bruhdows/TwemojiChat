# TwemojiChat

Bring Twemoji to Minecraft chat.

TwemojiChat is a client-side mod that renders chat emoji using Twitter's Twemoji artwork and provides instant `:shortcode:` autocomplete directly in the chat box.

No server-side setup required—install the mod and start using emoji immediately.

![Emoji autocomplete suggestions](assets/tab-completion.png)

## Features

### Emoji Autocomplete
Type `:` while chatting to open an autocomplete menu with matching emoji shortcodes, names, and live previews.

### Twemoji Rendering
Both `:shortcode:` syntax and standard Unicode emoji are automatically rendered using Twemoji artwork in chat messages.

### Client-Side Only
Works on any vanilla or modded server without plugins, server mods, or additional configuration.

### Extensive Emoji Support
Includes hundreds of emoji from the Twemoji project and can be updated as new emoji are added upstream.

![Twemoji rendering comparison](assets/comparasion.png)

## Installation

1. Install Fabric or NeoForge for your Minecraft version.
2. Download the latest TwemojiChat release for your mod loader.
3. Place the `.jar` file in your `mods` directory.
4. Launch Minecraft.

## Supported Versions

| Minecraft | Fabric | NeoForge | Forge |
|-----------|---------|-----------|--------|
| 1.20.1 | ✓ | | ✓ |
| 1.21.1 | ✓ | ✓ | |
| 1.21.11 | ✓ | ✓ | |
| 26.1 | ✓ | ✓ | |

## Usage

Use emoji in chat as you normally would:

- Type `:smile:` and press **Tab** to insert the corresponding emoji.
- Paste or type Unicode emoji such as 😊 and they will render using Twemoji.
- Browse available emoji through the autocomplete menu while typing.

## Development

TwemojiChat uses a shared `common` module alongside loader-specific implementations for Fabric, NeoForge, and Forge.

### Requirements

- Java 21+
- Python 3
- Pillow (for emoji asset generation)

### Build

```bash
./gradlew build
```

### Run a Development Client

```bash
./gradlew :fabric:1_21_1:runClient
./gradlew :neoforge:1_21_1:runClient
```

### Updating Emoji Assets

1. Update the pinned references in `tools/twemoji_sources.json`
2. Run:

```bash
./gradlew syncTwemoji
```

3. Rebuild the project:

```bash
./gradlew build
```

4. Commit the updated files from:

```
common/src/generated/resources
```

## Project Structure

```text
common/     Shared chat logic, mixins, resources, and generated emoji assets
fabric/     Fabric loader integration and metadata
forge/      Forge loader integration and metadata
neoforge/   NeoForge loader integration and metadata
tools/      Scripts for synchronizing Twemoji data
```

## Credits

- Twemoji artwork: https://github.com/jdecked/twemoji
- Emoji shortcode data: https://github.com/iamcal/emoji-data

## License

Licensed under the MIT License.