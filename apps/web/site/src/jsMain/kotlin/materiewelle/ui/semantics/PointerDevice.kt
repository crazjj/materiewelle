package materiewelle.ui.semantics

import org.w3c.dom.events.MouseEvent

private external interface NativePointerDevice { val pointerType: String? }

/** CSS-pixel hit areas are independent of the native coordinates written to the measurement. */
fun markerHitRadiusCss(event: MouseEvent): Double =
    if (event.unsafeCast<NativePointerDevice>().pointerType == "touch") 22.0 else 14.0
