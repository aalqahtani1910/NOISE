package com.example.noise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen() {
    var isConfirmed by remember { mutableStateOf<Boolean?>(null) }
    val userName = "User" // Placeholder for actual user name
    val estimatedArrivalTime = "10:15 AM" // Placeholder for actual arrival time

    val busLocation = LatLng(34.0522, -118.2437) // Los Angeles
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(busLocation, 12f) // Zoom level 12
    }

    val density = LocalDensity.current
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            density = density,
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false,
            skipPartiallyExpanded = false
        )
    )
    val scope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            BusStatusBottomSheetContent(
                userName = userName,
                isConfirmed = isConfirmed,
                estimatedArrivalTime = estimatedArrivalTime,
                onConfirm = { isConfirmed = true },
                onDeny = { isConfirmed = false }
            )
        },
        sheetPeekHeight = 128.dp
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = busLocation),
                    title = "Bus Location",
                    snippet = "This is a placeholder for the bus."
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
                            scaffoldState.bottomSheetState.expand()
                        } else {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Show Bus Status")
            }
        }
    }
}

@Composable
fun BusStatusBottomSheetContent(
    userName: String,
    isConfirmed: Boolean?,
    estimatedArrivalTime: String,
    onConfirm: () -> Unit,
    onDeny: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello, $userName!",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        val statusText = when (isConfirmed) {
            true -> "You have confirmed you are going."
            false -> "You have indicated you are not going."
            null -> "Are you taking the bus today?"
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isConfirmed == null) {
            Button(onClick = onConfirm) {
                Text("Yes, I'm going")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onDeny) {
                Text("No, not today")
            }
        } else {
            Button(onClick = { /* TODO: Allow changing decision */ }) {
                Text("Change Decision")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Estimated bus arrival time: $estimatedArrivalTime",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
