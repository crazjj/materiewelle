#!/usr/bin/env bash
set -euo pipefail
TASK_REPO_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
TASK_TOOLS_ROOT="${TASK_TOOLS_ROOT:-${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools}"
export TASK_TOOLS_ROOT
export PIP_CACHE_DIR="$TASK_TOOLS_ROOT/pip-cache"
command -v node >/dev/null || { echo 'Node.js wird vorausgesetzt (CI: Node 24).' >&2; exit 1; }
"$TASK_REPO_ROOT/tools/build/bootstrap.sh"
python3 -m venv "$TASK_TOOLS_ROOT/typst"
"$TASK_TOOLS_ROOT/typst/bin/python" -m pip install --disable-pip-version-check -r "$TASK_REPO_ROOT/content/typst/requirements.txt" -r "$TASK_REPO_ROOT/tools/browser/requirements.txt"
"$TASK_TOOLS_ROOT/typst/bin/python" -m pip check
echo 'JDK, Gradle und Typst vorbereitet. App-Builds und Startbefehle: jeweilige README.'
