package com.example.noise

import android.content.Context
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

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    fun login(context: Context, parentId: String, password: String, rememberMe: Boolean, onLoginSuccess: () -> Unit) {
        if (_isLoggingIn.value) return

        viewModelScope.launch {
            _isLoggingIn.value = true
            _loginError.value = null

            val parent = repository.getParent(parentId.trim(), password.trim())
            if (parent != null) {
                if (rememberMe) {
                    SessionManager.saveSession(context, parent.id)
                }
                _loggedInParent.value = parent
                onLoginSuccess()
            } else {
                _loginError.value = "Invalid Parent ID or Password"
            }
            _isLoggingIn.value = false
        }
    }

    fun checkForSavedSession(context: Context, onLoginSuccess: () -> Unit) {
        val savedParentId = SessionManager.getSavedParentId(context)
        if (savedParentId != null) {
            viewModelScope.launch {
                _isLoggingIn.value = true
                val parent = repository.getParentById(savedParentId) // Assumes password is not needed for re-login
                if (parent != null) {
                    _loggedInParent.value = parent
                    onLoginSuccess()
                } else {
                    // Clear invalid session
                    SessionManager.clearSession(context)
                }
                _isLoggingIn.value = false
            }
        }
    }

    fun logout(context: Context) {
        SessionManager.clearSession(context)
        _loggedInParent.value = null
    }

    fun clearLoginError() {
        _loginError.value = null
    }
}