package materiewelle.core.course

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ReadingPositionTest {
    @Test fun everyPersistedWorkIdentityBelongsToOneSection() {
        StationId.entries.forEach { station ->
            val saved = ReadingPosition(section = AppSection.forStation(station), station = station)
            assertEquals(saved, ReadingPosition.parse(saved.json()))
            assertEquals(AppRoute.Course(station), saved.route)
        }
    }

    @Test fun bookmarkRejectsMismatchedSectionAndUnknownSchema() {
        assertFailsWith<IllegalArgumentException> {
            ReadingPosition(section = AppSection.DE_BROGLIE, station = StationId.S14_MEASUREMENT).validate()
        }
        assertFailsWith<IllegalArgumentException> {
            ReadingPosition(schemaVersion = 2, section = AppSection.JONSSON).validate()
        }
        assertFailsWith<IllegalArgumentException> { ReadingPosition.parse("{\"section\":\"UNKNOWN\"}") }
    }

    @Test fun navigationWithoutReadingLocationDoesNotReplaceBookmark() {
        assertNull(ReadingPosition.fromRoute(AppRoute.Tutor))
        assertNull(ReadingPosition.fromRoute(AppRoute.Overview))
        val position = ReadingPosition.fromRoute(AppRoute.Course(StationId.S14_MEASUREMENT))!!
        assertEquals(AppSection.JONSSON, position.section)
        assertEquals(StationId.S14_MEASUREMENT, position.station)
        assertEquals(position, ReadingPosition.fromRoute(AppRoute.parse("#joensson/messung")!!))
    }

    @Test fun publicChapterAnchorsAndOldWorkLinksResolveToSameWork() {
        ChapterAnchor.entries.forEach { anchor ->
            assertEquals(AppRoute.Course(anchor.station), AppRoute.parse("#${anchor.path}"))
            assertEquals(AppRoute.parse("#lernen/${anchor.station.key}"), AppRoute.parse("#${anchor.path}"))
        }
        assertEquals(AppRoute.DocumentAt(DocumentSectionId.SOURCES), AppRoute.parse("#quellen"))
        assertNull(AppRoute.parse("#davisson-germer/unbekannt"))
    }
}
