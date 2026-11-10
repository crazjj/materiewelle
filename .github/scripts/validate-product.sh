#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
git ls-files --error-unmatch tools/build/build_inputs.py tools/build/gradle-run tools/build/bootstrap.sh tools/build/setup-cloud.sh tools/build/package-delivery.py > /dev/null
python content/typst/test-handout.py
python content/tutor/test-compact-pack.py
python tools/build/test-package-delivery.py
python apps/web/scripts/test-ui-contract.py
python apps/web/scripts/test-math-text-build.py
python apps/web/scripts/test-export-spa.py
apps/web/run-gradle.sh :physics-core:jvmTest :physics-core:jsNodeTest :browser-design:jsNodeTest :site:jsNodeTest
python .github/scripts/run-ci.py --log release-evidence/build-ci.log -- python .github/scripts/build-release-web.py
python apps/web/scripts/test-theme-bootstrap.py
python apps/web/scripts/test-browser-assets.py
