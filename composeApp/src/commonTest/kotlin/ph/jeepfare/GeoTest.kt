package ph.jeepfare

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.jeepfare.domain.LatLng
import ph.jeepfare.domain.ROAD_DISTANCE_FACTOR
import ph.jeepfare.domain.estimatedRoadKm
import ph.jeepfare.domain.haversineKm
import ph.jeepfare.domain.partialPolyline

class GeoTest {

    private val manilaCityHall = LatLng(14.5995, 120.9842)
    private val quezonMemorialCircle = LatLng(14.6510, 121.0493)

    @Test
    fun samePointIsZero() {
        assertEquals(0.0, haversineKm(manilaCityHall, manilaCityHall), 1e-9)
    }

    @Test
    fun knownDistanceWithinTolerance() {
        // Manila City Hall to Quezon Memorial Circle is roughly 9 km as the crow flies.
        val km = haversineKm(manilaCityHall, quezonMemorialCircle)
        assertTrue(abs(km - 9.0) < 1.0, "expected ~9 km, got $km")
    }

    @Test
    fun isSymmetric() {
        assertEquals(
            haversineKm(manilaCityHall, quezonMemorialCircle),
            haversineKm(quezonMemorialCircle, manilaCityHall),
            1e-9,
        )
    }

    @Test
    fun roadEstimateScalesStraightLine() {
        val straight = haversineKm(manilaCityHall, quezonMemorialCircle)
        assertEquals(straight * ROAD_DISTANCE_FACTOR, estimatedRoadKm(manilaCityHall, quezonMemorialCircle), 1e-9)
    }

    // Simple 3-point polyline along the equator/meridian for predictable lengths.
    private val line = listOf(
        LatLng(0.0, 0.0),
        LatLng(0.0, 1.0),
        LatLng(0.0, 2.0),
    )

    @Test
    fun partialAtZeroIsJustStart() {
        assertEquals(listOf(line.first()), partialPolyline(line, 0f))
    }

    @Test
    fun partialAtOneIsWholeLine() {
        assertEquals(line, partialPolyline(line, 1f))
    }

    @Test
    fun partialAtHalfReachesMidpoint() {
        val half = partialPolyline(line, 0.5f)
        // Half the total length lands exactly on the middle vertex (1.0 lon).
        assertEquals(0.0, half.last().latitude, 1e-6)
        assertEquals(1.0, half.last().longitude, 1e-6)
    }

    @Test
    fun partialInterpolatesWithinASegment() {
        val quarter = partialPolyline(line, 0.25f)
        // Quarter of the 2-unit line ends halfway through the first segment.
        assertEquals(0.5, quarter.last().longitude, 1e-6)
    }

    @Test
    fun partialClampsAndHandlesDegenerateInput() {
        assertEquals(line, partialPolyline(line, 2f))
        assertEquals(listOf(line.first()), partialPolyline(line, -1f))
        val single = listOf(LatLng(1.0, 1.0))
        assertEquals(single, partialPolyline(single, 0.5f))
    }
}
