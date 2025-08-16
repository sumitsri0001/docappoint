package com.example.doctorappointment

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PatientViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val patientData = mutableStateOf<Patient?>(null)

    fun registerPatient(
        name: String,
        age: Int,
        gender: String,
        healthConditions: String,
        email: String,
        profile: Uri?,
        geometry: Geometry
    ) {
        val patientid = auth.currentUser?.uid ?: return
        val profileUrl = profile?.toString() ?: ""
        val patient = Patient(
            role = "patient",
            name = name,
            age = age,
            gender = gender,
            healthConditions = healthConditions,
            profileUri = profileUrl,
            email = email,
            geometry = geometry
        )

        firestore.collection("patients").document(patientid)
            .set(patient)
            .addOnSuccessListener { Log.d("Patient", "Registered") }
            .addOnFailureListener { Log.e("Patient", "Error", it) }
    }
    fun getPatientDetails() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("patients").document(uid)
            .get()
            .addOnSuccessListener { document ->
                document?.toObject(Patient::class.java)?.let {
                    patientData.value = it
                }
            }
    }
    fun updatePatientDetails(updatedPatient: Patient) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("patients").document(uid).set(updatedPatient)
    }
}
