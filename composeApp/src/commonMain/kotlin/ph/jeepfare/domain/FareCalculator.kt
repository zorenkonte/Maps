package ph.jeepfare.domain

import kotlin.math.ceil
import kotlin.math.roundToLong

/** Fare for one passenger category within a trip. */
data class PassengerFare(
    val type: PassengerType,
    val count: Int,
    val farePerHead: Double,
    val discountPerHead: Double,
) {
    val subtotal: Double get() = farePerHead * count
}

/** Full fare computation result, itemized so the math can be shown to the rider. */
data class FareBreakdown(
    val jeepneyType: JeepneyType,
    val distanceKm: Double,
    val rate: FareRate,
    /** Started kilometers charged beyond the base distance. */
    val extraKm: Int,
    /** Charge for the extra kilometers before rounding. */
    val extraCharge: Double,
    /** Undiscounted fare for one passenger, rounded to the fare step. */
    val regularFare: Double,
    /** Discounted fare for one passenger, rounded to the fare step. */
    val discountedFare: Double,
    val passengers: List<PassengerFare>,
) {
    val totalPassengers: Int get() = passengers.sumOf { it.count }
    val total: Double get() = passengers.sumOf { it.subtotal }
}

object FareCalculator {

    /**
     * Computes the itemized fare for a trip.
     *
     * @param distanceKm trip distance in kilometers; must be finite and >= 0.
     * @param jeepneyType traditional or modern PUJ.
     * @param passengers passenger count per category; negative counts are treated as 0.
     */
    fun calculate(
        distanceKm: Double,
        jeepneyType: JeepneyType,
        passengers: Map<PassengerType, Int>,
    ): FareBreakdown {
        require(distanceKm.isFinite() && distanceKm >= 0.0) {
            "distanceKm must be a non-negative number, got $distanceKm"
        }
        val rate = FareRules.rateFor(jeepneyType)

        // Distance beyond the base kilometers is charged per started km.
        val beyondBase = distanceKm - rate.baseKm
        val extraKm = if (beyondBase > 0) ceil(beyondBase).toInt() else 0
        val extraCharge = extraKm * rate.perKm

        val rawFare = rate.baseFare + extraCharge
        val regularFare = roundToStep(rawFare)
        val discountedFare = roundToStep(rawFare * (1 - FareRules.DISCOUNT_RATE))

        val lines = PassengerType.entries.mapNotNull { type ->
            val count = passengers[type] ?: 0
            if (count <= 0) return@mapNotNull null
            val fare = if (type.discounted) discountedFare else regularFare
            PassengerFare(
                type = type,
                count = count,
                farePerHead = fare,
                discountPerHead = if (type.discounted) regularFare - discountedFare else 0.0,
            )
        }

        return FareBreakdown(
            jeepneyType = jeepneyType,
            distanceKm = distanceKm,
            rate = rate,
            extraKm = extraKm,
            extraCharge = extraCharge,
            regularFare = regularFare,
            discountedFare = discountedFare,
            passengers = lines,
        )
    }

    /** Rounds to the nearest [FareRules.ROUNDING_STEP], computed in centavos to avoid float drift. */
    fun roundToStep(value: Double): Double {
        val stepCentavos = (FareRules.ROUNDING_STEP * 100).roundToLong()
        val centavos = (value * 100).roundToLong()
        val rounded = ((centavos + stepCentavos / 2) / stepCentavos) * stepCentavos
        return rounded / 100.0
    }
}

/** Formats a peso amount as e.g. "14.00" (caller prepends the currency sign). */
fun Double.toPesoString(): String {
    val centavos = (this * 100).roundToLong()
    val whole = centavos / 100
    val frac = (centavos % 100).toInt()
    val fracStr = if (frac < 10) "0$frac" else "$frac"
    return "$whole.$fracStr"
}
