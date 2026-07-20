package ph.jeepfare.domain

/** Kind of public utility jeepney (PUJ). */
enum class JeepneyType {
    TRADITIONAL,
    MODERN,
}

/** Passenger category recognized by the LTFRB fare matrix. */
enum class PassengerType(val discounted: Boolean) {
    REGULAR(false),
    STUDENT(true),
    SENIOR(true),
    PWD(true),
}

/** Fare rate for one jeepney type: a flat base fare covering the first
 * [baseKm] kilometers, then [perKm] for every succeeding kilometer. */
data class FareRate(
    val baseFare: Double,
    val baseKm: Int,
    val perKm: Double,
)

/**
 * LTFRB fare matrix effective March 19, 2026 (fare hike approved March 17, 2026).
 *
 * Traditional PUJ: P14.00 base (first 4 km) + P2.00 per succeeding km.
 * Modern PUJ:      P17.00 base (first 4 km) + P2.40 per succeeding km.
 * Students, senior citizens, and PWDs get a 20% discount on the full fare.
 */
object FareRules {
    const val EFFECTIVE_DATE = "2026-03-19"

    const val DISCOUNT_RATE = 0.20

    /** Fares are rounded to the nearest P0.25, following fare-matrix convention. */
    const val ROUNDING_STEP = 0.25

    /** Distance beyond the base kilometers is charged per started kilometer. */
    val rates: Map<JeepneyType, FareRate> = mapOf(
        JeepneyType.TRADITIONAL to FareRate(baseFare = 14.00, baseKm = 4, perKm = 2.00),
        JeepneyType.MODERN to FareRate(baseFare = 17.00, baseKm = 4, perKm = 2.40),
    )

    fun rateFor(type: JeepneyType): FareRate = rates.getValue(type)
}
