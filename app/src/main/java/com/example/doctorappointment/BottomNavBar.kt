package com.example.doctorappointment

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {

    val icons = listOf(
        painterResource(R.drawable.home),
        painterResource(R.drawable.appointment),
        painterResource(R.drawable.doctor),
        painterResource(R.drawable.chatbot)
    )
    val labels = listOf("Home", "Appointment", "Doctors", "Chatbot")
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val routes = listOf(
        "patient_home",
        "patient_appointments",
        "patient_view_doctors",
        "chatbot"
    )

    NavigationBar {
        routes.forEachIndexed { index, route ->
            NavigationBarItem(
                icon = {
                    Icon(
                        icons[index],
                        contentDescription = labels[index],
                        modifier = Modifier.size(35.dp),
                        tint = Color.Unspecified
                    )
                },
                label = { Text(labels[index]) },
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            // Simplified navigation logic: only pop up to startDestination if needed
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

            )
        }
    }
    Log.d("BottomNav", "Current route: $currentRoute, Routes: $routes")

}

