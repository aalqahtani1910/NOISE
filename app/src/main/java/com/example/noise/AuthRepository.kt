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

            if (parent?.password == password) {
                parent.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getParentById(parentId: String): Parent? {
        return try {
            val document = firestore.collection("parents").document(parentId).get().await()
            document.toObject<Parent>()?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }
}
