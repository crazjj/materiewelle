package materiewelle.ui

import org.jetbrains.compose.web.dom.Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.style.addVariantBase
import org.jetbrains.compose.web.css.px

/** Silk behavior and sizing, with the laboratory's shared CSS states and tokens. */
val InstrumentButtonVariant = ButtonStyle.addVariantBase {
    Modifier.borderRadius(6.px).minHeight(44.px).padding(10.px, 16.px)
}

@Composable
fun ExperimentButton(label: String, active: Boolean, id: ElementId, choose: () -> Unit) {
    Button(
        onClick = { choose() },
        variant = InstrumentButtonVariant,
        modifier = Modifier.elementId(id).styles(LabClass.NAV_ITEM, if (active) LabClass.ACTIVE else LabClass.INACTIVE)
            .ariaPressed(active),
    ) { Text(label) }
}
