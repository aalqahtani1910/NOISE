package com.example.noise

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(viewModel: StudentViewModel = viewModel()) {
    val students by viewModel.students.collectAsState()
    val attendingStudents = remember(students) { students.filter { it.isAttending } }
    val notAttendingStudents = remember(students) { students.filter { !it.isAttending } }
    var showNotAttendingDialog by remember { mutableStateOf<Student?>(null) }
    var showBoardingDialog by remember { mutableStateOf<Student?>(null) }
    val context = LocalContext.current

    val previouslyAttending = remember { mutableMapOf<String, Boolean>() }

    var simulationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(students) {
        students.forEach { student ->
            val wasAttending = previouslyAttending[student.name] ?: true // Default to true
            if (wasAttending && !student.isAttending) {
                showNotAttendingDialog = student
            }
            previouslyAttending[student.name] = student.isAttending
        }
    }

    var driverLocation by remember { mutableStateOf(GeoPoint(25.35, 51.48)) } // Default location
    val startLocation = remember { driverLocation }

    // Simulate driver moving along the route
    LaunchedEffect(simulationStarted) {
        if (simulationStarted) {
            val route = RouteCalculator.calculateFurthestToClosestRoute(driverLocation, attendingStudents)
            var currentLocation = driverLocation

            for (student in route) {
                // Move to student
                val studentStartLocation = currentLocation
                val studentEndLocation = student.location
                for (i in 1..100) {
                    val fraction = i / 100f
                    val lat = (1 - fraction) * studentStartLocation.latitude + fraction * studentEndLocation.latitude
                    val lng = (1 - fraction) * studentStartLocation.longitude + fraction * studentEndLocation.longitude
                    driverLocation = GeoPoint(lat, lng)
                    delay(50)
                }
                currentLocation = student.location

                // Wait for boarding confirmation
                showBoardingDialog = student
                while (showBoardingDialog != null) {
                    delay(100)
                }
            }

            // Move back to start
            val returnStartLocation = currentLocation
            for (i in 1..100) {
                val fraction = i / 100f
                val lat = (1 - fraction) * returnStartLocation.latitude + fraction * startLocation.latitude
                val lng = (1 - fraction) * returnStartLocation.longitude + fraction * startLocation.longitude
                driverLocation = GeoPoint(lat, lng)
                delay(50)
            }

            val updatedStudents = students.map {
                if (it.boardedStatus == BoardedStatus.BOARDED) {
                    it.copy(boardedStatus = BoardedStatus.TRIP_COMPLETED)
                } else {
                    it
                }
            }
            updatedStudents.forEach { viewModel.updateStudent(it) }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(driverLocation.latitude, driverLocation.longitude), 12f)
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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    showNotAttendingDialog?.let { student ->
        AlertDialog(
            onDismissRequest = { showNotAttendingDialog = null },
            title = { Text("Student Not Attending") },
            text = { Text("${student.name} is not attending today and has been removed from the route.") },
            confirmButton = {
                TextButton(onClick = { showNotAttendingDialog = null }) {
                    Text("OK")
                }
            }
        )
    }

    showBoardingDialog?.let { student ->
        BoardingConfirmationDialog(
            student = student,
            onConfirmation = { boarded ->
                viewModel.updateStudent(student.copy(boardedStatus = if (boarded) BoardedStatus.BOARDED else BoardedStatus.NOT_BOARDED))
                sendBoardingNotification(context, student, boarded)
                showBoardingDialog = null
            },
            onDismiss = { showBoardingDialog = null }
        )
    }

    if (isLandscape) {
        val screenWidth = configuration.screenWidthDp.dp
        var studentListWidth by remember { mutableStateOf(screenWidth / 4) }

        Row(modifier = Modifier.fillMaxSize()) {
            StudentList(
                attendingStudents = attendingStudents,
                notAttendingStudents = notAttendingStudents,
                onStartSimulation = { simulationStarted = true },
                onStudentClick = { student ->
                    showBoardingDialog = student
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .width(studentListWidth)
                    .padding(16.dp)
            )
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            studentListWidth = (studentListWidth + dragAmount.x.toDp()).coerceIn(screenWidth / 4, screenWidth / 3)
                        }
                    },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            GoogleMap(
                modifier = Modifier.weight(1f),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = LatLng(driverLocation.latitude, driverLocation.longitude)),
                    title = "Your Location"
                )
                attendingStudents.forEach { student ->
                    Marker(
                        state = MarkerState(position = LatLng(student.location.latitude, student.location.longitude)),
                        title = student.name
                    )
                }
            }
        }
    } else {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                StudentListBottomSheetContent(
                    attendingStudents = attendingStudents,
                    notAttendingStudents = notAttendingStudents,
                    onStartSimulation = { simulationStarted = true },
                    onStudentClick = { student ->
                        showBoardingDialog = student
                    }
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
                        state = MarkerState(position = LatLng(driverLocation.latitude, driverLocation.longitude)),
                        title = "Your Location"
                    )
                    attendingStudents.forEach { student ->
                        Marker(
                            state = MarkerState(position = LatLng(student.location.latitude, student.location.longitude)),
                            title = student.name
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BoardingConfirmationDialog(
    student: Student,
    onConfirmation: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Boarding") },
        text = { Text("Did ${student.name} get on the bus?") },
        confirmButton = {
            TextButton(onClick = { onConfirmation(true) }) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = { onConfirmation(false) }) {
                Text("No")
            }
        }
    )
}

@Composable
fun StudentListBottomSheetContent(
    attendingStudents: List<Student>,
    notAttendingStudents: List<Student>,
    onStartSimulation: () -> Unit,
    onStudentClick: (Student) -> Unit
) {
    StudentList(
        attendingStudents = attendingStudents,
        notAttendingStudents = notAttendingStudents,
        onStartSimulation = onStartSimulation,
        onStudentClick = onStudentClick,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun StudentList(
    attendingStudents: List<Student>,
    notAttendingStudents: List<Student>,
    onStartSimulation: () -> Unit,
    onStudentClick: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        Button(onClick = onStartSimulation, modifier = Modifier.fillMaxWidth()) {
            Text("Start Simulation")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Students List", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(attendingStudents) { student ->
                StudentListItem(student = student, isAttending = true, onStudentClick = { onStudentClick(student) })
            }
            items(notAttendingStudents) { student ->
                StudentListItem(student = student, isAttending = false, onStudentClick = { onStudentClick(student) })
            }
        }
    }
}

@Composable
fun StudentListItem(student: Student, isAttending: Boolean, onStudentClick: () -> Unit) {
    val backgroundColor = when (student.boardedStatus) {
        BoardedStatus.BOARDED -> Color.Green.copy(alpha = 0.5f)
        BoardedStatus.NOT_BOARDED -> Color.Red.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onStudentClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    textDecoration = if (isAttending) TextDecoration.None else TextDecoration.LineThrough
                )
                Text(
                    text = "Parents: ${student.parents.values.joinToString()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

private fun sendBoardingNotification(context: Context, student: Student, boarded: Boolean) {
    val message = if (boarded) {
        "${student.name} has boarded the school bus."
    } else {
        "${student.name} did not board the school bus."
    }
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
