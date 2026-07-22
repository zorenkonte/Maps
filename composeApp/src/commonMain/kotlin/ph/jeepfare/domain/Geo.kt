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

/**
 * Returns the leading portion of [points] covering [fraction] (0..1) of the
 * polyline's total length, interpolating the final tip within its segment.
 * Used to animate a route drawing on from its start. Always returns at least
 * two points once [fraction] > 0 so it forms a drawable line.
 */
fun partialPolyline(points: List<LatLng>, fraction: Float): List<LatLng> {
    if (points.size < 2) return points
    val f = fraction.coerceIn(0f, 1f)
    if (f <= 0f) return listOf(points.first())
    if (f >= 1f) return points

    val segments = points.zipWithNext { a, b -> haversineKm(a, b) }
    val total = segments.sum()
    if (total <= 0.0) return points
    val target = total * f

    val result = mutableListOf(points.first())
    var acc = 0.0
    for (i in segments.indices) {
        val len = segments[i]
        if (acc + len < target || len <= 0.0) {
            result.add(points[i + 1])
            acc += len
        } else {
            val t = ((target - acc) / len).coerceIn(0.0, 1.0)
            val a = points[i]
            val b = points[i + 1]
            result.add(
                LatLng(
                    latitude = a.latitude + (b.latitude - a.latitude) * t,
                    longitude = a.longitude + (b.longitude - a.longitude) * t,
                ),
            )
            break
        }
    }
    return result
}

private fun Double.toRadians(): Double = this * PI / 180.0
