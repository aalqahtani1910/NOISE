package com.example.noise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loggedInParent = MutableStateFlow<Parent?>(null)
    val loggedInParent: StateFlow<Parent?> = _loggedInParent.asStateFlow()

    fun login(username: String, password: String, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            val parent = repository.getParent(username, password)
            if (parent != null) {
                _loggedInParent.value = parent
                onLoginSuccess()
            }
        }
    }
}