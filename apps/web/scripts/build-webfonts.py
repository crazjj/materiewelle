#!/usr/bin/env python3
"""Rebuild committed WOFF2 assets from the bundled, unmodified Latin Modern OTFs.

Optional maintenance command; the application build consumes committed WOFF2s.
Toolchain: fonttools==4.61.1 and brotli==1.1.0. No subsetting or glyph edits.
"""
from pathlib import Path
import hashlib
from fontTools.ttLib import TTFont

FONTS = Path(__file__).resolve().parents[1] / "site/src/jsMain/resources/public/fonts"
FACES = (
    "lmroman10-regular", "lmroman10-bold", "lmroman10-italic", "lmroman10-bolditalic",
    "lmmono10-regular", "lmmono10-italic",
)


def main():
    records = []
    for face in FACES:
        source = FONTS / (face + ".otf")
        target = FONTS / (face + ".woff2")
        font = TTFont(source, recalcTimestamp=False)
        font.flavor = "woff2"
        font.save(target)
        compressed = TTFont(target)
        assert compressed.getBestCmap() == font.getBestCmap()
        assert compressed["hmtx"].metrics == font["hmtx"].metrics
        assert compressed["head"].unitsPerEm == font["head"].unitsPerEm
        for path in (source, target):
            records.append(hashlib.sha256(path.read_bytes()).hexdigest() + "  " + path.name)
    math = FONTS / "latinmodern-math.otf"
    records.append(hashlib.sha256(math.read_bytes()).hexdigest() + "  " + math.name)
    (FONTS / "LatinModern-MANIFEST.txt").write_text(
        "Latin Modern distribution 2.005; Roman/Mono OTF version 2.004; Math 1.959\n"
        "Based on Donald E. Knuth's Computer Modern; not the identical original font.\n"
        "Copyright and authors: see LatinModern-COPYRIGHT.txt. GUST Font License.\n"
        "Sources: the unmodified OTFs distributed by the GUST e-foundry,\n"
        "https://www.gust.org.pl/projects/e-foundry/latin-modern\n"
        "https://www.gust.org.pl/projects/e-foundry/lm-math\n"
        "WOFF2 files: lossless format conversion only, no subsets or renamed glyphs.\n"
        "Rebuild: scripts/build-webfonts.py, fonttools 4.61.1 + brotli 1.1.0.\n"
        "Browser: six local WOFF2 faces; Typst: bundled OTFs. OTFs are build inputs\n"
        "and are excluded from the static SPA export by product_assets.py.\n\n"
        + "\n".join(records) + "\n"
    )
    print(f"Built {len(FACES)} local WOFF2 faces; glyph mappings and advances preserved.")


if __name__ == "__main__":
    main()
