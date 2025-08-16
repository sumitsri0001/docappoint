package com.example.doctorappointment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.doctorappointment.ui.theme.DoctorAppointmentTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class PatientHomeScreen : ComponentActivity() {
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
                    PatientHome(navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHome(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val user = Firebase.auth.currentUser
    val db = Firebase.firestore

    var userName by remember { mutableStateOf("Patient") }
    var profileUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("patients").document(uid).get()
                .addOnSuccessListener { document ->
                    val patient = document.toObject(Patient::class.java)
                    patient?.let {
                        userName = it.name
                        profileUri = it.profileUri
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load user profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,

        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(250.dp), // Set custom width here
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                drawerContainerColor = Color.White,
                drawerContentColor = Color.Black,
                drawerTonalElevation = 8.dp,
            ) {
                // Drawer header
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (profileUri != null) {
                        AsyncImage(
                            model = profileUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Default Profile",
                            modifier = Modifier.size(72.dp)
                        )
                    }
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )
                }

                // Drawer items
                listOf("Edit Profile", "Settings", "Feedback", "Logout").forEach { item ->
                    TextButton(
                        onClick = {
                            when (item) {
                                "Edit Profile" -> navController.navigate("edit_profile")
                                "Logout" -> {
                                    Firebase.auth.signOut()
                                    navController.navigate("login") {
                                        popUpTo("patient_home") { inclusive = true }
                                    }
                                }
                            }
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        modifier = Modifier.background(Color(0xFFFFFFFF))
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Doctor Appointment") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavigationBar(navController)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                WelcomeCard()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Government Healthcare Initiatives",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(getGovernmentInitiatives()) { initiative ->
                        Spacer(modifier = Modifier.height(8.dp))
                        InitiativeCard(
                            title = initiative.title,
                            description = initiative.description,
                            imageRes = initiative.imageRes,
                            url = initiative.url,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(initiative.url))
                                startActivity(context, intent, null)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD) // Light blue background
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_welcome),
                contentDescription = "Welcome",
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1) // Dark blue text
                )
                Text(
                    text = "Explore healthcare initiatives by the Indian government",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}
@Composable
fun InitiativeCard(
    title: String,
    description: String,
    imageRes: Int,
    url: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image taking full width of card
            Box(modifier = Modifier.height(180.dp)) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }

            // Content below image
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
data class GovernmentInitiative(
    val title: String,
    val description: String,
    val imageRes: Int,
    val url: String
)
fun getGovernmentInitiatives(): List<GovernmentInitiative> {
    return listOf(
        GovernmentInitiative(
            title = "Ayushman Bharat Yojana",
            description = "Provides health coverage of ₹5 lakh per family per year for secondary and tertiary care hospitalization",
            imageRes = R.drawable.ayushman_bharat, // Replace with your image
            url = "https://www.pmjay.gov.in/"
        ),
        GovernmentInitiative(
            title = "National Health Mission",
            description = "Aims to provide universal access to equitable, affordable and quality health care",
            imageRes = R.drawable.nhm, // Replace with your image
            url = "https://nhm.gov.in/"
        ),
        GovernmentInitiative(
            title = "Pradhan Mantri Swasthya Suraksha Yojana",
            description = "Seeks to correct regional imbalances in the availability of affordable healthcare",
            imageRes = R.drawable.pmssy, // Replace with your image
            url = "https://pmssy-mohfw.nic.in/"
        ),
        GovernmentInitiative(
            title = "Janani Shishu Suraksha Karyakram",
            description = "Entitles all pregnant women delivering in public health institutions to free delivery",
            imageRes = R.drawable.jssk, // Replace with your image
            url = "https://nhm.gov.in/index1.php?lang=1&level=2&sublinkid=822&lid=218"
        ),
        GovernmentInitiative(
            title = "Mission Indradhanush",
            description = "Aims to immunize all children under the age of 2 years against vaccine preventable diseases",
            imageRes = R.drawable.mission_indradhanush, // Replace with your image
            url = "https://www.missionindradhanush.in/"
        )
    )
}
//    val firestore :FirebaseFirestore= Firebase.firestore
//    var doctors by remember { mutableStateOf(listOf<DoctorDta>()) }
//    var selectedSpeciality by remember { mutableStateOf("All") }
//    var sortOption by remember { mutableStateOf("None") }
//
//    LaunchedEffect(Unit) {
//        firestore.collection("doctors")
//            .get()
//            .addOnSuccessListener { snapshot ->
//                val fetchedDoctors = snapshot.documents.mapNotNull {
//                    it.toObject(DoctorDta::class.java)
//                }
//                doctors = fetchedDoctors
//            }
//    }
//
//    val filteredDoctors = doctors.filter {
//        selectedSpeciality == "All" || it.specialty == selectedSpeciality
//    }.let {
//        when (sortOption) {
//            "Experience" -> it.sortedByDescending { doc -> doc.experience }
//            "Rating" -> it.sortedByDescending { doc -> doc.rating }
//            else -> it
//        }
//    }
//
//    Column(Modifier.padding(16.dp)) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            DropdownMenuBox(
//                label = "Speciality",
//                options = listOf("All", "Cardiologist", "Dermatologist", "Orthopedic", "Dentist"),
//                selectedOption = selectedSpeciality,
//                onOptionSelected = { selectedSpeciality = it }
//            )
//
//            DropdownMenuBox(
//                label = "Sort By",
//                options = listOf("None", "Experience", "Rating"),
//                selectedOption = sortOption,
//                onOptionSelected = { sortOption = it }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        LazyColumn {
//            items(filteredDoctors) { doctor ->
//                DoctorCard(doctor)
//                Spacer(modifier = Modifier.height(12.dp))
//            }
//        }
//    }

@Composable
fun DropdownMenuBox(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontWeight = FontWeight.SemiBold)
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(selectedOption)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            onOptionSelected(it)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DoctorCard(doctor: DoctorDta) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = doctor.profileUri,
                contentDescription = "Doctor Photo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(doctor.specialty, color = Color.Gray)
                Text("Exp: ${doctor.experience} yrs | Rating: ${doctor.rating}")
                Text("Location: ${doctor.geometry}", fontSize = 12.sp)
            }
        }
    }
}

// @Preview(showBackground = true)
// @Composable
// fun GreetingPreview() {
//    DoctorAppointmentTheme {
//        Greeting2("Android")
//    }
// }
