package materiewelle.ui.semantics

import materiewelle.core.Experiment
import materiewelle.core.ExperimentMode
import materiewelle.data.LabAsset
import kotlin.test.*

class DomContractTest {
    @Test fun identitiesRemainUniqueAcrossFixedAndGeneratedControls() {
        val identities: List<ElementId> = Id.entries + materiewelle.core.course.LearningPhase.entries.map { ElementId.LearningPhaseButton(it) } + LabDestination.entries.flatMap {
            listOf(ElementId.Navigation(it), ElementId.Panel(it))
        } + Experiment.entries.flatMap { experiment ->
            ExperimentMode.entries.map { ElementId.Mode(experiment, it) }
        } + (0..1).map { ElementId.CalibrationChoice(UiIndex(it)) } +
            (0..2).flatMap { listOf(ElementId.TutorPrompt(UiIndex(it)), ElementId.MediaScene(UiIndex(it))) }
        val values = identities.map { it.value }
        assertEquals(values.size, values.toSet().size, "IDs must not collide between hidden panels")
        assertTrue(values.all { Regex("[a-z][a-z0-9-]*").matches(it) })
    }

    @Test fun navigationPreservesPublishedFragmentsAndHistoricalAliases() {
        LabDestination.entries.forEach { assertEquals(it, LabDestination.fromHash(it.fragment)) }
        assertEquals(LabDestination.DAVISSON_GERMER, LabDestination.fromHash("#dg"))
        assertEquals(LabDestination.JONSSON, LabDestination.fromHash("#jonsson"))
        assertNull(LabDestination.fromHash("#voltage"))
        assertNull(LabDestination.fromHash("#unknown"))
        assertEquals("#werkstatt", LabDestination.WORKSHOP.fragment)
    }

    @Test fun modeIdentityRetainsShortPrefixesAndSvgReferencesResolveFromIdentity() {
        assertEquals("dg-mode-lab", ElementId.Mode(Experiment.DAVISSON_GERMER, ExperimentMode.LAB).value)
        assertEquals("jonsson-mode-guided", ElementId.Mode(Experiment.JONSSON, ExperimentMode.GUIDED).value)
        assertEquals("url(#dg-incoming-arrow)", Id.DG_INCOMING_ARROW.svgReference)
        assertEquals("#voltage", Id.VOLTAGE.fragment)
    }

    @Test fun dynamicIndicesRejectInvalidOrImpossibleChoices() {
        assertFailsWith<IllegalArgumentException> { UiIndex(-1) }
        assertFailsWith<IllegalArgumentException> { ElementId.CalibrationChoice(UiIndex(2)) }
        assertEquals("media-scene-2", ElementId.MediaScene(UiIndex(2)).value)
        assertNotEquals<ElementId>(ElementId.MediaScene(UiIndex(1)), ElementId.TutorPrompt(UiIndex(1)))
    }

    @Test fun unknownBrowserKeysAndSchematicPartsRemainUnknown() {
        LabKey.entries.forEach { assertEquals(it, LabKey.fromBrowser(it.browserValue)) }
        assertNull(LabKey.fromBrowser("Left"))
        assertNull(LabKey.fromBrowser("a"))
        SetupPart.entries.forEach { assertEquals(it, SetupPart.fromAttribute(it.key)) }
        assertNull(SetupPart.fromAttribute("source,screen"))
        assertNull(SetupPart.fromAttribute(null))
    }

    @Test fun metadataEncodingPreservesNativeFractionsAndRejectsNonFiniteValues() {
        assertEquals("551.25", LabData.NATIVE_X.encode(551.25))
        assertEquals("13.714285714285714", LabData.SPACING_PX.encode(96.0 / 7))
        assertEquals("true", LabData.CORRECT.encode(true))
        assertEquals("failed", LabData.LOAD_STATE.encode(DataLoadState.FAILED))
        assertFailsWith<IllegalArgumentException> { LabData.VALUE_PM.encode(Double.NaN) }
        assertFailsWith<IllegalArgumentException> { LabData.VALUE_NM.encode(Double.POSITIVE_INFINITY) }
    }

    @Test fun assetUrlsStayRelativeAndTutorManifestNamesStayStable() {
        assertTrue(LabAsset.entries.all { !it.path.startsWith('/') && ".." !in it.path && ':' !in it.path })
        assertEquals("manifest.json", LabAsset.TUTOR_MANIFEST.fileName)
        assertEquals("START-IM-CHAT.md", LabAsset.TUTOR_START.fileName)
        assertEquals("materiewellen-tutor.zip", LabAsset.TUTOR_SKILL.fileName)
    }
}
