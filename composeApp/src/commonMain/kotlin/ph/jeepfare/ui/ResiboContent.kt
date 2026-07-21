package ph.jeepfare.ui

import ph.jeepfare.domain.FareBreakdown
import ph.jeepfare.domain.toPesoString
import ph.jeepfare.ui.components.ResiboRow

const val PESO = "₱"

fun Double.peso(): String = PESO + toPesoString()

/** Receipt lines for a fare breakdown + the index the pre-passenger divider goes before. */
fun resiboRows(b: FareBreakdown): Pair<List<ResiboRow>, Int?> {
    val rows = mutableListOf(
        ResiboRow(Strings.BASE_FARE_LABEL.replace("%d", b.rate.baseKm.toString()), b.rate.baseFare.peso()),
    )
    if (b.extraKm > 0) {
        rows += ResiboRow(
            Strings.EXTRA_LABEL.replace("%d", b.extraKm.toString()).replace("%s", b.rate.perKm.peso()),
            b.extraCharge.peso(),
        )
    }
    rows += ResiboRow(Strings.PER_HEAD_LABEL, b.regularFare.peso(), strong = true)
    val dividerAt = if (b.passengers.isNotEmpty()) rows.size else null
    b.passengers.forEach { line ->
        val disc = if (line.type.discounted) Strings.DISCOUNT_SUFFIX else ""
        rows += ResiboRow(
            "${Strings.passengerTypeLabel(line.type)} × ${line.count}$disc",
            line.subtotal.peso(),
        )
    }
    return rows to dividerAt
}

/** Plain-text version of the resibo for the platform share sheet. */
fun resiboShareText(b: FareBreakdown, dateLabel: String): String = buildString {
    appendLine("PAMASAHE — resibo")
    appendLine("${Strings.jeepneyTypeLong(b.jeepneyType)} · ${formatKm(b.distanceKm)} km · $dateLabel")
    appendLine("----------------------------")
    resiboRows(b).first.forEach { row -> appendLine("${row.label}: ${row.value}") }
    appendLine("----------------------------")
    appendLine("${Strings.RESIBO_TOTAL}: ${b.total.peso()}")
    appendLine(Strings.FARE_MATRIX_NOTE)
    append(Strings.RESIBO_FOOTER)
}

fun formatKm(km: Double): String {
    val tenths = kotlin.math.round(km * 10).toLong()
    return "${tenths / 10}.${kotlin.math.abs(tenths % 10)}"
}
