package materiewelle

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import materiewelle.ui.LabTheme

@App
@Composable
fun AppEntry(content: @Composable () -> Unit) {
    SilkApp { LabTheme { content() } }
}
