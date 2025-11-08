package com.example.noise

import com.google.firebase.firestore.GeoPoint

enum class BoardedStatus {
    DEFAULT,
    BOARDED,
    NOT_BOARDED,
    TRIP_COMPLETED
}

data class Student(
    val id: String = "",
    val name: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val parents: Map<String, String> = emptyMap(),
    val isAttending: Boolean = true,
    val boardedStatus: BoardedStatus = BoardedStatus.DEFAULT
)