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

    @Test
    fun parsesDistanceFromRoute() = runTest {
        val client = clientReturning("""{"code":"Ok","routes":[{"distance":10500.0,"duration":1200.0}]}""")
        val result = client.routeDistanceKm(from, to)
        assertEquals(10.5, result.getOrThrow(), 1e-9)
    }

    @Test
    fun requestUsesLonLatOrder() = runTest {
        var requestedUrl = ""
        val engine = MockEngine { request ->
            requestedUrl = request.url.toString()
            respond(
                content = """{"code":"Ok","routes":[{"distance":1000.0}]}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        OsrmClient(HttpClient(engine)).routeDistanceKm(from, to)
        assertTrue(
            requestedUrl.contains("120.9842,14.5995;121.0493,14.651"),
            "expected lon,lat;lon,lat in $requestedUrl",
        )
    }

    @Test
    fun failsOnOsrmErrorCode() = runTest {
        val client = clientReturning("""{"code":"NoRoute","routes":[]}""")
        assertTrue(client.routeDistanceKm(from, to).isFailure)
    }

    @Test
    fun failsOnEmptyRoutes() = runTest {
        val client = clientReturning("""{"code":"Ok","routes":[]}""")
        assertTrue(client.routeDistanceKm(from, to).isFailure)
    }

    @Test
    fun failsOnHttpError() = runTest {
        val client = clientReturning("""server error""", HttpStatusCode.InternalServerError)
        assertTrue(client.routeDistanceKm(from, to).isFailure)
    }

    @Test
    fun failsOnMalformedJson() = runTest {
        val client = clientReturning("""not json at all""")
        assertTrue(client.routeDistanceKm(from, to).isFailure)
    }
}
