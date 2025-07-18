package com.capstone.safehito.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.capstone.safehito.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import java.io.ByteArrayOutputStream

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    navController: NavHostController,
    onLogout: () -> Unit,
    darkTheme: Boolean
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    val db = FirebaseDatabase.getInstance().reference

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var imageBase64 by remember { mutableStateOf<String?>(null) }

    var isEditing by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val backgroundColor = if (darkTheme) Color(0xFF121212) else Color(0xFFF4F8FB)

    val scrollState = rememberScrollState()

    // Set status bar color to match background
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = backgroundColor,
            darkIcons = !darkTheme // true for light background, false for dark
        )
    }

    LaunchedEffect(uid) {
        uid?.let {
            db.child("users").child(it).get().addOnSuccessListener { snapshot ->
                fullName = snapshot.child("fullName").getValue<String>() ?: ""
                phone = snapshot.child("phone").getValue<String>() ?: ""
                imageBase64 = snapshot.child("profileImageBase64").getValue<String>()
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)

            if (base64 != null && uid != null) {
                db.child("users").child(uid).child("profileImageBase64")
                    .setValue(base64)
                    .addOnSuccessListener {
                        imageBase64 = base64
                        Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_LONG).show()
                        Log.e("FirebaseUpload", "Failed to upload image: ${it.message}")
                    }
            } else {
                Toast.makeText(context, "Failed to convert image. Try a different file.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No image selected.", Toast.LENGTH_SHORT).show()
        }
    }

    var totalScans by remember { mutableStateOf(0) }
    var lastScanDate by remember { mutableStateOf("No scans yet") }

    LaunchedEffect(uid) {
        uid?.let {
            db.child("users").child(it).get().addOnSuccessListener { snapshot ->
                fullName = snapshot.child("fullName").getValue<String>() ?: ""
                phone = snapshot.child("phone").getValue<String>() ?: ""
                email = auth.currentUser?.email ?: ""
                imageBase64 = snapshot.child("profileImageBase64").getValue<String>()

                val scansSnapshot = snapshot.child("scans")
                totalScans = scansSnapshot.childrenCount.toInt()

                val latestScan = scansSnapshot.children.maxByOrNull {
                    it.child("timestamp").getValue<Long>() ?: 0L
                }

                val timestamp = latestScan?.child("timestamp")?.getValue<Long>() ?: 0L
                if (timestamp != 0L) {
                    val date = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(timestamp * 1000))
                    lastScanDate = date
                }
            }
        }
    }

    @Composable
    fun ProfileInfoRow(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        isEditing: Boolean
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text("SafeHito", fontSize = MaterialTheme.typography.labelSmall.fontSize, color = Color.Gray)
                        Text(
                            if (isEditing) "Edit Profile" else "Profile",
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (darkTheme) Color(0xFF1E1E1E) else Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // 🔧 Settings Button
                    IconButton(onClick = { navController.navigate("settings") }, modifier = Modifier.padding(end = 18.dp)) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (darkTheme) Color(0xFF1E1E1E) else Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        /*bottomBar = {
            FloatingNavBar(
                selectedRoute = selectedRoute,
                onItemSelected = onItemSelected,
                modifier = Modifier.padding(bottom = 25.dp)
            )
        }*/
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .padding(bottom = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (darkTheme) Color(0xFF1A1A1A) else Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // --- Profile Image ---
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable { showImageOptions = true }
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageBase64 != null) {
                                val bitmap = base64ToBitmap(imageBase64!!)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.default_profile),
                                    contentDescription = "Default Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- Profile Details ---
                        ProfileInfoRow("Full Name", fullName, { fullName = it }, isEditing)
                        ProfileInfoRow("Phone", phone, { phone = it }, isEditing)
                        ProfileInfoRow("Email", email, {}, false)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Scans", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text("$totalScans", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Last Scan", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text(lastScanDate, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }



                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Diagnosis History Button ---
                        OutlinedButton(
                            onClick = { onItemSelected("records") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text("View Diagnosis History")
                        }


                        Spacer(modifier = Modifier.height(4.dp))

                        if (isEditing) {
                            Button(
                                onClick = {
                                    uid?.let { id ->
                                        db.child("users").child(id).updateChildren(
                                            mapOf("fullName" to fullName, "phone" to phone)
                                        ).addOnSuccessListener {
                                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                            isEditing = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Save")
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { showLogoutConfirmation = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB71C1C), // dark red
                                contentColor = Color.White
                            )
                        ) {
                            Text("Log Out")
                        }


                    }
                }
            }


        }

        // Add this state
        var showFullImage by remember { mutableStateOf(false) }

        if (showImageOptions) {
            AlertDialog(
                onDismissRequest = { showImageOptions = false },
                confirmButton = {},
                title = { Text("Profile Picture") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .clickable {
                                    showFullImage = true // open full image on click
                                }
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        )
                        {
                            if (imageBase64 != null) {
                                val bitmap = base64ToBitmap(imageBase64!!)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Current Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.default_profile),
                                    contentDescription = "Default Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Change Profile Picture",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    launcher.launch("image/*")
                                    showImageOptions = false
                                }
                                .padding(vertical = 8.dp)
                        )
                        Divider()

                        Text(
                            "Remove Profile Picture",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImageOptions = false
                                    showRemoveConfirmation = true
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImageOptions = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }




        if (showRemoveConfirmation) {
            AlertDialog(
                onDismissRequest = { showRemoveConfirmation = false },
                title = { Text("Confirm Removal") },
                text = { Text("Are you sure you want to remove your profile picture?") },
                confirmButton = {
                    TextButton(onClick = {
                        uid?.let {
                            db.child("users").child(it).child("profileImageBase64").removeValue()
                                .addOnSuccessListener {
                                    imageBase64 = null
                                    Toast.makeText(context, "Profile picture removed.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        showRemoveConfirmation = false
                    }) {
                        Text("REMOVE", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveConfirmation = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        if (showLogoutConfirmation) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(onClick = {
                        auth.signOut()
                        onLogout()
                        showLogoutConfirmation = false
                    }) {
                        Text("LOG OUT", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmation = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        if (showFullImage) {
            Dialog(onDismissRequest = { showFullImage = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showFullImage = false },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageBase64 != null) {
                            val bitmap = base64ToBitmap(imageBase64!!)
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Full Profile Picture",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_profile),
                                contentDescription = "Default Profile Picture",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
    }
}

@RequiresApi(Build.VERSION_CODES.P)
fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        Log.e("uriToBase64", "Error converting URI: ${e.message}")
        null
    }
}


fun base64ToBitmap(base64Str: String): Bitmap {
    val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}
