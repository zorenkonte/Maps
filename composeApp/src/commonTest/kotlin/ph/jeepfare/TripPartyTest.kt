package ph.jeepfare

import kotlin.test.Test
import kotlin.test.assertEquals
import ph.jeepfare.domain.FareCalculator
import ph.jeepfare.domain.JeepneyType
import ph.jeepfare.domain.PassengerType
import ph.jeepfare.domain.TripParty

class TripPartyTest {

    @Test
    fun commuterAloneCountsAsOneRider() {
        val party = TripParty(myFareType = PassengerType.STUDENT)
        assertEquals(mapOf(PassengerType.STUDENT to 1), party.counts())
        assertEquals(0, party.companionCount)
        assertEquals(1, party.riderCount)
    }

    @Test
    fun companionsOfTheSameTypeStackOntoTheCommuter() {
        val party = TripParty(
            myFareType = PassengerType.REGULAR,
            companions = mapOf(PassengerType.REGULAR to 2),
        )
        assertEquals(mapOf(PassengerType.REGULAR to 3), party.counts())
        assertEquals(3, party.riderCount)
    }

    @Test
    fun zeroAndNegativeCompanionsAreIgnored() {
        val party = TripParty(
            myFareType = PassengerType.SENIOR,
            companions = mapOf(
                PassengerType.REGULAR to 0,
                PassengerType.STUDENT to -3,
                PassengerType.PWD to 1,
            ),
        )
        assertEquals(mapOf(PassengerType.SENIOR to 1, PassengerType.PWD to 1), party.counts())
        assertEquals(1, party.companionCount)
        assertEquals(0, party.companionsOf(PassengerType.STUDENT))
    }

    @Test
    fun withCompanionsClampsBelowZero() {
        val party = TripParty().withCompanions(PassengerType.STUDENT, -1)
        assertEquals(0, party.companionsOf(PassengerType.STUDENT))
        assertEquals(2, party.withCompanions(PassengerType.STUDENT, 2).companionsOf(PassengerType.STUDENT))
    }

    @Test
    fun discountedCommuterPaysTheDiscountedFare() {
        val alone = FareCalculator.calculate(
            4.0, JeepneyType.TRADITIONAL, TripParty(myFareType = PassengerType.STUDENT).counts(),
        )
        // P14.00 base - 20% = P11.20, rounded to the nearest P0.25.
        assertEquals(11.25, alone.total)

        val withParent = FareCalculator.calculate(
            4.0,
            JeepneyType.TRADITIONAL,
            TripParty(
                myFareType = PassengerType.STUDENT,
                companions = mapOf(PassengerType.REGULAR to 1),
            ).counts(),
        )
        assertEquals(11.25 + 14.00, withParent.total)
    }
}
