#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
TASK_TOOLS_ROOT="${TASK_TOOLS_ROOT:-${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools}"
if [[ ! -x "$TASK_TOOLS_ROOT/gradle-run" ]]; then ../../tools/build/bootstrap.sh; fi
exec "$TASK_TOOLS_ROOT/gradle-run" "$@"
