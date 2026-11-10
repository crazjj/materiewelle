#!/usr/bin/env python3
"""Prepare a verified Kobweb root export for `vercel deploy --prebuilt`."""
import argparse
import json
from pathlib import Path
from product_delivery import stage_public_site


def prepare(source, output, commit):
    source, output = source.resolve(), output.resolve()
    assert not output.is_relative_to(source) and not source.is_relative_to(output), "Source and deployment workspace must not overlap"
    assert not output.exists() or not any(output.iterdir()), "Choose a fresh deployment workspace; existing files are preserved"
    static = output / ".vercel/output/static"
    public = stage_public_site(source, static, base="/", commit=commit)
    # Hash navigation needs no catch-all rewrite. Missing source files stay 404.
    (static.parent / "config.json").write_text(json.dumps({"version": 3}, indent=2) + "\n")
    return public


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, required=True, help="Fresh verified root export (base path /)")
    parser.add_argument("--output", type=Path, required=True, help="New minimal deployment workspace")
    parser.add_argument("--commit", required=True)
    args = parser.parse_args()
    manifest = prepare(args.source, args.output, args.commit)
    print(json.dumps({"output": str(args.output.resolve()), "files": len(manifest["files"]),
                      "deployment_base": "/", "source_maps_published": False, "vercel_build_output_version": 3}))


if __name__ == "__main__":
    main()
