package com.example.noise

import com.google.firebase.firestore.GeoPoint

data class Driver(
    val id: String = "",
    val name: String = "",
    val password: String = "",
    val startlocation: GeoPoint = GeoPoint(0.0, 0.0),
    val live_location: GeoPoint = GeoPoint(0.0, 0.0),
    val students: Map<String, String> = emptyMap()
)
