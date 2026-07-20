package ph.jeepfare

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import ph.jeepfare.domain.FareCalculator
import ph.jeepfare.domain.JeepneyType
import ph.jeepfare.domain.PassengerType
import ph.jeepfare.domain.toPesoString

class FareCalculatorTest {

    private fun calc(
        km: Double,
        type: JeepneyType = JeepneyType.TRADITIONAL,
        passengers: Map<PassengerType, Int> = mapOf(PassengerType.REGULAR to 1),
    ) = FareCalculator.calculate(km, type, passengers)

    @Test
    fun baseFareWithinFirstFourKm() {
        assertEquals(14.00, calc(0.0).total)
        assertEquals(14.00, calc(1.5).total)
        assertEquals(14.00, calc(4.0).total)
        assertEquals(0, calc(4.0).extraKm)
    }

    @Test
    fun startedKilometerIsChargedInFull() {
        val breakdown = calc(4.1)
        assertEquals(1, breakdown.extraKm)
        assertEquals(16.00, breakdown.total)
    }

    @Test
    fun traditionalSevenKmTrip() {
        val breakdown = calc(7.0)
        assertEquals(3, breakdown.extraKm)
        assertEquals(6.00, breakdown.extraCharge)
        assertEquals(20.00, breakdown.total)
    }

    @Test
    fun modernJeepneyRates() {
        assertEquals(17.00, calc(4.0, JeepneyType.MODERN).total)
        // 17.00 + 3 km x 2.40 = 24.20, rounded to the nearest 0.25 -> 24.25
        assertEquals(24.25, calc(7.0, JeepneyType.MODERN).total)
    }

    @Test
    fun discountAppliesToStudentsSeniorsAndPwd() {
        for (type in listOf(PassengerType.STUDENT, PassengerType.SENIOR, PassengerType.PWD)) {
            val breakdown = calc(4.0, passengers = mapOf(type to 1))
            // 14.00 x 0.80 = 11.20, rounded to the nearest 0.25 -> 11.25
            assertEquals(11.25, breakdown.total, "for $type")
        }
        // Regular passengers get no discount.
        assertEquals(14.00, calc(4.0, passengers = mapOf(PassengerType.REGULAR to 1)).total)
    }

    @Test
    fun mixedGroupTotalsPerPassengerType() {
        val breakdown = calc(
            7.0,
            passengers = mapOf(
                PassengerType.REGULAR to 2,
                PassengerType.STUDENT to 1,
            ),
        )
        // Regular: 20.00 each; student: 20.00 x 0.80 = 16.00.
        assertEquals(2 + 1, breakdown.totalPassengers)
        assertEquals(20.00 * 2 + 16.00, breakdown.total)
        val studentLine = breakdown.passengers.first { it.type == PassengerType.STUDENT }
        assertEquals(4.00, studentLine.discountPerHead)
    }

    @Test
    fun zeroOrNegativeCountsAreIgnored() {
        val breakdown = calc(
            4.0,
            passengers = mapOf(
                PassengerType.REGULAR to 0,
                PassengerType.STUDENT to -3,
                PassengerType.SENIOR to 1,
            ),
        )
        assertEquals(1, breakdown.totalPassengers)
        assertEquals(listOf(PassengerType.SENIOR), breakdown.passengers.map { it.type })
    }

    @Test
    fun emptyPassengersYieldZeroTotal() {
        val breakdown = calc(7.0, passengers = emptyMap())
        assertEquals(0.0, breakdown.total)
        assertTrue(breakdown.passengers.isEmpty())
        // The per-head fares are still computed for display.
        assertEquals(20.00, breakdown.regularFare)
    }

    @Test
    fun rejectsInvalidDistance() {
        assertFailsWith<IllegalArgumentException> { calc(-1.0) }
        assertFailsWith<IllegalArgumentException> { calc(Double.NaN) }
        assertFailsWith<IllegalArgumentException> { calc(Double.POSITIVE_INFINITY) }
        // Absurd distances would saturate the extra-km integer conversion.
        assertFailsWith<IllegalArgumentException> { calc(FareCalculator.MAX_SUPPORTED_KM + 1) }
    }

    @Test
    fun longTripDoesNotDriftFromExpectedValue() {
        // 50 km traditional: 14 + 46 x 2.00 = 106.00
        assertEquals(106.00, calc(50.0).total)
        // 50 km modern: 17 + 46 x 2.40 = 127.40 -> rounded 127.50
        assertEquals(127.50, calc(50.0, JeepneyType.MODERN).total)
    }

    @Test
    fun roundingToQuarterStep() {
        assertEquals(19.50, FareCalculator.roundToStep(19.40))
        assertEquals(19.25, FareCalculator.roundToStep(19.37))
        assertEquals(11.25, FareCalculator.roundToStep(11.20))
        assertEquals(14.00, FareCalculator.roundToStep(14.00))
    }

    @Test
    fun pesoFormatting() {
        assertEquals("14.00", 14.0.toPesoString())
        assertEquals("11.25", 11.25.toPesoString())
        assertEquals("127.50", 127.5.toPesoString())
        assertEquals("0.00", 0.0.toPesoString())
    }
}
