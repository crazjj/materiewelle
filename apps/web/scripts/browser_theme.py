"""Public browser intent for the actual binary theme button; no synthetic select or state writes."""
from playwright.sync_api import expect

THEME_CONTROL = "#theme-preference"
THEME_UI_CONTRACT = "theme-toggle-2026-10-14"


def set_theme(page, theme: str):
    if theme not in {"light", "dark"}:
        raise ValueError("Theme intent must be light or dark")
    button = page.locator(THEME_CONTROL)
    expect(button).to_be_visible()
    expect(button).to_be_enabled()
    wanted = "true" if theme == "dark" else "false"
    if button.get_attribute("aria-pressed") != wanted:
        button.click()
    expect(button).to_have_attribute("aria-pressed", wanted)
    expect(page.locator("html")).to_have_attribute("data-theme", theme)
