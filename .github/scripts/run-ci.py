#!/usr/bin/env python3
"""Stream a command and expose its failure tail through GitHub check annotations."""
import argparse
from collections import deque
import os
from pathlib import Path
import re
import subprocess
import sys

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--log", required=True, type=Path)
parser.add_argument("command", nargs=argparse.REMAINDER)
args = parser.parse_args()
command = args.command[1:] if args.command[:1] == ["--"] else args.command
if not command:
    parser.error("A command is required after --")
args.log.parent.mkdir(parents=True, exist_ok=True)
tail = deque(maxlen=45)
with args.log.open("w", encoding="utf8") as logfile:
    process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                               text=True, encoding="utf8", errors="replace", bufsize=1)
    for line in process.stdout:
        sys.stdout.write(line)
        sys.stdout.flush()
        logfile.write(line)
        tail.append(line)
    code = process.wait()
if code and os.environ.get("GITHUB_ACTIONS") == "true":
    detail = re.sub(r"(https?://[^\s?]+)\?[^\s]+", r"\1?[redacted]", "".join(tail))
    message = ("Command exited " + str(code) + ":\n" + detail)[-6000:]
    message = message.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
    print("::error title=Build or browser failure::" + message, file=sys.stderr)
raise SystemExit(code)
