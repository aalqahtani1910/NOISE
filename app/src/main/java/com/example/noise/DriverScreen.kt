package com.example.noise

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
fun DriverScreen(navController: NavController, authViewModel: AuthViewModel, studentViewModel: StudentViewModel = viewModel(), driverViewModel: DriverViewModel = viewModel()) {
    val loggedInDriver by authViewModel.loggedInDriver.collectAsState()

    loggedInDriver?.let { driver ->
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Welcome, ${driver.name}!",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            DriverMapAndStudentList(driver, studentViewModel, driverViewModel) {
                authViewModel.logout(navController.context)
                navController.navigate("role_selection") {
                    popUpTo(0)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverMapAndStudentList(driver: Driver, studentViewModel: StudentViewModel, driverViewModel: DriverViewModel, onLogout: () -> Unit) {
    val allStudents by studentViewModel.students.collectAsState()
    val context = LocalContext.current

    val myStudents = remember(driver, allStudents) {
        allStudents.filter { student -> driver.students.containsKey(student.id) }
    }

    var showNotAttendingDialog by remember { mutableStateOf<Student?>(null) }
    var showBoardingDialog by remember { mutableStateOf<Student?>(null) }

    val previouslyAttending = remember { mutableMapOf<String, Boolean>() }

    var simulationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(myStudents) {
        myStudents.forEach { student ->
            val wasAttending = previouslyAttending[student.id] ?: true // Use unique ID as key
            if (wasAttending && !student.isAttending) {
                showNotAttendingDialog = student
            }
            previouslyAttending[student.id] = student.isAttending
        }
    }

    var driverLocation by remember { mutableStateOf(driver.startlocation) } 

    // Simulate driver moving along the route
    LaunchedEffect(simulationStarted) {
        if (simulationStarted) {
            val attendingStudentsOnStart = studentViewModel.students.value.filter { s -> driver.students.containsKey(s.id) && s.isAttending }
            val route = RouteCalculator.calculateFurthestToClosestRoute(driverLocation, attendingStudentsOnStart)
            var currentLocation = driverLocation

            for (studentInRoute in route) {
                val upToDateStudent = studentViewModel.students.value.find { it.id == studentInRoute.id }

                if (upToDateStudent != null && upToDateStudent.isAttending) {
                    // Move to student
                    val studentStartLocation = currentLocation
                    val studentEndLocation = upToDateStudent.location
                    for (i in 1..100) {
                        val fraction = i / 100f
                        val lat = (1 - fraction) * studentStartLocation.latitude + fraction * studentEndLocation.latitude
                        val lng = (1 - fraction) * studentStartLocation.longitude + fraction * studentEndLocation.longitude
                        driverLocation = GeoPoint(lat, lng)
                        driverViewModel.updateDriverLocation(driver.id, driverLocation)
                        delay(50)
                    }
                    currentLocation = upToDateStudent.location

                    // Wait for boarding confirmation
                    showBoardingDialog = upToDateStudent
                    while (showBoardingDialog != null) {
                        delay(100)
                    }
                }
            }

            // Move back to start
            val returnStartLocation = currentLocation
            for (i in 1..100) {
                val fraction = i / 100f
                val lat = (1 - fraction) * returnStartLocation.latitude + fraction * driver.startlocation.latitude
                val lng = (1 - fraction) * returnStartLocation.longitude + fraction * driver.startlocation.longitude
                driverLocation = GeoPoint(lat, lng)
                driverViewModel.updateDriverLocation(driver.id, driverLocation)
                delay(50)
            }

            val updatedStudents = myStudents.map {
                if (it.boardedStatus == BoardedStatus.BOARDED) {
                    it.copy(boardedStatus = BoardedStatus.TRIP_COMPLETED)
                } else {
                    it
                }
            }
            updatedStudents.forEach { studentViewModel.updateStudent(it) }
            simulationStarted = false // Reset simulation state
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

    val onReset = {
        myStudents.forEach { student ->
            studentViewModel.updateStudent(
                student.copy(
                    boardedStatus = BoardedStatus.DEFAULT,
                    isAttending = true
                )
            )
        }
        driverLocation = driver.startlocation
        simulationStarted = false
    }

    showNotAttendingDialog?.let { student ->
        AlertDialog(
            onDismissRequest = { showNotAttendingDialog = null },
            title = { Text("Student Not Attending") },
            text = { Text("${formatNameForDisplay(student.name)} is not attending today and has been removed from the route.") },
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
                studentViewModel.updateStudent(student.copy(boardedStatus = if (boarded) BoardedStatus.BOARDED else BoardedStatus.NOT_BOARDED))
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
                students = myStudents,
                viewModel = studentViewModel,
                onStartSimulation = { simulationStarted = true },
                onReset = onReset,
                simulationInProgress = simulationStarted,
                onStudentClick = { student ->
                    showBoardingDialog = student
                },
                onLogout = onLogout,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(studentListWidth)
                    .padding(16.dp)
            )
            HorizontalDivider(
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
                    title = "Your Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
                myStudents.forEach { student ->
                    Marker(
                        state = MarkerState(position = LatLng(student.location.latitude, student.location.longitude)),
                        title = formatNameForDisplay(student.name),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        alpha = if (student.isAttending) 1.0f else 0.5f
                    )
                }
            }
        }
    } else {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                StudentListBottomSheetContent(
                    students = myStudents,
                    viewModel = studentViewModel,
                    onStartSimulation = { simulationStarted = true },
                    onReset = onReset,
                    simulationInProgress = simulationStarted,
                    onStudentClick = { student ->
                        showBoardingDialog = student
                    },
                    onLogout = onLogout
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
                        title = "Your Location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                    myStudents.forEach { student ->
                        Marker(
                            state = MarkerState(position = LatLng(student.location.latitude, student.location.longitude)),
                            title = formatNameForDisplay(student.name),
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                            alpha = if (student.isAttending) 1.0f else 0.5f
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
        text = { Text("Did ${formatNameForDisplay(student.name)} get on the bus?") },
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
    students: List<Student>,
    viewModel: StudentViewModel,
    onStartSimulation: () -> Unit,
    onReset: () -> Unit,
    simulationInProgress: Boolean,
    onStudentClick: (Student) -> Unit,
    onLogout: () -> Unit
) {
    StudentList(
        students = students,
        viewModel = viewModel,
        onStartSimulation = onStartSimulation,
        onReset = onReset,
        simulationInProgress = simulationInProgress,
        onStudentClick = onStudentClick,
        onLogout = onLogout,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun StudentList(
    students: List<Student>,
    viewModel: StudentViewModel,
    onStartSimulation: () -> Unit,
    onReset: () -> Unit,
    simulationInProgress: Boolean,
    onStudentClick: (Student) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attendingStudents = students.filter { it.isAttending }
    val notAttendingStudents = students.filter { !it.isAttending }

    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onStartSimulation, enabled = !simulationInProgress) {
                    Text("Start Simulation")
                }
                Button(onClick = onReset) {
                    Text("Reset Trip")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Students List", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(attendingStudents) { student ->
            StudentListItem(student = student, isAttending = true, onStudentClick = { onStudentClick(student) })
        }
        items(notAttendingStudents) { student ->
            StudentListItem(student = student, isAttending = false, onStudentClick = { onStudentClick(student) })
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLogout) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun StudentListItem(student: Student, isAttending: Boolean, onStudentClick: () -> Unit) {
    val backgroundColor = if (isAttending) {
        Color.Green.copy(alpha = 0.25f)
    } else {
        Color.Red.copy(alpha = 0.25f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStudentClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatNameForDisplay(student.name),
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
        "${formatNameForDisplay(student.name)} has boarded the school bus."
    } else {
        "${formatNameForDisplay(student.name)} did not board the school bus."
    }
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
