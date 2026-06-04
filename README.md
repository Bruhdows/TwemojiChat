# TwemojiChat

Emoji in Minecraft chat, the way they were meant to look.

TwemojiChat is a client-side mod that adds Twitter's Twemoji set to Minecraft chat. It also brings instant `:shortcode:` autocomplete so you can find and insert emoji without leaving chat.

No server setup required. Just install and go.

![Tab completion showing emoji suggestions](assets/tab-completion.png)

## Features

- **Tab-complete emoji** — Type `:` in chat and a suggestion popup appears with matching Twemoji, complete with shortcode labels and live previews
- **Beautiful rendering** — All `:shortcode:` and Unicode emoji in incoming chat messages are replaced with crisp Twemoji glyphs
- **Fully client-side** — Works on any vanilla or modded server. No plugins, no server mods, no configuration needed
- **Wide emoji support** — Hundreds of emoji from the Twemoji library, kept up to date

![Before and after comparison](assets/comparasion.png)

## Installation

1. Install [Fabric](https://fabricmc.net/) or [NeoForge](https://neoforged.net/) for your Minecraft version
2. Download the latest TwemojiChat release for your mod loader and Minecraft version
3. Drop the `.jar` file into your `mods` folder
4. Launch Minecraft and enjoy emoji in chat

### Supported versions

| Minecraft | Fabric | NeoForge | Forge |
|-----------|--------|----------|-------|
| 1.20.1    | Yes    |          | Yes   |
| 1.21.1    | Yes    | Yes      |       |
| 1.21.11   | Yes    | Yes      |       |
| 26.1      | Yes    | Yes      |       |

## Usage

Just type in chat as normal:

- Type `:smile:` and press **Tab** to insert the grinning face emoji
- Type any Unicode emoji (like 😊) and it will render as Twemoji in chat
- Browse the full list in the suggestion popup while you type

## For developers

This project uses a shared `common` module with loader-specific modules for Fabric, NeoForge, and Forge.

### Building

Requirements:
- Java 21+
- Python 3 with Pillow (for emoji asset generation)

```bash
./gradlew build
```

### Running a dev client

```bash
./gradlew :fabric:1_21_1:runClient
./gradlew :neoforge:1_21_1:runClient
```

### Updating emoji data

1. Edit pinned refs in [`tools/twemoji_sources.json`](tools/twemoji_sources.json)
2. Run `./gradlew syncTwemoji`
3. Run `./gradlew build`
4. Commit the updated files in `common/src/generated/resources`

### Project structure

```
common/     Shared chat logic, mixins, resources, and generated emoji assets
fabric/     Fabric loader bootstrap and metadata
forge/      Forge loader bootstrap and metadata
neoforge/   NeoForge loader bootstrap and metadata
tools/      Scripts for syncing upstream Twemoji data
```

## Credits

- **Twemoji artwork** — [jdecked/twemoji](https://github.com/jdecked/twemoji)
- **Shortcode data** — [iamcal/emoji-data](https://github.com/iamcal/emoji-data)

## License

MIT
