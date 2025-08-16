package com.example.doctorappointment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.doctorappointment.ui.theme.DoctorAppointmentTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PatientLogin : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoctorAppointmentTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFFFF)
                ) {
                    PatientLoginScreen(navController)
                }
            }
        }
    }
}

@Composable
fun PatientLoginScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    if (auth.currentUser != null) {
        navController.navigate("patient_home") // This will navigate to the home screen after login
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            " Welcome User ",
            modifier = Modifier.padding(20.dp),
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(" Email : ") },
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(" Password : ") },
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            visualTransformation = PasswordVisualTransformation()
        )
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Email and password cannot be empty", Toast.LENGTH_LONG).show()
                }

                loading = true
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener {
                        val uid = auth.currentUser!!.uid
                        db.collection("patients").document(uid).get()
                            .addOnSuccessListener {
                                navController.navigate("patient_home") {
                                    popUpTo("login") { inclusive = true }
                                }
                                loading = false
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error fetching user role", Toast.LENGTH_LONG).show()
                                loading = false
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
                        loading = false
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Text("Login")
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

// @Preview(showBackground = true)
// @Composable
// fun GreetingPreview2() {
//    DoctorAppointmentTheme {
//        PatientLoginScreen()
//    }
// }
