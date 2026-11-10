package materiewelle.data

import kotlinx.browser.window
import kotlinx.serialization.SerializationException
import materiewelle.core.LoadFailure
import materiewelle.core.LoadState
import kotlin.js.Promise

/** Only the browser adapter knows Response; the same loading boundary is testable in Node. */
internal data class AssetResponse(val status: Int, val readText: () -> Promise<String>)

private fun requestAsset(asset: LabAsset): Promise<AssetResponse> =
    window.fetch(asset.path).then { response -> AssetResponse(response.status.toInt()) { response.text() } }

/** Each asynchronous failure belongs to its stage; consumer errors must not become data errors. */
internal fun <T> loadAsset(
    asset: LabAsset,
    decode: (String) -> T,
    failureMessage: (LoadFailure) -> String,
    deliver: (LoadState<T>) -> Unit,
    request: (LabAsset) -> Promise<AssetResponse> = ::requestAsset,
): Promise<Unit> {
    fun failed(reason: LoadFailure): LoadState<T> = LoadState.Failed(failureMessage(reason), reason)
    fun decoded(text: String): LoadState<T> = try {
        LoadState.Ready(decode(text))
    } catch (_: SerializationException) {
        failed(LoadFailure.Decode)
    } catch (_: IllegalArgumentException) {
        failed(LoadFailure.Validation)
    } catch (_: Throwable) {
        failed(LoadFailure.Internal)
    }

    deliver(LoadState.Loading)
    val response = try {
        request(asset)
    } catch (_: Throwable) {
        deliver(failed(LoadFailure.Transport))
        return Promise.resolve(Unit)
    }
    return response.then(onFulfilled = { received ->
        if (received.status !in 200..299) {
            Promise.resolve(failed(LoadFailure.Http(received.status)))
        } else {
            try {
                received.readText().then(onFulfilled = ::decoded, onRejected = { failed(LoadFailure.Transport) })
            } catch (_: Throwable) {
                Promise.resolve(failed(LoadFailure.Transport))
            }
        }
    }, onRejected = {
        Promise.resolve(failed(LoadFailure.Transport))
    }).then { state -> deliver(state) }
}

internal fun LoadFailure.description(): String = when (this) {
    LoadFailure.Transport -> "Die Datendatei konnte nicht übertragen werden."
    is LoadFailure.Http -> "Die Datendatei ist nicht verfügbar (HTTP $status)."
    LoadFailure.Decode -> "Die Datei hat kein lesbares Datenformat."
    LoadFailure.Validation -> "Die Datei passt nicht zur geprüften Datenspezifikation."
    LoadFailure.Internal -> "Die Daten konnten wegen eines internen Fehlers nicht verarbeitet werden."
}
