package com.example.doctorappointment

data class Patient(
    val role: String = "patient",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val geometry: Geometry,
    val email: String ="",
    val healthConditions: String = "",
    val profileUri: String = ""
){
    constructor() : this(
        name = "",
        age = 0,
        gender = "",
        healthConditions = "",
        email = "",
        profileUri = "",
        role = "patient",
        geometry = Geometry(0.0, 0.0)
    )
}
data class Geometry(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
){
    constructor() : this(0.0, 0.0)
}


