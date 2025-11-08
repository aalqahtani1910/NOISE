package com.example.noise

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class StudentRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getStudents(): Flow<List<Student>> = flow {
        try {
            val snapshot = firestore.collection("students").get().await()
            val students = snapshot.toObjects<Student>()
            if (students.isNotEmpty()) {
                emit(students)
            } else {
                emit(FallbackData.getStaticStudents())
            }
        } catch (e: Exception) {
            // Fallback to mock data if Firestore fails
            emit(FallbackData.getStaticStudents())
        }
    }

    suspend fun updateStudent(student: Student) {
        try {
            firestore.collection("students").document(student.id).set(student).await()
        } catch (e: Exception) {
            // Handle update failure, perhaps by updating mock data for the session
            FallbackData.updateStudent(student)
        }
    }
}