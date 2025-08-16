package com.example.doctorappointment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.doctorappointment.ui.theme.DoctorAppointmentTheme

class AIChatbot : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoctorAppointmentTheme {
                Surface {
                    AIChatbotScreen()
                }
            }
        }
    }
}

@Composable
fun AIChatbotScreen() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://c94yhk.csb.app/chat"))
        context.startActivity(intent)
    }

    // Optional: Show a loading message while redirecting
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Opening chatbot...")
    }
}

//https://c94yhk.csb.app/chat