package com.example.noise

import android.content.res.Configuration
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(authViewModel: AuthViewModel, studentViewModel: StudentViewModel = viewModel()) {
    val loggedInParent by authViewModel.loggedInParent.collectAsState()
    val allStudents by studentViewModel.students.collectAsState()

    val myStudents = remember(loggedInParent, allStudents) {
        loggedInParent?.let { parent ->
            allStudents.filter { student -> student.parents.containsKey(parent.id) }
        } ?: emptyList()
    }

    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(25.35, 51.48), 10f)
    }

    val density = LocalDensity.current
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = androidx.compose.material3.SheetState(
            density = density,
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false,
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
            onStudentSelected = { student -> selectedStudent = student },
            selectedStudent = selectedStudent,
            onAttendanceConfirmed = onAttendanceConfirmed,
            onDismissDialog = { selectedStudent = null }
        )
    }

    val mapContent = @Composable { modifier: Modifier ->
        GoogleMap(
            modifier = modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            selectedStudent?.let {
                Marker(
                    state = MarkerState(position = LatLng(it.location.latitude, it.location.longitude)),
                    title = it.name,
                    snippet = "Student Location"
                )
            }
        }
    }

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
            sheetContent = { studentScreenContent(Modifier.padding(16.dp)) },
            sheetPeekHeight = 128.dp
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                mapContent(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun StudentScreenContent(
    modifier: Modifier = Modifier,
    students: List<Student>,
    onStudentSelected: (Student) -> Unit,
    selectedStudent: Student?,
    onAttendanceConfirmed: (Student, Boolean) -> Unit,
    onDismissDialog: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (selectedStudent == null) {
            Text(text = "My Children", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(students) { student ->
                    Text(
                        text = student.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStudentSelected(student) }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        } else {
            when (selectedStudent.boardedStatus) {
                BoardedStatus.DEFAULT -> {
                    Text(text = "Is ${selectedStudent.name} attending today?", style = MaterialTheme.typography.headlineMedium)
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
                    BoardingStatusDialog(studentName = selectedStudent.name, didBoard = true, onDismiss = onDismissDialog)
                }
                BoardedStatus.NOT_BOARDED -> {
                    BoardingStatusDialog(studentName = selectedStudent.name, didBoard = false, onDismiss = onDismissDialog)
                }
                BoardedStatus.TRIP_COMPLETED -> {
                    // Handled by the LaunchedEffect in UserMainScreen
                }
            }
        }
    }
}

@Composable
fun BoardingStatusDialog(studentName: String, didBoard: Boolean, onDismiss: () -> Unit) {
    val statusText = if (didBoard) "has boarded the bus" else "did not board the bus"
    val dialogColor = if (didBoard) Color.Green.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Bus Arrival Update",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black
            )
        },
        text = {
            Text(
                text = "The bus has arrived and $studentName $statusText.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
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
