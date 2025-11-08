package com.example.noise

import com.google.firebase.firestore.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object RouteCalculator {

    fun calculateFurthestToClosestRoute(driverLocation: GeoPoint, students: List<Student>): List<Student> {
        if (students.isEmpty()) return emptyList()

        return students.sortedByDescending { student ->
            haversineDistance(driverLocation, student.location)
        }
    }

    private fun haversineDistance(loc1: GeoPoint, loc2: GeoPoint): Double {
        val R = 6371 // Radius of the earth in km
        val latDistance = Math.toRadians(loc2.latitude - loc1.latitude)
        val lonDistance = Math.toRadians(loc2.longitude - loc1.longitude)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(loc1.latitude)) * cos(Math.toRadians(loc2.latitude)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
