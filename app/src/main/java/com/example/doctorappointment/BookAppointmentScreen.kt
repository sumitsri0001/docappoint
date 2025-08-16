package com.example.doctorappointment

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookAppointmentDialog(
    doctorId: String,
    navController: NavHostController,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    var selectedTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var showFailure by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Book Appointment")
        },
        text = {
            Column {
                Text("Choose Appointment Time", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    // Simulate time picker logic (current time + 1 hr)
                    selectedTime = LocalDateTime.now().plusHours(1)
                }) {
                    Text("Pick Time")
                }

                selectedTime?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Selected: ${it.format(formatter)}")
                }

                if (showSuccess) {
                    Toast.makeText(context, "Appointment booked!", Toast.LENGTH_SHORT).show()
                }

                if (showFailure) {
                    Toast.makeText(context, "Time slot already taken!", Toast.LENGTH_SHORT).show()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val time = selectedTime ?: return@Button
                    val date = Date.from(time.atZone(ZoneId.systemDefault()).toInstant())
                    val timestamp = Timestamp(date)

                    BookAppointmentIfNoConflict(
                        doctorId = doctorId,
                        context = context,
                        patientId = patientId,
                        appointmentTime = timestamp,
                        distance = 0.0, // You can customize this
                        onSuccess = {
                            showSuccess = true
                            val reminderIntent = Intent(context, AppointmentReminderReceiver::class.java).apply {
                                putExtra("title", "Appointment with Doctor")
                                putExtra("doctorLat", 28.6139)  // Replace with actual doctor latitude
                                putExtra("doctorLng", 77.2090)  // Replace with actual doctor longitude
                                putExtra("patientLat", 28.7041) // Replace with actual patient latitude
                                putExtra("patientLng", 77.1025) // Replace with actual patient longitude
                            }

                            val pendingIntent = PendingIntent.getBroadcast(
                                context,
                                System.currentTimeMillis().toInt(),
                                reminderIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

                            val notifyTimeMillis = date.time - 60 * 60 * 1000  // 1 hour before appointment
                            alarmManager.setExactAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                notifyTimeMillis,
                                pendingIntent
                            )
                            navController.navigate("patient_appointments")
                            onDismissRequest()
                        },
                        onFailure = {
                            showFailure = true
                        }
                    )
                },
                enabled = selectedTime != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
