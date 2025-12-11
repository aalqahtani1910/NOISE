package com.example.noise

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "AuthRepository"

    suspend fun getParent(parentId: String, password: String): Parent? {
        return try {
            val document = firestore.collection("parents").document(parentId).get().await()
            val parent = document.toObject(Parent::class.java)

            if (parent?.password == password) {
                parent.copy(id = document.id)
            } else {
                Log.w(TAG, "Parent password does not match for ID: $parentId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting parent with ID: $parentId", e)
            null
        }
    }

    suspend fun getParentById(parentId: String): Parent? {
        return try {
            val document = firestore.collection("parents").document(parentId).get().await()
            document.toObject(Parent::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting parent by ID: $parentId", e)
            null
        }
    }

    suspend fun getDriver(driverId: String, password: String): Driver? {
        return try {
            Log.d(TAG, "Attempting to fetch driver with ID: $driverId")
            val document = firestore.collection("driver").document(driverId).get().await()

            if (!document.exists()) {
                Log.w(TAG, "Driver document does not exist for ID: $driverId")
                return null
            }

            val driver = document.toObject(Driver::class.java)
            Log.d(TAG, "Document data for $driverId: ${document.data}")
            Log.d(TAG, "Converted driver object: $driver")

            if (driver == null) {
                Log.e(TAG, "Failed to convert document to Driver object. Check for field mismatches between your Driver data class and Firestore document.")
                return null
            }

            if (driver.password == password) {
                Log.d(TAG, "Driver password matches for ID: $driverId")
                driver.copy(id = document.id)
            } else {
                Log.w(TAG, "Driver password does not match for ID: $driverId. Firestore password: '${driver.password}', Entered password: '$password'")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting driver with ID: $driverId", e)
            null
        }
    }

    suspend fun getDriverById(driverId: String): Driver? {
        return try {
            val document = firestore.collection("driver").document(driverId).get().await()
            document.toObject(Driver::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting driver by ID: $driverId", e)
            null
        }
    }
}
