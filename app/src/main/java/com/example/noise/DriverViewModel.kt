package com.example.noise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DriverViewModel : ViewModel() {

    private val repository = DriverRepository()

    private val _drivers = MutableStateFlow<List<Driver>>(emptyList())
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllDrivers().catch { 
                // Handle error
            }.collect { 
                _drivers.value = it
            }
        }
    }

    fun updateDriverLocation(driverId: String, location: GeoPoint) {
        viewModelScope.launch {
            repository.updateDriverLocation(driverId, location)
        }
    }
}
