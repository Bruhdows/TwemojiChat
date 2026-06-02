#!/usr/bin/env python3

from __future__ import annotations

import io
import json
import math
import shutil
import tempfile
import urllib.request
import zipfile
from dataclasses import dataclass
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parent.parent
CONFIG_PATH = ROOT / "tools" / "twemoji_sources.json"
GENERATED_ROOT = ROOT / "src" / "generated" / "resources"
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


def build_aliases(base_aliases: list[str], modifier_key: str | None) -> list[str]:
    aliases = list(dict.fromkeys(alias.strip(":").lower() for alias in base_aliases if alias))
    if modifier_key is None:
        return aliases
    tone = SKIN_TONE_NAMES.get(modifier_key)
    if tone is None:
        return aliases
    return [f"{alias}_{tone}" for alias in aliases]


def collect_entries(rows: list[dict], assets: dict[str, Path]) -> list[EmojiEntry]:
    entries: list[EmojiEntry] = []
    next_codepoint = PAGES_START

    for row in sorted(rows, key=lambda item: item.get("sort_order", 10**9)):
        base_aliases = row.get("short_names") or ([row["short_name"]] if row.get("short_name") else [])
        image_name = row.get("image")
        if image_name and image_name in assets:
            glyph = chr(next_codepoint)
            next_codepoint += 1
            aliases = build_aliases(base_aliases, None)
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
            glyph = chr(next_codepoint)
            next_codepoint += 1
            aliases = build_aliases(base_aliases, modifier_key)
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
        entries = collect_entries(shortcode_rows, assets)
        render_font_pages(entries, assets, config)
        write_index(entries, config)

    print(f"Generated {len(entries)} emoji glyphs")


if __name__ == "__main__":
    main()
