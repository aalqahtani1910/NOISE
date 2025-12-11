package com.example.noise

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(navController: NavController, authViewModel: AuthViewModel, studentViewModel: StudentViewModel = viewModel(), driverViewModel: DriverViewModel = viewModel()) {
    val loggedInParent by authViewModel.loggedInParent.collectAsState()
    val allStudents by studentViewModel.students.collectAsState()
    val allDrivers by driverViewModel.drivers.collectAsState()
    val isLoading by studentViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var studentForAutoPopup by remember { mutableStateOf<Student?>(null) }
    val previousStudentStates = remember { mutableMapOf<String, BoardedStatus>() }

    val myStudents = remember(loggedInParent, allStudents) {
        loggedInParent?.let {
            parent -> allStudents.filter { student -> parent.children.containsKey(student.id) }
        } ?: emptyList()
    }

    LaunchedEffect(myStudents) {
        val studentWhoWasUpdated = myStudents.find { student ->
            val previousStatus = previousStudentStates[student.id]
            previousStatus != null &&
            previousStatus != student.boardedStatus &&
            (student.boardedStatus == BoardedStatus.BOARDED || student.boardedStatus == BoardedStatus.NOT_BOARDED)
        }

        if (studentWhoWasUpdated != null) {
            studentForAutoPopup = studentWhoWasUpdated
        }

        myStudents.forEach { student ->
            if (student.id.isNotEmpty()) {
                previousStudentStates[student.id] = student.boardedStatus
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(25.35, 51.48), 10f)
    }

    val density = LocalDensity.current
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            density = density,
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true, // <-- This prevents the sheet from being fully hidden
            skipPartiallyExpanded = false
        )
    )

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val onAttendanceConfirmed: (Student, Boolean) -> Unit = { student, attending ->
        studentViewModel.updateStudent(student.copy(isAttending = attending))
        selectedStudent = null // Go back to the student list
    }

    val studentScreenContent = @Composable { modifier: Modifier ->
        StudentScreenContent(
            modifier = modifier,
            students = myStudents,
            isLoading = isLoading,
            onStudentSelected = { student -> selectedStudent = student },
            selectedStudent = selectedStudent,
            onAttendanceConfirmed = onAttendanceConfirmed,
            // Add a dismiss action for the detail view
            onDismissSelection = { selectedStudent = null },
            onLogout = {
                authViewModel.logout(context)
                navController.navigate("role_selection") {
                    popUpTo(0)
                }
            }
        )
    }

    val mapContent = @Composable { modifier: Modifier ->
        GoogleMap(
            modifier = modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            myStudents.forEach { student ->
                Marker(
                    state = MarkerState(position = LatLng(student.location.latitude, student.location.longitude)),
                    title = formatNameForDisplay(student.name),
                    snippet = "Student Location"
                )
                val driver = allDrivers.find { driver -> driver.students.containsKey(student.id) }
                driver?.let {
                    Marker(
                        state = MarkerState(position = LatLng(it.livelocation.latitude, it.livelocation.longitude)),
                        title = "Bus Location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            Row(Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(0.4f)) {
                    studentScreenContent(Modifier.fillMaxSize().padding(16.dp))
                }
                Box(modifier = Modifier.weight(0.6f)) {
                    mapContent(Modifier.fillMaxSize())
                }
            }
        } else {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetContent = { studentScreenContent(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) },
                sheetPeekHeight = 128.dp
            ) { innerPadding ->
                // The map is the main content, so it will fill the background
                Box(modifier = Modifier.padding(innerPadding)) {
                    mapContent(Modifier.fillMaxSize())
                }
            }
        }

        loggedInParent?.let {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(50), // Pill shape
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "Logged in as: ${it.name}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    studentForAutoPopup?.let { student ->
        BoardingStatusDialog(
            studentName = formatNameForDisplay(student.name),
            didBoard = student.boardedStatus == BoardedStatus.BOARDED,
            onDismiss = { studentForAutoPopup = null }
        )
    }
}

@Composable
fun StudentScreenContent(
    modifier: Modifier = Modifier,
    students: List<Student>,
    isLoading: Boolean,
    onStudentSelected: (Student) -> Unit,
    selectedStudent: Student?,
    onAttendanceConfirmed: (Student, Boolean) -> Unit,
    onDismissSelection: () -> Unit, // New parameter for dismissing the selection
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading children...")
        } else if (selectedStudent == null) {
            LazyColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                item {
                    Text(text = "My Children", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (students.isEmpty()) {
                    item {
                        Text(
                            text = "No children found for this account. Please check your parent record in the database and ensure the student IDs are correctly added to your 'children' map.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(students) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onStudentSelected(student) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = formatNameForDisplay(student.name),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            }
        } else {
            // This logic is for when a user MANUALLY clicks a student
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                when (selectedStudent.boardedStatus) {
                    BoardedStatus.DEFAULT -> {
                        Text(text = "Is ${formatNameForDisplay(selectedStudent.name)} attending today?", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(onClick = { onAttendanceConfirmed(selectedStudent, true) }) {
                                Text("Yes")
                            }
                            Button(onClick = { onAttendanceConfirmed(selectedStudent, false) }) {
                                Text("No")
                            }
                        }
                    }
                    BoardedStatus.BOARDED -> {
                        Text("${formatNameForDisplay(selectedStudent.name)} has boarded the bus.", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF006400))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismissSelection) { Text("OK") }
                    }
                    BoardedStatus.NOT_BOARDED -> {
                        Text("${formatNameForDisplay(selectedStudent.name)} did not board the bus.", style = MaterialTheme.typography.headlineSmall, color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismissSelection) { Text("OK") }
                    }
                    BoardedStatus.TRIP_COMPLETED -> {
                        Text("${formatNameForDisplay(selectedStudent.name)}'s trip is complete.", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismissSelection) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Composable
fun BoardingStatusDialog(
    studentName: String,
    didBoard: Boolean,
    onDismiss: () -> Unit
) {
    val statusText = if (didBoard) "has boarded the bus" else "did not board the bus"
    val dialogColor = if (didBoard) Color(0xFFE8F5E9) else Color(0xFFFFEBEE) // Light green or light red

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Bus Arrival Update",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Text(
                text = "The bus has arrived and $studentName $statusText.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        containerColor = dialogColor
    )
}
