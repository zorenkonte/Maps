package ph.jeepfare

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.jeepfare.domain.LatLng
import ph.jeepfare.domain.ROAD_DISTANCE_FACTOR
import ph.jeepfare.domain.estimatedRoadKm
import ph.jeepfare.domain.haversineKm

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
}
