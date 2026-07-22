package ph.jeepfare.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ph.jeepfare.domain.LatLng

/**
 * Minimal client for the public OSRM demo router
 * (https://router.project-osrm.org). Returns driving distance between
 * two points; callers should fall back to a straight-line estimate on failure.
 */
class OsrmClient(
    private val httpClient: HttpClient = defaultHttpClient(),
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://router.project-osrm.org"
        const val TIMEOUT_MS = 10_000L

        private fun defaultHttpClient(): HttpClient = HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT_MS
                connectTimeoutMillis = TIMEOUT_MS
            }
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Driving route between [from] and [to] — distance plus the road-following
     * polyline used to animate the path on the map. Callers should fall back to a
     * straight-line estimate on failure.
     */
    suspend fun route(from: LatLng, to: LatLng): Result<RouteResult> = try {
        // OSRM expects lon,lat ordering; full geometry as GeoJSON for the polyline.
        val coords = "${from.longitude},${from.latitude};${to.longitude},${to.latitude}"
        val response = httpClient.get("$baseUrl/route/v1/driving/$coords?overview=full&geometries=geojson")
        check(response.status.isSuccess()) { "OSRM returned ${response.status}" }
        val body = json.decodeFromString<OsrmRouteResponse>(response.bodyAsText())
        check(body.code == "Ok") { "OSRM error: ${body.code}" }
        val route = body.routes.firstOrNull() ?: error("OSRM returned no routes")
        // GeoJSON coordinates are [longitude, latitude].
        val geometry = route.geometry?.coordinates.orEmpty().mapNotNull { c ->
            if (c.size >= 2) LatLng(latitude = c[1], longitude = c[0]) else null
        }
        Result.success(RouteResult(distanceKm = route.distance / 1000.0, geometry = geometry))
    } catch (e: CancellationException) {
        // Never swallow coroutine cancellation, or a cancelled request would
        // publish a stale result through the failure branch.
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/** A driving route: total distance and the road-following polyline (may be empty). */
data class RouteResult(
    val distanceKm: Double,
    val geometry: List<LatLng>,
)

@Serializable
internal data class OsrmRouteResponse(
    val code: String,
    val routes: List<OsrmRoute> = emptyList(),
)

@Serializable
internal data class OsrmRoute(
    /** Route distance in meters. */
    val distance: Double,
    val geometry: OsrmGeometry? = null,
)

@Serializable
internal data class OsrmGeometry(
    /** GeoJSON LineString coordinates, each [longitude, latitude]. */
    val coordinates: List<List<Double>> = emptyList(),
)
