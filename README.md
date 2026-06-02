# TwemojiChat

TwemojiChat is a NeoForge client-side mod for Minecraft 1.21.1 that adds Twemoji-backed emoji completion and chat rendering.

## What it does

- Autocompletes `:shortcode:` emoji names while you type in chat
- Shows the shortcode and emoji preview in the suggestion popup
- Rewrites `:shortcode:` and Unicode emoji in received chat into local Twemoji glyphs
- Stays fully client side with no server plugin or server mod requirement

## Development

Requirements:

- Java 21
- Python 3 with Pillow

Useful commands:

```bash
./gradlew build
./gradlew syncTwemoji
```

`syncTwemoji` regenerates the checked-in emoji font sheets and index from pinned upstream sources in [`tools/twemoji_sources.json`](/home/bruhdows/TwemojiChat/tools/twemoji_sources.json).

## Updating emoji data

1. Change the pinned refs in [`tools/twemoji_sources.json`](/home/bruhdows/TwemojiChat/tools/twemoji_sources.json).
2. Run `./gradlew syncTwemoji`.
3. Run `./gradlew build`.
4. Commit the updated generated resources in `src/generated/resources`.

## Sources

- Twemoji artwork: `jdecked/twemoji`
- Shortcode data: `iamcal/emoji-data`

Twemoji artwork keeps its upstream license and attribution requirements. Review the upstream asset licenses before redistributing packaged builds.
