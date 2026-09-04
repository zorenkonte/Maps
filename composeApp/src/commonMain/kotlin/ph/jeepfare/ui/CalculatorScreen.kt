package ph.jeepfare.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import ph.jeepfare.domain.TripParty
import ph.jeepfare.ui.components.PamBarTitle
import ph.jeepfare.ui.components.PamButton
import ph.jeepfare.ui.components.PamButtonSize
import ph.jeepfare.ui.components.PamButtonVariant
import ph.jeepfare.ui.components.PamCard
import ph.jeepfare.ui.components.PamChip
import ph.jeepfare.ui.components.PamChoiceGrid
import ph.jeepfare.ui.components.PamChoiceItem
import ph.jeepfare.ui.components.PamHeroTopBar
import ph.jeepfare.ui.components.PamIconButton
import ph.jeepfare.ui.components.PamOverline
import ph.jeepfare.ui.components.PamPinnedHeader
import ph.jeepfare.ui.components.PamSegmentItem
import ph.jeepfare.ui.components.PamSegmented
import ph.jeepfare.ui.components.PamStepper
import ph.jeepfare.ui.components.Resibo
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamIcons
import ph.jeepfare.ui.theme.PamTone
import ph.jeepfare.ui.theme.pamArriveEnter
import ph.jeepfare.ui.theme.pamArriveExit
import ph.jeepfare.ui.theme.pamEnter
import ph.jeepfare.ui.theme.pamRevealEnter
import ph.jeepfare.ui.theme.pamRevealExit
import ph.jeepfare.ui.theme.pamSwap

enum class DistanceInputMode { MAP, MANUAL }

/** Distance picked from the map, flagged when it is only a straight-line estimate. */
data class MapDistance(val distanceKm: Double, val isEstimate: Boolean)

