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

    private val _loggedInDriver = MutableStateFlow<Driver?>(null)
    val loggedInDriver: StateFlow<Driver?> = _loggedInDriver.asStateFlow()

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
                    SessionManager.saveSession(context, parent.id, "parent")
                }
                _loggedInParent.value = parent
                onLoginSuccess()
            } else {
                _loginError.value = "Invalid Parent ID or Password"
            }
            _isLoggingIn.value = false
        }
    }

    fun driverLogin(context: Context, driverId: String, password: String, rememberMe: Boolean, onLoginSuccess: () -> Unit) {
        if (_isLoggingIn.value) return

        viewModelScope.launch {
            _isLoggingIn.value = true
            _loginError.value = null

            val driver = repository.getDriver(driverId.trim(), password.trim())
            if (driver != null) {
                if (rememberMe) {
                    SessionManager.saveSession(context, driver.id, "driver")
                }
                _loggedInDriver.value = driver
                onLoginSuccess()
            } else {
                _loginError.value = "Invalid Driver ID or Password"
            }
            _isLoggingIn.value = false
        }
    }

    fun checkForSavedSession(context: Context, onParentLoginSuccess: () -> Unit, onDriverLoginSuccess: () -> Unit) {
        val (savedId, userType) = SessionManager.getSavedSession(context)
        if (savedId != null && userType != null) {
            viewModelScope.launch {
                _isLoggingIn.value = true
                if (userType == "parent") {
                    val parent = repository.getParentById(savedId)
                    if (parent != null) {
                        _loggedInParent.value = parent
                        onParentLoginSuccess()
                    } else {
                        SessionManager.clearSession(context)
                    }
                } else if (userType == "driver") {
                    val driver = repository.getDriverById(savedId)
                    if (driver != null) {
                        _loggedInDriver.value = driver
                        onDriverLoginSuccess()
                    } else {
                        SessionManager.clearSession(context)
                    }
                }
                _isLoggingIn.value = false
            }
        }
    }

    fun logout(context: Context) {
        SessionManager.clearSession(context)
        _loggedInParent.value = null
        _loggedInDriver.value = null
    }

    fun clearLoginError() {
        _loginError.value = null
    }
}