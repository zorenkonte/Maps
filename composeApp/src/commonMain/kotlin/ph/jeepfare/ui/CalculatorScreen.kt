package ph.jeepfare.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.jeepfare.domain.FareBreakdown
import ph.jeepfare.domain.FareCalculator
import ph.jeepfare.domain.JeepneyType
import ph.jeepfare.domain.PassengerType
import ph.jeepfare.ui.components.PamButton
import ph.jeepfare.ui.components.PamButtonSize
import ph.jeepfare.ui.components.PamButtonVariant
import ph.jeepfare.ui.components.PamCard
import ph.jeepfare.ui.components.PamChip
import ph.jeepfare.ui.components.PamHeroTopBar
import ph.jeepfare.ui.components.PamIconButton
import ph.jeepfare.ui.components.PamOverline
import ph.jeepfare.ui.components.PamSegmentItem
import ph.jeepfare.ui.components.PamSegmented
import ph.jeepfare.ui.components.PamStepper
import ph.jeepfare.ui.components.Resibo
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamIcons
import ph.jeepfare.ui.theme.PamTone

enum class DistanceInputMode { MAP, MANUAL }

/** Distance picked from the map, flagged when it is only a straight-line estimate. */
data class MapDistance(val distanceKm: Double, val isEstimate: Boolean)

const val MAX_PASSENGERS_PER_TYPE = 30

/** Longest accepted trip; well beyond any jeepney route. */
const val MAX_DISTANCE_KM = 500.0

private fun validDistanceOrNull(km: Double?): Double? =
    km?.takeIf { it.isFinite() && it >= 0.0 && it <= MAX_DISTANCE_KM }

/** Per-type signage: icon + tone (screens.jsx TYPES). */
fun passengerIconFor(type: PassengerType) = when (type) {
    PassengerType.REGULAR -> PamIcons.Person
    PassengerType.STUDENT -> PamIcons.School
    PassengerType.SENIOR -> PamIcons.Elderly
    PassengerType.PWD -> PamIcons.Accessible
}