/** Companions a commuter can pay for per fare type — a family, not a jeep-load. */
const val MAX_COMPANIONS_PER_TYPE = 8

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
    party: TripParty,
    onFareTypeChange: (PassengerType) -> Unit,
    onCompanionCountChange: (PassengerType, Int) -> Unit,
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
    // The commuter is always one rider, so a distance is the only thing a fare waits on.
    val breakdown = distanceKm?.let { FareCalculator.calculate(it, jeepneyType, party.counts()) }

    // Companion steppers stay collapsed until asked for — riding alone is the
    // common case, and an always-visible tally is what makes an app feel like
    // it belongs to the driver.
    var companionsExpanded by rememberSaveable { mutableStateOf(false) }
    val showCompanions = companionsExpanded || party.companionCount > 0

    Scaffold(containerColor = pal.bg) { padding ->
        PamPinnedHeader(
            modifier = Modifier.padding(padding).imePadding(),
            // The wordmark reads as a large title: it sits in the page and
            // scrolls away, handing its name to the bar that stays behind.
            largeHeader = { PamHeroTopBar(Modifier.pamEnter(index = 0)) },
            bar = { collapse ->
                PamBarTitle(
                    Strings.APP_TITLE,
                    // Lines the small mark up with the big one it takes over from.
                    Modifier.padding(start = 4.dp),
                    collapse = collapse,
                    mark = true,
                )
                // Spec: a single trailing action (theme toggle). The Rates screen is
                // reached via the tappable LTFRB note at the bottom of this screen.
                PamIconButton(
                    if (isDark) PamIcons.LightMode else PamIcons.DarkMode,
                    contentDescription = Strings.THEME,
                    onClick = onToggleDark,
                    modifier = Modifier.pamEnter(index = 0),
                )
            },
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PamCard(overline = Strings.OVERLINE_JEEP, modifier = Modifier.pamEnter(index = 1)) {
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

                PamCard(overline = Strings.OVERLINE_DISTANCE, modifier = Modifier.pamEnter(index = 2)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PamSegmented(
                            items = listOf(
                                PamSegmentItem(DistanceInputMode.MAP, Strings.TAB_MAP, icon = PamIcons.Map, iconTone = PamTone.BLUE),
                                PamSegmentItem(DistanceInputMode.MANUAL, Strings.TAB_MANUAL, icon = PamIcons.Edit, iconTone = PamTone.BLUE),
                            ),
                            selected = inputMode,
                            onSelect = onInputModeChange,
                        )
                        // The tab body grows and shrinks between the two modes; a
                        // plain swap would make the card snap to a new height.
                        AnimatedContent(
                            targetState = inputMode,
                            transitionSpec = { pamSwap(upward = targetState == DistanceInputMode.MANUAL) },
                            label = "distanceMode",
                        ) { mode ->
                            // Same 12dp rhythm the card used before the wrapper.
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                when (mode) {
                                    DistanceInputMode.MAP -> MapDistanceSection(mapDistance, onPickOnMap)
                                    DistanceInputMode.MANUAL ->
                                        ManualDistanceSection(manualKmText, manualKm, onManualKmTextChange)
                                }
                            }
                        }
                    }
                }

                PamCard(overline = Strings.OVERLINE_FARE_TYPE, modifier = Modifier.pamEnter(index = 3)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PamChoiceGrid(
                            items = PassengerType.entries.map { type ->
                                PamChoiceItem(
                                    value = type,
                                    label = Strings.passengerTypeLabel(type),
                                    icon = passengerIconFor(type),
                                    tone = passengerToneFor(type),
                                    note = if (type.discounted) Strings.DISCOUNT_CHIP else null,
                                )
                            },
                            selected = party.myFareType,
                            onSelect = onFareTypeChange,
                        )
                        Text(
                            Strings.FARE_TYPE_HINT,
                            fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                            color = pal.ink2,
                        )
                    }
                }

                CompanionsCard(
                    modifier = Modifier.pamEnter(index = 4),
                    party = party,
                    expanded = showCompanions,
                    onToggle = {
                        if (showCompanions) {
                            // "Just me" is a real answer, not just a fold — drop the companions too.
                            PassengerType.entries.forEach { onCompanionCountChange(it, 0) }
                            companionsExpanded = false
                        } else {
                            companionsExpanded = true
                        }
                    },
                    onCompanionCountChange = onCompanionCountChange,
                )

                PamOverline(Strings.OVERLINE_BREAKDOWN, Modifier.pamEnter(index = 5))

                // The receipt is the payoff of the whole screen, so it arrives as
                // one object — rising and settling — instead of blinking into
                // existence the instant a distance parses. Keying the content on
                // "is there a fare at all" means edits to an existing fare update
                // the receipt in place, and only appearing/disappearing animates.
                AnimatedContent(
                    targetState = breakdown,
                    transitionSpec = {
                        val enter = if (targetState != null) pamArriveEnter() else pamRevealEnter()
                        (enter togetherWith pamArriveExit()).using(SizeTransform(clip = false))
                    },
                    contentKey = { it != null },
                    label = "breakdown",
                ) { current ->
                    if (current != null) {
                        val (rows, dividerAt) = resiboRows(current, party)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Resibo(
                                header = Strings.RESIBO_HEADER,
                                sub = "${Strings.jeepneyTypeLong(jeepneyType)} · ${formatKm(current.distanceKm)} km",
                                rows = rows,
                                dividerBeforeIndex = dividerAt,
                                totalLabel = totalLabelFor(party),
                                totalValue = current.total.peso(),
                                footer = Strings.RESIBO_FOOTER,
                            )
                            PamButton(
                                Strings.SHARE_RESIBO,
                                onClick = { onShare(current) },
                                icon = PamIcons.Share,
                                size = PamButtonSize.LG,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        Text(
                            Strings.ENTER_DISTANCE_PROMPT,
                            fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                            color = pal.ink2,
                        )
                    }
                }

                Text(
                    Strings.FARE_MATRIX_NOTE,
                    fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = pal.ink2,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .pamEnter(index = 6)
                        .clickable(onClick = onOpenRates)
                        .padding(4.dp),
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Optional companion counts. A commuter mostly rides alone, so this stays a
 * one-line "Just me" until they say otherwise — and the caps are family-sized.
 */
@Composable
private fun CompanionsCard(
    party: TripParty,
    expanded: Boolean,
    onToggle: () -> Unit,
    onCompanionCountChange: (PassengerType, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    PamCard(overline = Strings.OVERLINE_COMPANIONS, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val summary = if (party.companionCount > 0) {
                    Strings.COMPANIONS_CHIP.replace("%d", party.companionCount.toString())
                } else {
                    Strings.COMPANIONS_HINT
                }
                // "Just me" -> "2 companions" slides rather than flickers.
                AnimatedContent(
                    targetState = summary,
                    transitionSpec = { pamSwap() },
                    modifier = Modifier.weight(1f),
                    label = "companionSummary",
                ) { text ->
                    Text(
                        text,
                        fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        color = pal.ink2,
                    )
                }
                PamButton(
                    if (expanded) Strings.HIDE_COMPANIONS else Strings.ADD_COMPANIONS,
                    onClick = onToggle,
                    icon = if (expanded) PamIcons.Remove else PamIcons.Add,
                    variant = PamButtonVariant.SECONDARY,
                    size = PamButtonSize.SM,
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = pamRevealEnter(),
                exit = pamRevealExit(),
            ) {
                Column {
                    PassengerType.entries.forEachIndexed { index, type ->
                        if (index > 0) HorizontalDivider(thickness = 1.5.dp, color = pal.line)
                        PamStepper(
                            icon = passengerIconFor(type),
                            tone = passengerToneFor(type),
                            label = Strings.passengerTypeLabel(type),
                            chip = if (type.discounted) Strings.DISCOUNT_CHIP else null,
                            value = party.companionsOf(type),
                            onValueChange = { onCompanionCountChange(type, it) },
                            max = MAX_COMPANIONS_PER_TYPE,
                        )
                    }
                }
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
