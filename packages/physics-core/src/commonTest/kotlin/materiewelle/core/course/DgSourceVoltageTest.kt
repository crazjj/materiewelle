package materiewelle.core.course

import materiewelle.core.Physics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DgSourceVoltageTest {
    @Test fun sourceRestoreValidationPreservesPhysicalValuesOutsideTheFreeEditingRange() {
        val source = DgSession(modelVoltageRaw = "0.5", validModelVoltage = 0.5,
            comparisonVoltage = 50_000.0, comparisonAngle = 50.0).validate()
        val restored = SessionSnapshot.fromJson(SessionSnapshot(dg = source).json()).dg
        assertEquals(0.5, restored.validModelVoltage)
        assertEquals(50_000.0, restored.comparisonVoltage)
        // The physical session domain must not silently broaden the free-input UI contract.
        assertNull(Physics.inputVoltage("0.5"))
        assertNull(Physics.inputVoltage("50000"))
        assertEquals(20.0, Physics.inputVoltage("20"))
        assertEquals(1000.0, Physics.inputVoltage("1000"))
    }

    @Test fun sourceDomainStillRejectsNonphysicalOrNonfiniteValues() {
        listOf(0.0, -0.5, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            assertFailsWith<IllegalArgumentException> { DgSession(validModelVoltage = value).validate() }
            assertFailsWith<IllegalArgumentException> { DgSession(comparisonVoltage = value, comparisonAngle = 50.0).validate() }
        }
    }
}
