package materiewelle.core.course

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArticleLinksTest {
    @Test fun articleLinksReuseExistingWorkIdentities() {
        assertEquals(AppRoute.Course(StationId.S05_DERIVATION), AppRoute.parse("#debroglie/herleitung"))
        assertEquals(AppRoute.Course(StationId.S06_CALCULATION), AppRoute.parse("#debroglie/rechnung"))
        assertEquals(AppRoute.Course(StationId.S16_CONCLUSION), AppRoute.parse("#fazit"))
        assertNull(AppRoute.parse("#debroglie/nicht-vorhanden"))
    }

    @Test fun oldWorkAndLinksRemainIndependent() {
        val original = SessionSnapshot()
        for (station in StationId.entries) {
            assertEquals(AppRoute.Course(station), AppRoute.parse("#lernen/${station.key}"))
        }
        assertEquals(original, SessionSnapshot.fromJson(original.json()))
        assertEquals(StationId.S07_RECEPTION, DeBroglieSection.RECEPTION.station)
    }

    @Test fun documentLinksResolveToExplanationWithoutChangingStorageIdentities() {
        for (section in DocumentSectionId.entries) {
            assertEquals(AppRoute.Course(section.station ?: StationId.S16_CONCLUSION),
                AppRoute.DocumentAt(section).explanationRoute())
        }
        assertEquals(AppRoute.Overview, AppRoute.Document.explanationRoute())
        assertEquals(AppRoute.Tutor, AppRoute.Tutor.explanationRoute())
    }
}
