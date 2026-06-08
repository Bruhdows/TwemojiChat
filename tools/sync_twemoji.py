#!/usr/bin/env python3

from __future__ import annotations

import io
import json
import math
import shutil
import tempfile
import urllib.request
import zipfile
from collections.abc import Iterable
from dataclasses import dataclass
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parent.parent
CONFIG_PATH = ROOT / "tools" / "twemoji_sources.json"
GENERATED_ROOT = ROOT / "common" / "src" / "generated" / "resources"
FONT_TEXTURE_ROOT = GENERATED_ROOT / "assets" / "twemojichat" / "textures" / "font"
FONT_JSON_PATH = GENERATED_ROOT / "assets" / "twemojichat" / "font" / "emoji.json"
INDEX_PATH = GENERATED_ROOT / "assets" / "twemojichat" / "twemoji" / "index.json"
PAGES_START = 0xE000

SKIN_TONE_NAMES = {
    "1F3FB": "light_skin_tone",
    "1F3FC": "medium_light_skin_tone",
    "1F3FD": "medium_skin_tone",
    "1F3FE": "medium_dark_skin_tone",
    "1F3FF": "dark_skin_tone",
}


@dataclass(frozen=True)
class EmojiEntry:
    aliases: list[str]
    glyph: str
    image_name: str
    name: str
    primary_alias: str | None
    sort_order: int
    unicode_value: str


def fetch_zip(repo: str, ref: str) -> zipfile.ZipFile:
    url = f"https://api.github.com/repos/{repo}/zipball/{ref}"
    request = urllib.request.Request(
        url,
        headers={
            "Accept": "application/vnd.github+json",
            "User-Agent": "twemojichat-sync",
        },
    )
    with urllib.request.urlopen(request) as response:
        return zipfile.ZipFile(io.BytesIO(response.read()))


def read_config() -> dict:
    return json.loads(CONFIG_PATH.read_text(encoding="utf-8"))


def load_shortcode_rows(config: dict) -> list[dict]:
    repo = config["shortcodes"]["repo"]
    ref = config["shortcodes"]["ref"]
    path = config["shortcodes"]["data_path"]
    with fetch_zip(repo, ref) as archive:
        for name in archive.namelist():
            if name.endswith(path):
                with archive.open(name) as handle:
                    return json.load(handle)
    raise FileNotFoundError(f"Unable to locate {path} in {repo}@{ref}")


def load_discord_shortcode_aliases(config: dict) -> dict[str, list[str]]:
    parity_config = config["shortcodes"].get("discord_parity")
    if parity_config is None:
        return {}

    version = parity_config["version"]
    locale = parity_config["locale"]
    preset = parity_config["preset"]
    url = f"https://unpkg.com/emojibase-data@{version}/{locale}/shortcodes/{preset}.json"
    request = urllib.request.Request(url, headers={"User-Agent": "twemojichat-sync"})
    with urllib.request.urlopen(request) as response:
        raw_aliases = json.load(response)

    aliases_by_unified: dict[str, list[str]] = {}
    for unified, aliases in raw_aliases.items():
        normalized_aliases = aliases if isinstance(aliases, list) else [aliases]
        aliases_by_unified[normalize_unified_key(unified)] = expand_alias_variants(normalized_aliases)

    return aliases_by_unified


def extract_assets(config: dict, workdir: Path) -> dict[str, Path]:
    repo = config["assets"]["repo"]
    ref = config["assets"]["ref"]
    asset_path = config["assets"]["asset_path"].rstrip("/") + "/"
    output_dir = workdir / "assets"
    output_dir.mkdir(parents=True, exist_ok=True)

    assets: dict[str, Path] = {}
    with fetch_zip(repo, ref) as archive:
        for member in archive.namelist():
            if not member.endswith(".png"):
                continue
            if asset_path not in member:
                continue

            image_name = Path(member).name
            output_path = output_dir / image_name
            with archive.open(member) as source, output_path.open("wb") as target:
                shutil.copyfileobj(source, target)
            assets[image_name] = output_path

    if not assets:
        raise RuntimeError(f"No assets extracted from {repo}@{ref}")

    return assets


def unicode_from_unified(value: str) -> str:
    return "".join(chr(int(part, 16)) for part in value.split("-"))


def normalize_unified_key(value: str) -> str:
    return value.upper()


def build_aliases(base_aliases: list[str], modifier_key: str | None) -> list[str]:
    aliases = expand_alias_variants(alias.strip(":").lower() for alias in base_aliases if alias)
    if modifier_key is None:
        return aliases
    tone = SKIN_TONE_NAMES.get(modifier_key)
    if tone is None:
        return aliases
    return [f"{alias}_{tone}" for alias in aliases]


