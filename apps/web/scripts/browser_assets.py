"""Wait for the actual drawn images before capture, reload or context disposal.

Font readiness alone does not wait for external Typst SVGs. Holding decoded
image consumers until completion prevents teardown from cancelling valid cold
requests. Missing/broken images still fail; no network errors are filtered out.
"""


ASSET_TIMEOUT_MS = 30_000


def _deadline(value):
    if not isinstance(value, int) or isinstance(value, bool) or not 1 <= value <= 60_000:
        raise ValueError('Browser asset deadline must be an integer between 1 and 60000 ms')
    return value


def settle_images(page, *, timeout_ms=ASSET_TIMEOUT_MS):
    page.evaluate("""async timeoutMs => {
      const sources = new Set([
        ...Array.from(document.images, image => image.currentSrc || image.src),
        ...Array.from(document.querySelectorAll('svg image'), image => image.href.baseVal)
      ].filter(Boolean).map(source => new URL(source, document.baseURI).href));
      await Promise.all(Array.from(sources, async source => {
        const image = new Image(); image.src = source;
        let timer;
        try {
          await Promise.race([
            image.decode().catch(error => {
              throw new Error('Image could not be decoded: ' + source + ' (' + error.message + ')');
            }),
            new Promise((_, reject) => {
              timer = setTimeout(() => reject(new Error(
                'Image decode timed out after ' + timeoutMs + ' ms: ' + source +
                '; complete=' + image.complete + '; naturalWidth=' + image.naturalWidth +
                '; page=' + location.href)), timeoutMs);
            })
          ]);
        } finally { clearTimeout(timer); }
      }));
    }""", _deadline(timeout_ms))


def settle_fonts(page, *, timeout_ms=ASSET_TIMEOUT_MS):
    page.evaluate("""async timeoutMs => {
      let timer;
      try {
        await Promise.race([
          document.fonts.ready,
          new Promise((_, reject) => {
            timer = setTimeout(() => reject(new Error(
              'Font readiness timed out after ' + timeoutMs + ' ms; status=' + document.fonts.status +
              '; loading=' + [...document.fonts].filter(font => font.status === 'loading')
                .map(font => font.family).join(', ') + '; page=' + location.href)), timeoutMs);
          })
        ]);
      } finally { clearTimeout(timer); }
    }""", _deadline(timeout_ms))


def reload_with_images(page):
    settle_images(page)
    return page.reload(wait_until="networkidle")


def close_with_images(page):
    settle_images(page)
    page.close()
