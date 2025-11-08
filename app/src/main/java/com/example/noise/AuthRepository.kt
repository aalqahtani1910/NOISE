package com.example.noise

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getParent(parentId: String, password: String): Parent? {
        return try {
            val document = firestore.collection("parents").document(parentId).get().await()
            val parent = document.toObject<Parent>()

            // First, get the parent document by ID.
            // Then, verify if the password matches.
            if (parent?.password == password) {
                parent
            } else {
                null
            }
        } catch (e: Exception) {
            // If the document doesn't exist or another error occurs
            null
        }
    }
}
