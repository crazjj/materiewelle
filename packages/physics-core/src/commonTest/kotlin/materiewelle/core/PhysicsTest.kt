package materiewelle.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhysicsTest {
    @Test fun independentElectronReferenceValues() {
        // Independent Decimal reference calculation, CODATA 2022.
        listOf(20.0 to 274.2371827833974, 150.0 to 100.1372607447561,
            600.0 to 50.06863037237805, 1000.0 to 38.78299431992701).forEach { (volts, pm) ->
            assertEquals(pm, Physics.electronWavelength(Voltage(volts)).picometers, 1e-10)
        }
    }

    @Test fun relativisticAndFringeModelAreIndependentOfImageScale() {
        val wavelength = Physics.relativisticElectronWavelength(Voltage(50_000.0))
        assertEquals(5.355306956823, wavelength.picometers, 1e-11)
        assertEquals(0.937178717444, Physics.fringeSpacing(Length(0.35), wavelength, Length(2e-6)).micrometers, 1e-11)
    }

    @Test fun independentNickelStructureAndSelectedSecondaryComparison() {
        val lattice = Length(3.52e-10)
        val spacing = Physics.nickel111RowSpacing(lattice)
        assertEquals(0.21555509736491968, spacing.nanometers, 1e-15)
        assertEquals(0.24890158697766472, Physics.nickel111NearestNeighbor(lattice).nanometers, 1e-15)
        assertEquals(0.20322729475474832, Physics.nickel111PlaneSpacing(lattice).nanometers, 1e-15)
        val geometry = Physics.surfaceDiffraction(spacing, SurfaceNormalAngle(50.0))
        val prediction = Physics.electronWavelength(Voltage(54.0))
        assertEquals(0.16512478452236695, geometry.nanometers, 1e-14)
        assertEquals(0.166895434575, prediction.nanometers, 1e-12)
        assertEquals(-1.060933786, Physics.signedRelativeDifferencePercent(geometry.meters, prediction.meters), 1e-8)
        // Independent Decimal SI oracle + asin: this inverse uses the NR prediction.
        // The nearby 50.736-degree value belongs to the relativistic prediction at 54 V.
        assertEquals(50.73788256785927, Physics.surfaceMaximumAngle(spacing, prediction)!!.degrees, 1e-10)
    }

    @Test fun braggUsesTwoExtraPathsAndItsOwnAngleReference() {
        val paths = Physics.braggPathDifference(Length(2e-10), BraggPlaneAngle(30.0))
        assertEquals(1e-10, paths.incomingExtra.meters, 1e-24)
        assertEquals(1e-10, paths.outgoingExtra.meters, 1e-24)
        assertEquals(2e-10, paths.total.meters, 1e-24)
        assertFailsWith<IllegalArgumentException> { BraggPlaneAngle(90.01) }
        assertFailsWith<IllegalArgumentException> { SurfaceNormalAngle(-1.0) }
    }

    @Test fun inaccessibleModelDirectionIsNotReportedAsAnObservedMaximum() {
        val spacing = Physics.nickel111RowSpacing(Length(3.52e-10))
        assertNull(Physics.surfaceMaximumAngle(spacing, Physics.electronWavelength(Voltage(32.0))))
        assertEquals(0.0, Physics.surfaceDiffraction(spacing, SurfaceNormalAngle(0.0)).meters)
        assertEquals(90.0, Physics.surfaceMaximumAngle(spacing, spacing)!!.degrees)
        assertFailsWith<IllegalArgumentException> { Physics.surfaceDiffraction(spacing, Angle(90.01)) }
    }

    @Test fun editingDoesNotPromoteInvalidTextToAcceptedInput() {
        val input = VoltageInput.initial().edit("600").edit("unvollständig")
        assertEquals("unvollständig", input.raw)
        assertNull(input.valid)
        assertEquals(600.0, input.lastValid.volts)
        assertEquals(150.5, input.edit(" 150,5 ").valid?.volts)
    }

    @Test fun unitsRejectNonfiniteAndWrongSign() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -1.0).forEach { value ->
            assertFailsWith<IllegalArgumentException> { Voltage(value) }
            assertFailsWith<IllegalArgumentException> { Length(value) }
            assertFailsWith<IllegalArgumentException> { NativePixel(value) }
        }
        assertFailsWith<IllegalArgumentException> { Voltage(0.0) }
        assertFailsWith<IllegalArgumentException> { Physics.fringeSpacing(Length(1.0), Length(1e-10), Length(0.0)) }
    }

    @Test fun pixelEndpointMeanAndInternalMovement() {
        val marks = listOf(551.0, 564.0, 578.0, 592.0, 606.0, 620.0, 634.0, 647.0)
        assertEquals(96.0 / 7.0, PixelMeasurement.nativeMean(marks.map(::NativePixel)).value, 1e-14)
        assertEquals(PixelMeasurement.mean(marks), PixelMeasurement.mean(PixelMeasurement.move(marks, 3, 1.0, 490.0, 703.0)))
        assertEquals(97.0 / 7.0, PixelMeasurement.mean(PixelMeasurement.move(marks, 7, 1.0, 490.0, 703.0)), 1e-14)
        assertTrue(!PixelMeasurement.canMove(marks, 1, -13.0, 490.0, 703.0))
        assertTrue(!PixelMeasurement.canMove(marks, 7, 56.0, 490.0, 703.0))
    }

    @Test fun malformedMarkersFailRatherThanProduceFalseMeasurements() {
        listOf(emptyList(), listOf(2.0), listOf(3.0, 2.0), listOf(2.0, 2.0), listOf(-1.0, 2.0), listOf(Double.NaN, 2.0)).forEach {
            assertFailsWith<IllegalArgumentException> { PixelMeasurement.mean(it) }
        }
    }

    @Test fun finiteExtremeInputsAvoidIntermediateUnderflow() {
        assertTrue(Physics.wavelengthMeters(Double.MIN_VALUE).isFinite())
        assertTrue(Physics.wavelengthMeters(Double.MAX_VALUE) > 0.0)
    }

    @Test fun formattingAndRoutesArePortable() {
        assertEquals("100,14", Physics.displayPicometers(150.0))
        assertEquals("−1,03", Physics.decimal(-1.025749877, 2))
        assertTrue(Physics.decimal(1e100, 2).contains("e"))
        assertFailsWith<IllegalArgumentException> { Physics.relativeDifferencePercent(1e308, 1e-300) }
        assertEquals(Experiment.DAVISSON_GERMER, Experiment.fromRoute("#dg"))
        assertEquals(Experiment.JONSSON, Experiment.fromRoute("#joensson"))
        assertEquals(Experiment.JONSSON, Experiment.fromRoute("#jonsson"))
        assertNull(Experiment.fromRoute("unknown"))
    }
}
