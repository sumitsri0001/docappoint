package com.example.doctorappointment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = if (FirebaseAuth.getInstance().currentUser != null) "patient_home" else "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("patient_login") { PatientLoginScreen(navController) }
        composable("doctor_login") { DoctorLoginScreen(navController) }
        composable("register") { RegistrationScreen(navController) }
        composable("patient_home") { PatientHome(navController) }
        composable("doc_home") { DoctorHome() }
        composable("patient_appointments") { PatientAppointmentScreen() }
        composable("patient_view_doctors") { AllDoctorsScreen(navController) }
        composable("chatbot") { AIChatbotScreen() }
        composable("edit_profile") { EditPatientProfileScreen(navController) }
        composable("book_appointment/{doctorId}") { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId")
            doctorId?.let {
                BookAppointmentDialog(doctorId = it, navController = navController, onDismissRequest = {navController.popBackStack()})
            } }
    }
}
