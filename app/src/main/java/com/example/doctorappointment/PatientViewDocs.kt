// File: PatientViewDocs.kt

package com.example.doctorappointment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.doctorappointment.ui.theme.DoctorAppointmentTheme
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class PatientViewDocs : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoctorAppointmentTheme {
                val navController = rememberNavController()
                Surface {
                    AllDoctorsScreen(navController)
                }
            }
        }
    }

}


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDoctorsScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var doctors by remember { mutableStateOf<List<DoctorDta>>(emptyList()) }
    var patientLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var distances by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var selectedSortOption by remember { mutableStateOf(SortOption.DISTANCE) }
    var selectedSpecialtyFilter by remember { mutableStateOf<String?>(null) }
    val specialties = listOf("Cardiologist", "Dermatologist", "Orthopedist", "Gynaecologist")

    LaunchedEffect(Unit) {
        db.collection("doctors").get().addOnSuccessListener { result ->
            doctors = result.documents.mapNotNull {
                it.toObject(DoctorDta::class.java)?.copy(id = it.id)
            }
        }

        if (isLocationPermissionGranted(context)) {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        patientLocation = Pair(it.latitude, it.longitude)
                    }
                }
        } else if (context is Activity) {
            requestLocationPermission(context)
        }
    }

    LaunchedEffect(patientLocation, doctors) {
        patientLocation?.let { (lat, lon) ->
            val newDistances = doctors.associate { doctor ->
                val dist = haversine(lat, lon, doctor.geometry.latitude, doctor.geometry.longitude)
                doctor.id to dist
            }
            distances = newDistances
        }
    }


    fun sortDoctors(doctors: List<DoctorDta>): List<DoctorDta> {
        return when (selectedSortOption) {
            SortOption.DISTANCE -> doctors.sortedBy { distances[it.id] ?: Double.MAX_VALUE }
            SortOption.RATING -> doctors.sortedByDescending { it.rating }
            SortOption.EXPERIENCE -> doctors.sortedByDescending { it.experience }
        }
    }

    fun filterDoctors(doctors: List<DoctorDta>, selectedSpecialtyFilter: String?): List<DoctorDta> {
        return if (!selectedSpecialtyFilter.isNullOrEmpty()) {
            doctors.filter { it.specialty.equals(selectedSpecialtyFilter, ignoreCase = true) }
        } else {
            doctors
        }
    }

    val displayedDoctors = sortDoctors(filterDoctors(doctors,selectedSpecialtyFilter))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Doctors") })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            var sortExpanded by remember { mutableStateOf(false) }
            var specialtyExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box {
                    Text(
                        text = "Sort: ${selectedSortOption.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        modifier = Modifier
                            .clickable { sortExpanded = true }
                            .padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedSortOption = option
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }

                Box {
                    Text(
                        text = "Filter: ${selectedSpecialtyFilter ?: "All"}",
                        modifier = Modifier
                            .clickable { specialtyExpanded = true }
                            .padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    DropdownMenu(
                        expanded = specialtyExpanded,
                        onDismissRequest = { specialtyExpanded = false }
                    ) {
                        specialties.forEach { specialty ->
                            DropdownMenuItem(
                                text = { Text(specialty) },
                                onClick = {
                                    selectedSpecialtyFilter = specialty
                                    specialtyExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Clear Filter") },
                            onClick = {
                                selectedSpecialtyFilter = null
                                specialtyExpanded = false
                            }
                        )
                    }
                }
            }


            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(displayedDoctors) { doctor ->
                    val patient = FirebaseAuth.getInstance()
                    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (doctor.profileUri.isNotEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(doctor.profileUri),
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Text(doctor.name, fontWeight = FontWeight.Bold)
                                    Text(doctor.specialty)
                                    Text("${doctor.experience} yrs experience")
                                    distances[doctor.id]?.let { Text("Distance: ${"%.1f".format(it / 1000)} km") }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

                                IconButton(onClick = { /* Chat */ }) {
                                    Icon(painterResource(R.drawable.chat), contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Unspecified)
                                }
                                var showDialog by remember { mutableStateOf(false) }

                                if (showDialog) {
                                    BookAppointmentDialog(
                                        doctorId = doctor.id,
                                        navController = navController,
                                        onDismissRequest = { showDialog = false }
                                    )
                                }

                                Button(onClick = { showDialog = true }) {
                                    Text("Book Appointment")
                                }
                            }


                        }
                    }
                }
            }
        }
    }
}
fun BookAppointmentIfNoConflict(
    context: Context,
    doctorId: String,
    patientId: String,
    appointmentTime: Timestamp,
    distance: Double,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("appointments")
        .whereEqualTo("doctorId", doctorId)
        .whereEqualTo("appointmentTime", appointmentTime)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                // No conflict, proceed to book
                val newAppointment = Appointment(
                    doctorId = doctorId,
                    patientId = patientId,
                    appointmentTime = appointmentTime,
                    distance = distance
                )

                db.collection("appointments")
                    .add(newAppointment)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Appointment booked!", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure("Failed to save: ${e.message}")
                    }
            } else {
                onFailure("This time slot is already booked.")
            }
        }
        .addOnFailureListener { e ->
            onFailure("Failed to check conflicts: ${e.message}")
        }
}
fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371e3 // metres
    val φ1 = Math.toRadians(lat1)
    val φ2 = Math.toRadians(lat2)
    val Δφ = Math.toRadians(lat2 - lat1)
    val Δλ = Math.toRadians(lon2 - lon1)

    val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return R * c // in metres
}


enum class SortOption {
    DISTANCE, RATING, EXPERIENCE
}


