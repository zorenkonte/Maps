package ph.jeepfare.domain

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A geographic coordinate, independent of any map SDK types. */
data class LatLng(val latitude: Double, val longitude: Double)

private const val EARTH_RADIUS_KM = 6371.0088

/** Factor applied to straight-line distance to approximate road distance. */
const val ROAD_DISTANCE_FACTOR = 1.3

/** Great-circle distance between two coordinates in kilometers. */
fun haversineKm(a: LatLng, b: LatLng): Double {
    val dLat = (b.latitude - a.latitude).toRadians()
    val dLon = (b.longitude - a.longitude).toRadians()
    val lat1 = a.latitude.toRadians()
    val lat2 = b.latitude.toRadians()

    val h = sin(dLat / 2) * sin(dLat / 2) +
        cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
    return 2 * EARTH_RADIUS_KM * atan2(sqrt(h), sqrt(1 - h))
}

/** Rough road-distance estimate when no routing service is reachable. */
fun estimatedRoadKm(a: LatLng, b: LatLng): Double = haversineKm(a, b) * ROAD_DISTANCE_FACTOR

private fun Double.toRadians(): Double = this * PI / 180.0
