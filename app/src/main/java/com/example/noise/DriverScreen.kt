package com.example.noise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen() {
    val students = remember {
        listOf(
            Student("Student A", LatLng(25.3727, 51.5400)),
            Student("Student B", LatLng(25.3250, 51.5270)),
            Student("Student C", LatLng(25.2600, 51.4600)),
        )
    }

    val driverLocation = LatLng(25.35, 51.48) // Placeholder for driver's location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(driverLocation, 12f)
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

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            StudentListBottomSheetContent(students = students, onNavigateToStudent = { /* Handle navigation */ })
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
                    state = MarkerState(position = driverLocation),
                    title = "Your Location"
                )
                students.forEach { student ->
                    Marker(
                        state = MarkerState(position = student.location),
                        title = student.name
                    )
                }
            }
        }
    }
}

@Composable
fun StudentListBottomSheetContent(
    students: List<Student>,
    onNavigateToStudent: (Student) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Students List", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(students) { student ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = student.name, modifier = Modifier.weight(1f))
                    Button(onClick = { onNavigateToStudent(student) }) {
                        Text(text = "Navigate")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
