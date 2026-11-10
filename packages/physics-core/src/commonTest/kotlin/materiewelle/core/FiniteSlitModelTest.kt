package materiewelle.core

import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FiniteSlitModelTest {
    private val model = FiniteSlitModel(Voltage(50_000.0), Length(0.35), Length(2e-6), Length(0.5e-6))

    @Test fun centralMaximumSymmetryAndRelativisticScale() {
        assertEquals(1.0, model.relativeIntensity(0.0))
        assertEquals(0.937178717444, model.paraxialFringeSpacing.micrometers, 1e-11)
        val nonrelativistic = model.copy(approximation = WavelengthApproximation.NONRELATIVISTIC)
        assertEquals(5.484743655668, nonrelativistic.wavelength.picometers, 1e-11)
        assertTrue(nonrelativistic.paraxialFringeSpacing.meters > model.paraxialFringeSpacing.meters)
        assertEquals(model.relativeIntensity(0.7e-6), model.relativeIntensity(-0.7e-6), 1e-14)
        (-100..100).forEach { assertTrue(model.relativeIntensity(it * 1e-7) in 0.0..1.0) }
    }

    @Test fun finiteWidthProducesAnEnvelopeAndMissingFourthOrder() {
        fun positionAtSin(s: Double) = model.distance.meters * s / sqrt(1.0 - s * s)
        val firstOrder = positionAtSin(model.wavelength.meters / model.slitSpacing.meters)
        assertEquals(8.0 / (PI * PI), model.relativeIntensity(firstOrder), 1e-13)
        val firstEnvelopeZero = positionAtSin(model.wavelength.meters / model.slitWidth.meters)
        assertTrue(model.relativeEnvelope(firstEnvelopeZero) < 1e-26)
        assertTrue(model.relativeIntensity(firstEnvelopeZero) < 1e-26)
        val interferenceMinimum = positionAtSin(model.wavelength.meters / (2.0 * model.slitSpacing.meters))
        assertTrue(model.relativeIntensity(interferenceMinimum) < 1e-26)
    }

    @Test fun invalidGeometryAndNonfiniteCoordinatesFail() {
        assertFailsWith<IllegalArgumentException> { model.copy(distance = Length(0.0)) }
        assertFailsWith<IllegalArgumentException> { model.copy(slitWidth = Length(3e-6)) }
        assertFailsWith<IllegalArgumentException> { model.relativeIntensity(Double.NaN) }
    }

    @Test fun farFieldAssumptionUsesTheEntireApertureAndIsSeparateFromSmallAngles() {
        // Historical nominal numbers do not establish free Fraunhofer propagation over 350 mm.
        assertEquals(2.5e-6, model.apertureExtent.meters, 1e-20)
        assertEquals(3.334476062925231, model.fullApertureFresnelNumber, 1e-12)
        assertTrue(model.fullApertureFresnelNumber > 1.0)
        // Synthetic teaching case: even the largest offered g+b has a small full-width number.
        val synthetic = FiniteSlitModel(Voltage(150.0), Length(10.0), Length(5e-6), Length(0.9e-6))
        assertTrue(synthetic.fullApertureFresnelNumber < 0.035)
        assertEquals(0.03476483598862957, synthetic.fullApertureFresnelNumber, 1e-14)
    }
}
