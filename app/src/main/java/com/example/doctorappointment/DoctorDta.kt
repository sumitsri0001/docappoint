package com.example.doctorappointment

data class DoctorDta(
    val id: String = "",
    val role: String = "doctor",
    val name: String = "",
    val profileUri: String = "",
    var specialty: String = "",
    val experience: Int = 0,
    val rating: Double = 0.0,
    val geometry: Geometry=Geometry(0.0,0.0),
    val email: String = "",
    val availability: Map<String, List<TimeSlot>> = emptyMap()
)


data class TimeSlot(
    val startTime: String = "",
    val endTime: String = ""
)
