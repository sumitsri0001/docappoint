package com.example.doctorappointment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.doctorappointment.ui.theme.DoctorAppointmentTheme

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoctorAppointmentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFFFF)
                ) {
                }
            }
        }
    }
}
fun isLocationPermissionGranted(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun requestLocationPermission(activity: Activity) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        LOCATION_PERMISSION_REQUEST_CODE
    )
}

const val LOCATION_PERMISSION_REQUEST_CODE = 1001

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = " Welcome to EasyHealth ",
            modifier = Modifier.padding(bottom = 80.dp, top = 100.dp, start = 20.dp, end = 20.dp),
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium
        )
        Image(
            painter = painterResource(R.drawable.app_icon),
            contentDescription = "app icon",
            modifier = Modifier.size(width = 400.dp, height = 200.dp)
                .padding(bottom = 40.dp)
        )
        Button(
            onClick = { navController.navigate("patient_login") },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF66A1FF)
            ),
            modifier = Modifier.fillMaxWidth().padding(20.dp).size(width = 120.dp, height = 55.dp)
        ) {
            Text(
                " Login as Patient ",
                fontSize = 15.sp
            )
        }
        Button(
            onClick = { navController.navigate("doctor_login") },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF66A1FF)
            ),
            modifier = Modifier.fillMaxWidth().padding(20.dp).size(width = 120.dp, height = 55.dp)
        ) {
            Text(
                " Login as Doctor ",
                fontSize = 15.sp
            )
        }
        Text(
            "New User? Register",
            modifier = Modifier.padding(top = 10.dp)
                .clickable { navController.navigate("register") }
        )
    }
}

// @Preview(showBackground = true)
// @Composable
// fun GreetingPreview() {
//    DoctorAppointmentTheme {
//        Greeting2("Android")
//    }
// }
