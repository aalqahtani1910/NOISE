package com.example.noise

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DriverRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getAllDrivers(): Flow<List<Driver>> = callbackFlow {
        val listener = firestore.collection("driver").addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val drivers = snapshot?.documents?.mapNotNull {
                it.toObject(Driver::class.java)?.copy(id = it.id)
            } ?: emptyList()
            trySend(drivers)
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateDriverLocation(driverId: String, location: GeoPoint) {
        firestore.collection("driver").document(driverId).update("livelocation", location).await()
    }
}
