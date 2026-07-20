package ph.jeepfare.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ph.jeepfare.domain.FareBreakdown
import ph.jeepfare.domain.FareCalculator
import ph.jeepfare.domain.JeepneyType
import ph.jeepfare.domain.PassengerType
import ph.jeepfare.domain.toPesoString

enum class DistanceInputMode { MAP, MANUAL }

/** Distance picked from the map, flagged when it is only a straight-line estimate. */
data class MapDistance(val distanceKm: Double, val isEstimate: Boolean)

private const val PESO = "₱"

@Composable
fun CalculatorScreen(
    jeepneyType: JeepneyType,
    onJeepneyTypeChange: (JeepneyType) -> Unit,
    inputMode: DistanceInputMode,
    onInputModeChange: (DistanceInputMode) -> Unit,
    manualKmText: String,
    onManualKmTextChange: (String) -> Unit,
    mapDistance: MapDistance?,
    onPickOnMap: () -> Unit,
    passengerCounts: Map<PassengerType, Int>,
    onPassengerCountChange: (PassengerType, Int) -> Unit,
) {
    val manualKm = manualKmText.replace(',', '.').toDoubleOrNull()
    val distanceKm = when (inputMode) {
        DistanceInputMode.MAP -> mapDistance?.distanceKm
        DistanceInputMode.MANUAL -> manualKm?.takeIf { it >= 0.0 && it.isFinite() }
    }
    val totalPassengers = passengerCounts.values.sum()
    val breakdown = distanceKm?.takeIf { totalPassengers > 0 }?.let {
        FareCalculator.calculate(it, jeepneyType, passengerCounts)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(Strings.APP_TITLE, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    Strings.APP_SUBTITLE,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionLabel(Strings.JEEP_TYPE_QUESTION)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                JeepneyType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = jeepneyType == type,
                        onClick = { onJeepneyTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = JeepneyType.entries.size),
                    ) {
                        Text(Strings.jeepneyTypeLabel(type))
                    }
                }
            }

            SectionLabel(Strings.DISTANCE_QUESTION)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                DistanceInputMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = inputMode == mode,
                        onClick = { onInputModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = DistanceInputMode.entries.size),
                    ) {
                        Text(if (mode == DistanceInputMode.MAP) Strings.TAB_MAP else Strings.TAB_MANUAL)
                    }
                }
            }

            when (inputMode) {
                DistanceInputMode.MAP -> MapDistanceSection(mapDistance, onPickOnMap)
                DistanceInputMode.MANUAL -> OutlinedTextField(
                    value = manualKmText,
                    onValueChange = onManualKmTextChange,
                    label = { Text(Strings.MANUAL_DISTANCE_LABEL) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = manualKmText.isNotBlank() && manualKm == null,
                )
            }

            SectionLabel(Strings.PASSENGERS_QUESTION)
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PassengerType.entries.forEach { type ->
                        PassengerRow(
                            type = type,
                            count = passengerCounts[type] ?: 0,
                            onCountChange = { onPassengerCountChange(type, it) },
                        )
                    }
                }
            }

            when {
                breakdown != null -> BreakdownCard(breakdown)
                totalPassengers == 0 -> HintText(Strings.NO_PASSENGERS_PROMPT)
                else -> HintText(Strings.ENTER_DISTANCE_PROMPT)
            }

            Text(
                Strings.FARE_MATRIX_NOTE,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun HintText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun MapDistanceSection(mapDistance: MapDistance?, onPickOnMap: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (mapDistance == null) {
            HintText(Strings.NO_DISTANCE_YET)
        } else {
            Text(
                "${formatKm(mapDistance.distanceKm)} km",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (mapDistance.isEstimate) {
                Text(
                    Strings.ESTIMATE_NOTE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
        OutlinedButton(onClick = onPickOnMap) {
            Text(if (mapDistance == null) Strings.PICK_ON_MAP else Strings.CHANGE_ON_MAP)
        }
    }
}

@Composable
private fun PassengerRow(type: PassengerType, count: Int, onCountChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(Strings.passengerTypeLabel(type), style = MaterialTheme.typography.bodyLarge)
            if (type.discounted) {
                Text(
                    Strings.DISCOUNT_LABEL,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        OutlinedButton(onClick = { onCountChange(count - 1) }, enabled = count > 0) { Text("−") }
        Text(
            "$count",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        OutlinedButton(onClick = { onCountChange(count + 1) }) { Text("+") }
    }
}

@Composable
private fun BreakdownCard(breakdown: FareBreakdown) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(Strings.BREAKDOWN_TITLE, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            BreakdownRow(
                Strings.BASE_FARE_LABEL.replace("%d", breakdown.rate.baseKm.toString()),
                "$PESO${breakdown.rate.baseFare.toPesoString()}",
            )
            if (breakdown.extraKm > 0) {
                BreakdownRow(
                    "${Strings.EXTRA_LABEL}: ${breakdown.extraKm} km × $PESO${breakdown.rate.perKm.toPesoString()}",
                    "$PESO${breakdown.extraCharge.toPesoString()}",
                )
            }
            HorizontalDivider()

            breakdown.passengers.forEach { line ->
                val fareText = "$PESO${line.farePerHead.toPesoString()} ${Strings.PER_HEAD}"
                BreakdownRow(
                    "${line.count}× ${Strings.passengerTypeLabel(line.type)} ($fareText)",
                    "$PESO${line.subtotal.toPesoString()}",
                )
            }
            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Strings.TOTAL_LABEL,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "$PESO${breakdown.total.toPesoString()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

internal fun formatKm(km: Double): String {
    val tenths = kotlin.math.round(km * 10).toLong()
    return "${tenths / 10}.${kotlin.math.abs(tenths % 10)}"
}
