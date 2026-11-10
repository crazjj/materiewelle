"""Shared artifact helpers for current and historical browser evidence.

This module has no Playwright or product-page dependency. Scenario assertions
remain in their existing harnesses; this is a first, deliberately small step
towards separating that infrastructure from the historical Run-01 suite.
"""
from contextlib import contextmanager
from functools import partial
import hashlib
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
import threading


def sha(raw):
    return hashlib.sha256(raw).hexdigest()


class QuietHandler(SimpleHTTPRequestHandler):
    def log_message(self, *_):
        pass


@contextmanager
def served(directory):
    server = ThreadingHTTPServer(("127.0.0.1", 0), partial(QuietHandler, directory=str(directory)))
    worker = threading.Thread(target=server.serve_forever, daemon=True)
    worker.start()
    try:
        yield f"http://127.0.0.1:{server.server_port}/"
    finally:
        server.shutdown()
        server.server_close()
        worker.join()
