package ph.jeepfare.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import ph.jeepfare.data.OsrmClient
import ph.jeepfare.domain.LatLng
import ph.jeepfare.domain.estimatedRoadKm
import ph.jeepfare.domain.partialPolyline
import ph.jeepfare.ui.components.PamButton
import ph.jeepfare.ui.components.PamChip
import ph.jeepfare.ui.components.PamIconButton
import ph.jeepfare.ui.components.PamIconButtonVariant
import ph.jeepfare.ui.components.ResiboDivider
import ph.jeepfare.ui.theme.LocalPamFonts
import ph.jeepfare.ui.theme.LocalPamPalette
import ph.jeepfare.ui.theme.PamBorderWidth
import ph.jeepfare.ui.theme.PamIcons
import ph.jeepfare.ui.theme.PamTone

private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

// Manila City Hall, a central default view for Metro Manila.
private val DEFAULT_CENTER = Position(longitude = 120.9842, latitude = 14.5995)

private const val ROUTE_DRAW_MS = 1100

private sealed interface RouteState {
    data object Idle : RouteState
    data object Loading : RouteState
    data class Ready(
        val distance: MapDistance,
        val usedFallback: Boolean,
        val geometry: List<LatLng>,
    ) : RouteState
}

@Composable
fun MapPickerScreen(
    osrmClient: OsrmClient,
    onUseDistance: (MapDistance) -> Unit,
    onBack: () -> Unit,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    val scope = rememberCoroutineScope()

    var origin by remember { mutableStateOf<LatLng?>(null) }
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var routeState by remember { mutableStateOf<RouteState>(RouteState.Idle) }
    // The leading slice of the route currently drawn — grows during the animation.
    var drawnRoute by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val currentOrigin = origin
    val currentDestination = destination

    val camera = rememberCameraState(
        firstPosition = CameraPosition(target = DEFAULT_CENTER, zoom = 12.0),
    )

    LaunchedEffect(currentOrigin, currentDestination) {
        drawnRoute = emptyList()
        if (currentOrigin == null || currentDestination == null) {
            routeState = RouteState.Idle
            return@LaunchedEffect
        }
        routeState = RouteState.Loading
        // Store the distance rounded to 0.1 km — the same precision it is
        // displayed with — so the shown km and the charged km never disagree.
        routeState = osrmClient.route(currentOrigin, currentDestination).fold(
            onSuccess = { result ->
                val geometry = result.geometry.ifEmpty { listOf(currentOrigin, currentDestination) }
                RouteState.Ready(MapDistance(roundToTenth(result.distanceKm), isEstimate = false), false, geometry)
            },
            onFailure = {
                val km = estimatedRoadKm(currentOrigin, currentDestination)
                // No road geometry offline — animate the straight estimate instead.
                RouteState.Ready(MapDistance(roundToTenth(km), isEstimate = true), true, listOf(currentOrigin, currentDestination))
            },
        )
    }

    // Draw the route on like a navigation app: ease the camera to fit the path,
    // then progressively reveal the polyline from origin to destination.
    LaunchedEffect(routeState) {
        val ready = routeState as? RouteState.Ready ?: return@LaunchedEffect
        val geometry = ready.geometry
        if (geometry.size < 2) {
            drawnRoute = geometry
            return@LaunchedEffect
        }
        boundingBoxOf(geometry)?.let { box ->
            launch {
                camera.animateTo(
                    boundingBox = box,
                    padding = PaddingValues(start = 48.dp, top = 96.dp, end = 48.dp, bottom = 220.dp),
                    duration = 700.milliseconds,
                )
            }
        }
        val progress = Animatable(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = ROUTE_DRAW_MS, easing = FastOutSlowInEasing)) {
            drawnRoute = partialPolyline(geometry, value)
        }
        drawnRoute = geometry
    }

    Scaffold(containerColor = pal.bg) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(STYLE_URL),
                cameraState = camera,
                onMapClick = { pos, _ ->
                    val tapped = LatLng(latitude = pos.latitude, longitude = pos.longitude)
                    when {
                        origin == null -> origin = tapped
                        destination == null -> destination = tapped
                        else -> { // third tap resets the pick
                            origin = null
                            destination = null
                        }
                    }
                    ClickResult.Consume
                },
            ) {
                val o = origin
                val d = destination
                val ready = routeState as? RouteState.Ready

                if (drawnRoute.size >= 2) {
                    // Synchronous, un-simplified updates so the per-frame growth is smooth.
                    val routeSource = rememberGeoJsonSource(
                        data = GeoJsonData.Features(
                            LineString(*drawnRoute.map { it.toPosition() }.toTypedArray())
                        ),
                        options = GeoJsonOptions(synchronousUpdate = true, tolerance = 0f),
                    )
                    // White casing beneath the colored line for a crisp, navigation-app look.
                    LineLayer(
                        id = "route-casing",
                        source = routeSource,
                        color = const(Color.White),
                        width = const(9.dp),
                        cap = const(LineCap.Round),
                        join = const(LineJoin.Round),
                        opacity = const(0.9f),
                    )
                    LineLayer(
                        id = "route-line",
                        source = routeSource,
                        // Yellow signals a straight-line estimate; blue is a real OSRM route.
                        color = const(if (ready?.distance?.isEstimate == true) pal.yellow else pal.blue),
                        width = const(5.dp),
                        cap = const(LineCap.Round),
                        join = const(LineJoin.Round),
                    )
                }
                if (o != null) {
                    MarkerLayer(id = "origin", latLng = o, color = pal.green)
                }
                if (d != null) {
                    MarkerLayer(id = "destination", latLng = d, color = pal.red)
                }
            }

            // Floating top bar: back, pill title, re-center.
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PamIconButton(PamIcons.ArrowBack, contentDescription = Strings.BACK, onClick = onBack, size = 44.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .shadow(2.dp, CircleShape)
                        .background(pal.surface, CircleShape)
                        .border(PamBorderWidth, pal.line, CircleShape)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        Strings.MAP_TITLE,
                        fontFamily = fonts.display, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pal.ink,
                    )
                }
                PamIconButton(
                    PamIcons.MyLocation, contentDescription = Strings.MY_LOCATION,
                    variant = PamIconButtonVariant.TONAL, size = 44.dp,
                    onClick = {
                        scope.launch {
                            camera.animateTo(
                                finalPosition = CameraPosition(target = DEFAULT_CENTER, zoom = 12.0),
                                duration = 1.seconds,
                            )
                        }
                    },
                )
            }

            // Bottom panel: tap hint chip + route card.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally)) {
                    PamChip(Strings.MAP_TAP_HINT, tone = PamTone.NEUTRAL, icon = PamIcons.TouchApp)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp))
                        .background(pal.surface, RoundedCornerShape(20.dp))
                        .border(PamBorderWidth, pal.line, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                ) {
                    EndpointRow(PamIcons.MyLocation, pal.green, Strings.ORIGIN_LABEL, currentOrigin)
                    EndpointRow(PamIcons.PinDrop, pal.red, Strings.DESTINATION_LABEL, currentDestination)
                    ResiboDivider(Modifier.padding(top = 8.dp, bottom = 10.dp))

                    val ready = routeState as? RouteState.Ready
                    // Guard the calculator's cap here too, so "Gamitin" can never hand
                    // back a distance the calculator will silently reject.
                    val tooFar = ready != null && ready.distance.distanceKm > MAX_DISTANCE_KM
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        when (routeState) {
                            is RouteState.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = pal.blue, strokeWidth = 2.5.dp)
                                Text("…", fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = pal.ink2)
                            }
                            is RouteState.Ready -> {
                                Text(
                                    "${formatKm(ready!!.distance.distanceKm)} km",
                                    fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = pal.ink,
                                )
                                if (ready.distance.isEstimate) {
                                    PamChip(Strings.CHIP_ESTIMATE, tone = PamTone.YELLOW, icon = PamIcons.SignalWifiOff)
                                } else {
                                    PamChip(Strings.CHIP_OSRM, tone = PamTone.GREEN, icon = PamIcons.Route)
                                }
                            }
                            RouteState.Idle -> Text(
                                "—", fontFamily = fonts.mono, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = pal.ink3,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        PamButton(
                            if (ready != null) Strings.USE_DISTANCE else Strings.PICK_FIRST,
                            onClick = { ready?.let { onUseDistance(it.distance) } },
                            enabled = ready != null && !tooFar,
                        )
                    }
                    if (tooFar) {
                        Text(
                            Strings.TOO_FAR_NOTE,
                            fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                            color = pal.red, modifier = Modifier.padding(top = 6.dp),
                        )
                    } else if (ready?.usedFallback == true) {
                        Text(
                            Strings.MAP_ROUTE_FAILED,
                            fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                            color = pal.ink2, modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EndpointRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    label: String,
    point: LatLng?,
) {
    val pal = LocalPamPalette.current
    val fonts = LocalPamFonts.current
    Row(
        modifier = Modifier.fillMaxWidth().height(34.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(19.dp))
        Text(
            label.uppercase(),
            fontFamily = fonts.body, fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, letterSpacing = 0.05.em,
            color = pal.ink3, modifier = Modifier.width(64.dp),
        )
        Text(
            point?.let { "${it.latitude.round4()}, ${it.longitude.round4()}" } ?: Strings.MAP_TAP_ON_MAP,
            fontFamily = fonts.body, fontWeight = FontWeight.Bold, fontSize = 14.sp,
            color = if (point != null) pal.ink else pal.ink3, maxLines = 1,
        )
    }
}

@Composable
private fun MarkerLayer(id: String, latLng: LatLng, color: androidx.compose.ui.graphics.Color) {
    val source = rememberGeoJsonSource(GeoJsonData.Features(Point(latLng.toPosition())))
    CircleLayer(
        id = id,
        source = source,
        color = const(color),
        radius = const(10.dp),
        strokeColor = const(androidx.compose.ui.graphics.Color.White),
        strokeWidth = const(3.dp),
    )
}

private fun LatLng.toPosition(): Position = Position(longitude = longitude, latitude = latitude)

/** Bounding box enclosing [points], padded slightly so a near-degenerate route still frames. */
private fun boundingBoxOf(points: List<LatLng>): BoundingBox? {
    if (points.isEmpty()) return null
    var west = points.first().longitude
    var east = west
    var south = points.first().latitude
    var north = south
    for (p in points) {
        west = minOf(west, p.longitude)
        east = maxOf(east, p.longitude)
        south = minOf(south, p.latitude)
        north = maxOf(north, p.latitude)
    }
    val pad = 0.002
    return BoundingBox(west = west - pad, south = south - pad, east = east + pad, north = north + pad)
}

private fun roundToTenth(km: Double): Double = kotlin.math.round(km * 10) / 10.0

private fun Double.round4(): String {
    val v = kotlin.math.round(this * 10_000) / 10_000
    return v.toString()
}
