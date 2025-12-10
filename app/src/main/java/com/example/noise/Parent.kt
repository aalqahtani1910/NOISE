package com.example.noise

data class Parent(
    val id: String = "",
    val name: String = "",
    val password: String = "",
    val children: Map<String, String> = emptyMap()
)