fun passengerToneFor(type: PassengerType) = when (type) {
    PassengerType.REGULAR -> PamTone.BLUE
    PassengerType.STUDENT -> PamTone.BLUE
    PassengerType.SENIOR -> PamTone.YELLOW
    PassengerType.PWD -> PamTone.RED
}

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
    isDark: Boolean,
    onToggleDark: () -> Unit,
    onOpenRates: () -> Unit,
    onShare: (FareBreakdown) -> Unit,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current

    val manualKm = manualKmText.replace(',', '.').toDoubleOrNull()
    val distanceKm = when (inputMode) {
        DistanceInputMode.MAP -> validDistanceOrNull(mapDistance?.distanceKm)
        DistanceInputMode.MANUAL -> validDistanceOrNull(manualKm)
    }
    val totalPassengers = passengerCounts.values.sum()
    val breakdown = distanceKm?.takeIf { totalPassengers > 0 }?.let {
        FareCalculator.calculate(it, jeepneyType, passengerCounts)
    }

    Scaffold(containerColor = pal.bg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            PamHeroTopBar {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PamIconButton(PamIcons.ReceiptLong, contentDescription = Strings.RATES_TITLE, onClick = onOpenRates)
                    PamIconButton(
                        if (isDark) PamIcons.LightMode else PamIcons.DarkMode,
                        contentDescription = Strings.THEME,
                        onClick = onToggleDark,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PamCard(overline = Strings.OVERLINE_JEEP) {
                    PamSegmented(
                        items = listOf(
                            PamSegmentItem(
                                JeepneyType.TRADITIONAL, Strings.jeepneyTypeLabel(JeepneyType.TRADITIONAL),
                                icon = PamIcons.AirportShuttle, iconTone = PamTone.RED, sub = "₱14 + ₱2/km",
                            ),
                            PamSegmentItem(
                                JeepneyType.MODERN, Strings.jeepneyTypeLabel(JeepneyType.MODERN),
                                icon = PamIcons.AirportShuttle, iconTone = PamTone.BLUE, sub = "₱17 + ₱2.40/km",
                            ),
                        ),
                        selected = jeepneyType,
                        onSelect = onJeepneyTypeChange,
                    )
                }

                PamCard(overline = Strings.OVERLINE_DISTANCE) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PamSegmented(
                            items = listOf(
                                PamSegmentItem(DistanceInputMode.MAP, Strings.TAB_MAP, icon = PamIcons.Map, iconTone = PamTone.BLUE),
                                PamSegmentItem(DistanceInputMode.MANUAL, Strings.TAB_MANUAL, icon = PamIcons.Edit, iconTone = PamTone.BLUE),
                            ),
                            selected = inputMode,
                            onSelect = onInputModeChange,
                        )
                        when (inputMode) {
                            DistanceInputMode.MAP -> MapDistanceSection(mapDistance, onPickOnMap)
                            DistanceInputMode.MANUAL -> ManualDistanceSection(manualKmText, manualKm, onManualKmTextChange)
                        }
                    }
                }

                PamCard(overline = Strings.OVERLINE_PASSENGERS, contentPadding = 12.dp) {
                    Column {
                        PassengerType.entries.forEachIndexed { index, type ->
                            if (index > 0) HorizontalDivider(thickness = 1.5.dp, color = pal.line)
                            PamStepper(
                                icon = passengerIconFor(type),
                                tone = passengerToneFor(type),
                                label = Strings.passengerTypeLabel(type),
                                chip = if (type.discounted) Strings.DISCOUNT_CHIP else null,
                                value = passengerCounts[type] ?: 0,
                                onValueChange = { onPassengerCountChange(type, it) },
                                max = MAX_PASSENGERS_PER_TYPE,
                            )
                        }
                    }
                }

                PamOverline(Strings.OVERLINE_BREAKDOWN)

                if (breakdown != null) {
                    val (rows, dividerAt) = resiboRows(breakdown)
                    Resibo(
                        header = Strings.RESIBO_HEADER,
                        sub = "${Strings.jeepneyTypeLong(jeepneyType)} · ${formatKm(breakdown.distanceKm)} km",
                        rows = rows,
                        dividerBeforeIndex = dividerAt,
                        totalLabel = Strings.RESIBO_TOTAL,
                        totalValue = breakdown.total.peso(),
                        footer = Strings.RESIBO_FOOTER,
                    )
                    PamButton(
                        Strings.SHARE_RESIBO,
                        onClick = { onShare(breakdown) },
                        icon = PamIcons.Share,
                        size = PamButtonSize.LG,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        if (totalPassengers == 0) Strings.NO_PASSENGERS_PROMPT else Strings.ENTER_DISTANCE_PROMPT,
                        fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        color = pal.ink2,
                    )
                }

                Text(
                    Strings.FARE_MATRIX_NOTE,
                    fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = pal.ink2,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(onClick = onOpenRates)
                        .padding(4.dp),
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MapDistanceSection(mapDistance: MapDistance?, onPickOnMap: () -> Unit) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    if (mapDistance == null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                Strings.NO_DISTANCE_YET,
                fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = pal.ink2,
                modifier = Modifier.weight(1f),
            )
            PamButton(
                Strings.OPEN_MAP, onClick = onPickOnMap, icon = PamIcons.Map,
                variant = PamButtonVariant.SECONDARY, size = PamButtonSize.SM,
            )
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(PamIcons.MyLocation, contentDescription = null, tint = pal.green, modifier = Modifier.height(20.dp))
            Text(
                Strings.ROUTE_FROM_MAP,
                fontFamily = fonts.body, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = pal.ink,
                modifier = Modifier.weight(1f), maxLines = 1,
            )
            Text(
                "${formatKm(mapDistance.distanceKm)} km",
                fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = pal.ink,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (mapDistance.isEstimate) {
                PamChip(Strings.CHIP_ESTIMATE, tone = PamTone.YELLOW, icon = PamIcons.SignalWifiOff)
            } else {
                PamChip(Strings.CHIP_OSRM, tone = PamTone.GREEN, icon = PamIcons.Route)
            }
            Spacer(Modifier.weight(1f))
            PamButton(
                Strings.OPEN_MAP, onClick = onPickOnMap, icon = PamIcons.Map,
                variant = PamButtonVariant.SECONDARY, size = PamButtonSize.SM,
            )
        }
    }
}

@Composable
private fun ManualDistanceSection(manualKmText: String, manualKm: Double?, onManualKmTextChange: (String) -> Unit) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    OutlinedTextField(
        value = manualKmText,
        onValueChange = onManualKmTextChange,
        label = { Text(Strings.MANUAL_DISTANCE_LABEL) },
        suffix = { Text("km", fontFamily = fonts.mono, color = pal.ink2) },
        supportingText = { Text(Strings.MANUAL_HINT) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = pal.focus,
            unfocusedBorderColor = pal.line2,
        ),
        modifier = Modifier.fillMaxWidth(),
        // Must mirror the acceptance filter, or invalid input shows a
        // valid-looking field with no fare and no explanation.
        isError = manualKmText.isNotBlank() && validDistanceOrNull(manualKm) == null,
    )
}
