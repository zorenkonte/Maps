package ph.jeepfare

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import ph.jeepfare.data.OsrmClient
import ph.jeepfare.domain.JeepneyType
import ph.jeepfare.domain.PassengerType
import ph.jeepfare.ui.CalculatorScreen
import ph.jeepfare.ui.DistanceInputMode
import ph.jeepfare.ui.MAX_PASSENGERS_PER_TYPE
import ph.jeepfare.ui.MapDistance
import ph.jeepfare.ui.MapPickerScreen

private val JeepColors = lightColorScheme(
    primary = Color(0xFFB4451F), // jeepney orange-red
    secondary = Color(0xFF1E6E5C), // route-sign green
    tertiary = Color(0xFFB58500), // chrome-trim yellow
)

private val MapDistanceSaver = listSaver<MapDistance?, Any>(
    save = { value -> value?.let { listOf(it.distanceKm, it.isEstimate) } ?: emptyList() },
    restore = { saved ->
        if (saved.size == 2) MapDistance(saved[0] as Double, saved[1] as Boolean) else null
    },
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    MaterialTheme(colorScheme = JeepColors) {
        var showMapPicker by rememberSaveable { mutableStateOf(false) }

        var jeepneyType by rememberSaveable { mutableStateOf(JeepneyType.TRADITIONAL) }
        var inputMode by rememberSaveable { mutableStateOf(DistanceInputMode.MAP) }
        var manualKmText by rememberSaveable { mutableStateOf("") }
        var mapDistance by rememberSaveable(stateSaver = MapDistanceSaver) {
            mutableStateOf<MapDistance?>(null)
        }

        // One app-scoped client: Ktor engines are heavyweight and OsrmClient has
        // no close(), so it must not be re-created per map-picker visit.
        val osrmClient = remember { OsrmClient() }

        var regularCount by rememberSaveable { mutableStateOf(1) }
        var studentCount by rememberSaveable { mutableStateOf(0) }
        var seniorCount by rememberSaveable { mutableStateOf(0) }
        var pwdCount by rememberSaveable { mutableStateOf(0) }

        val counts = mapOf(
            PassengerType.REGULAR to regularCount,
            PassengerType.STUDENT to studentCount,
            PassengerType.SENIOR to seniorCount,
            PassengerType.PWD to pwdCount,
        )
        val onCountChange: (PassengerType, Int) -> Unit = { type, value ->
            val clamped = value.coerceIn(0, MAX_PASSENGERS_PER_TYPE)
            when (type) {
                PassengerType.REGULAR -> regularCount = clamped
                PassengerType.STUDENT -> studentCount = clamped
                PassengerType.SENIOR -> seniorCount = clamped
                PassengerType.PWD -> pwdCount = clamped
            }
        }

        // System back closes the map picker instead of leaving the app.
        BackHandler(enabled = showMapPicker) { showMapPicker = false }

        if (showMapPicker) {
            MapPickerScreen(
                osrmClient = osrmClient,
                onUseDistance = { picked ->
                    mapDistance = picked
                    inputMode = DistanceInputMode.MAP
                    showMapPicker = false
                },
                onBack = { showMapPicker = false },
            )
        } else {
            CalculatorScreen(
                jeepneyType = jeepneyType,
                onJeepneyTypeChange = { jeepneyType = it },
                inputMode = inputMode,
                onInputModeChange = { inputMode = it },
                manualKmText = manualKmText,
                onManualKmTextChange = { manualKmText = it },
                mapDistance = mapDistance,
                onPickOnMap = { showMapPicker = true },
                passengerCounts = counts,
                onPassengerCountChange = onCountChange,
            )
        }
    }
}
