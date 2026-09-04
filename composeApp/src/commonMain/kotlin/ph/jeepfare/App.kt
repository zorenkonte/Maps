package ph.jeepfare

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import ph.jeepfare.data.OsrmClient
import ph.jeepfare.domain.FareBreakdown
import ph.jeepfare.domain.JeepneyType
import ph.jeepfare.domain.PassengerType
import ph.jeepfare.domain.TripParty
import ph.jeepfare.ui.CalculatorScreen
import ph.jeepfare.ui.DistanceInputMode
import ph.jeepfare.ui.MAX_COMPANIONS_PER_TYPE
import ph.jeepfare.ui.MapDistance
import ph.jeepfare.ui.MapPickerScreen
import ph.jeepfare.ui.RatesScreen
import ph.jeepfare.ui.ReceiptScreen
import ph.jeepfare.ui.theme.PamasaheTheme

private enum class Screen { CALCULATOR, MAP_PICKER, RATES, RECEIPT }

private val MapDistanceSaver = listSaver<MapDistance?, Any>(
    save = { value -> value?.let { listOf(it.distanceKm, it.isEstimate) } ?: emptyList() },
    restore = { saved ->
        if (saved.size == 2) MapDistance(saved[0] as Double, saved[1] as Boolean) else null
    },
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    // null = follow the system; the header toggle overrides per-session.
    var darkOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val isDark = darkOverride ?: isSystemInDarkTheme()

    PamasaheTheme(darkTheme = isDark) {
        var screen by rememberSaveable { mutableStateOf(Screen.CALCULATOR) }

        var jeepneyType by rememberSaveable { mutableStateOf(JeepneyType.TRADITIONAL) }
        var inputMode by rememberSaveable { mutableStateOf(DistanceInputMode.MAP) }
        var manualKmText by rememberSaveable { mutableStateOf("") }
        var mapDistance by rememberSaveable(stateSaver = MapDistanceSaver) {
            mutableStateOf<MapDistance?>(null)
        }
        // Receipt snapshot is not saveable; after process death we land back on the calculator.
        var receiptBreakdown by remember { mutableStateOf<FareBreakdown?>(null) }
        var receiptParty by remember { mutableStateOf(TripParty()) }

        // The commuter's own fare type, plus anyone they are paying for.
        var myFareType by rememberSaveable { mutableStateOf(PassengerType.REGULAR) }
        var regularCompanions by rememberSaveable { mutableStateOf(0) }
        var studentCompanions by rememberSaveable { mutableStateOf(0) }
        var seniorCompanions by rememberSaveable { mutableStateOf(0) }
        var pwdCompanions by rememberSaveable { mutableStateOf(0) }

        val party = TripParty(
            myFareType = myFareType,
            companions = mapOf(
                PassengerType.REGULAR to regularCompanions,
                PassengerType.STUDENT to studentCompanions,
                PassengerType.SENIOR to seniorCompanions,
                PassengerType.PWD to pwdCompanions,
            ),
        )
        val onCompanionCountChange: (PassengerType, Int) -> Unit = { type, value ->
            val clamped = value.coerceIn(0, MAX_COMPANIONS_PER_TYPE)
            when (type) {
                PassengerType.REGULAR -> regularCompanions = clamped
                PassengerType.STUDENT -> studentCompanions = clamped
                PassengerType.SENIOR -> seniorCompanions = clamped
                PassengerType.PWD -> pwdCompanions = clamped
            }
        }

        // One app-scoped client: Ktor engines are heavyweight and OsrmClient has
        // no close(), so it must not be re-created per map-picker visit.
        val osrmClient = remember { OsrmClient() }

        // System back returns to the calculator instead of leaving the app.
        BackHandler(enabled = screen != Screen.CALCULATOR) { screen = Screen.CALCULATOR }

        when (screen) {
            Screen.MAP_PICKER -> MapPickerScreen(
                osrmClient = osrmClient,
                onUseDistance = { picked ->
                    mapDistance = picked
                    inputMode = DistanceInputMode.MAP
                    screen = Screen.CALCULATOR
                },
                onBack = { screen = Screen.CALCULATOR },
            )
            Screen.RATES -> RatesScreen(onBack = { screen = Screen.CALCULATOR })
            Screen.RECEIPT -> {
                val breakdown = receiptBreakdown
                if (breakdown != null) {
                    ReceiptScreen(
                        breakdown = breakdown,
                        party = receiptParty,
                        onBack = { screen = Screen.CALCULATOR },
                    )
                } else {
                    // Snapshot lost (e.g. process death) — fall back to the calculator.
                    screen = Screen.CALCULATOR
                }
            }
            Screen.CALCULATOR -> CalculatorScreen(
                jeepneyType = jeepneyType,
                onJeepneyTypeChange = { jeepneyType = it },
                inputMode = inputMode,
                onInputModeChange = { inputMode = it },
                manualKmText = manualKmText,
                onManualKmTextChange = { manualKmText = it },
                mapDistance = mapDistance,
                onPickOnMap = { screen = Screen.MAP_PICKER },
                party = party,
                onFareTypeChange = { myFareType = it },
                onCompanionCountChange = onCompanionCountChange,
                isDark = isDark,
                onToggleDark = { darkOverride = !isDark },
                onOpenRates = { screen = Screen.RATES },
                onShare = { breakdown ->
                    receiptBreakdown = breakdown
                    receiptParty = party
                    screen = Screen.RECEIPT
                },
            )
        }
    }
}
