package materiewelle.data

import kotlinx.serialization.json.Json
import materiewelle.core.LoadFailure
import materiewelle.core.LoadState
import materiewelle.core.ScientificData
import materiewelle.core.Voltage
import kotlin.js.Promise
import kotlin.test.*

class AssetLoadTest {
    private val states = mutableListOf<LoadState<Voltage>>()
    private val decode: (String) -> Voltage = { Voltage(Json.decodeFromString<Double>(it)) }

    private fun load(
        request: (LabAsset) -> Promise<AssetResponse>,
        parse: (String) -> Voltage = decode,
    ): Promise<Unit> = loadAsset(LabAsset.SCIENTIFIC_DATA, parse, { it.description() }, { states += it }, request)

    private fun response(text: String, status: Int = 200): Promise<AssetResponse> =
        Promise.resolve(AssetResponse(status) { Promise.resolve(text) })

    private fun checkFailure(expected: LoadFailure) {
        assertEquals(2, states.size)
        assertEquals(LoadState.Loading, states.first())
        val failed = assertIs<LoadState.Failed>(states.last())
        assertEquals(expected, failed.reason)
        assertTrue(failed.message.isNotBlank())
        assertFalse(failed.message.contains("private response"))
        assertTrue(states.none { it is LoadState.Ready })
    }

    @Test fun httpFailureKeepsStatusAndNeverReadsOrDecodesErrorPage() = load({ asset ->
        assertEquals(LabAsset.SCIENTIFIC_DATA, asset)
        Promise.resolve(AssetResponse(404) { error("Error body must not be read") })
    }, { error("Error body must not be decoded") }).then { checkFailure(LoadFailure.Http(404)) }

    @Test fun rejectedRequestIsTransportFailureWithoutRawErrorText() =
        load({ Promise.reject(IllegalStateException("private response")) })
            .then { checkFailure(LoadFailure.Transport) }

    @Test fun synchronouslyRejectedRequestAlsoFinishesLoading() =
        load({ error("private response") }).then { checkFailure(LoadFailure.Transport) }

    @Test fun interruptedResponseBodyIsTransportFailureNotMalformedData() =
        load({ Promise.resolve(AssetResponse(200) { Promise.reject(IllegalStateException("private response")) }) })
            .then { checkFailure(LoadFailure.Transport) }

    @Test fun invalidJsonCannotEnterCalculationState() =
        load({ response("private response") }).then { checkFailure(LoadFailure.Decode) }

    @Test fun wrongJsonShapeCannotEnterCalculationState() =
        load({ response("{}") }).then { checkFailure(LoadFailure.Decode) }

    @Test fun decodedButPhysicallyInvalidValueRetainsValidationFailure() =
        load({ response("-150") }).then { checkFailure(LoadFailure.Validation) }

    @Test fun unsupportedScientificSpecificationRetainsExistingParserRejection() =
        load({ response("""{"schema_version":"unsupported"}""") }, { text ->
            ScientificData.fromJson(text)
            error("Unsupported schema must be rejected")
        }).then { checkFailure(LoadFailure.Validation) }

    @Test fun unexpectedDecoderBugIsNotReportedAsNetworkOrInvalidScientificData() =
        load({ response("150") }, { error("private response") })
            .then { checkFailure(LoadFailure.Internal) }

    @Test fun validatedValueIsDeliveredUnchangedAfterLoading() =
        load({ response("150") }).then {
            assertEquals(listOf(LoadState.Loading, LoadState.Ready(Voltage(150.0))), states)
        }

    @Test fun consumerBugCannotBeSwallowedAsResourceFailure() =
        loadAsset(LabAsset.SCIENTIFIC_DATA, decode, { it.description() }, { state ->
            states += state
            if (state is LoadState.Ready) error("consumer failure")
        }, { response("150") }).then(onFulfilled = {
            fail("Consumer error must remain a rejected delivery")
        }, onRejected = { error ->
            assertEquals("consumer failure", error.message)
            assertEquals(listOf(LoadState.Loading, LoadState.Ready(Voltage(150.0))), states)
        })
}
