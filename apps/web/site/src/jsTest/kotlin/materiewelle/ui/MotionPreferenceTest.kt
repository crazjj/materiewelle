package materiewelle.ui

import kotlin.test.*

class MotionPreferenceTest {
    @Test fun defaultsAndLegacyValuesAnimate() {
        listOf(null, "system", "unknown", "ANIMATED").forEach { assertEquals(MotionPreference.ANIMATED, MotionPreference.fromStorage(it)) }
        assertEquals(MotionPreference.STILL, MotionPreference.fromStorage("still"))
    }
    @Test fun toggleRoundTrips() {
        MotionPreference.entries.forEach { preference ->
            assertNotEquals(preference, preference.toggled())
            assertEquals(preference, preference.toggled().toggled())
            assertEquals(preference, MotionPreference.fromStorage(preference.key))
        }
    }
    @Test fun reducedMotionAlwaysWins() {
        MotionPreference.entries.forEach { preference ->
            listOf(false, true).forEach { visible -> assertEquals(MotionState.REDUCED, resolveMotion(preference, true, visible)) }
        }
    }
    @Test fun userPauseAndTabVisibilityAreIndependent() {
        assertEquals(MotionState.RUNNING, resolveMotion(MotionPreference.ANIMATED, false, true))
        assertEquals(MotionState.HIDDEN, resolveMotion(MotionPreference.ANIMATED, false, false))
        assertEquals(MotionState.PAUSED, resolveMotion(MotionPreference.STILL, false, true))
        assertEquals(MotionState.PAUSED, resolveMotion(MotionPreference.STILL, false, false))
    }
}