def apply_manual_aliases(aliases: list[str], shortcode_config: dict) -> list[str]:
    manual_aliases = shortcode_config.get("manual_aliases", {})
    extra_aliases: list[str] = []

    for alias in aliases:
        extra_aliases.extend(manual_aliases.get(alias, []))

    normalized = [alias.strip(":").lower() for alias in extra_aliases if alias]
    return list(dict.fromkeys([*aliases, *normalized]))


def expand_alias_variants(aliases: Iterable[str]) -> list[str]:
    normalized_aliases = list(dict.fromkeys(str(alias).strip(":").lower() for alias in aliases if alias))
    variants: list[str] = []
    for alias in normalized_aliases:
        if "_" in alias:
            variants.append(alias.replace("_", "-"))
        if "-" in alias:
            variants.append(alias.replace("-", "_"))
    return list(dict.fromkeys([*normalized_aliases, *variants]))


def tone_suffix(modifier_key: str | None) -> str:
    if modifier_key is None:
        return ""
    tone = SKIN_TONE_NAMES.get(modifier_key)
    return f"_{tone}" if tone is not None else ""


def split_alias_tone(alias: str) -> tuple[str, str]:
    for tone in SKIN_TONE_NAMES.values():
        suffix = f"_{tone}"
        if alias.endswith(suffix):
            return alias.removesuffix(suffix), suffix

    legacy_tones = {
        "_tone1": "_light_skin_tone",
        "_tone2": "_medium_light_skin_tone",
        "_tone3": "_medium_skin_tone",
        "_tone4": "_medium_dark_skin_tone",
        "_tone5": "_dark_skin_tone",
    }
    for legacy_suffix, normalized_suffix in legacy_tones.items():
        if alias.endswith(legacy_suffix):
            return alias.removesuffix(legacy_suffix), normalized_suffix

    return alias, ""


def preferred_primary_alias(
    aliases: list[str], shortcode_config: dict, modifier_key: str | None
) -> str | None:
    preferred_aliases = shortcode_config.get("preferred_primary_aliases", {})
    desired_tone_suffix = tone_suffix(modifier_key)

    for alias in aliases:
        base_alias, alias_tone_suffix = split_alias_tone(alias)
        preferred = preferred_aliases.get(base_alias)
        if preferred:
            return preferred.strip(":").lower() + (desired_tone_suffix or alias_tone_suffix)

    return None


def finalize_aliases(
    aliases: list[str], shortcode_config: dict, modifier_key: str | None = None
) -> list[str]:
    preferred = preferred_primary_alias(aliases, shortcode_config, modifier_key)
    if preferred is None:
        return aliases

    return list(dict.fromkeys([*expand_alias_variants([preferred]), *aliases]))


def aliases_for_entry(
    base_aliases: list[str],
    unified_key: str,
    shortcode_config: dict,
    parity_aliases: dict[str, list[str]],
    modifier_key: str | None = None,
) -> list[str]:
    aliases = [*parity_aliases.get(normalize_unified_key(unified_key), []), *build_aliases(base_aliases, modifier_key)]
    aliases = expand_alias_variants(aliases)
    aliases = apply_manual_aliases(aliases, shortcode_config)
    return finalize_aliases(aliases, shortcode_config, modifier_key)


