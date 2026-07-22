package ph.jeepfare

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import ph.jeepfare.data.OsrmClient
import ph.jeepfare.domain.LatLng

class OsrmClientTest {

    private val from = LatLng(14.5995, 120.9842)
    private val to = LatLng(14.6510, 121.0493)

    private fun clientReturning(body: String, status: HttpStatusCode = HttpStatusCode.OK): OsrmClient {
        val engine = MockEngine { _ ->
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        return OsrmClient(HttpClient(engine))
    }

    private val geometryBody = """
        {"code":"Ok","routes":[{"distance":10500.0,"duration":1200.0,
        "geometry":{"type":"LineString","coordinates":[[120.9842,14.5995],[121.0,14.62],[121.0493,14.651]]}}]}
    """.trimIndent()

    @Test
    fun parsesDistanceAndGeometry() = runTest {
        val result = clientReturning(geometryBody).route(from, to).getOrThrow()
        assertEquals(10.5, result.distanceKm, 1e-9)
        assertEquals(3, result.geometry.size)
        // GeoJSON is [lon,lat]; the client must flip it to LatLng.
        assertEquals(14.5995, result.geometry.first().latitude, 1e-9)
        assertEquals(120.9842, result.geometry.first().longitude, 1e-9)
        assertEquals(14.651, result.geometry.last().latitude, 1e-9)
    }

    @Test
    fun succeedsWithEmptyGeometryWhenAbsent() = runTest {
        val result = clientReturning("""{"code":"Ok","routes":[{"distance":1000.0}]}""").route(from, to).getOrThrow()
        assertEquals(1.0, result.distanceKm, 1e-9)
        assertTrue(result.geometry.isEmpty())
    }

    @Test
    fun requestAsksForFullGeojsonGeometryInLonLatOrder() = runTest {
        var requestedUrl = ""
        val engine = MockEngine { request ->
            requestedUrl = request.url.toString()
            respond(
                content = """{"code":"Ok","routes":[{"distance":1000.0}]}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        OsrmClient(HttpClient(engine)).route(from, to)
        assertTrue(requestedUrl.contains("120.9842,14.5995;121.0493,14.651"), "lon,lat order in $requestedUrl")
        assertTrue(requestedUrl.contains("overview=full"), "full geometry in $requestedUrl")
        assertTrue(requestedUrl.contains("geometries=geojson"), "geojson geometry in $requestedUrl")
    }

    @Test
    fun failsOnOsrmErrorCode() = runTest {
        assertTrue(clientReturning("""{"code":"NoRoute","routes":[]}""").route(from, to).isFailure)
    }

    @Test
    fun failsOnEmptyRoutes() = runTest {
        assertTrue(clientReturning("""{"code":"Ok","routes":[]}""").route(from, to).isFailure)
    }

    @Test
    fun failsOnHttpError() = runTest {
        assertTrue(clientReturning("""server error""", HttpStatusCode.InternalServerError).route(from, to).isFailure)
    }

    @Test
    fun failsOnMalformedJson() = runTest {
        assertTrue(clientReturning("""not json at all""").route(from, to).isFailure)
    }
}
