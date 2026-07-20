package ph.jeepfare.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import ph.jeepfare.data.OsrmClient
import ph.jeepfare.domain.LatLng
import ph.jeepfare.domain.estimatedRoadKm

private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

// Manila City Hall, a central default view for Metro Manila.
private val DEFAULT_CENTER = Position(longitude = 120.9842, latitude = 14.5995)

private sealed interface RouteState {
    data object Idle : RouteState
    data object Loading : RouteState
    data class Ready(val distance: MapDistance, val usedFallback: Boolean) : RouteState
}

@Composable
fun MapPickerScreen(
    osrmClient: OsrmClient,
    onUseDistance: (MapDistance) -> Unit,
    onBack: () -> Unit,
) {
    var origin by remember { mutableStateOf<LatLng?>(null) }
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var routeState by remember { mutableStateOf<RouteState>(RouteState.Idle) }

    val currentOrigin = origin
    val currentDestination = destination

    LaunchedEffect(currentOrigin, currentDestination) {
        routeState = if (currentOrigin != null && currentDestination != null) {
            RouteState.Loading
        } else {
            RouteState.Idle
        }
        if (currentOrigin != null && currentDestination != null) {
            // Store the distance rounded to 0.1 km — the same precision it is
            // displayed with — so the shown km and the charged km never disagree.
            routeState = osrmClient.routeDistanceKm(currentOrigin, currentDestination).fold(
                onSuccess = { km ->
                    RouteState.Ready(MapDistance(roundToTenth(km), isEstimate = false), usedFallback = false)
                },
                onFailure = {
                    val km = estimatedRoadKm(currentOrigin, currentDestination)
                    RouteState.Ready(MapDistance(roundToTenth(km), isEstimate = true), usedFallback = true)
                },
            )
        }
    }

    val camera = rememberCameraState(
        firstPosition = CameraPosition(target = DEFAULT_CENTER, zoom = 12.0),
    )

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                MaplibreMap(
                    modifier = Modifier.fillMaxSize(),
                    baseStyle = BaseStyle.Uri(STYLE_URL),
                    cameraState = camera,
                    onMapClick = { pos, _ ->
                        val tapped = LatLng(latitude = pos.latitude, longitude = pos.longitude)
                        when {
                            origin == null -> origin = tapped
                            destination == null -> destination = tapped
                            else -> {
                                origin = tapped
                                destination = null
                            }
                        }
                        ClickResult.Consume
                    },
                ) {
                    val o = origin
                    val d = destination

                    if (o != null && d != null) {
                        val lineSource = rememberGeoJsonSource(
                            GeoJsonData.Features(
                                LineString(o.toPosition(), d.toPosition())
                            )
                        )
                        LineLayer(
                            id = "route-line",
                            source = lineSource,
                            color = const(Color(0xFFB4451F)),
                            width = const(3.dp),
                        )
                    }
                    if (o != null) {
                        MarkerLayer(id = "origin", latLng = o, color = Color(0xFF1E6E5C))
                    }
                    if (d != null) {
                        MarkerLayer(id = "destination", latLng = d, color = Color(0xFFB4451F))
                    }
                }
            }

            BottomPanel(
                origin = currentOrigin,
                destination = currentDestination,
                routeState = routeState,
                onUseDistance = onUseDistance,
                onReset = {
                    origin = null
                    destination = null
                },
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun MarkerLayer(id: String, latLng: LatLng, color: Color) {
    val source = rememberGeoJsonSource(GeoJsonData.Features(Point(latLng.toPosition())))
    CircleLayer(
        id = id,
        source = source,
        color = const(color),
        radius = const(9.dp),
        strokeColor = const(Color.White),
        strokeWidth = const(2.dp),
    )
}

@Composable
private fun BottomPanel(
    origin: LatLng?,
    destination: LatLng?,
    routeState: RouteState,
    onUseDistance: (MapDistance) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                origin == null -> Text(Strings.MAP_TAP_ORIGIN, style = MaterialTheme.typography.titleMedium)
                destination == null -> Text(Strings.MAP_TAP_DESTINATION, style = MaterialTheme.typography.titleMedium)
                else -> when (routeState) {
                    is RouteState.Loading, RouteState.Idle -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(Strings.MAP_ROUTE_LOADING)
                    }
                    is RouteState.Ready -> {
                        Text(
                            "${formatKm(routeState.distance.distanceKm)} km",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        if (routeState.usedFallback) {
                            Text(
                                Strings.MAP_ROUTE_FAILED,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack) { Text(Strings.BACK) }
                OutlinedButton(onClick = onReset, enabled = origin != null) { Text(Strings.RESET) }
                val ready = routeState as? RouteState.Ready
                Button(
                    onClick = { ready?.let { onUseDistance(it.distance) } },
                    enabled = ready != null,
                ) {
                    Text(Strings.USE_DISTANCE)
                }
            }
        }
    }
}

private fun LatLng.toPosition(): Position = Position(longitude = longitude, latitude = latitude)

private fun roundToTenth(km: Double): Double = kotlin.math.round(km * 10) / 10.0
