package com.example.doctorappointment

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun registerDoctor(
        name: String,
        specialty: String,
        experience: Int,
        availability: Map<String, List<TimeSlot>>,
        email: String,
        profile: Uri?,
        geometry: Geometry
    ) {
        val docid = auth.currentUser?.uid ?: return
        val docAvailability = availability.mapValues { entry ->
            entry.value.map { slotmap ->
                val start = slotmap.startTime ?: ""
                val end = slotmap.endTime ?: ""
                TimeSlot(start, end)
            }
        }
        val profileUrl = profile?.toString() ?: ""

        val doctor = DoctorDta(
            role = "doctor",
            name = name,
            specialty = specialty,
            experience = experience,
            profileUri = profileUrl,
            availability = docAvailability,
            geometry = geometry,
            email = email
        )

        firestore.collection("doctors").document(docid)
            .set(doctor)
            .addOnSuccessListener { Log.d("Doctor", "Registered") }
            .addOnFailureListener { Log.e("Doctor", "Error", it) }
    }
}
