package com.example.doctorappointment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class PatientAppoitment : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PatientAppointmentScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAppointmentScreen() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val patientId = auth.currentUser?.uid ?: ""
    var appointments by remember { mutableStateOf(listOf<Appointment>()) }

    LaunchedEffect(Unit) {
        db.collection("appointments")
            .whereEqualTo("patientId", patientId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = mutableListOf<Appointment>()
                for (doc in querySnapshot.documents) {
                    val doctorId = doc.getString("doctorId") ?: continue
                    val appointmentTime =
                        doc.getTimestamp("appointmentTime")?.toDate() ?: continue
                    val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(appointmentTime)

                    db.collection("doctors").document(doctorId).get()
                        .addOnSuccessListener { doctorDoc ->
                            val doctorName = doctorDoc.getString("name") ?: "Unknown Doctor"
                            list.add(
                                Appointment(
                                    doctorId,
                                    patientId,
                                    Timestamp(appointmentTime.time,0),
                                    doc.getDouble("distance")?:0.0
                                )
                            )
                            appointments = list.toList()

                            // Schedule notification 1 hour before
                            scheduleAppointmentNotification(
                                context,
                                appointmentTime.time - 60 * 60 * 1000,
                                "You have an appointment with Dr. $doctorName at ${
                                SimpleDateFormat(
                                    "hh:mm a, dd MMM",
                                    Locale.getDefault()
                                ).format(appointmentTime)
                                }"
                            )
                        }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Appointments") }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }

    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(appointments.size) { index ->
                val appt = appointments[index]
                AppointmentCard(appt)
            }
        }
    }
}

@Composable
fun AppointmentCard(appt: Appointment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Doctor: ${appt.doctorId}", style = MaterialTheme.typography.titleMedium)
            Text("Time: ${SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault()).format(appt.appointmentTime)}")
        }
    }
}

data class Appointment(
    val doctorId: String = "",
    val patientId: String = "",
    val appointmentTime: Timestamp = Timestamp.now(),
    val distance: Double = 0.0
)

fun scheduleAppointmentNotification(context: Context, triggerTimeMillis: Long, message: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            return
        }
    }

    val intent = Intent(context, AppointmentReminderReceiver::class.java).apply {
        putExtra("message", message)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        triggerTimeMillis.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMillis,
            pendingIntent
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

// @Preview(showBackground = true)
// @Composable
// fun GreetingPreview2() {
//    DoctorAppointmentTheme {
//        Greeting4("Android")
//    }
// }
