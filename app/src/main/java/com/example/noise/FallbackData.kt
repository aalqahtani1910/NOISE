package com.example.noise

import com.google.firebase.firestore.GeoPoint

object FallbackData {
    private val students = mutableListOf(
        Student(
            id = "mock_student_a",
            name = "Student A",
            location = GeoPoint(25.3727, 51.5400),
            parents = mapOf("mock_parent_1" to "Parent 1")
        ),
        Student(
            id = "mock_student_b",
            name = "Student B",
            location = GeoPoint(25.3250, 51.5270),
            parents = mapOf("mock_parent_2" to "Parent 2", "mock_parent_3" to "Parent 3")
        ),
        Student(
            id = "mock_student_c",
            name = "Student C",
            location = GeoPoint(25.2600, 51.4600),
            parents = mapOf("mock_parent_4" to "Parent 4")
        ),
    )

    fun getStaticStudents(): List<Student> = students

    fun updateStudent(updatedStudent: Student) {
        val index = students.indexOfFirst { it.id == updatedStudent.id }
        if (index != -1) {
            students[index] = updatedStudent
        }
    }
}