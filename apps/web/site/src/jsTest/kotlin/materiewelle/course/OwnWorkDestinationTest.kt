package materiewelle.course

import kotlin.test.Test
import kotlin.test.assertEquals
import materiewelle.core.course.*
import materiewelle.ui.semantics.Id

class OwnWorkDestinationTest {
    @Test fun missingReasonWithUsableMarkersOpensTheReasonField() {
        val entry = OwnWorkEntry(OwnWorkSection.MEASUREMENT, OwnWorkPresence.PARTIAL,
            listOf(OwnWorkField.MEASUREMENT_REASON))
        assertEquals(Id.JONSSON_JUSTIFICATION, entry.destination().second)
    }

    @Test fun missingMarkersTakePriorityOverTheReason() {
        val entry = OwnWorkEntry(OwnWorkSection.MEASUREMENT, OwnWorkPresence.PARTIAL,
            listOf(OwnWorkField.MEASUREMENT_MARKS, OwnWorkField.MEASUREMENT_REASON))
        assertEquals(Id.JONSSON_INTERACTIVE_IMAGE, entry.destination().second)
    }

    @Test fun completeMeasurementOpensItsPicture() {
        val entry = OwnWorkEntry(OwnWorkSection.MEASUREMENT, OwnWorkPresence.PRESENT, emptyList())
        assertEquals(Id.JONSSON_INTERACTIVE_IMAGE, entry.destination().second)
    }
}
