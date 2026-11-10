package materiewelle.course.graphics

import kotlin.test.*
import materiewelle.core.ObservationPosition
import materiewelle.core.SlitConfiguration

class SlitExplorationUiStateTest {
    @Test fun inspectingTheMainPlotKeepsTheOptionalTheoryClosedAndOwnNotesIntact() {
        val view = SlitExplorationUiState()
        view.configuration = SlitConfiguration.LEFT_ONLY
        view.predict("Mit einem Spalt erwarte ich eine breitere Verteilung.")
        val draft = view.draft()

        view.inspect(ObservationPosition(.001))
        view.inspect(ObservationPosition(-.0005))

        assertEquals(ObservationPosition(-.0005), view.position)
        assertFalse(view.phaseOpen)
        assertEquals(draft, view.draft())
    }

    @Test fun inspectingAnotherPositionRespectsTheLearnersExplicitOpenAndCloseChoice() {
        val view = SlitExplorationUiState()
        view.phaseOpen = true
        view.inspect(ObservationPosition(.001))
        assertTrue(view.phaseOpen)
        assertEquals(ObservationPosition(.001), view.position)

        view.phaseOpen = false
        view.inspect(ObservationPosition(.002))
        assertFalse(view.phaseOpen)
        assertEquals(ObservationPosition(.002), view.position)
    }
}
