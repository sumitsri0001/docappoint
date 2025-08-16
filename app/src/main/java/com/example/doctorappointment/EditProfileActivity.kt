package com.example.doctorappointment

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.doctorappointment.ui.theme.DoctorAppointmentTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoctorAppointmentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    val navController = rememberNavController()
                    val viewModel: PatientViewModel = viewModel()
                    EditPatientProfileScreen(navController, viewModel)
                }
            }
        }
    }
}

@Composable
fun EditPatientProfileScreen(
    navController: NavController,
    viewModel: PatientViewModel = viewModel()
) {
    val context = LocalContext.current
    val patient = viewModel.patientData.value
    var name by remember { mutableStateOf(patient?.name ?: "") }
    var age by remember { mutableStateOf(patient?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(patient?.gender ?: "") }
    var healthConditions by remember { mutableStateOf(patient?.healthConditions ?: "") }
    var geometry by remember { mutableStateOf(patient?.geometry?:Geometry(0.0,0.0)) }
    var profileUri by remember { mutableStateOf<Uri?>(patient?.profileUri?.let { Uri.parse(it) }) }

    var isMapOpen by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.getPatientDetails()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Edit Profile", style = MaterialTheme.typography.headlineSmall)

        ProfilePicturePicker(profileUri) {
            profileUri = it
        }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        GenderSelector(gender) { gender = it }
        OutlinedTextField(value = healthConditions, onValueChange = { healthConditions = it }, label = { Text("Health Conditions") })
        Button(onClick = { isMapOpen =true
            // Location is now saved in the `location` state
        }) {
            Text("Choose Location")
        }
        if(isMapOpen){
            MapDialog(
                onDismiss = { isMapOpen = false },
                onLocationPicked = { lat, lon ,address->
                    Toast.makeText(context, "Selected: $lat, $lon \n $address", Toast.LENGTH_LONG).show()

                    // Optional: Save to Firestore here
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@MapDialog
                    geometry = Geometry(lat, lon)
                    FirebaseFirestore.getInstance().collection("patients")
                        .document(uid)
                        .update("geometry", geometry)
                    isMapOpen = false
                }
            )
        }
        Button(onClick = {
            if (name.isNotEmpty()) {
                val updatedPatient = Patient(
                    name = name,
                    age = age.toIntOrNull() ?: 0,
                    gender = gender,
                    healthConditions = healthConditions,
                    geometry = geometry,
                    profileUri = profileUri.toString(),
                    email = patient?.email ?: ""
                )
                viewModel.updatePatientDetails(updatedPatient)
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } else {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Save Changes")
        }
    }
}
