#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
export_directory="${2:-dist-run-two}"
if [[ ! -f "$export_directory/index.html" && ! -f "$export_directory/materiewelle/index.html" ]]; then
  echo 'Kein Webexport in diesem Ordner. Zuerst ./build-static.sh ausführen oder einen Exportordner als zweites Argument angeben.' >&2
  exit 1
fi
exec python3 -m http.server "${1:-8082}" --bind 0.0.0.0 --directory "$export_directory"
