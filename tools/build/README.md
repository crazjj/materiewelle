# Build-Werkzeuge

Der Bootstrap ist für **Linux x86_64** ausgelegt. Er installiert Oracle JDK
21.0.12.1 und Gradle 8.14.3 aus festen Versions-URLs und prüft die Downloads gegen
SHA-256-Prüfsummen. Voraussetzungen sind Bash, curl, tar, unzip, sha256sum, Python 3
und flock. Rootrechte sind dafür nicht erforderlich.

Vom Repository-Stamm:

```bash
tools/build/bootstrap.sh
# Optional: eigener beschreibbarer Installationsort
TASK_TOOLS_ROOT="$PWD/.tools" tools/build/bootstrap.sh
```

Standardziel ist `${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools`. Bei einem
abweichenden Ziel muss `TASK_TOOLS_ROOT` auch für spätere Build-Aufrufe gesetzt
sein. Binärdistributionen und Caches gehören nicht in Git. JDK und Gradle behalten
ihre jeweiligen Lizenzen; die Repository-Lizenz ersetzt diese nicht.

`gradle-run` nutzt die installierte Werkzeugkette, beschränkt Gradle auf zwei
Worker und serialisiert Builds mit flock. Ein separater Bootstrap-Lock verhindert
konkurrierende Installationen. HTTP-/HTTPS-Proxyvariablen werden als Host und Port
an Java übergeben; Zugangsdaten werden nicht ausgegeben. Ein vorhandener
System-Java-Truststore wird genutzt; `TASK_JAVA_TRUSTSTORE` erlaubt einen eigenen
Truststore. TLS- und Prüfsummenprüfung bleiben aktiv.

`tools/build/setup-cloud.sh` ergänzt eine Python-Umgebung mit den gepinnten
Typst- und Browserabhängigkeiten. Dafür sind Python mit venv und eine vorhandene
Node-Laufzeit erforderlich (erprobt: Node 24). Die Python-Umgebung liegt unter
`$TASK_TOOLS_ROOT/typst`. Der erste Kotlin-Build lädt zusätzlich Maven-/npm-Pakete;
Kotlin verwaltet seine Node-Laufzeit selbst. Browser und Systempakete werden
separat installiert:

```bash
export TASK_TOOLS_ROOT="${TASK_TOOLS_ROOT:-${XDG_CACHE_HOME:-$HOME/.cache}/materiewelle-tools}"
tools/build/setup-cloud.sh
"$TASK_TOOLS_ROOT/typst/bin/python" -m playwright install --with-deps chromium
# Auf Debian/Ubuntu für PDF-Prüfungen: apt-get install poppler-utils fontconfig
```

Playwrights `--with-deps` kann Rootrechte für Systembibliotheken benötigen. Ein
frischer Linux-Build benötigt Netzwerkzugriff auf die offiziellen Paketquellen.
Andere Betriebssysteme benötigen eine angepasste Werkzeuginstallation; die
Shell-Helfer setzen Bash und flock voraus.

`build_inputs.py` bindet Web-Exporte an die aktuellen Quellen, Generatoren,
Fachdaten, Typst-Auswahl sowie eingebundene Fonts und ihre Lizenzdateien. Für eine
lokale Lieferung nutzt `package-delivery.py` die gemeinsamen lokalen Build- und Browserprüfungen:

```bash
python3 .github/scripts/build-release-web.py
python3 .github/scripts/check-release-web.py
python3 tools/build/package-delivery.py release-files
```

Die Quellen müssen eingecheckt sein und das Zielverzeichnis muss leer sein. Die
Lieferung enthält statische Web-Archive für `/` und `/materiewelle/`, das aktuelle
Handout als PDF und Prüfmanifeste. Dieser Befehl veröffentlicht nichts. Die
Produkt-Website wird über die bestehende Vercel-Anbindung ausgeliefert; es gibt
keine aktiven GitHub-Actions-Workflows. Frühere Workflows liegen im Archiv.
