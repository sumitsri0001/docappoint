package com.example.doctorappointment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.doctorappointment.ui.theme.DoctorAppointmentTheme

class DoctorHomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoctorAppointmentTheme {
                Surface {
                    DoctorHome()
                }
            }
        }
    }
}

@Composable
fun DoctorHome() {
    Text("doctors")
}

// @Preview(showBackground = true)
// @Composable
// fun GreetingPreview() {
//    DoctorAppointmentTheme {
//        Greeting2("Android")
//    }
// }
