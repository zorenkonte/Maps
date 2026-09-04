package ph.jeepfare.domain

/**
 * Who is paying, from the commuter's point of view: the rider using the app
 * ([myFareType]) plus any [companions] whose fare they are covering.
 *
 * The commuter is always exactly one rider and always present — this is a
 * passenger's app, not a conductor's tally sheet — so a fare can always be
 * computed as soon as a distance is known.
 */
data class TripParty(
    val myFareType: PassengerType = PassengerType.REGULAR,
    val companions: Map<PassengerType, Int> = emptyMap(),
) {
    /** Companions being paid for, ignoring zero and negative entries. */
    val companionCount: Int get() = companions.values.sumOf { it.coerceAtLeast(0) }

    /** The commuter plus their companions. */
    val riderCount: Int get() = 1 + companionCount

    /** Head count per fare type, in the shape [FareCalculator.calculate] expects. */
    fun counts(): Map<PassengerType, Int> {
        val counts = mutableMapOf(myFareType to 1)
        companions.forEach { (type, count) ->
            if (count > 0) counts[type] = (counts[type] ?: 0) + count
        }
        return counts
    }

    /** Companions of [type] only, never negative. */
    fun companionsOf(type: PassengerType): Int = (companions[type] ?: 0).coerceAtLeast(0)

    fun withCompanions(type: PassengerType, count: Int): TripParty =
        copy(companions = companions + (type to count.coerceAtLeast(0)))
}
