#!/usr/bin/env bash
set -euo pipefail
TASK_TOOLS_ROOT="${TASK_TOOLS_ROOT:-${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools}"
export TASK_TOOLS_ROOT
TASK_SCRIPTS_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
if [[ "$(uname -s)" != Linux || "$(uname -m)" != x86_64 ]]; then
  echo 'Dieser Bootstrap ist für Linux x86_64 gepinnt. Andere Plattformen: JDK21 und Gradle8.14.3 passend installieren.' >&2
  exit 1
fi
for tool in curl tar unzip sha256sum python3 flock; do command -v "$tool" >/dev/null || { echo "Voraussetzung fehlt: $tool" >&2; exit 1; }; done
mkdir -p "$TASK_TOOLS_ROOT/downloads"
exec 9>"$TASK_TOOLS_ROOT/bootstrap.lock"
flock 9
fetch_verified() {
  local name="$1" url="$2" checksum="$3" target="$TASK_TOOLS_ROOT/downloads/$1"
  if [[ ! -f "$target" ]]; then
    curl --fail --location --retry 2 --output "$target.part" "$url"
    echo "$checksum  $target.part" | sha256sum --check --status || { echo "Prüfsumme falsch: $name; Artefakt wird nicht verwendet." >&2; exit 1; }
    mv "$target.part" "$target"
  fi
  echo "$checksum  $target" | sha256sum --check --status || { echo "Prüfsumme falsch: $target; keine Installation." >&2; exit 1; }
}
if [[ ! -x "$TASK_TOOLS_ROOT/jdk-21/bin/javac" ]]; then
  fetch_verified jdk-21.tar.gz https://download.oracle.com/java/21/archive/jdk-21.0.12.1_linux-x64_bin.tar.gz 12f870b21301b42292558a3f872ce543affa2b86cb6458591c78388c41ddb111
  TASK_JDK_STAGE="$(mktemp -d "$TASK_TOOLS_ROOT/jdk-stage.XXXXXX")"
  tar -xzf "$TASK_TOOLS_ROOT/downloads/jdk-21.tar.gz" -C "$TASK_JDK_STAGE" --strip-components=1
  if [[ -e "$TASK_TOOLS_ROOT/jdk-21" ]]; then echo 'Vorhandener unvollständiger jdk-21-Ordner wird nicht überschrieben.' >&2; exit 1; fi
  mv "$TASK_JDK_STAGE" "$TASK_TOOLS_ROOT/jdk-21"
fi
if [[ ! -x "$TASK_TOOLS_ROOT/gradle-8.14.3/bin/gradle" ]]; then
  fetch_verified gradle-8.14.3-bin.zip https://services.gradle.org/distributions/gradle-8.14.3-bin.zip bd71102213493060956ec229d946beee57158dbd89d0e62b91bca0fa2c5f3531
  TASK_GRADLE_STAGE="$(mktemp -d "$TASK_TOOLS_ROOT/gradle-stage.XXXXXX")"
  unzip -q "$TASK_TOOLS_ROOT/downloads/gradle-8.14.3-bin.zip" -d "$TASK_GRADLE_STAGE"
  if [[ -e "$TASK_TOOLS_ROOT/gradle-8.14.3" ]]; then echo 'Vorhandener unvollständiger Gradle-Ordner wird nicht überschrieben.' >&2; exit 1; fi
  mv "$TASK_GRADLE_STAGE/gradle-8.14.3" "$TASK_TOOLS_ROOT/gradle-8.14.3"
  rmdir "$TASK_GRADLE_STAGE"
fi
cp "$TASK_SCRIPTS_DIR/gradle-run" "$TASK_TOOLS_ROOT/gradle-run"
chmod +x "$TASK_TOOLS_ROOT/gradle-run"
"$TASK_TOOLS_ROOT/jdk-21/bin/javac" -version
"$TASK_TOOLS_ROOT/gradle-run" --version