def collect_entries(
    rows: list[dict], assets: dict[str, Path], parity_aliases: dict[str, list[str]]
) -> list[EmojiEntry]:
    entries: list[EmojiEntry] = []
    used_assets: set[str] = set()
    next_codepoint = PAGES_START
    shortcode_config = read_config()["shortcodes"]

    for row in sorted(rows, key=lambda item: item.get("sort_order", 10**9)):
        base_aliases = row.get("short_names") or ([row["short_name"]] if row.get("short_name") else [])
        image_name = row.get("image")
        if image_name and image_name in assets:
            used_assets.add(image_name)
            glyph = chr(next_codepoint)
            next_codepoint += 1
            aliases = aliases_for_entry(base_aliases, row["unified"], shortcode_config, parity_aliases)
            entries.append(
                EmojiEntry(
                    aliases=aliases,
                    glyph=glyph,
                    image_name=image_name,
                    name=row.get("name", ""),
                    primary_alias=aliases[0] if aliases else None,
                    sort_order=row.get("sort_order", 10**9),
                    unicode_value=unicode_from_unified(row["unified"]),
                )
            )

        for modifier_key, variant in sorted((row.get("skin_variations") or {}).items()):
            image_name = variant.get("image")
            if not image_name or image_name not in assets:
                continue
            used_assets.add(image_name)
            glyph = chr(next_codepoint)
            next_codepoint += 1
            aliases = aliases_for_entry(
                base_aliases,
                variant["unified"],
                shortcode_config,
                parity_aliases,
                modifier_key,
            )
            entries.append(
                EmojiEntry(
                    aliases=aliases,
                    glyph=glyph,
                    image_name=image_name,
                    name=f"{row.get('name', '')} {SKIN_TONE_NAMES.get(modifier_key, '').replace('_', ' ')}".strip(),
                    primary_alias=aliases[0] if aliases else None,
                    sort_order=row.get("sort_order", 10**9),
                    unicode_value=unicode_from_unified(variant["unified"]),
                )
            )

    for image_name in sorted(assets):
        if image_name in used_assets:
            continue

        codepoint = Path(image_name).stem
        alias = codepoint.lower().replace("-", "_")
        glyph = chr(next_codepoint)
        next_codepoint += 1
        base_aliases = [alias]
        aliases = aliases_for_entry(base_aliases, codepoint, shortcode_config, parity_aliases)
        entries.append(
            EmojiEntry(
                aliases=aliases,
                glyph=glyph,
                image_name=image_name,
                name=codepoint.upper(),
                primary_alias=aliases[0] if aliases else alias,
                sort_order=10**9,
                unicode_value=unicode_from_unified(codepoint.upper()),
            )
        )

    if next_codepoint >= 0xF8FF:
        raise RuntimeError("Generated glyph set exceeds the BMP private-use range")

    return entries


def ensure_output_dirs() -> None:
    FONT_TEXTURE_ROOT.mkdir(parents=True, exist_ok=True)
    FONT_JSON_PATH.parent.mkdir(parents=True, exist_ok=True)
    INDEX_PATH.parent.mkdir(parents=True, exist_ok=True)


def render_font_pages(entries: list[EmojiEntry], assets: dict[str, Path], config: dict) -> None:
    font_config = config["font"]
    columns = font_config["columns"]
    rows = font_config["rows"]
    cell_size = font_config["cell_size"]
    page_size = columns * rows

    for stale in FONT_TEXTURE_ROOT.glob("twemoji_sheet_*.png"):
        stale.unlink()

    providers = []
    page_count = math.ceil(len(entries) / page_size)

    for page_index in range(page_count):
        page_entries = entries[page_index * page_size : (page_index + 1) * page_size]
        image = Image.new("RGBA", (columns * cell_size, rows * cell_size), (0, 0, 0, 0))
        char_rows = []

        for row_index in range(rows):
            row_chars = []
            for column_index in range(columns):
                entry_index = row_index * columns + column_index
                if entry_index >= len(page_entries):
                    row_chars.append("\u0000")
                    continue

                entry = page_entries[entry_index]
                asset = Image.open(assets[entry.image_name]).convert("RGBA")
                asset = asset.resize((cell_size, cell_size), Image.Resampling.LANCZOS)
                image.alpha_composite(asset, (column_index * cell_size, row_index * cell_size))
                row_chars.append(entry.glyph)
            char_rows.append("".join(row_chars))

        page_name = f"twemoji_sheet_{page_index:02d}.png"
        image.save(FONT_TEXTURE_ROOT / page_name)
        providers.append(
            {
                "type": "bitmap",
                "file": f"twemojichat:font/{page_name}",
                "height": font_config["render_height"],
                "ascent": font_config["ascent"],
                "chars": char_rows,
            }
        )

    FONT_JSON_PATH.write_text(json.dumps({"providers": providers}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_index(entries: list[EmojiEntry], config: dict) -> None:
    payload = {
        "emoji_count": len(entries),
        "sources": config,
        "entries": [
            {
                "aliases": entry.aliases,
                "glyph": entry.glyph,
                "name": entry.name,
                "primary_alias": entry.primary_alias,
                "sort_order": entry.sort_order,
                "unicode": entry.unicode_value,
            }
            for entry in entries
        ],
    }
    INDEX_PATH.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    config = read_config()
    ensure_output_dirs()

    with tempfile.TemporaryDirectory(prefix="twemojichat-sync-") as tempdir:
        workdir = Path(tempdir)
        shortcode_rows = load_shortcode_rows(config)
        assets = extract_assets(config, workdir)
        parity_aliases = load_discord_shortcode_aliases(config)
        entries = collect_entries(shortcode_rows, assets, parity_aliases)
        render_font_pages(entries, assets, config)
        write_index(entries, config)

    print(f"Generated {len(entries)} emoji glyphs")


if __name__ == "__main__":
    main()
