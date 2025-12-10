package com.example.noise

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StudentRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "StudentRepository"

    fun getStudents(): Flow<List<Student>> = callbackFlow {
        val subscription = firestore.collection("students")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching students", error)
                    trySend(emptyList()).isSuccess // On error, emit an empty list.
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val students = snapshot.documents.mapNotNull { document ->
                        val student = document.toObject(Student::class.java)
                        // Overwrite the (empty) id field with the actual document ID.
                        student?.copy(id = document.id)
                    }
                    trySend(students).isSuccess // Send the latest student list to the flow.
                } else {
                    Log.d(TAG, "Student collection is empty.")
                    // Snapshot is null or empty, send an empty list.
                    trySend(emptyList()).isSuccess
                }
            }

        // When the flow is cancelled, remove the listener.
        awaitClose { subscription.remove() }
    }

    suspend fun updateStudent(student: Student) {
        try {
            // The student.id now correctly holds the document ID for updates.
            firestore.collection("students").document(student.id).set(student).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating student: ${student.id}", e)
            // Handle update failure. We are no longer updating mock data.
            // You might want to throw the exception to notify the caller.
            throw e
        }
    }
}
