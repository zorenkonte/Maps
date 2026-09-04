package ph.jeepfare.ui

import ph.jeepfare.domain.FareBreakdown
import ph.jeepfare.domain.PassengerType
import ph.jeepfare.domain.TripParty
import ph.jeepfare.domain.toPesoString
import ph.jeepfare.ui.components.ResiboRow

const val PESO = "₱"

fun Double.peso(): String = PESO + toPesoString()

/** What one rider of [type] pays on this trip. */
fun FareBreakdown.fareFor(type: PassengerType): Double =
    if (type.discounted) discountedFare else regularFare

/** "TOTAL" once companions are involved; a lone commuter just sees their own fare. */
fun totalLabelFor(party: TripParty): String =
    if (party.companionCount > 0) Strings.RESIBO_TOTAL else Strings.RESIBO_YOUR_FARE

/**
 * Receipt lines for a fare breakdown + the index the pre-rider divider goes before.
 *
 * The commuter's own line comes first and is labelled "You", so the fare they
 * hand over is the one they read first; companions follow as separate lines.
 */
fun resiboRows(b: FareBreakdown, party: TripParty): Pair<List<ResiboRow>, Int?> {
    val rows = mutableListOf(
        ResiboRow(Strings.BASE_FARE_LABEL.replace("%d", b.rate.baseKm.toString()), b.rate.baseFare.peso()),
    )
    if (b.extraKm > 0) {
        rows += ResiboRow(
            Strings.EXTRA_LABEL.replace("%d", b.extraKm.toString()).replace("%s", b.rate.perKm.peso()),
            b.extraCharge.peso(),
        )
    }
    // The undiscounted per-rider fare is only worth a line when something is
    // measured against it — a discount, or more than one rider.
    if (party.riderCount > 1 || party.myFareType.discounted) {
        // Not `strong`: the emphasized line on a commuter's receipt is their own.
        rows += ResiboRow(Strings.FULL_FARE_LABEL, b.regularFare.peso())
    }

    val dividerAt = rows.size
    rows += ResiboRow(
        Strings.YOU_LABEL.replace("%s", Strings.passengerTypeLabel(party.myFareType)) +
            (if (party.myFareType.discounted) Strings.DISCOUNT_SUFFIX else ""),
        b.fareFor(party.myFareType).peso(),
        strong = true,
    )
    PassengerType.entries.forEach { type ->
        val count = party.companionsOf(type)
        if (count <= 0) return@forEach
        val disc = if (type.discounted) Strings.DISCOUNT_SUFFIX else ""
        rows += ResiboRow(
            "${Strings.passengerTypeLabel(type)} × $count$disc",
            (b.fareFor(type) * count).peso(),
        )
    }
    return rows to dividerAt
}

/**
 * File-safe name for the saved or shared receipt image, e.g.
 * "pamasahe-Sep-4-2026-9-41-AM" — readable in a gallery, and unique per minute
 * so a second receipt does not overwrite the first.
 */
fun receiptFileName(dateLabel: String): String {
    val slug = dateLabel
        .map { if (it.isLetterOrDigit()) it else '-' }
        .joinToString("")
        .split('-')
        .filter { it.isNotEmpty() }
        .joinToString("-")
    return if (slug.isEmpty()) "pamasahe-receipt" else "pamasahe-$slug"
}

fun formatKm(km: Double): String {
    val tenths = kotlin.math.round(km * 10).toLong()
    return "${tenths / 10}.${kotlin.math.abs(tenths % 10)}"
}
