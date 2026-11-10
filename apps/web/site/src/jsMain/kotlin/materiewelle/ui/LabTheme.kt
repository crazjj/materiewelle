package materiewelle.ui

import androidx.compose.runtime.*
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import kotlinx.browser.document
import kotlinx.browser.window
import materiewelle.presentation.PresentationKeys
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** Browser-test compatibility changes independently from the course and stored scientific work. */
const val BROWSER_UI_CONTRACT = "run-one-2026-11-02"

/** The binary user choice is independent of operating-system appearance. */
/** Only this adapter lets the typed preference cross LocalStorage as a String. */
internal object BrowserThemeStorage {
    const val KEY = PresentationKeys.THEME

    fun load(): ThemePreference {
        val stored = runCatching { window.localStorage.getItem(KEY) }.getOrNull()
        val resolved = ThemePreference.fromStorage(stored)
        if (stored != null && stored != resolved.key) save(resolved)
        return resolved
    }

    fun save(preference: ThemePreference) {
        runCatching { window.localStorage.setItem(KEY, preference.key) }
    }
}

@InitSilk
fun initializeLaboratoryTheme(ctx: InitSilkContext) {
    ctx.config.initialColorMode =
        if (BrowserThemeStorage.load() == ThemePreference.DARK) ColorMode.DARK else ColorMode.LIGHT
}

data class ThemeController(val preference: ThemePreference, val toggle: () -> Unit)

val LocalLabTheme = staticCompositionLocalOf { ThemeController(ThemePreference.DARK) {} }

@Composable
fun LabTheme(content: @Composable () -> Unit) {
    var preference by remember { mutableStateOf(BrowserThemeStorage.load()) }
    val silkMode = ColorMode.currentState
    SideEffect {
        document.documentElement?.setAttribute(
            LabData.THEME.attribute,
            LabData.THEME.encode(
                if (preference == ThemePreference.DARK) ResolvedTheme.DARK else ResolvedTheme.LIGHT
            ),
        )
        silkMode.value = if (preference == ThemePreference.DARK) ColorMode.DARK else ColorMode.LIGHT
    }
    CompositionLocalProvider(
        LocalLabTheme provides
            ThemeController(preference) {
                // Read the latest state when clicked, including two clicks before the next Compose
                // frame.
                val next = preference.toggled()
                preference = next
                BrowserThemeStorage.save(next)
            }
    ) {
        content()
    }
}

/** A native button provides keyboard, touch and focus behavior without a simulated select. */
@Composable
fun ThemeChooser() {
    val theme = LocalLabTheme.current
    Div(attrs = { styles(LabClass.THEME_CHOOSER) }) {
        Button(
            attrs = {
                elementId(Id.THEME_PREFERENCE)
                ariaLabel("Dunkle Darstellung")
                ariaPressed(theme.preference == ThemePreference.DARK)
                onClick { theme.toggle() }
            }
        ) {
            Span(
                attrs = {
                    styles(LabClass.THEME_TOGGLE_TRACK)
                    ariaHidden(true)
                }
            ) {
                Span(attrs = { styles(LabClass.THEME_TOGGLE_THUMB) }) {
                    Text(if (theme.preference == ThemePreference.DARK) "☾" else "☀")
                }
            }
            Span(attrs = { styles(LabClass.THEME_TOGGLE_LABEL) }) { Text(theme.preference.label) }
        }
    }
}
