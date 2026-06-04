# TwemojiChat

Twemoji in Minecraft chat.

TwemojiChat is a client-side mod that adds Twemoji rendering and `:shortcode:` autocomplete to Minecraft chat.

No server setup is required.

![Emoji autocomplete](assets/tab-completion.png)

## Features

* Autocomplete emoji shortcodes while typing in chat
* Render both `:shortcode:` and Unicode emoji using Twemoji
* Works on any server
* Client-side only
* Supports the full Twemoji emoji set

![Comparison](assets/comparasion.png)

## Installation

1. Install Fabric, NeoForge, or Forge for your Minecraft version.
2. Download the correct TwemojiChat release.
3. Place the `.jar` file in your `mods` folder.
4. Start Minecraft.

## Supported Versions

| Minecraft | Fabric | NeoForge | Forge |
| --------- | ------ | -------- | ----- |
| 1.20.1    | Yes    |          | Yes   |
| 1.20.2    | Yes    |          | Yes   |
| 1.20.3    | Yes    |          | Yes   |
| 1.20.4    | Yes    |          | Yes   |
| 1.20.5    | Yes    | Yes      |       |
| 1.20.6    | Yes    | Yes      |       |
| 1.21      | Yes    | Yes      |       |
| 1.21.1    | Yes    | Yes      |       |
| 1.21.2    | Yes    | Yes      |       |
| 1.21.3    | Yes    | Yes      |       |
| 1.21.4    | Yes    | Yes      |       |
| 1.21.5    | Yes    | Yes      |       |
| 1.21.6    | Yes    | Yes      |       |
| 1.21.7    | Yes    | Yes      |       |
| 1.21.8    | Yes    | Yes      |       |
| 1.21.9    | Yes    | Yes      |       |
| 1.21.10   | Yes    | Yes      |       |
| 1.21.11   | Yes    | Yes      |       |
| 26.1      | Yes    | Yes      |       |

## Usage

* Type `:smile:` and press **Tab** to insert an emoji
* Type or paste Unicode emoji such as 😊
* Browse emoji from the autocomplete menu

## For Developers

This project uses a shared `common` module with loader-specific modules for Fabric, NeoForge, and Forge.

### Building

Requirements:

* Java 21+
* Python 3
* Pillow

```bash
./gradlew build
```

### Running a Development Client

```bash
./gradlew :fabric:1_21_1:runClient
./gradlew :neoforge:1_21_1:runClient
```

### Updating Emoji Data

1. Update `tools/twemoji_sources.json`
2. Run `./gradlew syncTwemoji`
3. Run `./gradlew build`
4. Commit the updated files in `common/src/generated/resources`

## Project Structure

```text
common/     Shared code and generated emoji assets
fabric/     Fabric loader implementation
forge/      Forge loader implementation
neoforge/   NeoForge loader implementation
tools/      Twemoji sync tools
```

## Credits

* Twemoji artwork: https://github.com/jdecked/twemoji
* Shortcode data: https://github.com/iamcal/emoji-data

## License

MIT
