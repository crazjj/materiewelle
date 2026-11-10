package materiewelle.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemePreferenceTest {
    @Test fun defaultLegacySystemAndUnknownStorageChooseDark() {
        listOf(null, "system", "unknown", "", "LIGHT", " dark ").forEach {
            assertEquals(ThemePreference.DARK, ThemePreference.fromStorage(it))
        }
    }

    @Test fun explicitSavedLightAndDarkAreRetained() {
        assertEquals(ThemePreference.LIGHT, ThemePreference.fromStorage("light"))
        assertEquals(ThemePreference.DARK, ThemePreference.fromStorage("dark"))
    }

    @Test fun togglingTwiceReturnsToTheExactOriginalChoice() {
        ThemePreference.entries.forEach {
            assertEquals(it, it.toggled().toggled())
            assertEquals(if (it == ThemePreference.DARK) ThemePreference.LIGHT else ThemePreference.DARK, it.toggled())
        }
    }
}
