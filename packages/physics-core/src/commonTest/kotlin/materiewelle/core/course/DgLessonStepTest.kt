package materiewelle.core.course

import kotlin.test.*

class DgLessonStepTest {
    @Test fun sixStepsHaveRoundTripLinksAndOneChapter() {
        assertEquals(6, DgLessonStep.entries.size)
        assertEquals(6, DgLessonStep.entries.map { it.key }.toSet().size)
        DgLessonStep.entries.forEach { step ->
            assertEquals(step.route, AppRoute.parse(step.route.hash))
            assertEquals(AppSection.DAVISSON_GERMER, AppSection.forRoute(step.route))
            assertEquals(step, DgLessonStep.forRoute(step.route))
        }
        assertNull(AppRoute.parse("#davisson-germer/ungueltiger-schritt"))
    }

    @Test fun navigationHasNoCircularOrLockedSteps() {
        assertNull(DgLessonStep.SETUP.previous)
        assertNull(DgLessonStep.CONCLUSION.next)
        DgLessonStep.entries.zipWithNext().forEach { (first, second) ->
            assertEquals(second, first.next)
            assertEquals(first, second.previous)
        }
    }

    @Test fun legacyLinksKeepTheirWorkIdentityAndSelectTheRightStep() {
        val steps = listOf(DgLessonStep.SETUP, DgLessonStep.OBSERVATION, DgLessonStep.BRAGG, DgLessonStep.CALCULATION)
        val stations = listOf(StationId.S08_DG_SETUP, StationId.S09_CRYSTAL, StationId.S10_GEOMETRY, StationId.S11_DG_EVALUATION)
        stations.zip(steps).forEach { (station, step) ->
            val route = AppRoute.parse("#lernen/${station.key}")!!
            assertEquals(AppRoute.Course(station), route)
            assertEquals(step, DgLessonStep.forRoute(route))
        }
    }

    @Test fun bookmarkRestoresAnExactStepWithoutCreatingScientificWork() {
        DgLessonStep.entries.forEach { step ->
            val saved = ReadingPosition.fromRoute(step.route)!!
            assertEquals(step.route, ReadingPosition.parse(saved.json()).route)
            assertNull(saved.station)
        }
        assertFailsWith<IllegalArgumentException> {
            ReadingPosition(section = AppSection.JONSSON, dgStep = DgLessonStep.GEOMETRY).validate()
        }
        assertFailsWith<IllegalArgumentException> {
            ReadingPosition(section = AppSection.DAVISSON_GERMER, station = StationId.S11_DG_EVALUATION, dgStep = DgLessonStep.CALCULATION).validate()
        }
    }

    @Test fun oldBookmarkBytesRemainReadable() {
        val raw = """{"section":"DAVISSON_GERMER","station":"S10_GEOMETRY"}"""
        val saved = ReadingPosition.parse(raw)
        assertEquals(AppRoute.Course(StationId.S10_GEOMETRY), saved.route)
        assertNull(saved.dgStep)
    }
}
