package materiewelle.core

import kotlin.test.*

class StoryboardTest {
    private val brief = videoBrief(Voltage(150.0))

    @Test fun boundariesFollowActualSceneDurationsAndClampEndpoints() {
        val timeline = StoryboardTimeline(brief)
        assertEquals(20, timeline.durationSeconds)
        assertEquals(0, timeline.at(-1.0).sceneIndex)
        assertEquals(0, timeline.at(5.999).sceneIndex)
        assertEquals(1, timeline.at(6.0).sceneIndex)
        assertEquals(2, timeline.at(13.0).sceneIndex)
        assertEquals(2, timeline.at(25.0).sceneIndex)
        assertEquals(1.0, timeline.at(25.0).sceneProgress)
        assertEquals(.5, timeline.at(9.5).sceneProgress)
        assertEquals(13, timeline.sceneStart(2))
        assertFailsWith<IllegalArgumentException> { timeline.at(Double.NaN) }
        assertFailsWith<IllegalArgumentException> { timeline.at(Double.POSITIVE_INFINITY) }
        assertFailsWith<IllegalArgumentException> { timeline.sceneStart(3) }
    }

    @Test fun invalidDurationsAndUnboundedOrEmptyNarrationAreRejected() {
        assertFailsWith<IllegalArgumentException> { StoryboardTimeline(brief.copy(scenes = emptyList())) }
        for (seconds in listOf(0, -1, 61)) {
            assertFailsWith<IllegalArgumentException> { StoryboardTimeline(brief.copy(scenes = listOf(VideoScene(seconds, "Text", "Bild")))) }
        }
        assertFailsWith<IllegalArgumentException> { StoryboardTimeline(brief.copy(schemaVersion = 2)) }
        assertFailsWith<IllegalArgumentException> { StoryboardTimeline(brief.copy(scenes = List(11) { VideoScene(60, "Text", "Bild") })) }
        assertFailsWith<IllegalArgumentException> { StoryboardTimeline(brief.copy(scenes = listOf(VideoScene(6, " ", "Bild")))) }
    }

    @Test fun subtitlesHaveContiguousCuesAndEscapeMarkupAsLiteralText() {
        val vtt = StoryboardTimeline(brief).webVtt()
        assertTrue(vtt.startsWith("WEBVTT\n\n"))
        assertTrue(vtt.contains("00:00:00.000 --> 00:00:06.000"))
        assertTrue(vtt.contains("00:00:06.000 --> 00:00:13.000"))
        assertTrue(vtt.contains("00:00:13.000 --> 00:00:20.000"))
        val literal = StoryboardTimeline(brief.copy(scenes = listOf(VideoScene(6, "<b>x</b> & y\n\nnext", "Bild")))).webVtt()
        assertTrue(literal.contains("&lt;b&gt;x&lt;/b&gt; &amp; y next"))
        assertFalse(literal.contains("<b>"))
        assertTrue(StoryboardTimeline(brief).transcript().contains("Szene 3 · 00:00:13.000"))
    }

    @Test fun portableQuizRequestStatesUnitsAndRetainsIndependentlyCheckableAnswer() {
        val prompt = quizAuthoringPrompt(Voltage(150.0))
        assertTrue(prompt.contains("Pikometern"))
        assertTrue(prompt.contains("fachliche Prüfung des Wortlauts"))
        val json = prompt.substring(prompt.indexOf('{'))
        val example = kotlinx.serialization.json.Json.decodeFromString<QuizDraft>(json).verifyNumerics()
        assertEquals(600.0, example.targetVoltageV)
        assertEquals(50.068630372378, example.claimedAnswerPm, 1e-10)
        assertFailsWith<IllegalArgumentException> { quizAuthoringPrompt(Voltage(251.0)) }
    }
}
